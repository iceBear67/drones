package io.ib67.drones.entity;

import io.ib67.drones.CommonUtils;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.MatrixUtil;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.joml.Matrix3f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import static java.lang.Math.*;

public class DroneEntity extends MobEntity {
    public static final double MAX_SPEED_VERTICAL = 0.1;
    public static final double MAX_SPEED_HORIZONTAL = 0.25;
    protected Vec3d acceleration = new Vec3d(0, 0, 0);
    @Getter
    @Setter
    protected boolean driving;

    public DroneEntity(EntityType<? extends MobEntity> entityType, World world) {
        super(entityType, world);
        setCustomNameVisible(true);
    }

    @Override
    protected void mobTick(ServerWorld world) {
        super.mobTick(world);
        var originVec = getVelocity();
        var newX = originVec.x + (acceleration.x);
        var newZ = originVec.z + (acceleration.z);
        var newY = originVec.y + (acceleration.y);
        if (abs(newY) > MAX_SPEED_VERTICAL) {
            newY = signum(newY) * MAX_SPEED_VERTICAL;
        }
        if (abs(newX) > MAX_SPEED_HORIZONTAL) {
            newX = signum(newX) * MAX_SPEED_HORIZONTAL;
        }
        if (abs(newZ) > MAX_SPEED_HORIZONTAL) {
            newZ = signum(newZ) * MAX_SPEED_HORIZONTAL;
        }
        this.setVelocity(newX, newY, newZ);
        acceleration = Vec3d.ZERO;
        this.setGlowing(driving);
    }

    public void addAcceleration(Vec3d acceleration) {
        //todo fix
        double yawRad = Math.toRadians(-getYaw());

        double cosYaw = Math.cos(yawRad);
        double sinYaw = Math.sin(yawRad);

        double x = acceleration.getX();
        double y = acceleration.getY();
        double z = acceleration.getZ();

        double x1 = x * cosYaw - z * sinYaw;
        double z1 = x * sinYaw + z * cosYaw;
        this.acceleration = acceleration.add(x1, y, z1);
        setCustomName(Text.of((int)getYaw()+" | "+formattedVelocity(acceleration)+ " | "+formattedVelocity(this.acceleration)));
    }

    private String formattedVelocity(Vec3d velocity) {
        return "("+ CommonUtils.removeJitter((float) velocity.x)+", "+CommonUtils.removeJitter((float) velocity.y)+", "+CommonUtils.removeJitter((float) velocity.z)+")";
    }

    @Override
    protected double getGravity() {
        return 0;
    }

    @Override
    public boolean shouldSave() {
        return false;
    }
}
