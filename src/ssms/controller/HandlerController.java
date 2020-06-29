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
 *
 * @author Malte Schulze
 */
public class HandlerController {
    final public Controller controller;
    public float axisBtnConversionDeadzone, joystickDeadzone;
    protected int axisAccelerating, axisHeadingX, axisHeadingY, axisToggleFightersAndAutofire, axisSwitchWeaponGroup,
            btnTargetNxt, btnTargetPrevious, btnVenting, btnShieldOrCloak, btnUseSystem, btnFire, btnAlternateSteering, btnSelect, btnShowMenu,
            btnAccelerate, btnAccelerateBackwards, btnToggleFighters, btnToggleAutofire, btnNextWeaponGroup, btnPrevWeaponGroup;
    public boolean accelerationInverted, fightersAutofireInverted, weaponGroupsInverted;
    protected Vector2f heading = new Vector2f();

    public HandlerController() {
        this(new ControllerAdapter(), null);
    }

    public HandlerController(Controller controller, ControllerMapping mapping) {
        this.controller = controller;
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
            this.btnTargetNxt = getIndexCoercingNull(mapping.btnTargetNext,controller.getButtonCount());
            this.btnTargetPrevious = getIndexCoercingNull(mapping.btnTargetPrev,controller.getButtonCount());
            this.btnUseSystem = getIndexCoercingNull(mapping.btnUseSystem,controller.getButtonCount());
            this.btnVenting = getIndexCoercingNull(mapping.btnVenting,controller.getButtonCount());

            this.axisAccelerating = getIndexCoercingNull(axisIndices.get(mapping.acceleration.axis),controller.getAxisCount());
            this.accelerationInverted = mapping.acceleration.inverted;
            this.btnAccelerate = getIndexCoercingNull(mapping.acceleration.btnA,controller.getButtonCount());
            this.btnAccelerateBackwards = getIndexCoercingNull(mapping.acceleration.btnB,controller.getButtonCount());
            if ( this.accelerationInverted ) {
                int t = this.btnAccelerate;
                this.btnAccelerate = this.btnAccelerateBackwards;
                this.btnAccelerateBackwards = t;
            }

            this.axisToggleFightersAndAutofire = getIndexCoercingNull(axisIndices.get(mapping.fightersAutofire.axis),controller.getAxisCount());
            this.fightersAutofireInverted = mapping.fightersAutofire.inverted;
            this.btnToggleFighters = getIndexCoercingNull(mapping.fightersAutofire.btnA,controller.getButtonCount());
            this.btnToggleAutofire = getIndexCoercingNull(mapping.fightersAutofire.btnB,controller.getButtonCount());
            if ( this.fightersAutofireInverted ) {
                int t = this.btnToggleFighters;
                this.btnToggleFighters = this.btnToggleAutofire;
                this.btnToggleAutofire = t;
            }

            this.axisSwitchWeaponGroup = getIndexCoercingNull(axisIndices.get(mapping.weaponGroups.axis),controller.getAxisCount());
            this.weaponGroupsInverted = mapping.weaponGroups.inverted;
            this.btnNextWeaponGroup = getIndexCoercingNull(mapping.weaponGroups.btnA,controller.getButtonCount());
            this.btnPrevWeaponGroup = getIndexCoercingNull(mapping.weaponGroups.btnB,controller.getButtonCount());
            if ( this.weaponGroupsInverted ) {
                int t = this.btnNextWeaponGroup;
                this.btnNextWeaponGroup = this.btnPrevWeaponGroup;
                this.btnPrevWeaponGroup = t;
            }
            this.axisBtnConversionDeadzone = mapping.axisBtnConversionDeadzone;
            this.joystickDeadzone = mapping.joystickDeadzone * mapping.joystickDeadzone;
        } else {
            this.axisHeadingX = this.axisHeadingY = this.axisAccelerating = this.axisToggleFightersAndAutofire = this.axisSwitchWeaponGroup = -1;

            this.btnAlternateSteering = this.btnFire = this.btnShowMenu = this.btnSelect = this.btnShieldOrCloak = 
                    this.btnTargetNxt = this.btnTargetPrevious = this.btnUseSystem = this.btnVenting = this.btnAccelerate = 
                    this.btnAccelerateBackwards = this.btnToggleFighters = this.btnToggleAutofire = this.btnNextWeaponGroup = this.btnPrevWeaponGroup = -1;

            this.accelerationInverted = this.fightersAutofireInverted = this.weaponGroupsInverted = false;
            this.axisBtnConversionDeadzone = 0.85f;
            this.joystickDeadzone = 0.0625f;
        }
        
        if ( axisAccelerating >= 0 ) {
            controller.setDeadZone(axisAccelerating, axisBtnConversionDeadzone);
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
        return ( btnAccelerate >= 0 ? controller.isButtonPressed(btnAccelerate) : false ) || isAcceleratingViaAxis();
    }
    
    protected boolean isAcceleratingViaAxis() {
        return axisAccelerating >= 0 ? controller.getAxisValue(axisAccelerating) > axisBtnConversionDeadzone : false;
    }
    
    public boolean isAcceleratingBackwards() {
        return ( btnAccelerateBackwards >= 0 ? controller.isButtonPressed(btnAccelerateBackwards) : false ) || isAcceleratingBackwardsViaAxis();
    }
    
    protected boolean isAcceleratingBackwardsViaAxis() {
        return axisAccelerating >= 0 ? -controller.getAxisValue(axisAccelerating) > axisBtnConversionDeadzone : false;
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
    
    public boolean isTargetNext() {
        return btnTargetNxt >= 0 ? controller.isButtonPressed(btnTargetNxt) : false;
    }
    
    public boolean isTargetPrevious() {
        return btnTargetPrevious >= 0 ? controller.isButtonPressed(btnTargetPrevious) : false;
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
        return ( btnToggleFighters >= 0 ? controller.isButtonPressed(btnToggleFighters) : false ) || isToggleFightersViaAxis();
    }
    
    protected boolean isToggleFightersViaAxis() {
        return axisToggleFightersAndAutofire >= 0 ? controller.getAxisValue(axisToggleFightersAndAutofire) > axisBtnConversionDeadzone : false;
    }
    
    public boolean isToggleAutofire() {
        return ( btnToggleAutofire >= 0 ? controller.isButtonPressed(btnToggleAutofire) : false ) || isToggleAutofireViaAxis();
    }
    
    protected boolean isToggleAutofireViaAxis() {
        return axisToggleFightersAndAutofire >= 0 ? -controller.getAxisValue(axisToggleFightersAndAutofire) > axisBtnConversionDeadzone : false;
    }

    public boolean isSelectNextWeaponGroup() {
        return ( btnNextWeaponGroup >= 0 ? controller.isButtonPressed(btnNextWeaponGroup) : false ) || isSelectNextWeaponGroupViaAxis();
    }
    
    protected boolean isSelectNextWeaponGroupViaAxis() {
        return axisSwitchWeaponGroup >= 0 ? controller.getAxisValue(axisSwitchWeaponGroup) > axisBtnConversionDeadzone : false;
    }
    
    public boolean isSelectPreviousWeaponGroup() {
        return ( btnPrevWeaponGroup >= 0 ? controller.isButtonPressed(btnPrevWeaponGroup) : false ) || isSelectPreviousWeaponGroupViaAxis();
    }
    
    protected boolean isSelectPreviousWeaponGroupViaAxis() {
        return axisSwitchWeaponGroup >= 0 ? -controller.getAxisValue(axisSwitchWeaponGroup) > axisBtnConversionDeadzone : false;
    }

    public boolean isAlternateSteering() {
        return btnAlternateSteering >= 0 ? controller.isButtonPressed(btnAlternateSteering) : false;
    }

    public boolean isSelect() {
        return btnSelect >= 0 ? controller.isButtonPressed(btnSelect) : false;
    }

    public boolean isShowMenu() {
        return btnShowMenu >= 0 ? controller.isButtonPressed(btnShowMenu) : false;
    }
}
