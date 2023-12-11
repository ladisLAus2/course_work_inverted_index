package components;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FilesReader {
    private final List<File> files;

    public FilesReader() {
        files = new ArrayList<>();
    }

    public void readFilesFromDirectory(String path) {
        File directory = new File(path);
        if (directory.exists() && directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File f : files) {
                    if (f.isDirectory()) {
                        readFilesFromDirectory(f.getPath());
                    } else {
                        this.files.add(f);
                    }
                }
            }
        } else {
            System.out.println("does not exist or not a directory");
        }

    }

    public List<File> getFiles() {
        return files;
    }
}
