package components;

import components.concurrentMap.ConcurrentHashMapInterface;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class InvertedIndex  {
    private final ConcurrentHashMapInterface<String, Set<String>> map;

    public InvertedIndex(ConcurrentHashMapInterface<String, Set<String>> map) {
        this.map = map;
    }

    private static boolean isNotForbiddenWord(String word) {
        List<String> forbiddenWords = Arrays.asList("a", "i", "the", "is", "are");
        return !forbiddenWords.contains(word) && word.length() > 2; //містить більш ніж 2 літери в слові
    }

    public void addPairToMap(String word, String fileName) {
        map.computeIfAbsent(word,k -> new HashSet<>()).add(fileName);
        //map.get(word).add(fileName);
    }

    public Set<String> getDocumentNameByWord(String word) {
        return map.get(word);
    }

    public ConcurrentHashMapInterface<String, Set<String>> getMap() {
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

        List<String> validWords = Arrays.stream(splitted)
                .filter(InvertedIndex::isNotForbiddenWord)
                .collect(Collectors.toList());

        for (String w : validWords) {
            this.addPairToMap(w, file.toString());
        }
//        System.out.println(map.size());
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
    }

}
