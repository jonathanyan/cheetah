package com.jontera;

import java.io.PrintWriter;

public class DataGenerator {
    private final static int SIZE = 10000;
    private final static int COLNUMBER = 3;

    public static void main(String[] args) {

        RandomSeed rs = new RandomSeed(SIZE);
        int[][] col = new int[SIZE][COLNUMBER];
        int hostNumber = 1;
        if (args.length == 1) {
            hostNumber = Integer.parseInt(args[0]);
        }

        // int hostSeq = (new HashBucket()).hashBucket(randStart, hostNumber);

        for (int i = 0; i < SIZE; i++) {
            col[i][0] = (i % 2 == 0 ? SIZE / 2 - i / 2 - 1 : SIZE / 2 + i / 2);
            col[i][1] = rs.randomNumber[i];
            col[i][2] = rs.randomNumber[(i + 3571) % SIZE];
        }

        PrintWriter[] writer = new PrintWriter[hostNumber];
        for (int k = 0; k < hostNumber; k++) {
            try {
                writer[k] = new PrintWriter("../dat/cheetah_" + k + ".dat", "UTF-8");
            } catch (Exception ex) {
                System.out.println("Open file error"); 
            }
        }

        try {
            for (int i = 0; i < SIZE; i++) {
                int hostSeq = (new HashBucket()).hashBucket(col[i][0], hostNumber);
                writer[hostSeq].print("" + col[i][0] + ";" + col[i][1] + ";" + col[i][2]);
                writer[hostSeq].println();
            }

        } catch (Exception ex) {
            // report
        }

        for (int k = 0; k < hostNumber; k++) {
            try {
                writer[k].close();
            } catch (Exception ex) {
                // report
            }
        }

    }

}
