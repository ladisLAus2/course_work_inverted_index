import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;

public class ThreadPool {
    private final ExecutorService buildIndexThreadPool;
    private final ExecutorService searchIndexThreadPool;
    private final Integer numCPU;
    private final InvertedIndex index;
    public ThreadPool(Integer numCPU, InvertedIndex index){
        this.numCPU = numCPU;
        buildIndexThreadPool = Executors.newFixedThreadPool(numCPU);
        searchIndexThreadPool = Executors.newCachedThreadPool();
        this.index = index;
    }

    public void createInvertedIndexThreadPool(List<File> files){
        int filesPerThread = files.size() / numCPU;
        for (int i = 0; i < numCPU; i++) {
            if(i == numCPU - 1){
                int finalI = i;
                buildIndexThreadPool.execute(() -> index.addSomeFilesToTerms(files, filesPerThread * finalI, files.size()));
                break;
            }
            int fi = i;
            buildIndexThreadPool.execute(() -> index.addSomeFilesToTerms(files, filesPerThread * fi, filesPerThread * (fi +1)));
        }
        buildIndexThreadPool.shutdown();
        try {
            buildIndexThreadPool.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public Set<String> searchInvertedIndexThreadPool(String word){
        Future<Set<String>> future = searchIndexThreadPool.submit(() -> (index.getDocumentIndexByWord(word)));
        Set<String> result;
        try {
            result = future.get();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return result;
    }
}
