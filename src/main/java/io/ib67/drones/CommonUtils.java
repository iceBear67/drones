package io.ib67.drones;

public class CommonUtils {
    // little endian
    public static float fromBytes(int _offset, byte[] bytes) {
        int offset = Float.BYTES * _offset;
        int asInt = (bytes[offset] & 0xFF)
                | ((bytes[offset + 1] & 0xFF) << 8)
                | ((bytes[offset + 2] & 0xFF) << 16)
                | ((bytes[offset + 3] & 0xFF) << 24);
        return Float.intBitsToFloat(asInt);
    }

    public static float removeJitter(float f) {
//        return Math.round(f * 1000f) / 1000f;
        if (Math.abs(f) < 0.2) {
            return 0;
        }
        return f;
    }

    public static void writeBytes(int _offset, float value, byte[] bytes) {
        int intBits = Float.floatToIntBits(value);
        int offset = Float.BYTES * _offset;
        bytes[offset] = (byte) (intBits & 0xFF);
        bytes[offset + 1] = (byte) ((intBits >> 8) & 0xFF);
        bytes[offset + 2] = (byte) ((intBits >> 16) & 0xFF);
        bytes[offset + 3] = (byte) ((intBits >> 24) & 0xFF);
    }
}
