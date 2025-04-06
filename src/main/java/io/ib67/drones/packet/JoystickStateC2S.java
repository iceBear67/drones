package io.ib67.drones.packet;

import io.ib67.drones.CommonUtils;
import io.ib67.drones.Drones;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/**
 * Buttons:
 * Y A X B LB RB BACK START (8 bits, a byte)
 * AXES:
 * L(X,Y) R(X,Y) LT RT (6 float)
 *
 * @param buttons
 */
public record JoystickStateC2S(
        byte buttons,
        byte[] axes
) implements CustomPayload {
    public static final int BUTTON_Y = 1 << 7;
    public static final int BUTTON_A = 1 << 6;
    public static final int BUTTON_X = 1 << 5;
    public static final int BUTTON_B = 1 << 4;
    public static final int BUTTON_LB = 1 << 3;
    public static final int BUTTON_RB = 1 << 2;
    public static final int BUTTON_BACK = 1 << 1;
    public static final int BUTTON_START = 1;
    public static final Identifier PAYLOAD_ID = Identifier.of(Drones.MOD_ID, "jstate");
    public static final CustomPayload.Id<JoystickStateC2S> PKT_ID = new CustomPayload.Id<>(PAYLOAD_ID);
    public static final PacketCodec<RegistryByteBuf, JoystickStateC2S> PACKET_CODEC = PacketCodec.tuple(
            PacketCodecs.BYTE, JoystickStateC2S::buttons,
            PacketCodecs.BYTE_ARRAY, JoystickStateC2S::axes,
            JoystickStateC2S::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return PKT_ID;
    }

    public boolean y() {
        return (buttons & BUTTON_Y) != 0;
    }

    public boolean a() {
        return (buttons & BUTTON_A) != 0;
    }

    public boolean x() {
        return (buttons & BUTTON_X) != 0;
    }

    public boolean b() {
        return (buttons & BUTTON_B) != 0;
    }

    public boolean lb() {
        return (buttons & BUTTON_LB) != 0;
    }

    public boolean rb() {
        return (buttons & BUTTON_RB) != 0;
    }

    public boolean back() {
        return (buttons & BUTTON_BACK) != 0;
    }

    public boolean start() {
        return (buttons & BUTTON_START) != 0;
    }

    public float lsbX() {
        return CommonUtils.fromBytes(0, axes);
    }

    public float lsbY() {
        return CommonUtils.fromBytes(1, axes);
    }

    public float rsbX() {
        return CommonUtils.fromBytes(2, axes);
    }

    public float rsbY() {
        return CommonUtils.fromBytes(3, axes);
    }

    public float lt() {
        return CommonUtils.fromBytes(4, axes);
    }

    public float rt() {
        return CommonUtils.fromBytes(5, axes);
    }

    @Override
    public String toString() {
        var sb = new StringBuilder();
        sb.append("Buttons: ");
        if (y()) sb.append("Y ");
        if (a()) sb.append("A ");
        if (x()) sb.append("X ");
        if (b()) sb.append("B ");
        if (lb()) sb.append("LB");
        if (rb()) sb.append("RB ");
        if (back()) sb.append("Back ");
        if (start()) sb.append("Start ");
        sb.append("\n");
        sb.append("Axes: \n");
        sb.append("LSB: (").append(lsbX()).append(", ").append(lsbY()).append("), RSB: ").append(rsbX()).append(", ").append(rsbY()).append("\n");
        sb.append("LT: ").append(lt()).append(", RT: ").append(rt());
        return sb.toString();
    }
}
