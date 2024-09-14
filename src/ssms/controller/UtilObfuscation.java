/*
 * Copyright (C) 2021 Malte Schulze.
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
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.plugins.ShipSystemStatsScript;
import com.fs.starfarer.combat.CombatState;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import org.apache.log4j.Level;
import org.lwjgl.util.vector.Vector2f;

/**
 * Centralised class for all connections into the obfuscated code.
 * 
 * @author Malte Schulze
 */
public class UtilObfuscation {
    public static void AimWeapon(WeaponAPI weapon, Vector2f targetLocation) {
        //((com.fs.starfarer.combat.systems.R)weapon).getAimTracker().new(targetLocation);
        ((com.fs.starfarer.combat.systems.thissuper)weapon).getAimTracker().Ó00000(targetLocation);
        //((com.fs.starfarer.combat.systems.R)weapon).getAimTracker().o00000(targetLocation);
    }
    
    protected static Class shipSystemImpl;
    protected static Method mShipSystemGetScript;
    protected static Field fShipSystemScript;
    protected static boolean InitShipSystemImpl() {
        if ( shipSystemImpl == null ) {
            try {
                shipSystemImpl = Class.forName("com.fs.starfarer.combat.systems.OOoO");
                mShipSystemGetScript = shipSystemImpl.getMethod("getScript");
                fShipSystemScript = shipSystemImpl.getDeclaredField("float");
                fShipSystemScript.setAccessible(true);
            } catch (ClassNotFoundException | NoSuchMethodException | NoSuchFieldException | SecurityException ex) {
                Global.getLogger(SSMSControllerModPlugin.class).log(Level.ERROR, "Failed to reflect ship system implementation, ensure SSMSUnlock is installed!", ex);
                shipSystemImpl = null;
                return false;
            }
        }
        return true;
    }
    
    public static Object TryGetScript(ShipSystemAPI system) {
        if ( !InitShipSystemImpl() ) return null;
        if ( system == null || !shipSystemImpl.isAssignableFrom(system.getClass()) ) return null;
        try {
            return mShipSystemGetScript.invoke(system);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            return null;
        }
    }

    public static void SetScript(ShipSystemAPI system, ShipSystemStatsScript script) {
        if ( !InitShipSystemImpl() ) return;
        try {
            fShipSystemScript.set(system,script);
        } catch (IllegalArgumentException | IllegalAccessException ex) {
            
        }
    }

    public static List<File> GetRepositoryDirectories() {
        List<File> files = new ArrayList<>();
        for ( com.fs.util.Object.Oo repo : com.fs.util.Object.Object().Ô00000() ) {
            com.fs.util.Object.o enumValue;
            //try {
                enumValue = repo != null ? (com.fs.util.Object.o) repo.o00000/*getClass().getField("o00000").get(repo)*/ : com.fs.util.Object.o.String;
            /*} catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException ex) {
                enumValue = com.fs.util.Object.o.String;
            }*/
            if ( repo == null || repo.Object == null || enumValue != com.fs.util.Object.o.String ) continue;
            //int imagesFound = 0;
            File fRepo = new File(repo.Object);
            if ( fRepo.exists() && fRepo.isDirectory() ) {
                files.add(fRepo);
            }
        }
        return files;
    }
    
    static protected Method mTextureUtilGetTexture, mTextureUtilRegisterTexture, mTextureUtilRemoveTexture;
    
    protected static boolean InitTextureLoader() {
        if ( mTextureUtilGetTexture == null ) {
            try {
                Class textureUtil = Class.forName("com.fs.graphics.G");
                mTextureUtilRegisterTexture = textureUtil.getMethod("o00000", String.class, String.class);
                mTextureUtilGetTexture = textureUtil.getMethod("Ò00000", String.class);
                mTextureUtilRemoveTexture = textureUtil.getMethod("o00000", String.class);
            } catch (ClassNotFoundException | NoSuchMethodException | SecurityException ex) {
                Global.getLogger(SSMSControllerModPlugin.class).log(Level.ERROR, "Failed to reflect texture util implementation, ensure SSMSUnlock is installed!", ex);
                mTextureUtilGetTexture = null;
                return false;
            }
        }
        return true;
    }
    
    public static void TryRemoveTextureWithID(String id) {
        if ( !InitTextureLoader() ) return;
        try {
            if ( mTextureUtilGetTexture.invoke(null, id) != null ) mTextureUtilRemoveTexture.invoke(null,id);
        } catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException ex) {
            
        }
    }
    
    public static boolean RegisterTextureWithID(String id, String path) {
        if ( !InitTextureLoader() ) return false;
        TryRemoveTextureWithID(id);
        try {
            mTextureUtilRegisterTexture.invoke(null, id, path);
        } catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException ex) {
            return false;
        }
        return true;
    }

    public static void SetVideoFeedToPlayerShip(CombatState cs) {
        cs.setVideoFeedSource(null);
        cs.getViewMouseOffset().\u00D300000(0.0F, 0.0F);
        cs.getViewMouseOffset().o00000(0.0F, 0.0F);
    }

    static protected Method mCombatStateSetVideoFeedSource = null;
    static protected Field fCombatStateZoomTracker = null;
    static protected Field fZoomTrackerSetMaximum = null, fZoomTrackerSetMinimum = null;
    
    protected static boolean InitZoomTracker() {
        if ( fZoomTrackerSetMaximum == null ) {
            try {
                Class zoomTracker = Class.forName("com.fs.starfarer.util.super");
                fZoomTrackerSetMaximum = zoomTracker.getDeclaredField("void");
                fZoomTrackerSetMaximum.setAccessible(true);
                fZoomTrackerSetMinimum = zoomTracker.getDeclaredField("\u00D300000");
                fZoomTrackerSetMinimum.setAccessible(true);
            } catch (ClassNotFoundException | SecurityException | NoSuchFieldException ex) {
                Global.getLogger(SSMSControllerModPlugin.class).log(Level.ERROR, "Failed to reflect zoom tracker, ensure SSMSUnlock is installed!", ex);
                fZoomTrackerSetMaximum = null;
                return false;
            }
        }
        return true;
    }
    
    protected static boolean InitCombatState() {
        if ( mCombatStateSetVideoFeedSource == null ) {
            try {
                for ( Method m : CombatState.class.getMethods() ) {
                    if ( m.getName().equals("setVideoFeedSource") ) {
                        mCombatStateSetVideoFeedSource = m;
                        break;
                    }
                }
                if ( mCombatStateSetVideoFeedSource == null ) return false;
                fCombatStateZoomTracker = CombatState.class.getDeclaredField("zoomTracker");
                fCombatStateZoomTracker.setAccessible(true);
            } catch (SecurityException | NoSuchFieldException ex) {
                Global.getLogger(SSMSControllerModPlugin.class).log(Level.ERROR, "Failed to reflect combat state, ensure SSMSUnlock is installed!", ex);
                mCombatStateSetVideoFeedSource = null;
                return false;
            }
        }
        return true;
    }
    
    public static void SetVideoFeedToShipTarget(CombatState cs, ShipAPI shipTarget) {
        if ( !InitCombatState() ) return;
        try {
            mCombatStateSetVideoFeedSource.invoke(cs, shipTarget);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            
        }
    }

    public static void SetZoom(CombatState cs, float desiredZoomFactor) {
        if ( cs.getZoomFactor() == desiredZoomFactor ) {
            return;
        }
        
        if ( !InitCombatState() ) return;
        if ( !InitZoomTracker() ) return;
        
        try {
            Object zoomTracker = fCombatStateZoomTracker.get(cs);
            //Clamping it by setting minimum and maximum
            fZoomTrackerSetMaximum.set(zoomTracker, desiredZoomFactor);
            fZoomTrackerSetMinimum.set(zoomTracker, desiredZoomFactor);
        } catch (NullPointerException | IllegalAccessException | IllegalArgumentException t) {
            Global.getLogger(SSMSControllerModPlugin.class).log(Level.ERROR, "Failed to adjust zoom tracker, ensure SSMSUnlock is installed!", t);
        }
    }
    
    static public class ShipEngineControllerAdapter {
        static protected Method mGetEffectiveStrafeAcceleration;
        static protected boolean initialized = false;

        static public float getEffectiveStrafeAcceleration(Object original) {
            if ( !initialized ) {
                try {
                    Class c = Class.forName("com.fs.starfarer.combat.entities.ship.H");
                    mGetEffectiveStrafeAcceleration = c.getMethod("getEffectiveStrafeAcceleration", (Class[]) null);
                } catch (ClassNotFoundException | NoSuchMethodException | SecurityException ex) {
                    Global.getLogger(SSMSControllerModPlugin.class).log(org.apache.log4j.Level.ERROR, "Failed to find methods for "+ShipEngineControllerAdapter.class.getSimpleName()+"!", ex);
                }
            }
            try {
                return (float) mGetEffectiveStrafeAcceleration.invoke(original);
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {

            }
            return 0f;
        }
    }
    
    static public class SpecImplAdapter {
        static protected Method mIsTurningAllowed, mIsAccelerateAllowed, mIsAlwaysAccelerate, mIsStrafeAllowed;
        static protected boolean initialized = false;
        protected Object original;

        public SpecImplAdapter(Object original) {
            if ( !initialized ) {
                try {
                    Class c = Class.forName("com.fs.starfarer.loading.specs.M");
                    mIsTurningAllowed = c.getMethod("isTurningAllowed", (Class[]) null);
                    mIsAccelerateAllowed = c.getMethod("isAccelerateAllowed", (Class[]) null);
                    mIsAlwaysAccelerate = c.getMethod("isAlwaysAccelerate", (Class[]) null);
                    mIsStrafeAllowed = c.getMethod("isStrafeAllowed", (Class[]) null);
                } catch (ClassNotFoundException | NoSuchMethodException | SecurityException ex) {
                    Global.getLogger(SSMSControllerModPlugin.class).log(org.apache.log4j.Level.ERROR, "Failed to find methods for "+SpecImplAdapter.class.getName()+"!", ex);
                }
            }
            this.original = original;
        }

        public boolean isTurningAllowed() {
            try {
                return (boolean) mIsTurningAllowed.invoke(original);
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {

            }
            return false;
        }

        public boolean isAccelerateAllowed() {
            try {
                return (boolean) mIsAccelerateAllowed.invoke(original);
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {

            }
            return false;
        }

        public boolean isAlwaysAccelerate() {
            try {
                return (boolean) mIsAlwaysAccelerate.invoke(original);
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {

            }
            return false;
        }

        public boolean isStrafeAllowed() {
            try {
                return (boolean) mIsStrafeAllowed.invoke(original);
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {

            }
            return false;
        }
    }
}
