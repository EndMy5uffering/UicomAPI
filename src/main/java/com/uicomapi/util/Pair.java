package com.uicomapi.util;

public class Pair<K, V> {
    
    private K first;
    private V secound;

    public Pair(K first, V secound){
        this.first = first;
        this.secound = secound;
    }

    public K getFirst() {
        return first;
    }

    public void setFirst(K first) {
        this.first = first;
    }

    public V getSecound() {
        return secound;
    }

    public void setSecound(V secound) {
        this.secound = secound;
    }
    
}
