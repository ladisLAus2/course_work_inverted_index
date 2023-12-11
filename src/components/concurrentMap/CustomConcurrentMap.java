package components.concurrentMap;

import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class CustomConcurrentMap<K, V> {
    private final CustomHashMap<K,Set<V>> map;
    private final ReadWriteLock[] locks;
    private final ReadWriteLock putIfAbsentLock;
    private final int max_num_concurrent_threads = 16;

    public CustomConcurrentMap() {
        map = new CustomHashMap<>();
        putIfAbsentLock = new ReentrantReadWriteLock();
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
        putIfAbsentLock.writeLock().lock();
        try {
            map.computeIfAbsent(key, k -> value);
        } finally {
            putIfAbsentLock.writeLock().unlock();
        }
    }


    public synchronized int size() {
        return map.size();
    }
}