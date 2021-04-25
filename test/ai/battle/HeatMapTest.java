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
package ai.battle;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.lwjgl.util.Point;
import ssms.controller.ai.battle.HeatMap;
import ssms.controller.ai.battle.HeatProducer;

/**
 *
 * @author Malte Schulze
 */
public class HeatMapTest {
    private static class MockHeatProducer implements HeatProducer {
        float x, y;
        float localHeat; float radius;

        public MockHeatProducer(float x, float y, float localHeat, float radius) {
            this.localHeat = localHeat;
            this.radius = radius;
            this.x = x;
            this.y = y;
        }
        
        @Override
        public float getHeatAtDistance(float distance) {
            return localHeat / (distance + 1);
        }

        @Override
        public float getMaxHeatRadius() {
            return radius;
        }

        @Override
        public float getX() {
            return x;
        }

        @Override
        public float getY() {
            return y;
        }
    }
    
    public HeatMapTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    private List<Point> circleOutline(int r, float scaleX, float scaleY) {
        List<Point> points = new ArrayList<>();
        int outerRadius = r*r; int innerRadius = (r-1)*(r-1);
        float scaleXSquared = scaleX * scaleX; float scaleYSquared = scaleY * scaleY; 
        
        for (int i = 0; i <= r; i++) {
            for (int j = 0; j <= +r; j++ ) {
                int distanceSquared = (int)(j*j*scaleXSquared) + (int)(i*i*scaleYSquared);
                if ( distanceSquared > innerRadius && distanceSquared <= outerRadius ) {
                    points.add(new Point(i,j));
                    if ( i != 0 && j != 0 ) {
                        points.add(new Point(-i,j));
                        points.add(new Point(i,-j));
                        points.add(new Point(-i,-j));
                    } else {
                        points.add(new Point(-i,-j));
                    }
                }
            }
        }
        
        return points;
    }
    
    @Test
    public void testCircleIndexes() {
        Point[][] img = new Point[20][20];
        List<Point> l = circleOutline(4,1,1);
        for ( Point p : l ) {
            img[p.getX()+10][p.getY()+10] = p;
        }
        NumberFormat nf = NumberFormat.getIntegerInstance();
        nf.setMinimumIntegerDigits(2);
        for ( int i = 0; i < img.length; i++ ) {
            StringBuilder sb = new StringBuilder();
            for ( int j = 0; j < img[0].length; j++ ) {
                if ( img[i][j] == null )
                    sb.append("  ");
                else
                    sb.append(nf.format(l.indexOf(img[i][j])));
            }
            System.out.println(sb.toString());
        }
        System.out.println(l.size());
    }

    @Test
    public void accumulateHeat() {
        HeatMap map = new HeatMap(100,100,1f/10f);
        Collection<HeatProducer> heatProducers = new HashSet<>();
        heatProducers.add(new MockHeatProducer(200f,300f,8f,2));
        map.setup(heatProducers);
    }
}
