package io.ib67.drones.entity;

import io.ib67.drones.CommonUtils;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.projectile.FireballEntity;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.ItemStackParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.MatrixUtil;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.awt.*;

import static java.lang.Math.*;

public class DroneEntity extends MobEntity {
    public static final double MAX_SPEED_VERTICAL = 0.1;
    public static final double MAX_SPEED_HORIZONTAL = 0.25;
    private static final Vec3d PLANE = new Vec3d(0, 1, 0);
    private static final Vector3f EAST = new Vector3f(1, 0, 0);
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
        var yawRad = toRadians(getYaw());
        Vector3f view = new Vector3f(
                (float)-Math.sin(yawRad),
                0,
                (float)Math.cos(yawRad)
        ).normalize();
        Vector3f zAxis = new Vector3f();
        view.cross(PLANE.toVector3f(), zAxis).normalize();
        Vector3f yAxis = new Vector3f();
        zAxis.cross(view, yAxis).normalize();
        Matrix4f basis = new Matrix4f(
                view.x, yAxis.x, zAxis.x, 0,
                view.y, yAxis.y, zAxis.y, 0,
                view.z, yAxis.z, zAxis.z, 0,
                0,       0,       0,       1
        );
        Matrix4f inverseBasis = new Matrix4f(basis).invert();
        Vector3f localAccel = inverseBasis.transformDirection(acceleration.toVector3f());

//        var angleAccEast = acceleration.toVector3f().angleSigned(EAST, PLANE.toVector3f());
//        var angleFacingEast = getYaw() - 90;
//        acceleration = this.acceleration.add(rotateAroundAxis(acceleration, PLANE, angleAccEast + angleFacingEast));
        this.acceleration = this.acceleration.add(new Vec3d(localAccel));
        if (getWorld() instanceof ServerWorld sw) {
            var pos = getPos().add(acceleration.multiply(4));
            sw.spawnParticles(new DustParticleEffect(Color.RED.getRGB(), 1f), pos.x, pos.y, pos.z, 3, 0d, 0d, 0d, 0d);
        }
        setCustomName(Text.of((int) getYaw() + " | " + formattedVelocity(acceleration) + " | " + formattedVelocity(this.acceleration)));
    }

    public Vec3d rotateAroundAxis(Vec3d toRot, Vec3d axis, double angle) {
        // 绕轴旋转
        // Ref: Rodrigues' rotation formula
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        double dot = toRot.dotProduct(axis);
        return new Vec3d(
                toRot.x * cos + axis.x * dot * (1 - cos) + axis.y * toRot.z * sin - axis.z * toRot.y * sin,
                toRot.y * cos + axis.y * dot * (1 - cos) + axis.z * toRot.x * sin - axis.x * toRot.z * sin,
                toRot.z * cos + axis.z * dot * (1 - cos) + axis.x * toRot.y * sin - axis.y * toRot.x * sin
        );
    }

    public void launchFireball() {
        var fb = new FireballEntity(getWorld(), this, getFacing().getDoubleVector().multiply(2), 3);
        getWorld().spawnEntity(fb);
    }

    private String formattedVelocity(Vec3d velocity) {
        return "(" + CommonUtils.removeJitter((float) velocity.x) + ", " + CommonUtils.removeJitter((float) velocity.y) + ", " + CommonUtils.removeJitter((float) velocity.z) + ")";
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
