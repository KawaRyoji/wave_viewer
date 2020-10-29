package main_view;

import java.io.File;

import javafx.concurrent.Task;
import wave.WaveFormat;

public class WaveFileLoadTask extends Task<Short[]> {
    private File waveFile;

    public WaveFileLoadTask(File waveFile) {
        this.waveFile = waveFile;
    }

    public void start() {
        Thread th = new Thread(this);
        th.setDaemon(true);
        th.start();
    }

    @Override
    protected Short[] call() throws Exception {
        WaveFormat reader = new WaveFormat();
        reader.readWavFile(waveFile);

        short[] waveDataPre;
        waveDataPre = (reader.isMonaural()) ? reader.getWaveData() : reader.getLeftWaveData();

        Short[] waveData = new Short[waveDataPre.length];
        for (int i = 0; i < waveData.length; i++) {
            waveData[i] = waveDataPre[i];
        }

        return waveData;
    }
}
