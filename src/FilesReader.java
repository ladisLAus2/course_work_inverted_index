import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FilesReader {
    private final List<File> fileList = new ArrayList<>();
    private final InvertedIndex index = new InvertedIndex();
    public FilesReader() {
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
                        fileList.add(f);
                    }
                }
            }
        } else {
            System.out.println("does not exist or not a directory");
        }
    }

    public List<File> getFileList() {
        return fileList;
    }

    public InvertedIndex getIndex() {
        return index;
    }

    public void readFileByIndex(int index) {
        if (index >= 0 && index < fileList.size()) {
            File file = fileList.get(index);
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
