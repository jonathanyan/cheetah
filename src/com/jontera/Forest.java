package com.jontera;

import java.io.BufferedReader;
import java.io.FileReader;

import org.cliffc.high_scale_lib.NonBlockingHashMap;

public class Forest {
  
    public void reloadTree(String fileName, int cheetahTreeNumber,
            NonBlockingHashMap<Integer, BTree> cheetahCache) {
        FileReader fr;
        BufferedReader br;
        BTree bTreeRoot = null;
        try {
            fr = new FileReader(fileName);
            br = new BufferedReader(fr);
            String line;
            
           

            while ((line = br.readLine()) != null) {
                String[] stringItems = line.split(";");
                int[] intItems = new int[stringItems.length];
                for (int i = 0; i < stringItems.length; i++) {
                    intItems[i] = Integer.parseInt(stringItems[i]);
                }
                
                Integer mapKey;
                HashBucket hashVal = new HashBucket();
                
                
                mapKey = new Integer(hashVal.hashBucket(intItems[0], cheetahTreeNumber));
                if (!cheetahCache.containsKey(mapKey)) {
                    bTreeRoot = new BTree();
                    cheetahCache.put(mapKey, bTreeRoot);
                } else {
                    bTreeRoot = cheetahCache.get(mapKey);

                }

                Apple apple = new Apple(new FruitKey(intItems[0]), intItems[1],
                        intItems[2]);
                
                bTreeRoot = bTreeRoot.insertNewFruit(apple, null, bTreeRoot, -1);

                // BTree node = resetOneNode(intItems);

            }
            br.close();
        } catch (Exception ex) {

        } finally {
            // br.close();
        }
        //bTreeRoot.traversalTree("LOAD", bTreeRoot, 0);
    }

}
