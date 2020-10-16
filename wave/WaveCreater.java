package wave;

import util.TypeConvert;

public class WaveCreater {
    /**
     * ノコギリ波を生成します
     * 
     * @param A             振幅
     * @param f0            基本周波数
     * @param fs            サンプリング周波数
     * @param bps           量子化bit数
     * @param timeMillis    再生時間(ms単位)
     * @param overtones     重ね合わせる倍音の個数
     * @return              ノコギリ波のデータ
     */
    public static short[] createSawWave(double A, double f0, double fs, int bps, double timeMillis, int overtones) {
        int length = (int) Math.floor(fs * timeMillis / 1000.0);

        short[] signal = new short[length];
        for (int i = 0; i < length; i++) {
            for (int j = 0; j < overtones; j++) {
                signal[i] += TypeConvert.double2short(A / j * Math.sin(2.0 * Math.PI * f0 * i * j / fs));
            }
        }

        return signal;
    }

    /**
     * 矩形波を生成します
     * 
     * @param A             振幅
     * @param f0            基本周波数
     * @param fs            サンプリング周波数
     * @param bps           量子化bit数
     * @param timeMillis    再生時間(ms単位)
     * @param overtones     重ね合わせる倍音の個数
     * @return              ノコギリ波のデータ
     */
    public static short[] createSquareWave(double A, double f0, double fs, int bps, double timeMillis, int overtones) {
        int length = (int) Math.floor(fs * timeMillis / 1000.0);

        short[] signal = new short[length];
        for (int i = 0; i < length; i++) {
            for (int j = 1; j <= overtones; j++) {
                if (j % 2 == 0)
                    continue;
                signal[i] += TypeConvert.double2short(A / j * Math.sin(2.0 * Math.PI * f0 * i * j / fs));
            }
        }

        return signal;
    }
}
