package util;

public class TypeConvert {
    public static short double2short(double d) {
        short s = (short) (d * (double) Short.MAX_VALUE);

        s = (short) Math.min((int) s, (int) Short.MAX_VALUE);
        s = (short) Math.max((int) s, (int) Short.MIN_VALUE);

        return s;
    }

    public static double short2double(short s) {
        double d = (double) s / (double) Short.MAX_VALUE;

        d = Math.min(d, 1.0);
        d = Math.max(d, -1.0);

        return d;
    }
}
