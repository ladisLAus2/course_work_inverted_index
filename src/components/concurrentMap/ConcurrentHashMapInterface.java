package components.concurrentMap;

import java.util.function.Function;

public interface ConcurrentHashMapInterface<K,V> {
    V get(K key);
    int size();
     V computeIfAbsent(K key, Function<? super K,? extends V> mappingFunction);
}