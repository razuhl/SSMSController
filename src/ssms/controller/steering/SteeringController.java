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
package ssms.controller.steering;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ViewportAPI;
import ssms.controller.HandlerController;

/**
 *
 * @author Malte Schulze
 */
public interface SteeringController {
    boolean activate(ShipAPI playerShip, HandlerController controller, CombatEngineAPI engine);
    void onTargetSelected();
    boolean isTargetValid();
    void steer(float timeAdvanced, float offsetFacingAngle);
    void renderInWorldCoords(ViewportAPI viewport, float offsetFacingAngle);
}
