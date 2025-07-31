package net.shuyanmc.mpem;

public class MathEngine {
    // 快速近似版本
    public static native float nativeSin(float x, boolean precise);
    public static native float nativeCos(float x, boolean precise);
    public static native float nativeFloor(float x);
    public static native float nativeCeil(float x);
    public static native float nativeAbs(float x);
    public static native float nativeClamp(float value, float min, float max);
    public static native float nativeLerp(float start, float end, float t);
    public static native float nativeWrapDegrees(float degrees);
    public static native float nativeAngleBetween(float a, float b);
    public static native float nativeInvSqrt(float x);
    public static native float nativeSqrt(float x);
    public static native float nativeAtan2(float y, float x);
    public static native float[] nativeHsvToRgb(float h, float s, float v);
    public static native int nativeSmallestEncompassingPowerOfTwo(int value);
    public static native boolean nativeIsPowerOfTwo(int value);
    public static native int nativeCeilLog2(int value);
    public static native int nativeFloorLog2(int value);
    public static native int nativeFloorDiv(int x, int y);
    public static native int nativeCeilDiv(int x, int y);
    public static native float nativeMod(float x, float y);
    public static native void nativeSimdSinCos(float[] angles, float[] sinResults, float[] cosResults, int count);

    // 包装方法
    public static float sin(float x) {
        return nativeSin(x, false);
    }

    public static float preciseSin(float x) {
        return nativeSin(x, true);
    }

    public static float cos(float x) {
        return nativeCos(x, false);
    }

    public static float preciseCos(float x) {
        return nativeCos(x, true);
    }

    public static float floor(float x) {
        return nativeFloor(x);
    }

    public static float ceil(float x) {
        return nativeCeil(x);
    }

    public static float abs(float x) {
        return nativeAbs(x);
    }

    public static float clamp(float value, float min, float max) {
        return nativeClamp(value, min, max);
    }

    public static float lerp(float start, float end, float t) {
        return nativeLerp(start, end, t);
    }

    public static float wrapDegrees(float degrees) {
        return nativeWrapDegrees(degrees);
    }

    public static float angleBetween(float a, float b) {
        return nativeAngleBetween(a, b);
    }

    public static float invSqrt(float x) {
        return nativeInvSqrt(x);
    }

    public static float sqrt(float x) {
        return nativeSqrt(x);
    }

    public static float atan2(float y, float x) {
        return nativeAtan2(y, x);
    }

    public static float[] hsvToRgb(float h, float s, float v) {
        return nativeHsvToRgb(h, s, v);
    }

    public static int smallestEncompassingPowerOfTwo(int value) {
        return nativeSmallestEncompassingPowerOfTwo(value);
    }

    public static boolean isPowerOfTwo(int value) {
        return nativeIsPowerOfTwo(value);
    }

    public static int ceilLog2(int value) {
        return nativeCeilLog2(value);
    }

    public static int floorLog2(int value) {
        return nativeFloorLog2(value);
    }

    public static int floorDiv(int x, int y) {
        return nativeFloorDiv(x, y);
    }

    public static int ceilDiv(int x, int y) {
        return nativeCeilDiv(x, y);
    }

    public static float mod(float x, float y) {
        return nativeMod(x, y);
    }

    public static void simdSinCos(float[] angles, float[] sinResults, float[] cosResults) {
        if (angles.length != sinResults.length || angles.length != cosResults.length) {
            throw new IllegalArgumentException("Arrays must have the same length");
        }
        nativeSimdSinCos(angles, sinResults, cosResults, angles.length);
    }
}