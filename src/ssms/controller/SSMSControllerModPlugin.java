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

import com.fs.graphics.OooO;
import com.fs.graphics.TextureLoader;
import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.loading.LoadingUtils;
import com.fs.util.C;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.Priority;
import org.lwjgl.input.Controller;
import org.lwjgl.input.Controllers;
import ssms.controller.inputScreens.Indicators;
import ssms.controller.inputScreens.InputScope_360;
import ssms.controller.inputScreens.InputScope_Battle;
import ssms.controller.inputScreens.InputScreenManager;
import ssms.controller.inputScreens.InputScreen_BattleMenu;
import ssms.controller.inputScreens.InputScreen_BattleSteering;
import ssms.controller.inputScreens.InputScreen_BattleTargeting;
import ssms.controller.inputScreens.InputScreen_Bluescreen;
import ssms.controller.steering.SteeringControllerOption_Label;
import ssms.controller.steering.SteeringController_FreeFlight;
import ssms.controller.steering.SteeringController_OrbitTarget;
import ssms.qol.properties.PropertiesContainer;
import ssms.qol.properties.PropertiesContainerConfiguration;
import ssms.qol.properties.PropertiesContainerConfiguration.PostSetter;
import ssms.qol.properties.PropertiesContainerConfigurationFactory;
import ssms.qol.properties.PropertyConfigurationFloat;
import ssms.qol.properties.PropertyConfigurationInteger;
import ssms.qol.properties.PropertyConfigurationListContainer;
import ssms.qol.properties.PropertyConfigurationSelectable;
import ssms.qol.properties.PropertyConfigurationString;
import ssms.qol.properties.PropertyValueGetter;
import ssms.qol.properties.PropertyValueSetter;
import ssms.controller.steering.SteeringControllerOption_AllowsEveryTarget;
import ssms.qol.properties.PropertiesContainerMerger;
import ssms.qol.properties.PropertyConfigurationContainer;

/**
 * Sets up configuration and application scoped persistency for the mod.
 * 
 * @author Malte Schulze
 */
public final class SSMSControllerModPlugin extends BaseModPlugin {
    static public HandlerController controller = new HandlerController();
    static public List<ControllerMapping> controllerMappings;
    static public Class primarySteeringMode = SteeringController_FreeFlight.class, alternativeSteeringMode = SteeringController_OrbitTarget.class;
    static public List<Class> registeredSteeringController = new ArrayList<Class>();
    static public EnumMap<Indicators,String> defaultIndicators;
    
    @Override
    public void onApplicationLoad() throws Exception {
        ControllerMapping xbox360 = new ControllerMapping();
        xbox360.btnA = 0;
        xbox360.btnB = 1;
        xbox360.btnX = 2;
        xbox360.btnY = 3;
        xbox360.btnBumperLeft = 4;
        xbox360.btnBumperRight = 5;
        xbox360.btnSelect = 6;
        xbox360.btnStart = 7;
        xbox360.btnLeftStick = 8;
        xbox360.btnRightStick = 9;
        xbox360.axisLeftStickX = "X Axis";
        xbox360.axisLeftStickY = "Y Axis";
        xbox360.axisRightStickX = "X Rotation";
        xbox360.axisRightStickY = "Y Rotation";
        xbox360.axisTrigger = "Z Axis";
        xbox360.axisBtnConversionDeadzone = 0.85f;
        xbox360.joystickDeadzone = 0.25f;
        xbox360.deviceName = "Controller (XBOX 360 For Windows)(5,10)";
        xbox360.indicators = new EnumMap<>(Indicators.class);
        EnumMap<Indicators,String> indicators = xbox360.indicators;
        indicators.put(Indicators.A, "XboxOne_A.png");
        indicators.put(Indicators.B, "XboxOne_B.png");
        indicators.put(Indicators.X, "XboxOne_X.png");
        indicators.put(Indicators.Y, "XboxOne_Y.png");
        indicators.put(Indicators.Start, "XboxOne_Menu.png");
        indicators.put(Indicators.Select, "XboxOne_Windows.png");
        indicators.put(Indicators.BumperLeft, "XboxOne_LB.png");
        indicators.put(Indicators.BumperRight, "XboxOne_RB.png");
        indicators.put(Indicators.LeftTrigger, "XboxOne_LT.png");
        indicators.put(Indicators.RightTrigger, "XboxOne_RT.png");
        indicators.put(Indicators.RightStickButton, "XboxOne_Right_Stick_Button.png");
        indicators.put(Indicators.LeftStickButton, "XboxOne_Left_Stick_Button.png");
        indicators.put(Indicators.RightStickUp, "XboxOne_Right_Stick_Up.png");
        indicators.put(Indicators.LeftStickUp, "XboxOne_Left_Stick_Up.png");
        indicators.put(Indicators.RightStickDown, "XboxOne_Right_Stick_Down.png");
        indicators.put(Indicators.LeftStickDown, "XboxOne_Left_Stick_Down.png");
        indicators.put(Indicators.RightStickLeft, "XboxOne_Right_Stick_Left.png");
        indicators.put(Indicators.LeftStickLeft, "XboxOne_Left_Stick_Left.png");
        indicators.put(Indicators.RightStickRight, "XboxOne_Right_Stick_Right.png");
        indicators.put(Indicators.LeftStickRight, "XboxOne_Left_Stick_Right.png");
        indicators.put(Indicators.RightStick, "XboxOne_Right_Stick.png");
        indicators.put(Indicators.LeftStick, "XboxOne_Left_Stick.png");
        
        defaultIndicators = new EnumMap<>(indicators);
        
        if ( controllerMappings == null ) controllerMappings = new ArrayList<>();
        controllerMappings.add(xbox360);
        reconnectController();
        
        configureSettingsApplicationController();
        registeredSteeringController.add(SteeringController_FreeFlight.class);
        registeredSteeringController.add(SteeringController_OrbitTarget.class);
        
        InputScreenManager man = InputScreenManager.getInstance();
        
        man.registerScope(InputScope_360.class);
        man.registerScope(InputScope_Battle.class);
        
        man.registerScreen(InputScreen_Bluescreen.class);
        man.registerScreen(InputScreen_BattleSteering.class);
        man.registerScreen(InputScreen_BattleTargeting.class);
        man.registerScreen(InputScreen_BattleMenu.class);
        
        man.updateIndicators();
    }
    
    protected void configureSettingsApplicationController() {
        PropertiesContainerConfigurationFactory confFactory = PropertiesContainerConfigurationFactory.getInstance();
        
        PropertiesContainerConfiguration<EnumMap> confIndicators = confFactory.getOrCreatePropertiesContainerConfiguration("SSMSControllerIndicators", EnumMap.class);
        final String labelIndicator = "Select from a list of available icons to represent the indicator with.";
        final List<String> icons = new ArrayList<>();
        //Order in which repositories are searched through in vanilla is not ordering mods correctly. The first mod with an entry is used instead of the last one.
        //That way a mod that wants to override something has to create it before the other mod is loaded which would mean code and resource extensions have to be
        //split into two mods to achieve this...
        FilenameFilter filterPngFiles = new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name != null && name.endsWith(".png");
                }
            };
        for ( com.fs.util.C.Oo repo : com.fs.util.C.Ó00000().Ô00000() ) {
            if ( repo == null || repo.Ó00000 == null || repo.o00000 != C.o.String ) continue;
            //int imagesFound = 0;
            File fRepo = new File(repo.Ó00000);
            if ( fRepo.exists() && fRepo.isDirectory() ) {
                File fIndicators = new File(fRepo,"graphics/indicators");
                if ( fIndicators.exists() && fIndicators.isDirectory() ) {
                    for ( String filename : fIndicators.list(filterPngFiles) ) {
                        if ( !icons.contains(filename) ) icons.add(filename);
                        //imagesFound++;
                    }
                }
            }
            //Global.getLogger(SSMSControllerModPlugin.class).log(Level.ERROR, "Found "+imagesFound+" .png images for indicators in repo \""+repo.Ó00000+"\".");
        }
        
        for ( Indicators ind : Indicators.values() ) {
            final Indicators indicator = ind;
            confIndicators.addProperty(new PropertyConfigurationSelectable<EnumMap,String>(indicator.name(),indicator.toString(),labelIndicator,null,0,String.class,
                new PropertyValueGetter<EnumMap, String>() {
                    @Override
                    public String get(EnumMap sourceObject) {
                        return (String)sourceObject.get(indicator);
                    }
                }, new PropertyValueSetter<EnumMap, String>() {
                    @Override
                    public void set(EnumMap sourceObject, String value) {
                        sourceObject.put(indicator, value);
                    }
                }, true) {
                    @Override
                    public List<String> buildOptions() {
                        return icons;
                    }

                    @Override
                    public String getOptionLabel(String o) {
                        return o;
                    }
                });
        }
        confIndicators.configureMinorApplicationScoped(new PropertyValueGetter<PropertiesContainer<EnumMap>, String>() {
            @Override
            public String get(PropertiesContainer<EnumMap> con) {
                return "Indicators";
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
        confControllerMapping.addProperty(new PropertyConfigurationInteger<>("btnA","[B] A","Zero based index for button A or Cross.",null,10,
            new PropertyValueGetter<ControllerMapping, Integer>() {
                @Override
                public Integer get(ControllerMapping sourceObject) {
                    return sourceObject.btnA;
                }
            }, new PropertyValueSetter<ControllerMapping, Integer>() {
                @Override
                public void set(ControllerMapping sourceObject, Integer value) {
                    sourceObject.btnA = value;
                }
            }, true, 0, 100));
        confControllerMapping.addProperty(new PropertyConfigurationInteger<>("btnB","[B] B","Zero based index for button B or Circle.",null,20,
            new PropertyValueGetter<ControllerMapping, Integer>() {
                @Override
                public Integer get(ControllerMapping sourceObject) {
                    return sourceObject.btnB;
                }
            }, new PropertyValueSetter<ControllerMapping, Integer>() {
                @Override
                public void set(ControllerMapping sourceObject, Integer value) {
                    sourceObject.btnB = value;
                }
            }, true, 0, 100));
        confControllerMapping.addProperty(new PropertyConfigurationInteger<>("btnX","[B] X","Zero based index for button X or Square.",null,30,
            new PropertyValueGetter<ControllerMapping, Integer>() {
                @Override
                public Integer get(ControllerMapping sourceObject) {
                    return sourceObject.btnX;
                }
            }, new PropertyValueSetter<ControllerMapping, Integer>() {
                @Override
                public void set(ControllerMapping sourceObject, Integer value) {
                    sourceObject.btnX = value;
                }
            }, true, 0, 100));
        confControllerMapping.addProperty(new PropertyConfigurationInteger<>("btnY","[B] Y","Zero based index for button Y or Triangle.",null,40,
            new PropertyValueGetter<ControllerMapping, Integer>() {
                @Override
                public Integer get(ControllerMapping sourceObject) {
                    return sourceObject.btnY;
                }
            }, new PropertyValueSetter<ControllerMapping, Integer>() {
                @Override
                public void set(ControllerMapping sourceObject, Integer value) {
                    sourceObject.btnY = value;
                }
            }, true, 0, 100));
        confControllerMapping.addProperty(new PropertyConfigurationInteger<>("btnBumperLeft","[B] Bumper Left","Zero based index for left bumper button.",null,50,
            new PropertyValueGetter<ControllerMapping, Integer>() {
                @Override
                public Integer get(ControllerMapping sourceObject) {
                    return sourceObject.btnBumperLeft;
                }
            }, new PropertyValueSetter<ControllerMapping, Integer>() {
                @Override
                public void set(ControllerMapping sourceObject, Integer value) {
                    sourceObject.btnBumperLeft = value;
                }
            }, true, 0, 100));
        confControllerMapping.addProperty(new PropertyConfigurationInteger<>("btnBumperRight","[B] Bumper Right","Zero based index for right bumper button.",null,60,
            new PropertyValueGetter<ControllerMapping, Integer>() {
                @Override
                public Integer get(ControllerMapping sourceObject) {
                    return sourceObject.btnBumperRight;
                }
            }, new PropertyValueSetter<ControllerMapping, Integer>() {
                @Override
                public void set(ControllerMapping sourceObject, Integer value) {
                    sourceObject.btnBumperRight = value;
                }
            }, true, 0, 100));
        confControllerMapping.addProperty(new PropertyConfigurationInteger<>("btnSelect","[B] Select","Zero based index for select button.",null,70,
            new PropertyValueGetter<ControllerMapping, Integer>() {
                @Override
                public Integer get(ControllerMapping sourceObject) {
                    return sourceObject.btnSelect;
                }
            }, new PropertyValueSetter<ControllerMapping, Integer>() {
                @Override
                public void set(ControllerMapping sourceObject, Integer value) {
                    sourceObject.btnSelect = value;
                }
            }, true, 0, 100));
        confControllerMapping.addProperty(new PropertyConfigurationInteger<>("btnStart","[B] Start","Zero based index for start button.",null,80,
            new PropertyValueGetter<ControllerMapping, Integer>() {
                @Override
                public Integer get(ControllerMapping sourceObject) {
                    return sourceObject.btnStart;
                }
            }, new PropertyValueSetter<ControllerMapping, Integer>() {
                @Override
                public void set(ControllerMapping sourceObject, Integer value) {
                    sourceObject.btnStart = value;
                }
            }, true, 0, 100));
        confControllerMapping.addProperty(new PropertyConfigurationInteger<>("btnLeftStick","[B] Left Stick","Zero based index for button when pushing left stick down.",null,90,
            new PropertyValueGetter<ControllerMapping, Integer>() {
                @Override
                public Integer get(ControllerMapping sourceObject) {
                    return sourceObject.btnLeftStick;
                }
            }, new PropertyValueSetter<ControllerMapping, Integer>() {
                @Override
                public void set(ControllerMapping sourceObject, Integer value) {
                    sourceObject.btnLeftStick = value;
                }
            }, true, 0, 100));
        confControllerMapping.addProperty(new PropertyConfigurationInteger<>("btnRightStick","[B] Right Stick","Zero based index for button when pushing right stick down.",null,100,
            new PropertyValueGetter<ControllerMapping, Integer>() {
                @Override
                public Integer get(ControllerMapping sourceObject) {
                    return sourceObject.btnRightStick;
                }
            }, new PropertyValueSetter<ControllerMapping, Integer>() {
                @Override
                public void set(ControllerMapping sourceObject, Integer value) {
                    sourceObject.btnRightStick = value;
                }
            }, true, 0, 100));
        confControllerMapping.addProperty(new PropertyConfigurationString<>("axisLeftStickX","[A] Left Stick-X","Name of the left to right axis used by the the left stick.",null,110,
            new PropertyValueGetter<ControllerMapping, String>() {
                @Override
                public String get(ControllerMapping sourceObject) {
                    return sourceObject.axisLeftStickX;
                }
            }, new PropertyValueSetter<ControllerMapping, String>() {
                @Override
                public void set(ControllerMapping sourceObject, String value) {
                    sourceObject.axisLeftStickX = value;
                }
            }, true));
        confControllerMapping.addProperty(new PropertyConfigurationString<>("axisLeftStickY","[A] Left Stick-Y","Name of the down to up axis used by the left stick.",null,120,
            new PropertyValueGetter<ControllerMapping, String>() {
                @Override
                public String get(ControllerMapping sourceObject) {
                    return sourceObject.axisLeftStickY;
                }
            }, new PropertyValueSetter<ControllerMapping, String>() {
                @Override
                public void set(ControllerMapping sourceObject, String value) {
                    sourceObject.axisLeftStickY = value;
                }
            }, true));
        confControllerMapping.addProperty(new PropertyConfigurationString<>("axisTrigger","[A] Trigger","Name of the axis used by the two triggers.",null,130,
            new PropertyValueGetter<ControllerMapping, String>() {
                @Override
                public String get(ControllerMapping sourceObject) {
                    return sourceObject.axisTrigger;
                }
            }, new PropertyValueSetter<ControllerMapping, String>() {
                @Override
                public void set(ControllerMapping sourceObject, String value) {
                    sourceObject.axisTrigger = value;
                }
            }, true));
        confControllerMapping.addProperty(new PropertyConfigurationString<>("axisRightStickX","[A] Right Stick-X","Name of the left to right axis used by the the right stick.",null,140,
            new PropertyValueGetter<ControllerMapping, String>() {
                @Override
                public String get(ControllerMapping sourceObject) {
                    return sourceObject.axisRightStickX;
                }
            }, new PropertyValueSetter<ControllerMapping, String>() {
                @Override
                public void set(ControllerMapping sourceObject, String value) {
                    sourceObject.axisRightStickX = value;
                }
            }, true));
        confControllerMapping.addProperty(new PropertyConfigurationString<>("axisRightStickY","[A] Right Stick-Y","Name of the down to up axis used by the right stick.",null,150,
            new PropertyValueGetter<ControllerMapping, String>() {
                @Override
                public String get(ControllerMapping sourceObject) {
                    return sourceObject.axisRightStickY;
                }
            }, new PropertyValueSetter<ControllerMapping, String>() {
                @Override
                public void set(ControllerMapping sourceObject, String value) {
                    sourceObject.axisRightStickY = value;
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
        confControllerMapping.addProperty(new PropertyConfigurationContainer<>("indicators","Indicators","Change icons that are displayed for button inputs.",new EnumMap<>(Indicators.class),
                "SSMSControllerIndicators",EnumMap.class,400,new PropertyValueGetter<ControllerMapping, EnumMap>() {
            @Override
            public EnumMap get(ControllerMapping sourceObject) {
                return sourceObject.indicators;
            }
        }, new PropertyValueSetter<ControllerMapping, PropertiesContainer>() {
            @Override
            public void set(ControllerMapping sourceObject, PropertiesContainer value) {
                
            }
        }, true));
        confControllerMapping.addSetter(new PropertiesContainerMerger<ControllerMapping>() {
            @Override
            public boolean merge(PropertiesContainer<ControllerMapping> container, ControllerMapping sourceObject) {
                if ( sourceObject.indicators == null ) {
                    sourceObject.indicators = new EnumMap<>(defaultIndicators);
                } else {
                    for ( Map.Entry<Indicators,String> e : defaultIndicators.entrySet() ) {
                        if ( !sourceObject.indicators.containsKey(e.getKey()) || sourceObject.indicators.get(e.getKey()) == null ) {
                            sourceObject.indicators.put(e.getKey(), e.getValue());
                        }
                    }
                }
                return true;
            }
        });
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
                ControllerMapping mapping = new ControllerMapping();
                mapping.indicators = new EnumMap<>(defaultIndicators);
                return mapping;
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
                    con.poll();
                    controller = new HandlerController(con,mapping);
                    break;
                }
            }
        }
        if ( controller == null ) controller = new HandlerController();
        controller.poll();
        InputScreenManager.getInstance().updateIndicators();
    }
}
