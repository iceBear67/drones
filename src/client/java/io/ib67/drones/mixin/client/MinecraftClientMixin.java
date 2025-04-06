package io.ib67.drones.mixin.client;

import io.ib67.drones.client.DronesClient;
import lombok.extern.log4j.Log4j2;
import net.minecraft.client.MinecraftClient;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {
    @Shadow
    @Final
    private static Logger LOGGER;

    @Inject(at = @At("TAIL"), method = "<init>")
    private void onInit(CallbackInfo ci) {
        var buffer = DronesClient.INSTANCE.readControllerMapping();
        if(buffer != null) {
            GLFW.glfwUpdateGamepadMappings(buffer);
            LOGGER.info("GAMEPAD MAPPING UPDATED");
        }
    }

    @Inject(at = @At("HEAD"), method = "handleInputEvents()V")
    private void tickGamepadHandlers(CallbackInfo ci) {
        var handler = DronesClient.INSTANCE.getGamepadHandler();
        if (handler != null) {
            handler.run();
        }
    }
}
