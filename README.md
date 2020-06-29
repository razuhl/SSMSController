# SSMSController

## Dependencies

SSMSUnlock - https://github.com/razuhl/SSMSUnlock
SSMSQoL - https://github.com/razuhl/SSMSQoL

## Features

* Provides controller support for combat controls.
* Orbiting mode that allows circling targets. Supports broadsides.

## FAQ

Can this be added to and removed from an ongoing save? Yes

Can't see my controller, controller stopped working. The controller must be plugged in when the game starts and can not be disconnected throughout the game.

Can I still use keyboard and mouse? Yes the controller works alongside them. Joysticks with few buttons will need the keyboard. In order to use the mouse the controller steering can be switched off during combat.

###### Controller Setup

In the game menu from SSMSQoL is an option called "SSMSControllers". 
The entry "Controller Mappings" is a list of configurations for installed controllers. 
The order in the list dictates which controller is used if multiple controllers are configured. 
Only one controller will be used at any time.
Adding a new entry to the list and editing it shows these properties.

* Device Name - Choose which controller the settings are for.
* [B] ... - These entries expect the index of a button. Their function is explained in a tooltip when hovering over the name of the property.
* [A] ... - These entries expect the exact name of an axis.
* Dead Zone Btn Axis - This value defines a minimal signal strength(0-1) for an axis whose input is converted to a button press. Increasing it would mean a joystick has to be pressed harder into a direction before it counts as a button press.
* Dead Zone Joystick - This value defines a minimal signal strength(0-1) for a joysticks input. Increasing it removes false inputs when the joystick should be neutral.
* Acceleration, Cycle Weapons Group, Fighter/Autofire - These three entries allow its paired buttons to be either used via an axis or two buttons, depending on your controller and preference.

Not all buttons or axis need to be configured. Any unconfigured functions are simply not available through a controller.

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

However which axis/button is which must be figured out manually or looked up through google. The framework in use is "DirectInpput".

###### Default Controls

* Using Target Next/Prev cycles through the available targets ordered by distance, whily the target is being selected the game pauses and continues after a second of no input. Pushing both targeting buttons at the same time clears the current target. Having a target allows weapons to aim for it instead, use advanced steering methods and omni shields will raise pointed at the target.
* Broadsides can be selected by opening the controller menu during a battle. The ship will turn omni shields and itself if applicable to allow utilizing the weapons on the selected broadside.
* Omni Shields will be guided by the AI, once the user has raised them. They behave as if the ship were on autopilot. They raise pointing at the target or without a target at the front. The selected broadside moves the shield to what is now considered frontal.
* The controller menu is opened with a button and repeatedly pressing that button cycles through the options. The currently selected option can then be selected. The combat is paused while this takes place.
* Weapons are aimed automatically.
* Steering is governed by the configured steering modes. By default the "[SSMS] Directional" mode turns the ship in the direction of the joystick and can be accelerated and decelerated. The "[SSMS] Orbit" mode requires a target and will maintain a position in orbit around it. The position is determined by the joystick and throttle. Throttle moves the desired position closer/further. Either mode paints a guide that shows where the ship is trying to fly.
* When using the alternative steering mode and a target is no longer supported by the steering mode(e.g. destroyed) the game switches to the default steering mode.