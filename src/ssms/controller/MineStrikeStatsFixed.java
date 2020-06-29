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

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipwideAIFlags;
import com.fs.starfarer.api.impl.combat.MineStrikeStats;
import com.fs.starfarer.api.plugins.ShipSystemStatsScript;
import com.fs.starfarer.api.util.Misc;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.lwjgl.util.vector.Vector2f;

/**
 *
 * @author Malte Schulze
 */
public class MineStrikeStatsFixed extends MineStrikeStats {
    @Override
    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        ShipAPI ship = null;
        if (stats.getEntity() instanceof ShipAPI) {
            ship = (ShipAPI) stats.getEntity();
        } else {
            return;
        }
        float jitterLevel = effectLevel;
        if (state == ShipSystemStatsScript.State.OUT) {
            jitterLevel *= jitterLevel;
        }
        float maxRangeBonus = 25.0F;
        float jitterRangeBonus = jitterLevel * maxRangeBonus;
        ship.setJitterUnder(this, JITTER_UNDER_COLOR, jitterLevel, 11, 0.0F, 3.0F + jitterRangeBonus);
        ship.setJitter(this, JITTER_COLOR, jitterLevel, 4, 0.0F, 0.0F + jitterRangeBonus);
        if (state != ShipSystemStatsScript.State.IN) {
            if (effectLevel >= 1.0F) {
                Vector2f target = ship.getMouseTarget();
                //Lookup for AIFlags is handling null AI scenario, keeping the check makes it impossible for the player ship to use the override
                if (/*ship.getShipAI() != null &&*/ ship.getAIFlags().hasFlag(ShipwideAIFlags.AIFlags.SYSTEM_TARGET_COORDS)) {
                    target = (Vector2f) ship.getAIFlags().getCustom(ShipwideAIFlags.AIFlags.SYSTEM_TARGET_COORDS);
                }
                if (target != null) {
                    float dist = Misc.getDistance(ship.getLocation(), target);
                    float max = getMaxRange(ship) + ship.getCollisionRadius();
                    if (dist > max) {
                        float dir = Misc.getAngleInDegrees(ship.getLocation(), target);
                        target = Misc.getUnitVectorAtDegreeAngle(dir);
                        target.scale(max);
                        Vector2f.add(target, ship.getLocation(), target);
                    }
                    try {
                        Method m = MineStrikeStats.class.getDeclaredMethod("findClearLocation", ShipAPI.class, Vector2f.class);
                        m.setAccessible(true);
                        target = (Vector2f) m.invoke(this, ship, target);
                    } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                        target = null;
                    }
                    if (target != null) {
                        spawnMine(ship, target);
                    }
                }
            }
        }
    }
}
