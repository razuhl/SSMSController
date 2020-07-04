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

/**
 * Stores information on how to interpret game controller signals.
 * 
 * @author Malte Schulze
 */
public class ControllerMapping {
    public String deviceName, axisSteeringX, axisSteeringY;
    public Integer btnTargetNext, btnTargetPrev, btnVenting, btnShield, btnUseSystem, btnFire, btnMenuOpen, btnSelectMenuItem, btnAltSteering;
    public AxisOrButton acceleration = new AxisOrButton(null, false, null, null), 
            weaponGroups = new AxisOrButton(null, false, null, null), 
            fightersAutofire = new AxisOrButton(null, false, null, null);
    public float axisBtnConversionDeadzone = 0.85f, joystickDeadzone = 0.25f;
    public Map<String,Object> customProperties = new HashMap<>();
    
    /**
     * Container that holds information for two exclusive or functionalities. They can be mapped to an axis or two buttons. 
     * This increases the flexibility for different controller layouts.
     */
    static public class AxisOrButton {
        public String axis;
        public boolean inverted;
        public Integer btnA, btnB;

        public AxisOrButton(String axis, boolean inverted, Integer btnA, Integer btnB) {
            this.axis = axis;
            this.inverted = inverted;
            this.btnA = btnA;
            this.btnB = btnB;
        }
    }
}
