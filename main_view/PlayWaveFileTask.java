package main_view;

import java.io.File;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineEvent;

import javafx.concurrent.Task;

public class PlayWaveFileTask extends Task<Void> {
    private File wavFile;
    private Clip clip;
    private int nowFrame = 0;
    private boolean nowPausing = false;

    @Override
    protected Void call() throws Exception {
        try (AudioInputStream ais = AudioSystem.getAudioInputStream(wavFile)) {
            AudioFormat format = ais.getFormat();

            DataLine.Info dataLine = new DataLine.Info(Clip.class, format);

            clip = (Clip) AudioSystem.getLine(dataLine);
            clip.addLineListener(event -> updateState(event));
            clip.open(ais);
            clip.start();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
    
    /** wavファイルを再生します */
    public void start(File wavFile) {
        this.wavFile = wavFile;

        Thread th = new Thread(this);
        th.setDaemon(true);
        th.start();
        System.out.println("再生");
    }

    /** 再生を一時停止します */
    public void pause() {
        if (clip == null)
            return;
        
        setNowPausing(true);
        clip.stop();
        nowFrame = clip.getFramePosition();
        updateMessage("Pause");
    }

    /** 一時停止を解除します */
    public void unpause() {
        if (clip == null || !getNowPausing())
            return;

        clip.setFramePosition(nowFrame);
        clip.start();
    }

    /** 再生を停止し, 1番初めの再生箇所に移動します */
    public void stop() {
        if (clip == null)
            return;

        clip.stop();
        clip.setFramePosition(0);
        nowFrame = 0;
        updateMessage("Stop");
    }

    /** 再生クリップを閉じます */
    public void clipClose() {
        if (clip == null)
            return;

        clip.close();
        System.out.println("close");
    }

    /** 現在再生しているかどうかを返します */
    public boolean nowPlaying() {
        return clip.isRunning();
    }

    /** 現在再生を一時停止いているかどうかを返します */
    public boolean nowPausing() {
        return getNowPausing();
    }

    /** 現在再生しているwavファイルを返します */
    public File playingFile() {
        return wavFile;
    }

    private final Object lock = new Object();

    private void setNowPausing(boolean nowPausing) {
        synchronized (lock) {
            this.nowPausing = nowPausing;
        }
    }

    private boolean getNowPausing() {
        synchronized (lock) {
            return nowPausing;
        }
    }

    private void updateState(LineEvent event) {
        LineEvent.Type type = event.getType();

        if (type == LineEvent.Type.START)
            updateMessage("Now Playing");
        else if (type == LineEvent.Type.STOP && !getNowPausing())
            updateMessage("Stop");
    }
}
