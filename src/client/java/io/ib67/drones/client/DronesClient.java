package io.ib67.drones.client;

import io.ib67.drones.Drones;
import io.ib67.drones.client.renderer.DroneAllayRenderer;
import io.ib67.drones.client.renderer.DroneEntityRenderer;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.entity.BeeEntityRenderer;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.impl.client.HttpClients;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWGamepadState;
import org.lwjgl.opengl.GL;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

@Log4j2
public class DronesClient implements ClientModInitializer {
    public static DronesClient INSTANCE;
    @Getter
    private GamepadHandler gamepadHandler;

    @Override
    public void onInitializeClient() {
        gamepadHandler = new GamepadHandler(GLFW.GLFW_JOYSTICK_1, this::onJoystickState);
        EntityRendererRegistry.register(Drones.DRONE, DroneAllayRenderer::new);
        INSTANCE = this;
    }

    private void onJoystickState(GLFWGamepadState state) {
        var network = MinecraftClient.getInstance().getNetworkHandler();
        if (network != null) {
            ClientPlayNetworking.send(Utils.toPacket(state));
        }
    }
    @SneakyThrows
    public ByteBuffer readControllerMapping(){
        try(var client = HttpClient.newHttpClient()){
            var req = HttpRequest.newBuilder(URI.create("https://raw.githubusercontent.com/mdqinc/SDL_GameControllerDB/refs/heads/master/gamecontrollerdb.txt"))
                    .build();
            var body = client.send(req, HttpResponse.BodyHandlers.ofString()).body();
            var buf = ByteBuffer.allocateDirect(body.length()+32);
            buf.mark();
            buf.put(body.getBytes(StandardCharsets.UTF_8));
            buf.put((byte)0);
            buf.reset();
            return buf;
        }
    }
}
