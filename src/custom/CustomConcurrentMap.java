package custom;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class CustomConcurrentMap<K, V> {
    private final CustomHashMap<K,Set<V>> map = new CustomHashMap<>();
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    public void put(K key, V value) {
        lock.writeLock().lock();
        try {
            map.computeIfAbsent(key, k -> new HashSet<>()).add(value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public Set<V> get(K key) {
        lock.readLock().lock();
        System.out.println(Thread.currentThread().getName() + " start");
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        try {
            return map.get(key);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            System.out.println(Thread.currentThread().getName() + " end");
            lock.readLock().unlock();
        }
    }
    public int size() {
        return map.size();
    }
}