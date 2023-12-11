package components.concurrentMap;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class BuiltInConcurrentHashMapUse<K,V extends Set> implements ConcurrentHashMapInterface<K,V> {
    private final ConcurrentHashMap<K, V> map = new ConcurrentHashMap<>();

    public void putIfAbsent(K key, V value){
        map.putIfAbsent(key, value);
    }
    public V get(K key){
        return map.get(key);
    }

    @Override
    public int size() {
        return map.size();
    }
}