package components.concurrentMap;

import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Function;

public class CustomConcurrentMap<K, V> {
    private final CustomHashMap<K,Set<V>> map;
    private final ReadWriteLock[] locks;
    private final ReadWriteLock writeLock;
    private final int max_num_concurrent_threads = 16;

    public CustomConcurrentMap() {
        map = new CustomHashMap<>();
        writeLock = new ReentrantReadWriteLock();
        locks = new ReadWriteLock[max_num_concurrent_threads];
        for (int i = 0; i < max_num_concurrent_threads; i++) {
            locks[i] = new ReentrantReadWriteLock();
        }
    }
    private ReadWriteLock getLock(K key) {
        int hashCode = key.hashCode();
        int lockIndex = Math.abs(hashCode % locks.length);
        return locks[lockIndex];
    }

    public Set<V> get(K key) {
        ReadWriteLock lock = getLock(key);
        lock.readLock().lock();
        try {
            return map.get(key);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            lock.readLock().unlock();
        }
    }

    public void putIfAbsent(K key, Set<V> value) {
        writeLock.writeLock().lock();
        try {
            map.computeIfAbsent(key, k -> value);
        } finally {
            writeLock.writeLock().unlock();
        }
    }

    public Set<V> computeIfAbsent(K key, Function<? super K, ? extends Set<V>> mappingFunction) {
        writeLock.writeLock().lock();
        try {
            return map.computeIfAbsent(key, mappingFunction);
        } finally {
            writeLock.writeLock().unlock();
        }
    }


    public synchronized int size() {
        return map.size();
    }
}