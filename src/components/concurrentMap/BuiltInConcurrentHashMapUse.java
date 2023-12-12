package components.concurrentMap;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class BuiltInConcurrentHashMapUse<K,V > implements ConcurrentHashMapInterface<K,V> {
    private final ConcurrentHashMap<K, V> map = new ConcurrentHashMap<>();


    public V get(K key){
        return map.get(key);
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
        return map.computeIfAbsent(key, mappingFunction);
    }
}