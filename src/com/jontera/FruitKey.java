package com.jontera;

public class FruitKey {
    public int id;
    
    FruitKey(int id) {
        this.id = id;
    }

    int compareTo(FruitKey fruitKey) {
        if (this.id < fruitKey.id) return -1;
        else if (this.id > fruitKey.id) return 1;
        else return 0;
    }
}
