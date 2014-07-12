package com.jontera;

public class HashBucket {
    int hashBucket(String key, int bucketNumber) {
        int hashVal = 0;
        for (int i = 0; i < key.length(); i++) {
            hashVal = (127 * hashVal + key.charAt(i)) % 15485867;
        }
        return hashVal % bucketNumber;
    }

    int hashBucket(int key, int bucketNumber) {
        return ((key * 17077 + 9013) % 15485867) % bucketNumber;
    }
}