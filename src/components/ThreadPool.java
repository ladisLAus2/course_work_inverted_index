package components;

import java.io.File;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class ThreadPool {
    private final ExecutorService buildIndexThreadPool;
    private final ExecutorService searchIndexThreadPool;
    private final Integer cores;
    private final InvertedIndex index;

    public ThreadPool(Integer cores, InvertedIndex index) {
        this.cores = cores;
        buildIndexThreadPool = Executors.newFixedThreadPool(cores);
        searchIndexThreadPool = Executors.newCachedThreadPool();
        this.index = index;
    }

    public long createInvertedIndexThreadPool(List<File> files) {
        long start = System.nanoTime();
        int filesPerThread = files.size() / cores;
        for (int i = 0; i < cores; i++) {
            int startI = filesPerThread * i;
            int endI = (i == cores - 1) ? files.size() : filesPerThread * (i + 1);
            buildIndexThreadPool.execute(() -> index.addSomeFilesToTerms(files, startI, endI));
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
        Future<Set<String>> future = searchIndexThreadPool.submit(() -> (index.getDocumentNameByWord(word)));

        Set<String> result;
        try {
            result = future.get();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return result;
    }
    public void shutdown(){
        this.buildIndexThreadPool.shutdownNow();
        this.searchIndexThreadPool.shutdownNow();
    }
}
