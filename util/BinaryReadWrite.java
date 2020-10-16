package util;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class BinaryReadWrite {
    /**
     * 入力ストリームからリトルエンディアン方式でint型の数値を取得し返却します.
     *
     * @param is            ファイルの入力ストリーム
     * @return              読み込んだ数値
     * @throws IOException  読み込みエラー
     * @throws EOFException ファイルの終端に達したとき
     */
    public static int readLittleEndianInt(InputStream is) throws IOException, EOFException {
        byte[] buffer = new byte[4];
        int bytesRead = is.read(buffer);

        if (bytesRead != 4) {
            throw new EOFException();
        }

        return ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN).getInt();
    }

    /**
     * 入力ストリームからリトルエンディアン方式でshort型の数値を取得し返却します.
     *
     * @param is            ファイルの入力ストリーム
     * @return              読み込んだ数値
     * @throws IOException  読み込みエラー
     * @throws EOFException ファイルの終端まで読み込んだ時
     */
    public static short readLittleEndianShort(InputStream is) throws IOException, EOFException {
        byte[] buffer = new byte[2];
        int bytesRead = is.read(buffer);

        if (bytesRead != 2) {
            throw new EOFException();
        }

        return ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN).getShort();
    }

    /**
     * リトルエンディアン方式でファイルにint型の数値を書き込みます.
     *
     * @param value         書き込む数値
     * @param os            ファイルの出力ストリーム
     * @throws IOException  書き込みエラー
     */
    public static void writeLittleEndianInt(int value, OutputStream os) throws IOException {
        byte[] buffer = new byte[4];
        buffer[0] = (byte) value;
        buffer[1] = (byte) (value >> 8);
        buffer[2] = (byte) (value >> 16);
        buffer[3] = (byte) (value >> 24);
        os.write(buffer);
        os.flush();
    }

    /**
     * リトルエンディアン方式でファイルにshort型の数値を書きこみます.
     *
     * @param value         書き込む数値
     * @param os            ファイルの出力ストリーム
     * @throws IOException  書き込みエラー
     */
    public static void writeLittleEndianShort(short value, OutputStream os) throws IOException {
        byte[] buffer = new byte[2];
        buffer[0] = (byte) value;
        buffer[1] = (byte) (value >> 8);
        os.write(buffer);
        os.flush();
    }
}
