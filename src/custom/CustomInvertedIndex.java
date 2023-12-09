package custom;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class CustomInvertedIndex {
    private final CustomConcurrentMap<String, String> map = new CustomConcurrentMap<>();

    public CustomInvertedIndex() {

    }

    private static boolean isNotForbiddenWord(String word) {
        List<String> forbiddenWords = Arrays.asList("a", "i", "the", "is", "are");
        return !forbiddenWords.contains(word) && word.length() > 2;
    }

    public void addPairToTerms(String word, String fileName) {
        map.put(word, fileName);
    }

    public Set<String> getDocumentIndexByWord(String word) {
        Set<String> words = map.get(word);
        return words;
    }


    public CustomConcurrentMap<String, String> getMap() {
        return map;
    }

    public void singleFileToTerms(File file) {
        StringBuilder text = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                text.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        String cleanedText = text.toString().replaceAll("<br /><br />", "").toLowerCase();
        String[] splitted = cleanedText.split("\\s*[^a-zA-Z]+\\s*");

        List<String> validWords = Arrays.stream(splitted).filter(CustomInvertedIndex::isNotForbiddenWord).toList();

        for (String w : validWords) {
            this.addPairToTerms(w, file.toString());
        }
        validWords.toArray(new String[0]);
    }

    public void allFilesToTerms(List<File> files) {
        for (File f : files) {
            this.singleFileToTerms(f);
        }
    }

    public void addSomeFilesToTerms(List<File> files, Integer start, Integer end) {
        for (int i = start; i < end; i++) {
            this.singleFileToTerms(files.get(i));
        }
        System.out.println(map.size());
    }

}