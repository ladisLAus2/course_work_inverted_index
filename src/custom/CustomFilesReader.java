package custom;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CustomFilesReader {
    private final List<File> files = new ArrayList<>();
    private final CustomInvertedIndex index = new CustomInvertedIndex();

    public CustomFilesReader() {
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

    public CustomInvertedIndex getIndex() {
        return index;
    }

    public void readFileByIndex(int index) {
        if (index >= 0 && index < files.size()) {
            File file = files.get(index);
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Invalid index");
        }
    }

}
