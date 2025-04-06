package io.ib67.drones;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import io.ib67.drones.entity.DroneEntity;
import io.ib67.drones.packet.JoystickStateC2S;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypeFilter;
import net.minecraft.util.math.Vec3d;

import java.util.*;

public class Drones implements ModInitializer {
    public static final String MOD_ID = "drones";
    public static final EntityType<DroneEntity> DRONE = Registry.register(
            Registries.ENTITY_TYPE,
            Identifier.of(MOD_ID, "drone"),
            EntityType.Builder.create(DroneEntity::new, SpawnGroup.CREATURE).dimensions(0.75f, 0.75f)
                    .build(RegistryKey.of(Registries.ENTITY_TYPE.getKey(), Identifier.of(MOD_ID, "drone")))
    );
    public static Drones INSTANCE;
    protected final Map<UUID, UUID> drivingDrones = new HashMap<>();
    protected final List<UUID> createdDrones = new ArrayList<>();

    @Override
    public void onInitialize() {
        PayloadTypeRegistry.playC2S().register(JoystickStateC2S.PKT_ID, JoystickStateC2S.PACKET_CODEC);
        ServerPlayNetworking.registerGlobalReceiver(JoystickStateC2S.PKT_ID, this::onPlayerJoystickState);
        FabricDefaultAttributeRegistry.register(DRONE, DroneEntity.createMobAttributes());
        CommandRegistrationCallback.EVENT.register(this::registerCommands);
        INSTANCE = this;
    }

    private void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registry, CommandManager.RegistrationEnvironment env) {
        dispatcher.register(CommandManager.literal("drone")
                .then(CommandManager.literal("create").executes(this::createDrone))
                .then(CommandManager.literal("clear").executes(this::clearDrones)));
    }

    private int createDrone(CommandContext<ServerCommandSource> ctx) {
        var player = Objects.requireNonNull(ctx.getSource().getPlayer());
        var sw = player.getServerWorld();
        var entity = new DroneEntity(DRONE, sw);
        sw.spawnEntity(entity);
        var spawnPos = player.getPos().add(player.getMovementDirection().getDoubleVector().normalize().multiply(2));
        entity.setPos(spawnPos.getX(), spawnPos.getY(), spawnPos.getZ());
        createdDrones.add(entity.getUuid());
        var old = drivingDrones.get(player.getUuid());
        if (old != null) {
            var e = sw.getEntity(old);
            if (e instanceof DroneEntity droneEntity) {
                droneEntity.setDriving(false);
            }
        }
        drivingDrones.put(player.getUuid(), entity.getUuid());
        entity.setDriving(true);
        return Command.SINGLE_SUCCESS;
    }

    private int clearDrones(CommandContext<ServerCommandSource> ctx) {
        if (!ctx.getSource().isExecutedByPlayer()) return Command.SINGLE_SUCCESS;
        var player = Objects.requireNonNull(ctx.getSource().getPlayer());
        var sw = player.getServerWorld();
        for (DroneEntity droneEntity : sw.getEntitiesByType(TypeFilter.instanceOf(DroneEntity.class), it -> true)) {
            droneEntity.remove(Entity.RemovalReason.KILLED);
        }
        return Command.SINGLE_SUCCESS;
    }

    private void onPlayerJoystickState(JoystickStateC2S state, ServerPlayNetworking.Context context) {
        var player = context.player();
        //player.sendMessage(Text.of("LSB: " + state.lsbX() + ", " + state.lsbY() + " | RSB: " + state.rsbX() + ", " + state.rsbY()), true);
        if (state.rb()) {
            reselectDrone(player);
            return;
        }
        var droneId = drivingDrones.get(player.getUuid());
        if (droneId == null) {
            player.sendMessage(Text.of("No drones driving"), true);
            return;
        }
        var drone = (DroneEntity) player.getServerWorld().getEntity(droneId);
        if (drone == null) {
            player.sendMessage(Text.of("Lastly driven drone is disappeared."), true);
            return;
        }
        float yRate = 0;
        if (state.lt() != -1) {
            yRate = Math.abs(state.lt());
        } else if (state.rt() != -1) {
            yRate = -1 * Math.abs(state.rt());
        }

        var vec = new Vec3d(
                (-1 * state.lsbY() * DroneEntity.MAX_SPEED_HORIZONTAL),
                (yRate * DroneEntity.MAX_SPEED_VERTICAL),
                (state.lsbX() * DroneEntity.MAX_SPEED_HORIZONTAL)
        );
        player.sendMessage(Text.of("X: " + -1 * state.lsbY() + " Y: " + state.lsbX()), true);
        drone.addAcceleration(vec);
        System.out.println(vec);
        drone.setYaw(drone.getYaw() + (state.rsbX() * 18));
        drone.setPitch(drone.getPitch() + state.rsbY() * 9);
        drone.setBodyYaw(drone.getYaw());
        drone.setHeadYaw(drone.getYaw());
    }

    private void reselectDrone(ServerPlayerEntity player) {
        if (createdDrones.isEmpty()) {
            player.sendMessage(Text.of("No drones created."));
            return;
        }
        var sw = player.getServerWorld();
        var currentDrone = drivingDrones.get(player.getUuid());
        var entity = (DroneEntity) sw.getEntity(currentDrone);
        if (entity != null) {
            entity.setDriving(false);
        }
        var index = createdDrones.indexOf(currentDrone);
        var drone = createdDrones.get(++index % createdDrones.size());
        entity = (DroneEntity) sw.getEntity(drone);
        if (entity != null) {
            entity.setDriving(true);
        }
        drivingDrones.put(player.getUuid(), drone);
    }
}
