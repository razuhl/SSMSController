/*
 * Copyright (C) 2020 Malte Schulze.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library;  If not, see 
 * <https://www.gnu.org/licenses/>.
 */
package ssms.controller;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatFleetManagerAPI;
import com.fs.starfarer.api.combat.CombatTaskManagerAPI;
import com.fs.starfarer.api.combat.FogOfWarAPI;
import com.fs.starfarer.api.combat.MutableStat;
import com.fs.starfarer.api.combat.ShieldAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipCommand;
import com.fs.starfarer.api.combat.ShipwideAIFlags;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.WeaponGroupAPI;
import com.fs.starfarer.api.impl.combat.MineStrikeStats;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.mission.FleetSide;
import com.fs.starfarer.api.util.Pair;
import com.fs.starfarer.combat.CombatState;
import com.fs.starfarer.combat.entities.Ship;
import com.fs.starfarer.combat.systems.R;
import com.fs.starfarer.prototype.Utils;
import com.fs.starfarer.util.oOOO;
import com.fs.state.AppDriver;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import org.apache.log4j.Level;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.Rectangle;
import org.lwjgl.util.vector.ReadableVector2f;
import org.lwjgl.util.vector.Vector2f;
import ssms.controller.steering.SteeringController;
import ssms.controller.steering.SteeringController_FreeFlight;
import ssms.qol.ui.AlignmentHorizontal;
import ssms.qol.ui.AlignmentVertical;
import ssms.qol.ui.UIComponentFactory;
import ssms.qol.ui.UIComponentParentFactory;
import ssms.qol.ui.UIComponent_Button;
import ssms.qol.ui.UIComponent_Column;
import ssms.qol.ui.UIComponent_Parent;
import ssms.qol.ui.UIComponent_Row;
import ssms.qol.ui.UIContext;
import ssms.qol.ui.UIUtil;

/**
 *
 * @author Malte Schulze
 */
public class EveryFrameCombatPlugin_Controller extends BaseEveryFrameCombatPlugin {
    static protected final String TDID_TARGETING = "TARGETING", TDID_MENU = "MENU";
    protected CombatEngineAPI engine;
    protected float nextLog, desiredDistance, desiredZoomFactor = 2f, offsetFacingAngle = 0f;
    protected Targeting targeting;
    protected PlayerShipCache psCache = new PlayerShipCache();
    protected boolean btnVentingDown = false, btnShieldDown = false, btnUseSystemDown = false, btnFighterToggleDown = false, btnToggleAutofireDown = false,
            isAlternateSteering = false, btnAlternateSteeringDown = false, btnSelectMenuEntryDown = false, btnDisplayMenuDown = false,
            btnTargetingBothDown = false, btnNextWeaponGroupDown = false, btnPrevWeaponGroupDown = false;
    protected boolean displayMenu = false, nextMenuEntry = false, selectMenuEntry = false, enableControllerSteering = true, wasShowingWarroom = false, wasShieldOn = false;
    protected int selectedBtnIndex = -1;
    protected UIComponent_Parent root;
    protected ReadableVector2f vDesiredHeadingLastValidInput = new Vector2f();
    
    static protected class PlayerShipCache {
        ShipAPI ps;
        boolean hasFighters;
        float offsetFacingAngle;
        SteeringController steeringController;

        public PlayerShipCache() {}
        
        void setShip(ShipAPI ps, HandlerController gameController, CombatEngineAPI engine) {
            discard();
            this.ps = ps;
            this.hasFighters = ps.getLaunchBaysCopy().isEmpty();
            this.offsetFacingAngle = 0f;
            try {
                createSteeringController(SSMSControllerModPlugin.primarySteeringMode, ps, gameController, engine);
            } catch (InstantiationException | IllegalAccessException ex) {
                Global.getLogger(SSMSControllerModPlugin.class).log(Level.ERROR, "Primary Steering Mode contains a controller without a puclic no argument constructor! Using fallback controller.", ex);
                this.steeringController = new SteeringController_FreeFlight();
                this.steeringController.activate(ps, gameController, engine);
            }
        }
        
        boolean isForShip(ShipAPI ship) {
            return ps != null && ps.equals(ship);
        }
        
        void discard() {
            ps = null;
            hasFighters = false;
            steeringController = null;
            offsetFacingAngle = 0f;
        }

        void setSteeringController(Class steeringMode, HandlerController gameController, CombatEngineAPI engine) {
            try {
                createSteeringController(steeringMode, ps, gameController, engine);
            } catch (InstantiationException | IllegalAccessException ex) {
                Global.getLogger(SSMSControllerModPlugin.class).log(Level.ERROR, "Steering Mode contains a controller without a puclic no argument constructor! Using fallback controller.", ex);
                this.steeringController = new SteeringController_FreeFlight();
                this.steeringController.activate(ps, gameController, engine);
            }
        }
        
        private void createSteeringController(Class steeringMode, ShipAPI ship, HandlerController gameController, CombatEngineAPI engine) throws InstantiationException, IllegalAccessException {
            this.steeringController = (SteeringController) steeringMode.newInstance();
            this.steeringController.activate(ship, gameController, engine);
        }
    }
    
    static protected class Targeting {
        List<ShipAPI> targets;
        int index;
        long createdOn;
        boolean cooldown;

        public Targeting(List<ShipAPI> targets) {
            this.targets = targets;
            this.index = -1;
            this.createdOn = 0;
        }
        
        public void discard() {
            targets.clear();
        }
        
        public boolean hasTargets() {
            return targets.size() > 0;
        }
        
        public ShipAPI next() {
            this.createdOn = System.currentTimeMillis();
            this.cooldown = true;
            if ( ++index >= targets.size() ) {
                index = 0;
            }
            return targets.get(index);
        }
        
        public ShipAPI previous() {
            this.createdOn = System.currentTimeMillis();
            this.cooldown = true;
            if ( --index < 0 ) {
                index = targets.size() - 1;
            }
            return targets.get(index);
        }
        
        public boolean isExpired() {
            return this.createdOn + 1000 < System.currentTimeMillis();
        }

        public boolean isNotOnCooldown() {
            return !cooldown;
        }

        private void setCooldown(boolean cooldown) {
            this.cooldown = cooldown;
        }
    }
    
    static protected interface SteeringMode {
        void steer(ShipAPI ps, HandlerController controller, float timePassed);
    }
    
    @Override
    public void init(CombatEngineAPI engine) {
        this.engine = engine;
        btnVentingDown = false; 
        btnShieldDown = false; 
        btnUseSystemDown = false;
        btnFighterToggleDown = false;
        btnToggleAutofireDown = false;
        isAlternateSteering = false;
        btnAlternateSteeringDown = false;
        btnSelectMenuEntryDown = false;
        btnDisplayMenuDown = false;
        btnNextWeaponGroupDown = false; 
        btnPrevWeaponGroupDown = false;
        displayMenu = false;
        nextMenuEntry = false;
        selectMenuEntry = false;
        selectedBtnIndex = -1;
        if ( psCache != null ) psCache.discard();
        else psCache = new PlayerShipCache();
        if ( targeting != null ) targeting.discard();
        targeting = null;
        nextLog = 0;
        offsetFacingAngle = 0;
        desiredDistance = 1000f;
        vDesiredHeadingLastValidInput = new Vector2f();
        desiredZoomFactor = 2f;
        wasShieldOn = false;
    }
    
    protected ShipAPI getControlledShip() {
        return engine.getPlayerShip();
    }
    
    protected boolean processMenuInputs() {
        return displayMenu;
    }
    
    protected boolean processShipInputs(ShipAPI ps) {
        return ps != null && !displayMenu && enableControllerSteering && engine.isEntityInPlay(ps) && !ps.isHulk();
    }
    
    protected void adjustZoom() {
        CombatState cs = (CombatState) AppDriver.getInstance().getState(CombatState.STATE_ID);
        if ( cs.getZoomFactor() != desiredZoomFactor ) {
            try {
                Field f = CombatState.class.getDeclaredField("zoomTracker");
                if ( !f.isAccessible() ) f.setAccessible(true);
                oOOO zoomTracker = (oOOO) f.get(cs);
                f = oOOO.class.getDeclaredField("\u00D800000");
                if ( !f.isAccessible() ) f.setAccessible(true);
                f.set(zoomTracker, desiredZoomFactor);
                zoomTracker.Ó00000(desiredZoomFactor);
            } catch (Throwable t) {
                engine.getCombatUI().addMessage(0, "error: "+t.getMessage());
                Global.getLogger(SSMSControllerModPlugin.class).log(Level.ERROR, "Failed to adjust zoom tracker, ensure SSMSUnlock is installed!", t);
            }
        }
    }
    
    protected void setZoom(float zoomFactor) {
        desiredZoomFactor = zoomFactor;
        adjustZoom();
    }

    @Override
    public void processInputPreCoreControls(float amount, List<InputEventAPI> events) {
        Vector2f v1 = new Vector2f();
        /*for ( InputEventAPI event : events ) {
            if ( event.isKeyDownEvent() && event.getEventValue() == ModPlugin.modMenuKey ) {
                try {
                    ModPlugin.reconnectController();
                } catch ( Throwable t ) {
                    if ( engine != null && engine.getContext() != null ) {
                        engine.getCombatUI().addMessage(0, "FAILED to reconnect controllers!");
                    }
                }
                if ( engine != null && engine.getContext() != null ) {
                    engine.getCombatUI().addMessage(0, "Reconnected controllers.");
                }
                event.consume();
            }
        }*/
        
        if ( engine != null && engine.getContext() != null && !engine.getCombatUI().isShowingCommandUI() ) {
            if ( wasShowingWarroom ) {
                adjustZoom();
            }
            
            HandlerController handler = SSMSControllerModPlugin.controller;
            handler.poll();
            
            if ( targeting != null && targeting.isExpired() ) {
                targeting.discard();
                targeting = null;
                timeDilation(false,TDID_TARGETING);
            }
            
            float totalElapsedTime = engine.getTotalElapsedTime(true);
            boolean log = totalElapsedTime > nextLog;
            if ( log ) nextLog += 4;
            //TODO inputs for the warroom
            //TODO menu entries for switching ships(camera jumps to the targeted eligeble ship like targeting next and previous then selecting to pick a ship)
            //TODO menu entry for ending combat/simulation
            
            if ( processMenuInputs() ) {
                if ( handler.isShowMenu() ) {
                    if ( !btnDisplayMenuDown ) {
                        nextMenuEntry = true;
                        btnDisplayMenuDown = true;
                    }
                } else if ( btnDisplayMenuDown ) {
                    btnDisplayMenuDown = false;
                }
                if ( handler.isSelect() ) {
                    if ( !btnSelectMenuEntryDown ) {
                        selectMenuEntry = true;
                        btnSelectMenuEntryDown = true;
                    }
                } else if ( btnSelectMenuEntryDown ) {
                    btnSelectMenuEntryDown = false;
                }
            } else {
                if ( handler.isShowMenu() ) {
                    if ( !btnDisplayMenuDown ) {
                        setDisplayMenu(true);
                        btnDisplayMenuDown = true;
                    }
                } else if ( btnDisplayMenuDown ) {
                    btnDisplayMenuDown = false;
                }
            }
            
            ShipAPI ps = getControlledShip();
            if ( processShipInputs(ps) ) {
                if ( ps != null && engine.isEntityInPlay(ps) ) {
                    if ( !psCache.isForShip(ps) ) {
                        psCache.setShip(ps, handler, engine);
                    }
                    
                    //autopilot flag is inverted!
                    if ( engine.isUIAutopilotOn() && !engine.isPaused() && amount > 0f ) {
                        if ( handler.isAlternateSteering() ) {
                            if ( !btnAlternateSteeringDown ) {
                                isAlternateSteering = !isAlternateSteering;
                                if ( isAlternateSteering ) {
                                    psCache.setSteeringController(SSMSControllerModPlugin.alternativeSteeringMode, handler, engine);
                                } else {
                                    psCache.setSteeringController(SSMSControllerModPlugin.primarySteeringMode, handler, engine);
                                }
                                btnAlternateSteeringDown = true;
                            }
                        } else if ( btnAlternateSteeringDown ) {
                            btnAlternateSteeringDown = false;
                        }
                        
                        if ( isAlternateSteering && ( !isValidTarget(ps.getShipTarget()) || !psCache.steeringController.isTargetValid() ) ) {
                            isAlternateSteering = false;
                            psCache.setSteeringController(SSMSControllerModPlugin.primarySteeringMode, handler, engine);
                        }
                        
                        psCache.steeringController.steer(amount, offsetFacingAngle);
                        
                        //Targeting dilates time if first clicked and unpauses if no further targeting buttons where used after x seconds
                        //When starting targeting with an already selected target we keep that target until a second input occurs to avoid
                        //  loosing targets by bumping bumpers by blunder
                        if ( handler.isTargetNext() && handler.isTargetPrevious() ) {
                            //remove targeting
                            if ( !btnTargetingBothDown ) {
                                if ( targeting != null ) targeting.discard();
                                targeting = null;
                                timeDilation(false,TDID_TARGETING);
                                CombatState cs = (CombatState) AppDriver.getInstance().getState(CombatState.STATE_ID);
                                cs.setVideoFeedSource(null);
                                ps.setShipTarget(null);
                                btnTargetingBothDown = true;
                            }
                        } else if ( handler.isTargetNext() ) {
                            if ( !btnTargetingBothDown ) {
                                if ( targeting == null ) {
                                    targeting = new Targeting(targetsByDistance(engine.getShips(), ps.getLocation(), ps.getOwner() == 100 ? engine.getFogOfWar(0) : engine.getFogOfWar(ps.getOwner())));
                                }
                                if ( targeting.hasTargets() && targeting.isNotOnCooldown() ) {
                                    ps.setShipTarget((Ship) targeting.next());
                                    psCache.steeringController.onTargetSelected();
                                    timeDilation(true,TDID_TARGETING);
                                }
                            }
                        } else if ( handler.isTargetPrevious() ) {
                            if ( !btnTargetingBothDown ) {
                                if ( targeting == null ) {
                                    targeting = new Targeting(targetsByDistance(engine.getShips(), ps.getLocation(), ps.getOwner() == 100 ? engine.getFogOfWar(0) : engine.getFogOfWar(ps.getOwner())));
                                }
                                if ( targeting.hasTargets() && targeting.isNotOnCooldown() ) {
                                    ps.setShipTarget((Ship) targeting.previous());
                                    psCache.steeringController.onTargetSelected();
                                    timeDilation(true,TDID_TARGETING);
                                }
                            }
                        } else {
                            if ( targeting != null ) targeting.setCooldown(false);
                            btnTargetingBothDown = false;
                        }
                        
                        Vector2f targetLocation;
                        List<WeaponAPI> weapons = ps.getSelectedGroupAPI().getWeaponsCopy();
                        for ( WeaponAPI weapon : weapons ) {
                            if ( weapon.isDisabled() ) continue;
                            if ( ps.getShipTarget() != null ) {
                                targetLocation = targetLeading(weapon.getLocation(),ps.getShipTarget().getLocation(),ps.getShipTarget().getVelocity(),weapon.getProjectileSpeed(),v1);
                            } else {
                                targetLocation = targetFrontal(ps.getLocation(),weapon.getRange(),ps.getFacing(),v1);
                            }

                            if ( targetLocation == null ) targetLocation = targetFrontal(ps.getLocation(),weapon.getRange(),ps.getFacing(),v1);

                            ((R)weapon).getAimTracker().o00000(targetLocation);
                        }
                        if ( handler.isFire() ) ps.giveCommand(ShipCommand.FIRE, v1, -1);
                        
                        //start venting
                        if ( handler.isVenting() ) {
                            if ( !btnVentingDown && !ps.getFluxTracker().isVenting() ) {
                                btnVentingDown = true;
                                ps.giveCommand(ShipCommand.VENT_FLUX, null, -1);
                            }
                        } else if ( btnVentingDown ) {
                            btnVentingDown = false;
                        }
                        
                        //TODO maybe adjust shield facing in the after input processed method if it got turned on this frame
                        //shield/cloak on/off
                        if ( handler.isShieldOrCloak() ) {
                            if ( !btnShieldDown ) {
                                if ( ps.getShield() != null ) {
                                    if ( ((Ship)ps).getShield().isOmni() ) {
                                        CombatState.AUTO_OMNI_SHIELDS = true;
                                        //we only want auto shields if they are turned on otherwise the AI decides when to turn on the shields as well
                                        CombatState cs = (CombatState) AppDriver.getInstance().getState(CombatState.STATE_ID);
                                        try {
                                            Field f = CombatState.class.getDeclaredField("playerShipShieldAIFlags");
                                            f.setAccessible(true);
                                            ShipwideAIFlags flags = (ShipwideAIFlags) f.get(cs);
                                            if ( ps.getShield() != null && ps.getShield().isOff() ) {
                                                flags.unsetFlag(ShipwideAIFlags.AIFlags.DO_NOT_USE_SHIELDS);
                                                flags.setFlag(ShipwideAIFlags.AIFlags.KEEP_SHIELDS_ON, Float.MAX_VALUE);
                                            } else {
                                                flags.setFlag(ShipwideAIFlags.AIFlags.DO_NOT_USE_SHIELDS, Float.MAX_VALUE);
                                                flags.unsetFlag(ShipwideAIFlags.AIFlags.KEEP_SHIELDS_ON);
                                            }
                                        } catch ( Throwable t ) {
                                            Global.getLogger(SSMSControllerModPlugin.class).log(Level.ERROR, "Failed to get field playerShipShieldAIFlags on CombatState, ensure SSMSUnlock is installed!", t);
                                            ps.giveCommand(ShipCommand.TOGGLE_SHIELD_OR_PHASE_CLOAK, null, -1);
                                        }
                                    } else {
                                        ps.giveCommand(ShipCommand.TOGGLE_SHIELD_OR_PHASE_CLOAK, null, -1);
                                    }
                                } else if ( ps.getPhaseCloak() != null ) {
                                    ps.giveCommand(ShipCommand.TOGGLE_SHIELD_OR_PHASE_CLOAK, null, -1);
                                }
                                btnShieldDown = true;
                            }
                        } else if ( btnShieldDown ) {
                            btnShieldDown = false;
                        }
                        
                        //activate system
                        if ( handler.isUseSystem() ) {
                            if ( !btnUseSystemDown ) {
                                if ( ps.getShipTarget() != null ) {
                                    //due to a bug in vanilla coding the getAI method must return not null in order for the minestrike to use the override
                                    //replacing the script with a corrected version that skips the AI check
                                    if ( ps.getSystem() != null && com.fs.starfarer.combat.systems.F.class.isAssignableFrom(ps.getSystem().getClass()) ) {
                                        com.fs.starfarer.combat.systems.F system = (com.fs.starfarer.combat.systems.F)ps.getSystem();
                                        if ( system.getScript() != null && MineStrikeStats.class == system.getScript().getClass() ) {
                                            try {
                                                Field f = com.fs.starfarer.combat.systems.F.class.getDeclaredField("\u00F800000");
                                                f.setAccessible(true);
                                                f.set(system, new MineStrikeStatsFixed());
                                            } catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException ex) {
                                                Global.getLogger(SSMSControllerModPlugin.class).log(Level.ERROR, "Failed to get field \u00F800000 on script, ensure SSMSUnlock is installed!", ex);
                                            }
                                        }
                                    }
                                    ps.getAIFlags().setFlag(ShipwideAIFlags.AIFlags.SYSTEM_TARGET_COORDS, 1, ps.getShipTarget().getLocation());
                                    ps.giveCommand(ShipCommand.USE_SYSTEM, null, -1);
                                } else ps.giveCommand(ShipCommand.USE_SYSTEM, null, -1);
                                btnUseSystemDown = true;
                            }
                        } else if ( btnUseSystemDown ) {
                            btnUseSystemDown = false;
                        }
                        
                        //second joystick cycles fighter modes and weapon groups if not held down. up fighter mode, left right weapon groups, down autofire
                        //toggle fighter mode
                        if ( psCache.hasFighters ) {
                            if ( handler.isToggleFighters() ) {
                                if ( !btnFighterToggleDown ) {
                                    ps.giveCommand(ShipCommand.PULL_BACK_FIGHTERS, null, -1);
                                    btnFighterToggleDown = true;
                                }
                            } else if ( btnFighterToggleDown ) {
                                btnFighterToggleDown = false;
                            }
                        }
                        //toggle autofire
                        if ( handler.isToggleAutofire() ) {
                            if ( !btnToggleAutofireDown ) {
                                ps.giveCommand(ShipCommand.TOGGLE_AUTOFIRE, null, ps.getWeaponGroupsCopy().indexOf(ps.getSelectedGroupAPI()));
                                btnToggleAutofireDown = true;
                            }
                        } else if ( btnToggleAutofireDown ) {
                            btnToggleAutofireDown = false;
                        }
                        //select weapon group
                        if ( handler.isSelectNextWeaponGroup() ) {
                            if ( !btnNextWeaponGroupDown ) {
                                List<WeaponGroupAPI> wgs = ps.getWeaponGroupsCopy();
                                int indx = wgs.indexOf(ps.getSelectedGroupAPI()) + 1;
                                if ( indx >= wgs.size() ) {
                                    indx = 0;
                                }
                                ps.giveCommand(ShipCommand.SELECT_GROUP, null, indx);
                                btnNextWeaponGroupDown = true;
                            }
                        } else if ( btnNextWeaponGroupDown ) {
                            btnNextWeaponGroupDown = false;
                        }
                        if ( handler.isSelectPreviousWeaponGroup() ) {
                            if ( !btnPrevWeaponGroupDown ) {
                                List<WeaponGroupAPI> wgs = ps.getWeaponGroupsCopy();
                                int indx = wgs.indexOf(ps.getSelectedGroupAPI()) - 1;
                                if ( indx < 0 ) {
                                    indx = wgs.size() > 0 ? wgs.size() - 1 : 0;
                                }
                                ps.giveCommand(ShipCommand.SELECT_GROUP, null, indx);
                                btnPrevWeaponGroupDown = true;
                            }
                        } else if ( btnPrevWeaponGroupDown ) {
                            btnPrevWeaponGroupDown = false;
                        }
                    }
                    
                    if ( targeting == null || ps.getShipTarget() == null ) {
                        //center on player ship
                        CombatState cs = (CombatState) AppDriver.getInstance().getState(CombatState.STATE_ID);
                        cs.setVideoFeedSource(null);
                        cs.getViewMouseOffset().\u00D200000(0, 0);
                    } else {
                        //center on target
                        CombatState cs = (CombatState) AppDriver.getInstance().getState(CombatState.STATE_ID);
                        cs.setVideoFeedSource((com.fs.starfarer.combat.OoOO.oOOO.o) ps.getShipTarget());
                    }
                }
            }
        }
        
        ShipAPI ps = psCache.ps;
        wasShieldOn = ps != null && ps.getShield() != null && ps.getShield().isOn();
        wasShowingWarroom = engine.getCombatUI().isShowingCommandUI();
    }

    @Override
    public void renderInUICoords(ViewportAPI viewport) {
        super.renderInUICoords(viewport);
        
        if ( displayMenu ) {
            renderMenu(viewport);
        }
    }
    
    @Override
    public void renderInWorldCoords(ViewportAPI viewport) {
        super.renderInWorldCoords(viewport);
        if ( psCache.steeringController != null ) {
            psCache.steeringController.renderInWorldCoords(viewport, offsetFacingAngle);
        }
    }
    
    @Override
    public void advance(float amount, List<InputEventAPI> events) {
        super.advance(amount, events);
        
        //If an omni shield was raised during the last frame we change its facing based on the selected broadside if no target is selected and otherwise facing the target
        if ( !wasShieldOn ) {
            ShipAPI ps = psCache.ps;
            if ( ps != null ) {
                ShieldAPI s = ps.getShield();
                if ( s != null && s.isOn() && ((Ship)ps).getShield().isOmni() ) {
                    if ( ps.getShipTarget() == null ) {
                        ps.getShield().forceFacing(ps.getFacing() + offsetFacingAngle);
                    } else {
                        Vector2f v = Vector2f.sub(ps.getShipTarget().getLocation(), ps.getLocation(), new Vector2f());
                        ps.getShield().forceFacing(Utils.Object(v));
                    }
                }
            }
        }
    }
    
    protected void setDisplayMenu(boolean display) {
        if ( this.displayMenu != display ) {
            timeDilation(display,TDID_MENU);
            if ( this.displayMenu = display ) selectedBtnIndex = -1;
            CombatState cs = (CombatState) AppDriver.getInstance().getState(CombatState.STATE_ID);
            try {
                Field f = CombatState.class.getDeclaredField("hideHud");
                if ( !f.isAccessible() ) f.setAccessible(true);
                f.set(cs, this.displayMenu);
            } catch (Throwable t) {
                engine.getCombatUI().addMessage(0, "error: "+t.getMessage());
                Global.getLogger(SSMSControllerModPlugin.class).log(Level.ERROR, "Failed to hide HUD, ensure SSMSUnlock is installed!", t);
            }
        }
    }
    
    protected void renderMenu(ViewportAPI viewport) {
        float xMin = viewport.convertWorldXtoScreenX(viewport.getLLX()), 
                yMin = viewport.convertWorldYtoScreenY(viewport.getLLY()), 
                xMax = viewport.convertWorldXtoScreenX(viewport.getLLX() + viewport.getVisibleWidth()), 
                yMax = viewport.convertWorldYtoScreenY(viewport.getLLY() + viewport.getVisibleHeight());
        
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        
        if ( root == null ) {
            root = assembleMenu();
            root.resize(new Rectangle((int)xMin, (int)yMin, (int)xMax, (int)yMax));
        }
        
        if ( selectedBtnIndex == -1 && !nextMenuEntry ) {
            selectedBtnIndex = selectNextButton(root, -1);
        }
        
        if ( nextMenuEntry ) {
            selectedBtnIndex = selectNextButton(root, selectedBtnIndex);
            nextMenuEntry = false;
        } else if ( selectMenuEntry ) {
            selectMenuEntry = false;
            List<UIComponent_Button> btns = UIUtil.getInstance().findComponents(root, UIComponent_Button.class);
            if ( this.selectedBtnIndex >= 0 && this.selectedBtnIndex < btns.size() ) {
                btns.get(this.selectedBtnIndex).onClick();
            }
        }
        
        if ( root.isLayoutDirty() ) {
            root.pack();
        }
        root.getContext().pushStyle(UIContext.StyleProperty.alphaFactor, viewport.getAlphaMult());
        root.render();
        root.getContext().popStyle(UIContext.StyleProperty.alphaFactor);
    }
    
    protected int selectNextButton(UIComponent_Parent root, int currentlySelectedBtnIndex) {
        if ( root == null ) return -1;
        List<UIComponent_Column> clmns = UIUtil.getInstance().findComponents(root, UIComponent_Column.class);
        UIComponent_Column activeColumn = clmns.get(clmns.size()-1);
        List<UIComponent_Button> btns = UIUtil.getInstance().findComponents(root, UIComponent_Button.class);
        
        int btnsSkipped = 0;
        Iterator<UIComponent_Button> it = btns.iterator();
        while ( it.hasNext() ) {
            UIComponent_Button btn = it.next();
            deselectButton(btn);
            
            UIComponent_Parent p = btn.parentComponent();
            while ( p != null && p != activeColumn ) {
                p = p.parentComponent();
            }
            if ( p == null ) {
                btnsSkipped++;
                it.remove();
            }
        }
        currentlySelectedBtnIndex -= btnsSkipped;
        if ( currentlySelectedBtnIndex < -1 ) currentlySelectedBtnIndex = -1;
        if ( btns.isEmpty() ) {
            return -1;
        } else if ( ++currentlySelectedBtnIndex < btns.size() ) {
            selectButton(btns.get(currentlySelectedBtnIndex));
            return currentlySelectedBtnIndex + btnsSkipped;
        } else {
            selectButton(btns.get(0));
            return btnsSkipped;
        }
    }
    
    protected void deselectButton(UIComponent_Button btn) {
        btn.addStyle(UIContext.StyleProperty.textColor, UIContext.StyleProperty.textColor.initialValue);
        btn.addStyle(UIContext.StyleProperty.textMouseOverColor, UIContext.StyleProperty.textColor.initialValue);
    }
    
    protected void selectButton(UIComponent_Button btn) {
        btn.addStyle(UIContext.StyleProperty.textColor, UIContext.StyleProperty.textMouseOverColor.initialValue);
        btn.addStyle(UIContext.StyleProperty.textMouseOverColor, UIContext.StyleProperty.textMouseOverColor.initialValue);
    }
    
    protected UIComponent_Parent assembleMenu() {
        UIComponent_Parent menu = UIComponentParentFactory.getFactory(new UIComponent_Row(AlignmentHorizontal.left, AlignmentVertical.top))
                    .addChild(UIComponentParentFactory.getFactory(new UIComponent_Column(AlignmentHorizontal.left, AlignmentVertical.top))
                        .addChild(UIComponentFactory.getFactory(new UIComponent_Button("Pause") {
                                @Override
                                public void onClick() {
                                    engine.setPaused(!engine.isPaused());
                                    setDisplayMenu(false);
                                }
                            })
                            .setWidth(200, 200, 200)
                            .setHeight(30, 30, 30)
                            .finish())
                        .addChild(UIComponentFactory.getFactory(new UIComponent_Button("Warroom") {
                                @Override
                                public void onClick() {
                                    CombatState cs = (CombatState) AppDriver.getInstance().getState(CombatState.STATE_ID);
                                    cs.showWarroom();
                                    setDisplayMenu(false);
                                }
                            })
                            .setWidth(200, 200, 200)
                            .setHeight(30, 30, 30)
                            .finish())
                        .addChild(UIComponentFactory.getFactory(new UIComponent_Button("Autopilot") {
                                @Override
                                public void onClick() {
                                    CombatState cs = (CombatState) AppDriver.getInstance().getState(CombatState.STATE_ID);
                                    cs.setAutopilot(!cs.isAutopilotOn());
                                    setDisplayMenu(false);
                                }
                            })
                            .setWidth(200, 200, 200)
                            .setHeight(30, 30, 30)
                            .finish())
                        .addChild(UIComponentFactory.getFactory(new UIComponent_Button("Broadside") {
                                @Override
                                public void onClick() {
                                    UIComponent_Parent parent = parentComponent();
                                    while ( parent != null && !UIComponent_Row.class.isAssignableFrom(parent.getClass()) ) {
                                        parent = parent.parentComponent();
                                    }
                                    if ( parent != null ) {
                                        final UIComponent_Parent rowComponent = parent;
                                        rowComponent.addChild(UIComponentParentFactory.getFactory(new UIComponent_Column(AlignmentHorizontal.left, AlignmentVertical.top))
                                            .addChild(UIComponentFactory.getFactory(new UIComponent_Button("Front") {
                                                    @Override
                                                    public void onClick() {
                                                        offsetFacingAngle = 0;
                                                        rowComponent.removeChild(this.parentComponent());
                                                        setDisplayMenu(false);
                                                    }
                                                })
                                                .setWidth(200, 200, 200)
                                                .setHeight(30, 30, 30)
                                                .finish())
                                            .addChild(UIComponentFactory.getFactory(new UIComponent_Button("Right") {
                                                    @Override
                                                    public void onClick() {
                                                        offsetFacingAngle = 90f;
                                                        rowComponent.removeChild(this.parentComponent());
                                                        setDisplayMenu(false);
                                                    }
                                                })
                                                .setWidth(200, 200, 200)
                                                .setHeight(30, 30, 30)
                                                .finish())
                                            .addChild(UIComponentFactory.getFactory(new UIComponent_Button("Left") {
                                                    @Override
                                                    public void onClick() {
                                                        offsetFacingAngle = -90f;
                                                        rowComponent.removeChild(this.parentComponent());
                                                        setDisplayMenu(false);
                                                    }
                                                })
                                                .setWidth(200, 200, 200)
                                                .setHeight(30, 30, 30)
                                                .finish())
                                            .finish());
                                        selectedBtnIndex = selectNextButton(root, -1);
                                    }
                                }
                            })
                            .setWidth(200, 200, 200)
                            .setHeight(30, 30, 30)
                            .finish())
                        .addChild(UIComponentFactory.getFactory(new UIComponent_Button("Zoom") {
                                @Override
                                public void onClick() {
                                    UIComponent_Parent parent = parentComponent();
                                    while ( parent != null && !UIComponent_Row.class.isAssignableFrom(parent.getClass()) ) {
                                        parent = parent.parentComponent();
                                    }
                                    if ( parent != null ) {
                                        final UIComponent_Parent rowComponent = parent;
                                        rowComponent.addChild(UIComponentParentFactory.getFactory(new UIComponent_Column(AlignmentHorizontal.left, AlignmentVertical.top))
                                            .addChild(UIComponentFactory.getFactory(new UIComponent_Button("2x") {
                                                    @Override
                                                    public void onClick() {
                                                        setZoom(2f);
                                                        rowComponent.removeChild(this.parentComponent());
                                                        setDisplayMenu(false);
                                                    }
                                                })
                                                .setWidth(200, 200, 200)
                                                .setHeight(30, 30, 30)
                                                .finish())
                                            .addChild(UIComponentFactory.getFactory(new UIComponent_Button("3x") {
                                                    @Override
                                                    public void onClick() {
                                                        setZoom(3f);
                                                        rowComponent.removeChild(this.parentComponent());
                                                        setDisplayMenu(false);
                                                    }
                                                })
                                                .setWidth(200, 200, 200)
                                                .setHeight(30, 30, 30)
                                                .finish())
                                            .addChild(UIComponentFactory.getFactory(new UIComponent_Button("4x") {
                                                    @Override
                                                    public void onClick() {
                                                        setZoom(4f);
                                                        rowComponent.removeChild(this.parentComponent());
                                                        setDisplayMenu(false);
                                                    }
                                                })
                                                .setWidth(200, 200, 200)
                                                .setHeight(30, 30, 30)
                                                .finish())
                                            .finish());
                                        selectedBtnIndex = selectNextButton(root, -1);
                                    }
                                }
                            })
                            .setWidth(200, 200, 200)
                            .setHeight(30, 30, 30)
                            .finish())
                        .addChild(UIComponentFactory.getFactory(new UIComponent_Button("Full Assault") {
                                @Override
                                public void onClick() {
                                    CombatFleetManagerAPI fleetManager = engine.getFleetManager(FleetSide.PLAYER);
                                    if ( fleetManager != null ) {
                                        CombatTaskManagerAPI taskManager = fleetManager.getTaskManager(false);
                                        taskManager.setFullAssault(!taskManager.isFullAssault());
                                    }
                                    setDisplayMenu(false);
                                }
                            })
                            .setWidth(200, 200, 200)
                            .setHeight(30, 30, 30)
                            .finish())
                        .addChild(UIComponentFactory.getFactory(new UIComponent_Button("Full Retreat") {
                                @Override
                                public void onClick() {
                                    CombatFleetManagerAPI fleetManager = engine.getFleetManager(FleetSide.PLAYER);
                                    if ( fleetManager != null ) {
                                        CombatTaskManagerAPI taskManager = fleetManager.getTaskManager(false);
                                        taskManager.orderFullRetreat();
                                    }
                                    setDisplayMenu(false);
                                }
                            })
                            .setWidth(200, 200, 200)
                            .setHeight(30, 30, 30)
                            .finish())
                            .addChild(UIComponentFactory.getFactory(new UIComponent_Button(new Callable<String>() {
                                @Override
                                public String call() throws Exception {
                                    return enableControllerSteering ? "Steering Off" : "Steering On";
                                }
                            }) {
                                @Override
                                public void onClick() {
                                    enableControllerSteering = !enableControllerSteering;
                                }
                            })
                            .setWidth(200, 200, 200)
                            .setHeight(30, 30, 30)
                            .finish())
                        .addChild(UIComponentFactory.getFactory(new UIComponent_Button("Cancel") {
                                @Override
                                public void onClick() {
                                    setDisplayMenu(false);
                                }
                            })
                            .setWidth(200, 200, 200)
                            .setHeight(30, 30, 30)
                            .finish())
                        .finish())
                .finish();
        
        //container to center the menu on the screen
        return UIComponentParentFactory.getFactory(new UIComponent_Column(AlignmentHorizontal.middle, AlignmentVertical.middle)
                .setGrowHorizontal(true).setGrowVertical(true))
            .addChild(menu).setContext(new UIContext()).finish();
    }
    
    protected Vector2f targetFrontal(Vector2f vPosition, float range, float facing, Vector2f result) {
        float rad = (float) Math.toRadians(facing);
        result.set(vPosition.x + range * (float)Math.cos(rad), vPosition.y + range * (float)Math.sin(rad));
        return result;
    }
    
    protected Vector2f targetLeading(Vector2f vShooterPos, Vector2f vTargetPos, Vector2f vTargetVelocity, float projectileSpeed, Vector2f result) {
        if ( projectileSpeed == 0f ) {
            return result.set(vTargetPos);
        }
        if ( projectileSpeed >= 1e10f ) projectileSpeed = 1e10f;
        float a, b, c, t1, t2;
        a = Vector2f.dot(vTargetVelocity, vTargetVelocity) - projectileSpeed * projectileSpeed;
        Vector2f.sub(vTargetPos, vShooterPos, result);
        b = 2f * Vector2f.dot(result, vTargetVelocity);
        c = Vector2f.dot(result, result);
        float sqrt = (float)Math.sqrt(b*b-4*a*c);
        t1 = (-b + sqrt)/(2*a);
        t2 = (-b - sqrt)/(2*a);
        
        if ( t1 != Float.NaN && t1 > 0 ) {
            result.set(vTargetPos.x+t1*vTargetVelocity.x,vTargetPos.y+t1*vTargetVelocity.y);
            return result;
        } else if ( t2 != Float.NaN && t2 > 0 ) {
            result.set(vTargetPos.x+t2*vTargetVelocity.x,vTargetPos.y+t2*vTargetVelocity.y);
            return result;
        }
        return null;
    }
    
    protected void timeDilation(boolean active, String id) {
        if ( engine == null ) return;
        id = "SSMSQoLTimeDilationController_"+id;
        MutableStat.StatMod timeDilationModifier = engine.getTimeMult().getMultStatMod(id);
        if ( active ) {
            if ( timeDilationModifier == null ) {
                engine.getTimeMult().modifyMult(id, 0f);
            }
        } else {
            if ( timeDilationModifier != null ) {
                engine.getTimeMult().unmodifyMult(id);
            }
        }
    }
    
    protected boolean isValidTarget(ShipAPI ship) {
        return ship != null && !ship.isHulk() && ship.getOwner() != 100 && psCache.ps.getOwner() != ship.getOwner() && ship.isTargetable() && !ship.isDrone() && !ship.isFighter() && !ship.isShuttlePod();
    }
    
    protected List<ShipAPI> targetsByDistance(List<ShipAPI> ships, Vector2f measureFromPoint, FogOfWarAPI fogOfWar) {
        List<Pair<Float,ShipAPI>> shipsByDistance = new ArrayList<>();
        for (ShipAPI ship : ships) {
            if (isValidTarget(ship) && fogOfWar.isVisible(ship)) {
                shipsByDistance.add(new Pair<>(Vector2f.sub(ship.getLocation(), measureFromPoint, new Vector2f()).lengthSquared(),ship));
            }
        }
        Collections.sort(shipsByDistance, new Comparator<Pair<Float, ShipAPI>>() {
            @Override
            public int compare(Pair<Float, ShipAPI> o1, Pair<Float, ShipAPI> o2) {
                return o1.one.compareTo(o2.one);
            }
        });
        List<ShipAPI> orderedShips = new ArrayList<>(shipsByDistance.size());
        for ( Pair<Float,ShipAPI> p : shipsByDistance ) orderedShips.add(p.two);
        return orderedShips;
    }
}
