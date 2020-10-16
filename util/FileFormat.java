package util;

public class FileFormat {
    public static String getExtension(String filePath) {
        return filePath.substring(filePath.lastIndexOf(".") + 1);
    }

    public static boolean equalExtension(String filePath, String extension) {
        if (getExtension(filePath).equals(extension))
            return true;

        return false;
    }
}
