package components.concurrentMap;

import java.util.Set;

public class CustomConcurrentHashMapUse<K,V extends Set> implements ConcurrentHashMapInterface<K,V> {
    private final CustomConcurrentMap<K, V> map = new CustomConcurrentMap<>();
    public void putIfAbsent(K key, V value) {
        map.putIfAbsent(key, value);
    }

    public V get(K key) {
        return (V) map.get(key);
    }

    @Override
    public int size() {
        return map.size();
    }
}