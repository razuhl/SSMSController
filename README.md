# SSMSController

## Dependencies

SSMSUnlock - https://github.com/razuhl/SSMSUnlock

SSMSQoL - https://github.com/razuhl/SSMSQoL

## Features

* Provides controller support for combat controls.
* Orbiting mode that allows circling targets. Supports broadsides.
* Rebindable keys and button prompts to support different controllers.

## FAQ

Can this be added to and removed from an ongoing save? Yes

Can't see my controller, controller stopped working. The controller must be plugged in when the game starts and can not be disconnected throughout the game.

Can I still use keyboard and mouse? Yes the controller works alongside them. Joysticks with few buttons will need the keyboard. In order to use the mouse the controller steering can be switched off during combat.

## Controller Setup

![Settings Main](https://raw.githubusercontent.com/razuhl/SSMSController/master/images/Settings_Main.png)

1. In the game menu from SSMSQoL click the option called "SSMSControllers".
2. A list of configurations for installed controllers.
3. Which control scheme to use for default steering.
4. Which control scheme to use for lockon steering.

![Settings Mappings](https://raw.githubusercontent.com/razuhl/SSMSController/master/images/Settings_Mappings.png)

1. Return to the previous screen.
2. Click a list entry to edi its properties.
3. Adds a new list entry.
4. Moves entry up in the list order.
5. Moves entry down in the list order.
6. Deletes an entry.

The order of list entries determines which controller is preferred if multiple controllers are connected and configured. Higher up means higher preference.

![Settings Mapping](https://raw.githubusercontent.com/razuhl/SSMSController/master/images/Settings_Mapping.png)

1. Return to the previous screen.
2. Allows selection of a detected controller device.
3. Button mappings for the controller.
4. Axis mappings for the controller.
5. How far an axis must be pushed before it counts as a button press.
6. How far a joystick must be pushed before its signal is picked up. This avoids loose joysticks from sending unintended commands.
7. Allows editing the button prompts used in controller compatible screens.

The control scheme is based around the layouts and capabilities of standard game controllers but can be mapped freely to support joysticks. Not all buttons or axis need to be configured. Any unconfigured functions are simply not available through a controller.

![Settings Select Controller](https://raw.githubusercontent.com/razuhl/SSMSController/master/images/Settings_Select_Controller.png)

1. Return to the previous screen.
2. Pick from the available options.

A list of all detected or configured controllers. The names are decorated with the number of available axis and buttons to create unique names. The G502 mouse exposes two controllers with the same name and axis/button count. Only the first controller would be used the second one with an identical id will always be ignored. However the main controller of the G502 comes with 31 buttons and can be mapped, this should be the case for any available device.

![Settings Indicators](https://raw.githubusercontent.com/razuhl/SSMSController/master/images/Settings_Indicators.png)

1. Return to the previous screen.
2. Allows selection of a detected indicator image.

Images can be installed by any mod and are searched in the sub directory "graphics/indicators/". They must be PNG files with the ending ".png" and will be displayed in 
25x25 dimension. The majority of default images come in a resolution of 100x100 and are provided by Nicolae Berbece through his [FREE Keyboard and controllers prompts pack](https://opengameart.org/content/free-keyboard-and-controllers-prompts-pack), the positional stick images are custom derivatives.

### Finding controller buttons and axis

Under windows going to the settings and typing "controllers" will show the option "Setup USB Game Controllers". Click that, select the controller and choose "properties". 
There a "Test" tab is available showing the interactive button mappings and axis names. If the lowest button is listed as 1 the number must be reduced by one when configuring it.
In addition the starsector log file will print out controller information like this.

Found controller: Controller (XBOX 360 For Windows)
with axis: Y Axis
with axis: X Axis
with axis: Y Rotation
with axis: X Rotation
with axis: Z Axis
with button: Button 0
with button: Button 1
...
with button: Button 9

However which axis/button is which must be figured out manually or looked up through google. The framework in use is "DirectInput".

## Default Controls

Weapons are aimed automatically and individually, even if the player fires them as a linked group. Omni shields will be raised in the direction of the broadside(by default front) and are controlled by the AI afterwards. Activating autopilot disables controller steering.

### Directional Steering

![Steering Directional](https://raw.githubusercontent.com/razuhl/SSMSController/master/images/Battle_Steering_Directional.png)

In this mode the left stick controls the facing of the ship. Triggers control forward and backward acceleration and bumpers allow strafing.

### Orbital Steering (Requires Target)

![Steering Orbital](https://raw.githubusercontent.com/razuhl/SSMSController/master/images/Battle_Steering_Orbital.png)

The ship will face the target with the selected broadside while accelerating and strafing to reach the desired position automatically. The desired position is marked with a green pentagon. The left stick controls the relative position of the ship to the target. The triggers change the desired distance to the target.

### Targeting

![Battle Targeting](https://raw.githubusercontent.com/razuhl/SSMSController/master/images/Battle_Targeting.png)

While targeting the combat is paused and the available targets can be cycled through using the bumpers. The targets are ordered by distance to the players ship. After selecting or clearing the target the battle resumes immediately.

### Battle Menu

![Battle Menu](https://raw.githubusercontent.com/razuhl/SSMSController/master/images/Battle_Menu.png)

Combat is paused when the menu is open. The available menu entries can be cycled through using the bumpers. After selecting an entry the battle resumes immediately.

## Custom Controls

Besides mapping the input it is also possible to add custom steering modes via modding. The mods mod_info.json should contain an entry `"loadAfter":["SSMSController"]`. Then adding this mods jar as a library dependency to the java project gives access to the required classes. The javadoc next to the jar should be linked so that the documentation is available when coding the necessary class. 

The javadoc for the interface "ssms.controller.steering.SteeringController" contains all the documentation on what the class must do and how it can be put into service. In addition the default steering mode classes "ssms.controller.steering.SteeringController_FreeFlight" and "ssms.controller.steering.SteeringController_OrbitTarget" can be viewed in the source files to get an idea on how to query the controller or steer the ship.