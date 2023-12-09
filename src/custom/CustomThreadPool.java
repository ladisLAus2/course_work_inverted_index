package custom;

import java.io.File;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class CustomThreadPool {
    private final ExecutorService buildIndexThreadPool;
    private final ExecutorService searchIndexThreadPool;
    private final Integer numCPU;
    private final CustomInvertedIndex index;

    public CustomThreadPool(Integer numCPU, CustomInvertedIndex index) {
        this.numCPU = numCPU;
        buildIndexThreadPool = Executors.newFixedThreadPool(numCPU);
        searchIndexThreadPool = Executors.newCachedThreadPool();
        this.index = index;
    }

    public long createInvertedIndexThreadPool(List<File> files) {
        long start = System.nanoTime();
        int filesPerThread = files.size() / numCPU;
        for (int i = 0; i < numCPU; i++) {
            int fi = i;
            if (i == numCPU - 1) {
                buildIndexThreadPool.execute(() -> index.addSomeFilesToTerms(files, filesPerThread * fi, files.size()));
                break;
            }
            buildIndexThreadPool.execute(() -> index.addSomeFilesToTerms(files, filesPerThread * fi, filesPerThread * (fi + 1)));
        }
        buildIndexThreadPool.shutdown();
        try {
            buildIndexThreadPool.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        long end = System.nanoTime();
        return end - start;

    }

    public Set<String> searchInvertedIndexThreadPool(String word) {
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
