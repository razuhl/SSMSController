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

import java.util.HashMap;
import java.util.Map;
import org.lwjgl.input.Controller;
import org.lwjgl.util.input.ControllerAdapter;
import org.lwjgl.util.vector.ReadableVector2f;
import org.lwjgl.util.vector.Vector2f;

/**
 * Provides abstracted access to a game controller based on a {@link ssms.controller.ControllerMapping ControllerMapping}. 
 * 
 * @author Malte Schulze
 */
public class HandlerController {
    final public Controller controller;
    final public ControllerMapping mapping;
    public float axisBtnConversionDeadzone, joystickDeadzone;
    protected int axisAccelerating, axisHeadingX, axisHeadingY, axisToggleFightersAndAutofire, axisSwitchWeaponGroup, axisStrafing,
            btnVenting, btnShieldOrCloak, btnUseSystem, btnFire, btnAlternateSteering, btnSelect, btnShowMenu,
            btnAccelerate, btnAccelerateBackwards, btnToggleFighters, btnToggleAutofire, btnNextWeaponGroup, btnPrevWeaponGroup, 
            btnShowTargeting, btnClearTarget, btnSelectTarget, btnStrafeRight, btnStrafeLeft,
            axisCycleMenuEntries, btnNextMenuEntry, btnPrevMenuEntry,
            axisCycleTargets, btnNextTarget, btnPrevTarget;
    public boolean accelerationInverted, fightersAutofireInverted, weaponGroupsInverted, strafeInverted, cycleMenuInverted, cycleTargetsInverted;
    protected Vector2f heading = new Vector2f();

    public HandlerController() {
        this(new ControllerAdapter(), null);
    }

    public HandlerController(Controller controller, ControllerMapping mapping) {
        this.controller = controller;
        this.mapping = mapping;
        Map<String,Integer> axisIndices = new HashMap<>();
        for ( int i = 0; i < controller.getAxisCount(); i++ ) {
            axisIndices.put(controller.getAxisName(i), i);
        }
        
        if ( mapping != null ) {
            this.axisHeadingX = getIndexCoercingNull(axisIndices.get(mapping.axisSteeringX),controller.getAxisCount());
            this.axisHeadingY = getIndexCoercingNull(axisIndices.get(mapping.axisSteeringY),controller.getAxisCount());

            this.btnAlternateSteering = getIndexCoercingNull(mapping.btnAltSteering,controller.getButtonCount());
            this.btnFire = getIndexCoercingNull(mapping.btnFire,controller.getButtonCount());
            this.btnShowMenu = getIndexCoercingNull(mapping.btnMenuOpen,controller.getButtonCount());
            this.btnSelect = getIndexCoercingNull(mapping.btnSelectMenuItem,controller.getButtonCount());
            this.btnShieldOrCloak = getIndexCoercingNull(mapping.btnShield,controller.getButtonCount());
            this.btnUseSystem = getIndexCoercingNull(mapping.btnUseSystem,controller.getButtonCount());
            this.btnVenting = getIndexCoercingNull(mapping.btnVenting,controller.getButtonCount());
            this.btnShowTargeting = getIndexCoercingNull(mapping.btnShowTargeting,controller.getButtonCount());
            this.btnClearTarget = getIndexCoercingNull(mapping.btnClearTarget,controller.getButtonCount());
            this.btnSelectTarget = getIndexCoercingNull(mapping.btnSelectTarget,controller.getButtonCount());

            if ( mapping.cycleMenuEntries != null ) {
                this.axisCycleMenuEntries = getIndexCoercingNull(axisIndices.get(mapping.cycleMenuEntries.axis),controller.getAxisCount());
                this.cycleMenuInverted = mapping.cycleMenuEntries.inverted;
                this.btnNextMenuEntry = getIndexCoercingNull(mapping.cycleMenuEntries.btnA,controller.getButtonCount());
                this.btnPrevMenuEntry = getIndexCoercingNull(mapping.cycleMenuEntries.btnB,controller.getButtonCount());
                if ( this.cycleMenuInverted ) {
                    int t = this.btnNextMenuEntry;
                    this.btnNextMenuEntry = this.btnPrevMenuEntry;
                    this.btnPrevMenuEntry = t;
                }
            } else {
                this.cycleMenuInverted = false;
                this.axisCycleMenuEntries = this.btnNextMenuEntry = this.btnPrevMenuEntry = -1;
            }
            
            if ( mapping.cycleTargets != null ) {
                this.axisCycleTargets = getIndexCoercingNull(axisIndices.get(mapping.cycleTargets.axis),controller.getAxisCount());
                this.cycleTargetsInverted = mapping.cycleTargets.inverted;
                this.btnNextTarget = getIndexCoercingNull(mapping.cycleTargets.btnA,controller.getButtonCount());
                this.btnPrevTarget = getIndexCoercingNull(mapping.cycleTargets.btnB,controller.getButtonCount());
                if ( this.cycleTargetsInverted ) {
                    int t = this.btnNextTarget;
                    this.btnNextTarget = this.btnPrevTarget;
                    this.btnPrevTarget = t;
                }
            } else {
                this.cycleTargetsInverted = false;
                this.axisCycleTargets = this.btnNextTarget = this.btnPrevTarget = -1;
            }
            
            if ( mapping.acceleration != null ) {
                this.axisAccelerating = getIndexCoercingNull(axisIndices.get(mapping.acceleration.axis),controller.getAxisCount());
                this.accelerationInverted = mapping.acceleration.inverted;
                this.btnAccelerate = getIndexCoercingNull(mapping.acceleration.btnA,controller.getButtonCount());
                this.btnAccelerateBackwards = getIndexCoercingNull(mapping.acceleration.btnB,controller.getButtonCount());
                if ( this.accelerationInverted ) {
                    int t = this.btnAccelerate;
                    this.btnAccelerate = this.btnAccelerateBackwards;
                    this.btnAccelerateBackwards = t;
                }
            } else {
                this.accelerationInverted = false;
                this.axisAccelerating = this.btnAccelerate = this.btnAccelerateBackwards = -1;
            }
            
            if ( mapping.strafe != null ) {
                this.axisStrafing = getIndexCoercingNull(axisIndices.get(mapping.strafe.axis),controller.getAxisCount());
                this.strafeInverted = mapping.strafe.inverted;
                this.btnStrafeLeft = getIndexCoercingNull(mapping.strafe.btnA,controller.getButtonCount());
                this.btnStrafeRight = getIndexCoercingNull(mapping.strafe.btnB,controller.getButtonCount());
                if ( this.strafeInverted ) {
                    int t = this.btnStrafeLeft;
                    this.btnStrafeLeft = this.btnStrafeRight;
                    this.btnStrafeRight = t;
                }
            } else {
                this.strafeInverted = false;
                this.axisStrafing = this.btnStrafeLeft = this.btnStrafeRight = -1;
            }

            if ( mapping.fightersAutofire != null ) {
                this.axisToggleFightersAndAutofire = getIndexCoercingNull(axisIndices.get(mapping.fightersAutofire.axis),controller.getAxisCount());
                this.fightersAutofireInverted = mapping.fightersAutofire.inverted;
                this.btnToggleFighters = getIndexCoercingNull(mapping.fightersAutofire.btnA,controller.getButtonCount());
                this.btnToggleAutofire = getIndexCoercingNull(mapping.fightersAutofire.btnB,controller.getButtonCount());
                if ( this.fightersAutofireInverted ) {
                    int t = this.btnToggleFighters;
                    this.btnToggleFighters = this.btnToggleAutofire;
                    this.btnToggleAutofire = t;
                }
            } else {
                this.fightersAutofireInverted = false;
                this.axisToggleFightersAndAutofire = this.btnToggleFighters = this.btnToggleAutofire = -1;
            }

            if ( mapping.weaponGroups != null ) {
                this.axisSwitchWeaponGroup = getIndexCoercingNull(axisIndices.get(mapping.weaponGroups.axis),controller.getAxisCount());
                this.weaponGroupsInverted = mapping.weaponGroups.inverted;
                this.btnNextWeaponGroup = getIndexCoercingNull(mapping.weaponGroups.btnA,controller.getButtonCount());
                this.btnPrevWeaponGroup = getIndexCoercingNull(mapping.weaponGroups.btnB,controller.getButtonCount());
                if ( this.weaponGroupsInverted ) {
                    int t = this.btnNextWeaponGroup;
                    this.btnNextWeaponGroup = this.btnPrevWeaponGroup;
                    this.btnPrevWeaponGroup = t;
                }
            } else {
                this.weaponGroupsInverted = false;
                this.axisSwitchWeaponGroup = this.btnNextWeaponGroup = this.btnPrevWeaponGroup = -1;
            }
            
            this.axisBtnConversionDeadzone = mapping.axisBtnConversionDeadzone;
            this.joystickDeadzone = mapping.joystickDeadzone * mapping.joystickDeadzone;
        } else {
            this.axisHeadingX = this.axisHeadingY = this.axisAccelerating = this.axisStrafing = this.axisToggleFightersAndAutofire = this.axisSwitchWeaponGroup = -1;
            this.axisCycleTargets = this.btnNextTarget = this.btnPrevTarget = 
                    this.axisCycleMenuEntries = this.btnNextMenuEntry = this.btnPrevMenuEntry = 
                    this.btnAlternateSteering = this.btnFire = this.btnShowMenu = this.btnSelect = this.btnShieldOrCloak = 
                    this.btnUseSystem = this.btnVenting = this.btnAccelerate = 
                    this.btnAccelerateBackwards = this.btnToggleFighters = this.btnToggleAutofire = this.btnNextWeaponGroup = this.btnPrevWeaponGroup =
                    this.btnNextMenuEntry = this.btnShowTargeting = this.btnClearTarget = this.btnStrafeLeft = this.btnStrafeRight = -1;

            this.cycleTargetsInverted = this.cycleMenuInverted = this.accelerationInverted = this.fightersAutofireInverted = this.weaponGroupsInverted = this.strafeInverted = false;
            this.axisBtnConversionDeadzone = 0.85f;
            this.joystickDeadzone = 0.0625f;
        }
        
        if ( axisAccelerating >= 0 ) {
            controller.setDeadZone(axisAccelerating, axisBtnConversionDeadzone);
        }
        if ( axisStrafing >= 0 ) {
            controller.setDeadZone(axisStrafing, axisBtnConversionDeadzone);
        }
        if ( axisToggleFightersAndAutofire >= 0 ) {
            controller.setDeadZone(axisToggleFightersAndAutofire, axisBtnConversionDeadzone);
        }
        if ( axisSwitchWeaponGroup >= 0 ) {
            controller.setDeadZone(axisSwitchWeaponGroup, axisBtnConversionDeadzone);
        }
        if ( axisHeadingX >= 0 ) {
            controller.setDeadZone(axisHeadingX, 0f);
        }
        if ( axisHeadingY >= 0 ) {
            controller.setDeadZone(axisHeadingY, 0f);
        }
    }
    
    protected final int getIndexCoercingNull(Integer value, int below) {
        if ( value == null ) return -1;
        if ( value >= below ) return -1;
        return value;
    }
    
    public void poll() {
        this.controller.poll();
    }
    
    public boolean isAccelerating() {
        return ( btnAccelerate >= 0 ? controller.isButtonPressed(btnAccelerate) : false ) || (accelerationInverted ? isAcceleratingBackwardsViaAxis() : isAcceleratingViaAxis());
    }
    
    protected boolean isAcceleratingViaAxis() {
        return axisAccelerating >= 0 ? -controller.getAxisValue(axisAccelerating) > axisBtnConversionDeadzone : false;
    }
    
    public boolean isAcceleratingBackwards() {
        return ( btnAccelerateBackwards >= 0 ? controller.isButtonPressed(btnAccelerateBackwards) : false ) || (accelerationInverted ? isAcceleratingViaAxis() : isAcceleratingBackwardsViaAxis());
    }
    
    protected boolean isAcceleratingBackwardsViaAxis() {
        return axisAccelerating >= 0 ? controller.getAxisValue(axisAccelerating) > axisBtnConversionDeadzone : false;
    }
    
    public boolean isStrafeLeft() {
        return ( btnStrafeLeft >= 0 ? controller.isButtonPressed(btnStrafeLeft) : false ) || (strafeInverted ? isStrafeRightViaAxis() : isStrafeLeftViaAxis());
    }
    
    protected boolean isStrafeLeftViaAxis() {
        return axisStrafing >= 0 ? controller.getAxisValue(axisStrafing) > axisBtnConversionDeadzone : false;
    }
    
    public boolean isStrafeRight() {
        return ( btnStrafeRight >= 0 ? controller.isButtonPressed(btnStrafeRight) : false ) || (strafeInverted ? isStrafeLeftViaAxis() : isStrafeRightViaAxis());
    }
    
    protected boolean isStrafeRightViaAxis() {
        return axisStrafing >= 0 ? -controller.getAxisValue(axisStrafing) > axisBtnConversionDeadzone : false;
    }

    public ReadableVector2f getHeading() {
        //TODO we could clamp the steering to something like 120 distinct values so that the directional input is more stable.
        //custom deadzone that takes into account the length of the vector to determine if it should be zero. That way we can steer with full precision in 360° 
        //but ignore a poorly resting joystick.
        heading.x = axisHeadingX >= 0 ? controller.getAxisValue(axisHeadingX) : 0f;
        heading.y = axisHeadingY >= 0 ? -controller.getAxisValue(axisHeadingY) : 0f;
        if ( heading.lengthSquared() < joystickDeadzone ) {
            heading.x = 0;
            heading.y = 0;
        }
        return heading;
    }
    
    public boolean isVenting() {
        return btnVenting >= 0 ? controller.isButtonPressed(btnVenting) : false;
    }
    
    public boolean isShieldOrCloak() {
        return btnShieldOrCloak >= 0 ? controller.isButtonPressed(btnShieldOrCloak) : false;
    }
    
    public boolean isUseSystem() {
        return btnUseSystem >= 0 ? controller.isButtonPressed(btnUseSystem) : false;
    }

    public boolean isFire() {
        return btnFire >= 0 ? controller.isButtonPressed(btnFire) : false;
    }

    public boolean isToggleFighters() {
        return ( btnToggleFighters >= 0 ? controller.isButtonPressed(btnToggleFighters) : false ) || (fightersAutofireInverted ? isToggleAutofireViaAxis() : isToggleFightersViaAxis());
    }
    
    protected boolean isToggleFightersViaAxis() {
        return axisToggleFightersAndAutofire >= 0 ? controller.getAxisValue(axisToggleFightersAndAutofire) > axisBtnConversionDeadzone : false;
    }
    
    public boolean isToggleAutofire() {
        return ( btnToggleAutofire >= 0 ? controller.isButtonPressed(btnToggleAutofire) : false ) || (fightersAutofireInverted ? isToggleFightersViaAxis() : isToggleAutofireViaAxis());
    }
    
    protected boolean isToggleAutofireViaAxis() {
        return axisToggleFightersAndAutofire >= 0 ? -controller.getAxisValue(axisToggleFightersAndAutofire) > axisBtnConversionDeadzone : false;
    }

    public boolean isSelectNextWeaponGroup() {
        return ( btnNextWeaponGroup >= 0 ? controller.isButtonPressed(btnNextWeaponGroup) : false ) || (weaponGroupsInverted ? isSelectPreviousWeaponGroupViaAxis() : isSelectNextWeaponGroupViaAxis());
    }
    
    protected boolean isSelectNextWeaponGroupViaAxis() {
        return axisSwitchWeaponGroup >= 0 ? controller.getAxisValue(axisSwitchWeaponGroup) > axisBtnConversionDeadzone : false;
    }
    
    public boolean isSelectPreviousWeaponGroup() {
        return ( btnPrevWeaponGroup >= 0 ? controller.isButtonPressed(btnPrevWeaponGroup) : false ) || (weaponGroupsInverted ? isSelectNextWeaponGroupViaAxis() : isSelectPreviousWeaponGroupViaAxis());
    }
    
    protected boolean isSelectPreviousWeaponGroupViaAxis() {
        return axisSwitchWeaponGroup >= 0 ? -controller.getAxisValue(axisSwitchWeaponGroup) > axisBtnConversionDeadzone : false;
    }

    public boolean isAlternateSteering() {
        return btnAlternateSteering >= 0 ? controller.isButtonPressed(btnAlternateSteering) : false;
    }

    public boolean isShowMenu() {
        return btnShowMenu >= 0 ? controller.isButtonPressed(btnShowMenu) : false;
    }
    
    public boolean isSelectMenuEntry() {
        return btnSelect >= 0 ? controller.isButtonPressed(btnSelect) : false;
    }
    
    public boolean isShowTargeting() {
        return btnShowTargeting >= 0 ? controller.isButtonPressed(btnShowTargeting) : false;
    }
    
    public boolean isClearTarget() {
        return btnClearTarget >= 0 ? controller.isButtonPressed(btnClearTarget) : false;
    }
    
    public boolean isSelectTarget() {
        return btnSelectTarget >= 0 ? controller.isButtonPressed(btnSelectTarget) : false;
    }
    
    public boolean isNextTarget() {
        return ( btnNextTarget >= 0 ? controller.isButtonPressed(btnNextTarget) : false ) || (cycleTargetsInverted ? isPrevTargetViaAxis() : isNextTargetViaAxis());
    }
    
    protected boolean isNextTargetViaAxis() {
        return axisCycleTargets >= 0 ? -controller.getAxisValue(axisCycleTargets) > axisBtnConversionDeadzone : false;
    }
    
    public boolean isPrevTarget() {
        return ( btnPrevTarget >= 0 ? controller.isButtonPressed(btnPrevTarget) : false ) || (cycleTargetsInverted ? isNextTargetViaAxis() : isPrevTargetViaAxis());
    }
    
    protected boolean isPrevTargetViaAxis() {
        return axisCycleTargets >= 0 ? controller.getAxisValue(axisCycleTargets) > axisBtnConversionDeadzone : false;
    }
    
    public boolean isNextMenuEntry() {
        return ( btnNextMenuEntry >= 0 ? controller.isButtonPressed(btnNextMenuEntry) : false ) || (cycleMenuInverted ? isPrevMenuEntryViaAxis() : isNextMenuEntryViaAxis());
    }
    
    protected boolean isNextMenuEntryViaAxis() {
        return axisCycleMenuEntries >= 0 ? -controller.getAxisValue(axisCycleMenuEntries) > axisBtnConversionDeadzone : false;
    }
    
    public boolean isPrevMenuEntry() {
        return ( btnPrevMenuEntry >= 0 ? controller.isButtonPressed(btnPrevMenuEntry) : false ) || (cycleMenuInverted ? isNextMenuEntryViaAxis() : isPrevMenuEntryViaAxis());
    }
    
    protected boolean isPrevMenuEntryViaAxis() {
        return axisCycleMenuEntries >= 0 ? controller.getAxisValue(axisCycleMenuEntries) > axisBtnConversionDeadzone : false;
    }
}
