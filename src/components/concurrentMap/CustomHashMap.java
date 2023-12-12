package components.concurrentMap;

import java.util.ArrayList;
import java.util.function.Function;

public class CustomHashMap<K,V>{
    private ArrayList<Node<K,V>> [] list;
    private int size;
    private final float max_load;

    public CustomHashMap(){
        this(16,0.8f);//default value
    }

    public CustomHashMap(int capacity, float max_load){
        this.list = new ArrayList[capacity];
        this.size = 0;
        this.max_load = max_load;
    }

    public V get(K key) throws Exception {
        if(key == null){
            throw new Exception("key is null");
        }
        int i = getIndex(key);
        if(list[i] != null){
            for(Node<K,V> node : list[i]){
                if(node.getKey().equals(key)){
                    return node.getValue();
                }
            }
        }
        return null;
    }

    public void put(K key, V value) throws Exception {
        if(key == null){
            throw new Exception("Key is null");
        }
        int i = getIndex(key);
        if(list[i] == null){
            list[i] = new ArrayList<>();
        }
        for(Node<K, V> node : list[i]){
            if(node.getKey().equals(key)){
                node.setValue(value);
                return;
            }
        }
        list[i].add(new Node<>(key, value));
        size++;
        if(size > list.length * max_load){
            resize();
        }
    }
    public V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
        int i = getIndex(key);
        if (list[i] != null) {
            for (Node<K, V> node : list[i]) {
                if (node.getKey().equals(key)) {
                    return node.getValue();
                }
            }
        }

        V computedValue = mappingFunction.apply(key);

        if (computedValue != null) {
            try {
                put(key, computedValue);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        return computedValue;
    }

    private int getIndex(K key){
        return Math.abs(key.hashCode() % list.length);
    }
    public int size(){
        return size;
    }
    private void resize(){
        int newSize = list.length * 2;
        ArrayList<Node<K,V>> [] newList = new ArrayList[newSize];
        for(ArrayList<Node<K, V>> listValue : list){
            if(listValue != null){
                for(Node<K, V> node : listValue){
                    int newIndex = Math.abs(node.getKey().hashCode()) % newSize;
                    if(newList[newIndex] == null){
                        newList[newIndex] = new ArrayList<>();
                    }
                    newList[newIndex].add(node);
                }
            }
        }
        list = newList;
    }


    private static class Node<K,V>{
        private final K key;
        private V value;

        public Node(K key, V value){
            this.key = key;
            this.value = value;
        }
        public void setValue(V value){
            this.value = value;
        }
        public K getKey(){
            return this.key;
        }
        public V getValue(){
            return this.value;
        }
    }
}