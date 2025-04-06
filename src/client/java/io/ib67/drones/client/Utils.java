package io.ib67.drones.client;

import io.ib67.drones.CommonUtils;
import io.ib67.drones.packet.JoystickStateC2S;
import org.lwjgl.glfw.GLFWGamepadState;

import static org.lwjgl.glfw.GLFW.*;

public class Utils {
    public static JoystickStateC2S toPacket (GLFWGamepadState state){
        byte buttons = 0;
        if (state.buttons(GLFW_GAMEPAD_BUTTON_Y) == GLFW_PRESS) {
            buttons |= (byte) JoystickStateC2S.BUTTON_Y;
        }
        if (state.buttons(GLFW_GAMEPAD_BUTTON_A) == GLFW_PRESS) {
            buttons |= (byte) JoystickStateC2S.BUTTON_A;
        }
        if (state.buttons(GLFW_GAMEPAD_BUTTON_X) == GLFW_PRESS) {
            buttons |= (byte) JoystickStateC2S.BUTTON_X;
        }
        if (state.buttons(GLFW_GAMEPAD_BUTTON_B) == GLFW_PRESS) {
            buttons |= (byte) JoystickStateC2S.BUTTON_B;
        }
        if (state.buttons(GLFW_GAMEPAD_BUTTON_BACK) == GLFW_PRESS) {
            buttons |= (byte) JoystickStateC2S.BUTTON_BACK;
        }
        if (state.buttons(GLFW_GAMEPAD_BUTTON_START) == GLFW_PRESS) {
            buttons |= (byte) JoystickStateC2S.BUTTON_START;
        }
        if (state.buttons(GLFW_GAMEPAD_BUTTON_LEFT_BUMPER) == GLFW_PRESS) {
            buttons |= (byte) JoystickStateC2S.BUTTON_LB;
        }
        if (state.buttons(GLFW_GAMEPAD_BUTTON_RIGHT_BUMPER) == GLFW_PRESS) {
            buttons |= (byte) JoystickStateC2S.BUTTON_RB;
        }
        var b = new byte[Float.BYTES * 6];
        CommonUtils.writeBytes(0, CommonUtils.removeJitter(state.axes(GLFW_GAMEPAD_AXIS_LEFT_X)), b);
        CommonUtils.writeBytes(1, CommonUtils.removeJitter(state.axes(GLFW_GAMEPAD_AXIS_LEFT_Y)), b);
        CommonUtils.writeBytes(2, CommonUtils.removeJitter(state.axes(GLFW_GAMEPAD_AXIS_RIGHT_X)), b);
        CommonUtils.writeBytes(3, CommonUtils.removeJitter(state.axes(GLFW_GAMEPAD_AXIS_RIGHT_Y)), b);
        CommonUtils.writeBytes(4, CommonUtils.removeJitter(state.axes(GLFW_GAMEPAD_AXIS_LEFT_TRIGGER)), b);
        CommonUtils.writeBytes(5, CommonUtils.removeJitter(state.axes(GLFW_GAMEPAD_AXIS_RIGHT_TRIGGER)), b);
        return new JoystickStateC2S(buttons, b);
    }
}
