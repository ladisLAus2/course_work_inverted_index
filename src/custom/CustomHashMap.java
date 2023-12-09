package custom;

import java.util.LinkedList;

class CustomHashMap<K,V>{
    private LinkedList<Node<K,V>> [] list;
    private int size;
    private float max_load;
    public CustomHashMap(){
        this(16,0.8f);//default value
    }
    public CustomHashMap(int capacity, float max_load){
        this.list = new LinkedList[capacity];
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
            list[i] = new LinkedList<>();
        }
        for(var node : list[i]){
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

    public V computeIfAbsent(K key, ValueProvider<K,V> valueProvider) throws Exception {
        if(key == null || valueProvider == null){
            throw new Exception("key or valueProvider is null");
        }
        int i = getIndex(key);
        if(list[i] == null){
            list[i] = new LinkedList<>();
        }
        for(var node : list[i]){
            if(node.getKey().equals(key)){
                return node.getValue();
            }
        }
        V value = valueProvider.compute(key);
        list[i].add(new Node<>(key, value));
        size++;
        if(size > list.length * max_load){
            resize();
        }
        return value;
    }
    public V putIfAbsent(K key, V value) throws Exception {
        if(key == null){
            throw new Exception("key is null");
        }
        int i = getIndex(key);
        if(list[i] == null){
            list[i] = new LinkedList<>();
        }
        for(var node : list[i]){
            if(node.getKey().equals(key)){
                return node.getValue();
            }
        }
        list[i].add(new Node<>(key,value));
        size++;
        if(size > list.length * max_load){
            resize();
        }
        return value;
    }

    private int getIndex(K key){
        return Math.abs(key.hashCode() % list.length);
    }
    public int size(){
        return size;
    }
    private void resize(){
        int newSize = list.length * 2;
        LinkedList<Node<K,V>> [] newList = new LinkedList[newSize];
        for(var listValue : list){
            if(listValue != null){
                for(var node : listValue){
                    int newIndex = Math.abs(node.getKey().hashCode()) % newSize;
                    if(newList[newIndex] == null){
                        newList[newIndex] = new LinkedList<>();
                    }
                    newList[newIndex].add(node);
                }
            }
        }
        list = newList;
    }
    @FunctionalInterface
    public interface ValueProvider<K, V> {
        V compute(K key);
    }

    private class Node<K,V>{
        private K key;
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