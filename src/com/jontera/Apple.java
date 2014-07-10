package com.jontera;

import java.io.PrintWriter;

public class Apple extends Fruit {
    private int demand;
    private int onhand;

    Apple() {
        this.id = new FruitKey(0);
        // this.id = super.id;
        this.demand = 0;
        this.onhand = 0;
    }

    Apple(FruitKey id) {
        super(id);
        // this.id = super.id;
        this.demand = 0;
        this.onhand = 0;
    }

    Apple(FruitKey id, int demand, int onhand) {
        super(id);
        // this.id = super.id;
        this.demand = demand;
        this.onhand = onhand;
    }

    public int getDemand() {
        return demand;
    }

    public synchronized void setDemand(String demand) {
        this.demand = Integer.parseInt(demand);
    }

    public synchronized void setDemandInc() {
        this.demand++;
    }

    public synchronized void setDemandDec() {
        this.demand--;
    }

    public int getOnhand() {
        return onhand;
    }

    public synchronized void setOnhand(String onhand) {
        this.onhand = Integer.parseInt(onhand);
    }

    void dumpAppleTree(BTree node, int level, PrintWriter writer) {
        if (node == null) {
            return;
        }

        try {

            for (int i = 0; i < node.entrySize; i++) {
                Apple apple = (Apple) node.fruits[i];
                writer.println("" + apple.id.id + ";" + apple.getDemand() + ";"
                        + apple.getOnhand());

            }

        } catch (Exception ex) {
            // report
        }

        if (!node.isLeaf) {
            for (int i = 0; i <= node.entrySize; i++)
                dumpAppleTree(node.nextNode[i], level + 1, writer);
        }
    }
}
