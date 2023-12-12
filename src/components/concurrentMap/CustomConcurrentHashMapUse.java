package components.concurrentMap;

import java.util.Set;
import java.util.function.Function;

public class CustomConcurrentHashMapUse<K,V > implements ConcurrentHashMapInterface<K,V> {
    private final CustomConcurrentMap<K, V> map = new CustomConcurrentMap<>();

    public V get(K key) {
        return (V) map.get(key);
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
        return (V) map.computeIfAbsent(key, (Function<? super K, ? extends Set<V>>) mappingFunction);
    }
}