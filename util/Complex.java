package util;

/**
 * 複素数を扱うクラスです
 * 配列を作成る場合はcreate()メソッドを使うと便利です
 */
public class Complex {
    public double real;
    public double imag;

    /**
     * @return a + b
     */
    public static Complex add(Complex a, Complex b) {
        Complex c = new Complex();
        c.real = a.real + b.real;
        c.imag = a.imag + b.imag;
        return c;
    }

    /**
     * @return a - b
     */
    public static Complex sub(Complex a, Complex b) {
        Complex c = new Complex();
        c.real = a.real - b.real;
        c.imag = a.imag - b.real;
        return c;
    }

    /**
     * @return a * b
     */
    public static Complex mul(Complex a, Complex b) {
        Complex c = new Complex();
        c.real = a.real * b.real - a.imag * b.imag;
        c.imag = a.real * b.imag + a.imag * b.real;
        return c;
    }

    /**
     * @return a / b
     */
    public static Complex div(Complex a, Complex b) {
        b.imag *= -1;
        Complex cu = mul(a, b);
        double cd = b.real * b.real + b.imag * b.imag;
        Complex c = new Complex();
        c.real = cu.real / cd;
        c.imag = cu.imag / cd;
        return c;
    }

    public static Complex[] create(int length) {
        Complex[] x = new Complex[length];

        for (int i = 0; i < x.length; i++) {
            x[i] = new Complex();
            x[i].real = 0;
            x[i].imag = 0;
        }

        return x;
    }

    /**
     * @return 複素数の大きさ
     */
    public static double power(Complex c) {
        return Math.sqrt(Math.pow(c.real, 2) + Math.pow(c.imag, 2));
    }

    @Override
    public String toString() {
        return String.format("%f + j%f", real, imag);
    }
}
