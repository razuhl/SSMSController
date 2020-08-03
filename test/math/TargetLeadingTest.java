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
package math;

import com.fs.starfarer.prototype.Utils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.lwjgl.util.vector.Vector2f;
import ssms.controller.EveryFrameCombatPlugin_Controller;
import ssms.controller.inputScreens.InputScreen_BattleSteering;

/**
 *
 * @author Malte Schulze
 */
public class TargetLeadingTest {
    private static class InputScreen_BattleSteeringMock extends InputScreen_BattleSteering {
        @Override
        public Vector2f targetLeading(Vector2f vShooterPos, Vector2f vTargetPos, Vector2f vTargetVelocity, float projectileSpeed, Vector2f result) {
            return super.targetLeading(vShooterPos, vTargetPos, vTargetVelocity, projectileSpeed, result);
        }
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }

    @Test
    public void targetLeading() {
        InputScreen_BattleSteeringMock con = new InputScreen_BattleSteeringMock();
        
        Vector2f vShooterPos = new Vector2f(0f, 0f);
        Vector2f vTargetPos = new Vector2f(0f, 10f);
        Vector2f vTargetVelocity = new Vector2f(1f, 0f);
        float projectileSpeed = 1f;
        Vector2f result = new Vector2f();
        //target flies at the same speed as the bullet and no leading is possible
        con.targetLeading(vShooterPos, vTargetPos, vTargetVelocity, projectileSpeed, result);
        assertEquals(result.x,0,1e-5);
        assertEquals(result.y,10,1e-5);
        
        //Trying to hit 10,10: The length of the hypothenusis is 10^2+10^2 and we want it to reach that spot in 10 steps
        projectileSpeed = (float)Math.sqrt(200)/10f;
        con.targetLeading(vShooterPos, vTargetPos, vTargetVelocity, projectileSpeed, result);
        assertEquals(result.x,10,1e-5);
        assertEquals(result.y,10,1e-5);
        
        //the same thing but with a velocity in x direction
        vTargetPos = new Vector2f(10f, 0f);
        vTargetVelocity = new Vector2f(0f, 1f);
        con.targetLeading(vShooterPos, vTargetPos, vTargetVelocity, projectileSpeed, result);
        assertEquals(result.x,10,1e-5);
        assertEquals(result.y,10,1e-5);
    }
}
