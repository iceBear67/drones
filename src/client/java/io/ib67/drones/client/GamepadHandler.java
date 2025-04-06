package io.ib67.drones.client;

import io.ib67.drones.CommonUtils;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.lwjgl.glfw.GLFWGamepadState;

import java.io.Closeable;
import java.io.IOException;
import java.util.function.Consumer;

import static org.lwjgl.glfw.GLFW.*;

@Log4j2
public class GamepadHandler implements Runnable, Closeable {
    private static final int BUTTON_CLICK_INTERVAL = 500;
    protected final GLFWGamepadState gamepadState;
    protected final Consumer<GLFWGamepadState> callback;
    protected final long[] buttonNextClickBarrier = new long[15];
    @Setter
    protected int joystick;

    public GamepadHandler(int initialJoystick, Consumer<GLFWGamepadState> stateConsumer) {
        this.joystick = initialJoystick;
        this.gamepadState = GLFWGamepadState.create();
        this.callback = stateConsumer;
    }

    @Override
    public void run() {
        if (!glfwJoystickPresent(joystick) || !glfwJoystickIsGamepad(joystick)) {
            return;
        }
        if (!glfwGetGamepadState(joystick, gamepadState)) {
            log.error("Cannot read gamepad state from joystick {}", joystick);
            return;
        }
        if (shouldSend(gamepadState)) callback.accept(gamepadState);
    }

    private boolean shouldSend(GLFWGamepadState state) {
        for (int i = 0; i < 4; i++) {
            var axis = CommonUtils.removeJitter(state.axes(i));
            if (axis != 0 && axis >= -1 && axis <= 1){
                return true;
            }
        }
        if(state.axes(GLFW_GAMEPAD_AXIS_LEFT_TRIGGER) != -1 || state.axes(GLFW_GAMEPAD_AXIS_RIGHT_TRIGGER) != -1) return true;
        for (int i = 0; i < 15; i++) {
            var time = buttonNextClickBarrier[i];
            if (state.buttons(i) > 0 && System.currentTimeMillis() > time) {
                buttonNextClickBarrier[i] = System.currentTimeMillis() + BUTTON_CLICK_INTERVAL;
                return true;
            }
        }
        return false;
    }

    @Override
    public void close() throws IOException {
        gamepadState.close();
    }
}
