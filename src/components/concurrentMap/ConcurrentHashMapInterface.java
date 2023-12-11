package components.concurrentMap;

public interface ConcurrentHashMapInterface<K,V> {
    void putIfAbsent(K key, V value);
    V get(K key);
    int size();
}