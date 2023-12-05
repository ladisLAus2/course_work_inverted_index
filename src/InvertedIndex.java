import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class InvertedIndex {
    private Map<String, Set<String>> terms = new ConcurrentHashMap<>();

    public InvertedIndex(){

    }

    public void addPairToTerms(String word, String fileName){
        terms.computeIfAbsent(word, (n) -> ConcurrentHashMap.newKeySet()).add(fileName);
    }

    public Set<String> getDocumentIndexByWord(String word){
        return terms.get(word);
    }

    public Map<String, Set<String>> getTerms() {
        return terms;
    }

    public String[] singleFileToTerms(File file){
        StringBuilder text = new StringBuilder();
        try(BufferedReader reader = new BufferedReader(new FileReader(file))){
            String line;
            while((line = reader.readLine()) != null){
                text.append(line);
            }
        }
        catch (IOException e){
            e.printStackTrace();
        }
        String cleanedText = text.toString().replaceAll("<br /><br />", "").toLowerCase();
        String[] splitted = cleanedText.split("\\s*[^a-zA-Z]+\\s*");

        List<String> validWords = Arrays.stream(splitted).filter(InvertedIndex::isNotForbiddenWord).toList();

        for(String w : validWords){
                this.addPairToTerms(w,file.toString());
        }
        return validWords.toArray(new String[0]);
    }

    private static boolean isNotForbiddenWord(String word){
        List<String> forbiddenWords = Arrays.asList("a","i", "the", "is", "are");
        return !forbiddenWords.contains(word) && word.length() > 2;
    }


    public void allFilesToTerms(List<File> files){
        for(File f : files){
            this.singleFileToTerms(f);
        }

    }

    @Override
    public String toString() {
        return "InvertedIndex{" +
                "terms=" + terms +
                '}';
    }

    public void show(){
        for(var a : terms.keySet()){
            System.out.println(a + " --- " + terms.get(a));
            System.out.println("\n\n");
        }
    }
}
