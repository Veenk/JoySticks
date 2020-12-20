package com.vlad.joysticks.listeners;

import com.vlad.joysticks.JoyStickHandler;

public interface JoystickListener {
    void onTouch(final JoyStickHandler joystick, short pX, final short pY);
}
