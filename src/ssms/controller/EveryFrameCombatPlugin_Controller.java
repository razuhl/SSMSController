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
import com.fs.starfarer.api.combat.BattleCreationContext;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.combat.CombatEngine;
import com.fs.starfarer.combat.CombatState;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Level;
import ssms.controller.inputScreens.InputScope;
import ssms.controller.inputScreens.InputScope_Battle;
import ssms.controller.inputScreens.InputScreenManager;

/**
 * Handles all controls for combat via game controller.
 * 
 * @author Malte Schulze
 */
public class EveryFrameCombatPlugin_Controller extends BaseEveryFrameCombatPlugin {
    protected CombatEngineAPI engine;
    protected float nextLog;
    protected boolean wasShowingWarroom = false, skipFrame = true;

    public EveryFrameCombatPlugin_Controller() {
        //Global.getLogger(SSMSControllerModPlugin.class).log(Level.ERROR, "created every frame");
    }
    
    @Override
    public void init(CombatEngineAPI engine) {
        //Global.getLogger(SSMSControllerModPlugin.class).log(Level.ERROR, "initialized every frame");
        this.engine = engine;
        nextLog = 0;
        skipFrame = true;
        
        /*if ( engine != null ) {
            StringBuilder sb = new StringBuilder();
            sb.append("isCombatOver: ").append(engine.isCombatOver()).append("\n")
                .append("isEnemyInFullRetreat: ").append(engine.isEnemyInFullRetreat()).append("\n")
                .append("isFleetsInContact: ").append(engine.isFleetsInContact()).append("\n")
                .append("isInCampaign: ").append(engine.isInCampaign()).append("\n")
                .append("isInCampaignSim: ").append(engine.isInCampaignSim()).append("\n")
                .append("isInFastTimeAdvance: ").append(engine.isInFastTimeAdvance()).append("\n")
                .append("isMission: ").append(engine.isMission()).append("\n")
                .append("isPaused: ").append(engine.isPaused()).append("\n")
                .append("isSimulation: ").append(engine.isSimulation()).append("\n")
                .append("isUIAutopilotOn: ").append(engine.isUIAutopilotOn()).append("\n")
                .append("isUIShowingDialog: ").append(engine.isUIShowingDialog()).append("\n")
                .append("isUIShowingHUD: ").append(engine.isUIShowingHUD()).append("\n")
                .append("getCombatUI: ").append(engine.getCombatUI()).append("\n")
                .append("getMissionId: ").append(engine.getMissionId()).append("\n")
            ;
            sb.append("--- CustomData ---").append("\n");
            for ( Map.Entry<String,Object> e : engine.getCustomData().entrySet() ) {
                sb.append("\t").append(e.getKey()).append(": ").append(e.getValue()).append("\n");
            }
            Global.getLogger(SSMSControllerModPlugin.class).log(Level.INFO, sb.toString());
        }*/
        
        if ( engine != null && engine.getContext() != null && (engine.isSimulation() || (engine.getCombatUI() != null && CombatState.class.isAssignableFrom(engine.getCombatUI().getClass())))
                && SSMSControllerModPlugin.controller != null && SSMSControllerModPlugin.controller.mapping != null ) {
            if ( !InputScreenManager.getInstance().transitionToScope("Battle", engine) ) {
                Global.getLogger(SSMSControllerModPlugin.class).log(Level.ERROR, "Failed to transition into battle scope!");
                InputScreenManager.getInstance().transitionToScope("NoScope");
            } else {
                skipFrame = false;
            }
        }
    }
    
    protected ShipAPI getControlledShip() {
        return engine.getPlayerShip();
    }
    
    protected boolean isControllerConfigured(HandlerController handler) {
        return handler != null && handler.mapping != null;
    }

    @Override
    public void processInputPreCoreControls(float amount, List<InputEventAPI> events) {
        if ( skipFrame ) return;
        
        InputScreenManager man = InputScreenManager.getInstance();
        InputScope_Battle battleScope = (InputScope_Battle) man.getCurrentScope();
        HandlerController handler = SSMSControllerModPlugin.controller;
        handler.poll();
        
        if ( !battleScope.engine.getCombatUI().isShowingCommandUI() ) {
            if ( wasShowingWarroom ) {
                battleScope.adjustZoom();
            }
            
            ShipAPI ps = getControlledShip();
            if ( ps != null && battleScope.engine.isEntityInPlay(ps) ) {
                if ( !battleScope.psCache.isForShip(ps) ) {
                    battleScope.psCache.setShip(ps, handler, battleScope.engine);
                }
            }
        }
        
        wasShowingWarroom = battleScope.engine.getCombatUI().isShowingCommandUI();
        if ( battleScope.engine.isPaused() ) {
            man.refreshIndicatorTimeout();
        }
        
        //TODO inputs for the warroom
        //TODO menu entries for switching ships(camera jumps to the targeted eligeble ship like targeting next and previous then selecting to pick a ship)
        //TODO menu entry for ending combat/simulation
            
        man.startFrame();
        man.preInput(amount);
    }
    
    @Override
    public void advance(float amount, List<InputEventAPI> events) {
        if ( skipFrame ) {
            /*Global.getLogger(SSMSControllerModPlugin.class).log(Level.ERROR, 
                "advance");*/
            return;
        }
        super.advance(amount, events);
        InputScreenManager.getInstance().postIntput(amount);
    }

    @Override
    public void renderInWorldCoords(ViewportAPI viewport) {
        if ( skipFrame ) {
            /*Global.getLogger(SSMSControllerModPlugin.class).log(Level.ERROR, 
                "renderInWorldCoords");*/
            return;
        }
        super.renderInWorldCoords(viewport);
        InputScreenManager.getInstance().renderInWorld(viewport);
    }
    
    @Override
    public void renderInUICoords(ViewportAPI viewport) {
        if ( skipFrame ) {
            /*Global.getLogger(SSMSControllerModPlugin.class).log(Level.ERROR, 
                "renderInUICoords");*/
            return;
        }
        super.renderInUICoords(viewport);
        InputScreenManager.getInstance().renderUI(viewport);
        InputScreenManager.getInstance().stopFrame();
    }
}
