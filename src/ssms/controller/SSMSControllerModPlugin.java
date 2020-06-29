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

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import java.lang.annotation.Annotation;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.lwjgl.input.Controller;
import org.lwjgl.input.Controllers;
import ssms.controller.steering.SteeringController;
import ssms.controller.steering.SteeringControllerOption_Label;
import ssms.controller.steering.SteeringController_FreeFlight;
import ssms.controller.steering.SteeringController_OrbitTarget;
import ssms.qol.properties.PropertiesContainer;
import ssms.qol.properties.PropertiesContainerConfiguration;
import ssms.qol.properties.PropertiesContainerConfiguration.PostSetter;
import ssms.qol.properties.PropertiesContainerConfigurationFactory;
import ssms.qol.properties.PropertyConfigurationBoolean;
import ssms.qol.properties.PropertyConfigurationContainer;
import ssms.qol.properties.PropertyConfigurationFloat;
import ssms.qol.properties.PropertyConfigurationInteger;
import ssms.qol.properties.PropertyConfigurationListContainer;
import ssms.qol.properties.PropertyConfigurationSelectable;
import ssms.qol.properties.PropertyConfigurationString;
import ssms.qol.properties.PropertyValueGetter;
import ssms.qol.properties.PropertyValueSetter;
import ssms.controller.steering.SteeringControllerOption_AllowsEveryTarget;

/**
 *
 * @author Malte Schulze
 */
public final class SSMSControllerModPlugin extends BaseModPlugin {
    static public HandlerController controller;
    static public List<ControllerMapping> controllerMappings;
    static public Class primarySteeringMode, alternativeSteeringMode;
    static public List<Class> registeredSteeringController = new ArrayList<Class>();
    
    @Override
    public void onApplicationLoad() throws Exception {
        configureSettingsApplicationController();
        registeredSteeringController.add(SteeringController_FreeFlight.class);
        registeredSteeringController.add(SteeringController_OrbitTarget.class);
    }
    
    protected void configureSettingsApplicationController() {
        PropertiesContainerConfigurationFactory confFactory = PropertiesContainerConfigurationFactory.getInstance();
        
        PropertiesContainerConfiguration<ControllerMapping.AxisOrButton> confAxisOrButton = confFactory.getOrCreatePropertiesContainerConfiguration("SSMSAxisOrButton", ControllerMapping.AxisOrButton.class);
        confAxisOrButton.addProperty(new PropertyConfigurationString<>("axis","Axis","Name of the axis used to trigger the two functions by moving the axis to it's opposite extremes.",null,10,
            new PropertyValueGetter<ControllerMapping.AxisOrButton, String>() {
                @Override
                public String get(ControllerMapping.AxisOrButton sourceObject) {
                    return sourceObject.axis;
                }
            }, new PropertyValueSetter<ControllerMapping.AxisOrButton, String>() {
                @Override
                public void set(ControllerMapping.AxisOrButton sourceObject, String value) {
                    sourceObject.axis = value;
                }
            }, true));
        confAxisOrButton.addProperty(new PropertyConfigurationBoolean<>("inverted","Inverted","Switches the first and second function.",Boolean.FALSE,20,
            new PropertyValueGetter<ControllerMapping.AxisOrButton, Boolean>() {
                @Override
                public Boolean get(ControllerMapping.AxisOrButton sourceObject) {
                    return sourceObject.inverted;
                }
            }, new PropertyValueSetter<ControllerMapping.AxisOrButton, Boolean>() {
                @Override
                public void set(ControllerMapping.AxisOrButton sourceObject, Boolean value) {
                    sourceObject.inverted = value;
                }
            }, false));
        confAxisOrButton.addProperty(new PropertyConfigurationInteger<>("btnA","[B] One","Zero based index for button that triggers the first function. Can be used in combination with an axis or instead of.",null,30,
            new PropertyValueGetter<ControllerMapping.AxisOrButton, Integer>() {
                @Override
                public Integer get(ControllerMapping.AxisOrButton sourceObject) {
                    return sourceObject.btnA;
                }
            }, new PropertyValueSetter<ControllerMapping.AxisOrButton, Integer>() {
                @Override
                public void set(ControllerMapping.AxisOrButton sourceObject, Integer value) {
                    sourceObject.btnA = value;
                }
            }, true, 0, 100));
        confAxisOrButton.addProperty(new PropertyConfigurationInteger<>("btnB","[B] Two","Zero based index for button that triggers the second function. Can be used in combination with an axis or instead of.",null,40,
            new PropertyValueGetter<ControllerMapping.AxisOrButton, Integer>() {
                @Override
                public Integer get(ControllerMapping.AxisOrButton sourceObject) {
                    return sourceObject.btnB;
                }
            }, new PropertyValueSetter<ControllerMapping.AxisOrButton, Integer>() {
                @Override
                public void set(ControllerMapping.AxisOrButton sourceObject, Integer value) {
                    sourceObject.btnB = value;
                }
            }, true, 0, 100));
        confAxisOrButton.configureMinorApplicationScoped(new PropertyValueGetter<PropertiesContainer<ControllerMapping.AxisOrButton>, String>() {
            @Override
            public String get(PropertiesContainer<ControllerMapping.AxisOrButton> con) {
                String axis = con.getFieldValue("axis", String.class);
                if ( axis == null ) {
                    return new StringBuilder("(").append(con.getFieldValue("btnA", Integer.class)).append(",").append(con.getFieldValue("btnB", Integer.class)).append(")").toString();
                } else {
                    if ( con.getFieldValue("inverted", Boolean.class) ) {
                        return new StringBuilder(axis).append("(inverted)").toString();
                    } else {
                        return axis;
                    }
                }
            }
        });
        
        PropertiesContainerConfiguration<ControllerMapping> confControllerMapping = confFactory.getOrCreatePropertiesContainerConfiguration("SSMSControllerMapping", ControllerMapping.class);
        confControllerMapping.addProperty(new PropertyConfigurationSelectable<ControllerMapping,String>("deviceName","Device Name","Select from a list of connected and configured devices.",null,0,String.class,
            new PropertyValueGetter<ControllerMapping, String>() {
                @Override
                public String get(ControllerMapping sourceObject) {
                    return sourceObject.deviceName;
                }
            }, new PropertyValueSetter<ControllerMapping, String>() {
                @Override
                public void set(ControllerMapping sourceObject, String value) {
                    sourceObject.deviceName = value;
                }
            }, true) {
                @Override
                public List<String> buildOptions() {
                    List<String> names = new ArrayList<>();
                    int j = Controllers.getControllerCount();
                    for ( int i = 0; i < j; i++ ) {
                        Controller con = Controllers.getController(i);
                        names.add(new StringBuilder(con.getName()).append("(").append(con.getAxisCount()).append(",").append(con.getButtonCount()).append(")").toString());
                    }
                    if ( controllerMappings != null ) {
                        for ( ControllerMapping mapping : controllerMappings ) {
                            if ( mapping.deviceName != null && !mapping.deviceName.isEmpty() && !names.contains(mapping.deviceName) ) {
                                names.add(mapping.deviceName);
                            }
                        }
                    }
                    return names;
                }

                @Override
                public String getOptionLabel(String o) {
                    return o;
                }
            });
        confControllerMapping.addProperty(new PropertyConfigurationString<>("axisSteeringX","[A] Steering-X","Name of the axis used by the joystick for steering left to right.",null,90,
            new PropertyValueGetter<ControllerMapping, String>() {
                @Override
                public String get(ControllerMapping sourceObject) {
                    return sourceObject.axisSteeringX;
                }
            }, new PropertyValueSetter<ControllerMapping, String>() {
                @Override
                public void set(ControllerMapping sourceObject, String value) {
                    sourceObject.axisSteeringX = value;
                }
            }, true));
        confControllerMapping.addProperty(new PropertyConfigurationString<>("axisSteeringY","[A] Steering-Y","Name of the axis used by the joystick for steering up to down.",null,100,
            new PropertyValueGetter<ControllerMapping, String>() {
                @Override
                public String get(ControllerMapping sourceObject) {
                    return sourceObject.axisSteeringY;
                }
            }, new PropertyValueSetter<ControllerMapping, String>() {
                @Override
                public void set(ControllerMapping sourceObject, String value) {
                    sourceObject.axisSteeringY = value;
                }
            }, true));
        confControllerMapping.addProperty(new PropertyConfigurationInteger<>("btnAltSteering","[B] Alt-Steering","Zero based index for button that changes between orbiting and free fly steering modes.",null,110,
            new PropertyValueGetter<ControllerMapping, Integer>() {
                @Override
                public Integer get(ControllerMapping sourceObject) {
                    return sourceObject.btnAltSteering;
                }
            }, new PropertyValueSetter<ControllerMapping, Integer>() {
                @Override
                public void set(ControllerMapping sourceObject, Integer value) {
                    sourceObject.btnAltSteering = value;
                }
            }, true, 0, 100));
        confControllerMapping.addProperty(new PropertyConfigurationInteger<>("btnTargetNext","[B] Target-Next","Zero based index for button that traverses forward through the list of available targets sorted by distance.",null,10,
            new PropertyValueGetter<ControllerMapping, Integer>() {
                @Override
                public Integer get(ControllerMapping sourceObject) {
                    return sourceObject.btnTargetNext;
                }
            }, new PropertyValueSetter<ControllerMapping, Integer>() {
                @Override
                public void set(ControllerMapping sourceObject, Integer value) {
                    sourceObject.btnTargetNext = value;
                }
            }, true, 0, 100));
        confControllerMapping.addProperty(new PropertyConfigurationInteger<>("btnTargetPrev","[B] Target-Prev","Zero based index for button that traverses backwards through the list of available targets sorted by distance.",null,20,
            new PropertyValueGetter<ControllerMapping, Integer>() {
                @Override
                public Integer get(ControllerMapping sourceObject) {
                    return sourceObject.btnTargetPrev;
                }
            }, new PropertyValueSetter<ControllerMapping, Integer>() {
                @Override
                public void set(ControllerMapping sourceObject, Integer value) {
                    sourceObject.btnTargetPrev = value;
                }
            }, true, 0, 100));
        confControllerMapping.addProperty(new PropertyConfigurationInteger<>("btnVenting","[B] Vent","Zero based index for button that starts venting.",null,30,
            new PropertyValueGetter<ControllerMapping, Integer>() {
                @Override
                public Integer get(ControllerMapping sourceObject) {
                    return sourceObject.btnVenting;
                }
            }, new PropertyValueSetter<ControllerMapping, Integer>() {
                @Override
                public void set(ControllerMapping sourceObject, Integer value) {
                    sourceObject.btnVenting = value;
                }
            }, true, 0, 100));
        confControllerMapping.addProperty(new PropertyConfigurationInteger<>("btnFire","[B] Fire","Zero based index for button that fires the ships weapons.",null,40,
            new PropertyValueGetter<ControllerMapping, Integer>() {
                @Override
                public Integer get(ControllerMapping sourceObject) {
                    return sourceObject.btnFire;
                }
            }, new PropertyValueSetter<ControllerMapping, Integer>() {
                @Override
                public void set(ControllerMapping sourceObject, Integer value) {
                    sourceObject.btnFire = value;
                }
            }, true, 0, 100));
        confControllerMapping.addProperty(new PropertyConfigurationInteger<>("btnUseSystem","[B] Ship-System","Zero based index for button that activates the ships system.",null,50,
            new PropertyValueGetter<ControllerMapping, Integer>() {
                @Override
                public Integer get(ControllerMapping sourceObject) {
                    return sourceObject.btnUseSystem;
                }
            }, new PropertyValueSetter<ControllerMapping, Integer>() {
                @Override
                public void set(ControllerMapping sourceObject, Integer value) {
                    sourceObject.btnUseSystem = value;
                }
            }, true, 0, 100));
        confControllerMapping.addProperty(new PropertyConfigurationInteger<>("btnShield","[B] Shield","Zero based index for button that activate/deactivates shields/cloak.",null,60,
            new PropertyValueGetter<ControllerMapping, Integer>() {
                @Override
                public Integer get(ControllerMapping sourceObject) {
                    return sourceObject.btnShield;
                }
            }, new PropertyValueSetter<ControllerMapping, Integer>() {
                @Override
                public void set(ControllerMapping sourceObject, Integer value) {
                    sourceObject.btnShield = value;
                }
            }, true, 0, 100));
        confControllerMapping.addProperty(new PropertyConfigurationInteger<>("btnMenuOpen","[B] Open Menu","Zero based index for button that opens the menu and cycles through the available items.",null,70,
            new PropertyValueGetter<ControllerMapping, Integer>() {
                @Override
                public Integer get(ControllerMapping sourceObject) {
                    return sourceObject.btnMenuOpen;
                }
            }, new PropertyValueSetter<ControllerMapping, Integer>() {
                @Override
                public void set(ControllerMapping sourceObject, Integer value) {
                    sourceObject.btnMenuOpen = value;
                }
            }, true, 0, 100));
        confControllerMapping.addProperty(new PropertyConfigurationInteger<>("btnSelectMenuItem","[B] Select Menu-Item","Zero based index for button that selects the currently active menu item.",null,80,
            new PropertyValueGetter<ControllerMapping, Integer>() {
                @Override
                public Integer get(ControllerMapping sourceObject) {
                    return sourceObject.btnSelectMenuItem;
                }
            }, new PropertyValueSetter<ControllerMapping, Integer>() {
                @Override
                public void set(ControllerMapping sourceObject, Integer value) {
                    sourceObject.btnSelectMenuItem = value;
                }
            }, true, 0, 100));
        confControllerMapping.addProperty(new PropertyConfigurationContainer<>("acceleration","Acceleration","Configuration for acceleration mode.",null,"SSMSAxisOrButton",ControllerMapping.AxisOrButton.class,200,
            new PropertyValueGetter<ControllerMapping, ControllerMapping.AxisOrButton>() {
                @Override
                public ControllerMapping.AxisOrButton get(ControllerMapping sourceObject) {
                    return sourceObject.acceleration;
                }
            }, new PropertyValueSetter<ControllerMapping, PropertiesContainer>() {
                @Override
                public void set(ControllerMapping sourceObject, PropertiesContainer value) {
                    if ( value == null ) sourceObject.acceleration = null;
                    else {
                        sourceObject.acceleration = new ControllerMapping.AxisOrButton(
                                (String)value.getFieldValue("axis", String.class),
                                (Boolean)value.getFieldValue("inverted", Boolean.class),
                                (Integer)value.getFieldValue("btnA", Integer.class),
                                (Integer)value.getFieldValue("btnB", Integer.class));
                    }
                }
            }, true));
        confControllerMapping.addProperty(new PropertyConfigurationContainer<>("weaponGroups","Cycle Weapon Groups","Configuration for cycling through weapon groups.",null,"SSMSAxisOrButton",ControllerMapping.AxisOrButton.class,210,
            new PropertyValueGetter<ControllerMapping, ControllerMapping.AxisOrButton>() {
                @Override
                public ControllerMapping.AxisOrButton get(ControllerMapping sourceObject) {
                    return sourceObject.weaponGroups;
                }
            }, new PropertyValueSetter<ControllerMapping, PropertiesContainer>() {
                @Override
                public void set(ControllerMapping sourceObject, PropertiesContainer value) {
                    if ( value == null ) sourceObject.weaponGroups = null;
                    else {
                        sourceObject.weaponGroups = new ControllerMapping.AxisOrButton(
                                (String)value.getFieldValue("axis", String.class),
                                (Boolean)value.getFieldValue("inverted", Boolean.class),
                                (Integer)value.getFieldValue("btnA", Integer.class),
                                (Integer)value.getFieldValue("btnB", Integer.class));
                    }
                }
            }, true));
        confControllerMapping.addProperty(new PropertyConfigurationContainer<>("fightersAutofire","Fighters/Autofire","Configuration for toggling fighter modes and toggling autofire for the current weapon group.",null,"SSMSAxisOrButton",ControllerMapping.AxisOrButton.class,220,
            new PropertyValueGetter<ControllerMapping, ControllerMapping.AxisOrButton>() {
                @Override
                public ControllerMapping.AxisOrButton get(ControllerMapping sourceObject) {
                    return sourceObject.fightersAutofire;
                }
            }, new PropertyValueSetter<ControllerMapping, PropertiesContainer>() {
                @Override
                public void set(ControllerMapping sourceObject, PropertiesContainer value) {
                    if ( value == null ) sourceObject.fightersAutofire = null;
                    else {
                        sourceObject.fightersAutofire = new ControllerMapping.AxisOrButton(
                                (String)value.getFieldValue("axis", String.class),
                                (Boolean)value.getFieldValue("inverted", Boolean.class),
                                (Integer)value.getFieldValue("btnA", Integer.class),
                                (Integer)value.getFieldValue("btnB", Integer.class));
                    }
                }
            }, true));
        confControllerMapping.addProperty(new PropertyConfigurationFloat<>("axisBtnConversionDeadzone","Dead Zone Btn Axis","Required signal strength from zero to one before an axis signal is treated as button pressed.",0.85f,300,
            new PropertyValueGetter<ControllerMapping, Float>() {
                @Override
                public Float get(ControllerMapping sourceObject) {
                    return sourceObject.axisBtnConversionDeadzone;
                }
            }, new PropertyValueSetter<ControllerMapping, Float>() {
                @Override
                public void set(ControllerMapping sourceObject, Float value) {
                    sourceObject.axisBtnConversionDeadzone = value;
                }
            }, false, 0f, 1f));
        confControllerMapping.addProperty(new PropertyConfigurationFloat<>("joystickDeadzone","Dead Zone Joystick","Required signal strength from zero to one for BOTH joystick axis combined.",0.25f,310,
            new PropertyValueGetter<ControllerMapping, Float>() {
                @Override
                public Float get(ControllerMapping sourceObject) {
                    return sourceObject.joystickDeadzone;
                }
            }, new PropertyValueSetter<ControllerMapping, Float>() {
                @Override
                public void set(ControllerMapping sourceObject, Float value) {
                    sourceObject.joystickDeadzone = value;
                }
            }, false, 0f, 1f));
        confControllerMapping.configureMinorApplicationScoped(new PropertyValueGetter<PropertiesContainer<ControllerMapping>, String>() {
            @Override
            public String get(PropertiesContainer<ControllerMapping> con) {
                return con.getFieldValue("deviceName", String.class);
            }
        });
        
        PropertiesContainerConfiguration<SSMSControllerModPlugin> confController = confFactory.getOrCreatePropertiesContainerConfiguration("SSMSController", SSMSControllerModPlugin.class);
        confController.addProperty(new PropertyConfigurationListContainer<>("controllerMappings","Controller Mappings",
            "A list of all configured controllers. A controllers buttons and axis must be manually configured before they can be used. The order of mappings determines which controller is preferred in case multiple mappings match connected controllers. The controller must be connected before the game starts and cannot be disconnected while the game is running.",
            null, 10, new PropertyValueGetter<SSMSControllerModPlugin, List>() {
                @Override
                public List get(SSMSControllerModPlugin sourceObject) {
                    return SSMSControllerModPlugin.controllerMappings;
                }
            },new PropertyValueSetter<SSMSControllerModPlugin, List>() {
                @Override
                public void set(SSMSControllerModPlugin sourceObject, List value) {
                    SSMSControllerModPlugin.controllerMappings = value;
                }
            }, true, "SSMSControllerMapping", true, true, new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                return new ControllerMapping();
            }
        }));
        confController.addProperty(new PropertyConfigurationSelectable<SSMSControllerModPlugin, String>("primarySteeringMode","Steering Mode 1",
            "Which steering mode should be used to pilot the ship by default, mods can add their own modes to individualize the user experience. This mode must work regardless of targeting.",
            SteeringController_FreeFlight.class.getName(), 20, String.class, new PropertyValueGetter<SSMSControllerModPlugin, String>() {
                @Override
                public String get(SSMSControllerModPlugin sourceObject) {
                    if ( SSMSControllerModPlugin.primarySteeringMode != null ) return SSMSControllerModPlugin.primarySteeringMode.getName();
                    return null;
                }
            },new PropertyValueSetter<SSMSControllerModPlugin, String>() {
                @Override
                public void set(SSMSControllerModPlugin sourceObject, String value) {
                    try {
                        SSMSControllerModPlugin.primarySteeringMode = Class.forName(value);
                    } catch (ClassNotFoundException ex) {
                    } catch (Throwable t) {
                        Global.getLogger(SSMSControllerModPlugin.class).log(Level.ERROR, t.getMessage(), t);
                    }
                }
            }, false) {
            @Override
            public List<String> buildOptions() {
                List<String> steeringControllersWithoutTargets = new ArrayList<>();
                for ( Class cls : registeredSteeringController ) {
                    if ( cls.isAnnotationPresent(SteeringControllerOption_AllowsEveryTarget.class) && 
                            ((SteeringControllerOption_AllowsEveryTarget)cls.getAnnotation(SteeringControllerOption_AllowsEveryTarget.class)).value() )
                    steeringControllersWithoutTargets.add(cls.getName());
                }
                return steeringControllersWithoutTargets;
            }

            @Override
            public String getOptionLabel(String o) {
                try {
                    Class cls = Class.forName(o);
                    SteeringControllerOption_Label anno = (SteeringControllerOption_Label)cls.getAnnotation(SteeringControllerOption_Label.class);
                    if ( anno != null ) return anno.value();
                    return "undefined";
                } catch (ClassNotFoundException ex) {
                    return "unknown";
                } catch (Throwable t) {
                    Global.getLogger(SSMSControllerModPlugin.class).log(Level.ERROR, t.getMessage(), t);
                    return "error";
                }
            }
        });
        confController.addProperty(new PropertyConfigurationSelectable<SSMSControllerModPlugin, String>("alternativeSteeringMode","Steering Mode 2",
            "Which steering mode should be used to pilot the ship in alternate mode, mods can add their own modes to individualize the user experience. If it doesn't support the current target the steering mode is switched to 1.",
            SteeringController_OrbitTarget.class.getName(), 30, String.class, new PropertyValueGetter<SSMSControllerModPlugin, String>() {
                @Override
                public String get(SSMSControllerModPlugin sourceObject) {
                    if ( SSMSControllerModPlugin.alternativeSteeringMode != null ) return SSMSControllerModPlugin.alternativeSteeringMode.getName();
                    return null;
                }
            },new PropertyValueSetter<SSMSControllerModPlugin, String>() {
                @Override
                public void set(SSMSControllerModPlugin sourceObject, String value) {
                    try {
                        SSMSControllerModPlugin.alternativeSteeringMode = Class.forName(value);
                    } catch (ClassNotFoundException ex) {
                    } catch (Throwable t) {
                        Global.getLogger(SSMSControllerModPlugin.class).log(Level.ERROR, t.getMessage(), t);
                    }
                }
            }, false) {
            @Override
            public List<String> buildOptions() {
                List<String> steeringControllersWithoutTargets = new ArrayList<>();
                for ( Class cls : registeredSteeringController ) {
                    steeringControllersWithoutTargets.add(cls.getName());
                }
                return steeringControllersWithoutTargets;
            }

            @Override
            public String getOptionLabel(String o) {
                try {
                    Class cls = Class.forName(o);
                    SteeringControllerOption_Label anno = (SteeringControllerOption_Label)cls.getAnnotation(SteeringControllerOption_Label.class);
                    if ( anno != null ) return anno.value();
                    return "undefined";
                } catch (ClassNotFoundException ex) {
                    return "unknown";
                } catch (Throwable t) {
                    Global.getLogger(SSMSControllerModPlugin.class).log(Level.ERROR, t.getMessage(), t);
                    return "error";
                }
            }
        });
                
        confController.configureApplicationScopedSingleInstance("SSMSControllers",this,false);
        confController.addPostSetter("SSMSControllers", new PostSetter() {
            @Override
            public void merge(List<PropertiesContainer> loadedSettings) {
                if ( loadedSettings.isEmpty() && SSMSControllerModPlugin.controller != null ) return;
                try {
                    reconnectController();
                } catch ( Throwable t ) {
                    Global.getLogger(SSMSControllerModPlugin.class).log(Level.ERROR, "Failed to connect possible controllers!", t);
                }
            }
        });
    }
    
    static public void reconnectController() throws Exception {
        //only works during application start lwjgl does not refresh the list if connection status changes.
        Logger logger = Global.getLogger(SSMSControllerModPlugin.class);
        logger.setLevel(Level.INFO);
        
        if ( Controllers.isCreated() ) Controllers.destroy();
        Controllers.create();
        controller = null;
        
        //"Controller (XBOX 360 For Windows)"
        /*
        buttons:
        0 - A
        1 - B
        2 - X
        3 - Y
        4 - left bumper
        5 - right bumper
        6 - back
        7 - start
        8 - left stick
        9 - right stick
        axis:
        X Axis: left stick, left -1 to right 1
        Y Axis: left stick, top -1 to bottom 1
        X Rotation: right stick, left -1 to right 1
        Y Rotation: right stick, top -1 to bottom 1
        Z Axis: sum of triggers, left trigger adds up to 1 and right trigger removes up to 1
        */
        for ( int i = 0; i < Controllers.getControllerCount(); i++ ) {
            Controller con = Controllers.getController(i);
            logger.info("Found controller: "+con.getName());
            for ( int j = 0; j < con.getAxisCount(); j++ ) {
                logger.info("with axis: "+con.getAxisName(j));
            }
            for ( int j = 0; j < con.getButtonCount(); j++ ) {
                logger.info("with button: "+con.getButtonName(j));
            }
            for ( int j = 0; j < con.getRumblerCount(); j++ ) {
                logger.info("with rumbler: "+con.getRumblerName(j));
            }
        }
        if ( controllerMappings != null ) {
            Map<String,Controller> namedControllers = new HashMap<>();
            for ( int i = 0; i < Controllers.getControllerCount(); i++ ) {
                Controller con = Controllers.getController(i);
                namedControllers.put(new StringBuilder(con.getName()).append("(").append(con.getAxisCount()).append(",").append(con.getButtonCount()).append(")").toString(), con);
            }
            for ( ControllerMapping mapping : controllerMappings ) {
                Controller con = namedControllers.get(mapping.deviceName);
                if ( con != null ) {
                    controller = new HandlerController(con,mapping);
                    break;
                }
            }
        }
        if ( controller == null ) controller = new HandlerController();
        controller.poll();
    }
}
