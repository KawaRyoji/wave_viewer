package wave;

import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

import util.BinaryReadWrite;
import util.FileFormat;

/**
 * WAVファイルの読み書きする機能を提供します
 */
public class WaveFormat {
    private AudioFormat format;
    private short[] waveData;
    private String fileName;

    private File openWavFile(String filePath) throws FileNotFoundException, IllegalArgumentException {
        File wavFile = new File(filePath);

        if (!wavFile.exists())
            throw new FileNotFoundException("このファイルは存在しません:" + filePath);

        if (!FileFormat.equalExtension(filePath, "wav"))
            throw new IllegalArgumentException("このファイルはwavファイルではありません:" + filePath);

        fileName = wavFile.getName();
        return wavFile;
    }

    /* ---------WAVファイルの読み込みメソッド類--------- */

    /**
     * WAVファイルを読み込みます
     * 
     * @param wavFile 読み込むwavファイル
     * @throws IOException                   読み込みに失敗した場合
     * @throws UnsupportedAudioFileException wavFileがAudioFormatに準拠していない場合
     */
    public void readWavFile(File wavFile) throws IOException, UnsupportedAudioFileException {
        AudioInputStream stream = AudioSystem.getAudioInputStream(wavFile);
        format = stream.getFormat();

        waveData = readWavData(stream);
    }

    /**
     * WAVファイルを読み込みます
     * 
     * @param filePath 読み込むwavファイルのパス
     * @throws IllegalArgumentException      読み込むファイルがwav出ない場合
     * @throws IOException                   読み込みに失敗した場合
     * @throws UnsupportedAudioFileException 読み込むwavファイルがAudioFormatに準拠していない場合
     */
    public void readWavFile(String filePath)
            throws IllegalArgumentException, IOException, UnsupportedAudioFileException {
        File wavFile = openWavFile(filePath);
        readWavFile(wavFile);
    }

    private short[] readWavData(AudioInputStream ais) throws IOException, EOFException {
        short[] data = new short[ais.available() / 2];

        for (int i = 0; i < data.length; i++) {
            data[i] = BinaryReadWrite.readLittleEndianShort(ais);
        }

        return data;
    }

    /**
     * 読み込んだ波形データを返します
     * 
     * @return 波形データ
     */
    public short[] getWaveData() {
        return waveData;
    }

    /**
     * 読み込んだ波形データから左の音声の波形データを取り出し, 返します
     * 
     * @return 左の音声の波形データ
     * @throws Exception 読み込んだ波形データがステレオではない場合
     */
    public short[] getLeftWaveData() throws Exception {
        if (format.getChannels() != 2)
            throw new Exception("このファイルのチャンネル数は2ではありません");

        short[] leftWaveData = new short[waveData.length / 2];
        for (int i = 0; i < waveData.length; i += 2) {
            leftWaveData[i / 2] = waveData[i];
        }

        return leftWaveData;
    }

    /**
     * 読み込んだ波形データから右の音声の波形データを取り出し, 返します
     * 
     * @return 右の音声の波形データ
     * @throws Exception 読み込んだ波形データがステレオではない場合
     */
    public short[] getRightWaveData() throws Exception {
        if (format.getChannels() != 2)
            throw new Exception("このファイルのチャンネル数は2ではありません");

        short[] rightWaveData = new short[waveData.length / 2];
        for (int i = 1; i < waveData.length; i += 2) {
            rightWaveData[(int) Math.floor((double) i / 2.0)] = waveData[i];
        }

        return rightWaveData;
    }

    /**
     * 読み込んだファイルのAudioFormatを返します
     * 
     * @return AudioFormat
     */
    public AudioFormat getAudioFormat() {
        return format;
    }

    /* ---------WAVファイルの書き込みメソッド類--------- */

    /**
     * WAVファイルを書き込みます
     * 
     * @param filePath 書き込むWAVファイルのファイルパス
     * @param format   書き込むWAVファイルのフォーマット
     * @param waveData 書き込む波形データ
     */
    public void writeWavFile(String filePath, AudioFormat format, short[] waveData) {
        if (!FileFormat.equalExtension(filePath, "wav")) {
            System.out.println("ファイルはwav拡張子である必要があります: " + filePath);
            return;
        }

        this.waveData = waveData;

        File wavFile = new File(filePath);
        boolean hasError = false;
        try (DataOutputStream dos = new DataOutputStream(new FileOutputStream(wavFile))) {
            writeRIFFChunk(dos, waveData.length * 2 + 24);
            writeFMTChunk(dos, format);
            writeDATAChunk(dos, format.getChannels() == 1);
        } catch (Exception e) {
            e.printStackTrace();
            hasError = true;
        }

        if (hasError) {
            wavFile.delete();
        }

        this.fileName = wavFile.getName();
        this.format = format;
    }

    /**
     * ステレオ形式のWAVファイルを書き込みます
     * 
     * @param filePath      書き込むWAVファイルのファイルパス
     * @param format        書き込むWAVファイルのフォーマット
     * @param leftWaveData  書き込むWAVファイルの左の波形データ
     * @param rightWaveData 書き込むWAVファイルの右の波形データ
     */
    public void writeWavFile(String filePath, AudioFormat format, short[] leftWaveData, short[] rightWaveData) {
        short[] waveData = integrateWaveData(leftWaveData, rightWaveData);
        writeWavFile(filePath, format, waveData);
    }

    private short[] integrateWaveData(short[] leftWaveData, short[] rightWaveData) {
        short[] integratedWaveData = new short[leftWaveData.length * 2];
        for (int i = 0; i < integratedWaveData.length; i++) {
            integratedWaveData[i] = (i % 2 == 0) ? leftWaveData[(int) Math.floor((double) i / 2.0)]
                    : rightWaveData[(int) Math.floor((double) i / 2.0)];
        }
        return integratedWaveData;
    }

    private void writeRIFFChunk(DataOutputStream dos, int chunkSize) throws IOException {
        dos.writeByte('R');
        dos.writeByte('I');
        dos.writeByte('F');
        dos.writeByte('F');
        BinaryReadWrite.writeLittleEndianInt(chunkSize, dos);
        dos.writeByte('W');
        dos.writeByte('A');
        dos.writeByte('V');
        dos.writeByte('E');
    }

    private void writeFMTChunk(DataOutputStream dos, AudioFormat format) throws IOException {
        dos.writeByte('f');
        dos.writeByte('m');
        dos.writeByte('t');
        dos.writeByte(' ');
        BinaryReadWrite.writeLittleEndianInt(16, dos);
        BinaryReadWrite.writeLittleEndianShort((short) 1, dos);
        BinaryReadWrite.writeLittleEndianShort((short) format.getChannels(), dos);
        BinaryReadWrite.writeLittleEndianInt((int) format.getSampleRate(), dos);
        BinaryReadWrite.writeLittleEndianInt(format.getFrameSize() * (int) format.getSampleRate(), dos);
        BinaryReadWrite.writeLittleEndianShort((short) format.getFrameSize(), dos);
        BinaryReadWrite.writeLittleEndianShort((short) format.getSampleSizeInBits(), dos);
    }

    private void writeDATAChunk(DataOutputStream dos, Boolean isMono) throws IOException {
        dos.writeByte('d');
        dos.writeByte('a');
        dos.writeByte('t');
        dos.writeByte('a');
        BinaryReadWrite.writeLittleEndianInt(waveData.length * 2, dos);

        if (isMono) {
            for (int i = 0; i < waveData.length; i++) {
                BinaryReadWrite.writeLittleEndianShort(waveData[i], dos);
            }
        } else {
            short[] leftWaveData;
            short[] rightWaveData;
            try {
                leftWaveData = getLeftWaveData();
                rightWaveData = getRightWaveData();
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
            for (int i = 0; i < rightWaveData.length; i++) {
                BinaryReadWrite.writeLittleEndianShort(leftWaveData[i], dos);
                BinaryReadWrite.writeLittleEndianShort(rightWaveData[i], dos);
            }
        }

    }

    /**
     * 拡張子を抜いたファイル名を返します
     * 
     * @return 拡張子を抜いたファイル名
     */
    public String getFileName() {
        String name = fileName.substring(0, fileName.lastIndexOf("."));

        return name;
    }

    /**
     * 読み込んだWaveファイルがモノラルかどうかbool値で返します
     * 
     * @return モノラルかどうか
     */
    public boolean isMonaural() {
        return format.getChannels() == 1;
    }

    @Override
    public String toString() {
        return format.toString();
    }
}