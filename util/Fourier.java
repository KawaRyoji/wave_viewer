package util;

public class Fourier {
    public static Complex[] DFT(short[] data, int N) {
        Complex[] x = dataInit(data);
        Complex[] X = Complex.create(N);

        for (int k = 0; k < N; k++) {
            for (int n = 0; n < N; n++) {
                Complex W = new Complex();
                W.real = Math.cos(2.0 * Math.PI * (double) k * (double) n / (double) N);
                W.imag = -Math.sin(2.0 * Math.PI * (double) k * (double) n / (double) N);

                X[k] = Complex.add(X[k], Complex.mul(W, x[n]));
            }
        }

        return X;
    }
    
    private static Complex[] dataInit(short[] data) {
        Complex[] com = Complex.create(data.length);
        
        for (int i = 0; i < data.length; i++) {
            double d = TypeConvert.short2double(data[i]);
            com[i].real = d;
            com[i].imag = 0;
        }

        return com;
    }
}
