package com.jontera;

import io.netty.buffer.ByteBuf;

public class Packet {

    /* 128 byte */
    /*
     * 0-3 Sequence number 4 command length 5-127 command buffer
     */
    public final static int GENERALMESSAGELENGTH = 128;
    public final static int BUFFERLENGTH = 120;
    public final int sequenceNumber;
    public final short hostId;
    public final byte commandType;
    public final byte commandLength;
    public final byte[] byteBuf = new byte[BUFFERLENGTH];

    public Packet(int sequenceNumber, short hostId) {
        this.sequenceNumber = sequenceNumber;
        this.hostId = hostId;
        this.commandType = -1;
        this.commandLength = 0;
    }

    public Packet(int sequenceNumber, short hostId, byte commandType,
            byte commandLength) {
        this.sequenceNumber = sequenceNumber;
        this.hostId = hostId;
        this.commandType = commandType;
        this.commandLength = commandLength;

    }

    public Packet(int sequenceNumber, short hostId, byte commandType,
            byte commandLength, byte[] b) {
        this.sequenceNumber = sequenceNumber;
        this.hostId = hostId;
        this.commandType = commandType;
        this.commandLength = commandLength;
        if (b.length > BUFFERLENGTH)
            return;
        for (int i = 0; i < b.length; i++) {
            this.byteBuf[i] = b[i];
        }

    }

    public Packet(int sequenceNumber, short hostId, byte commandType,
            String string) {
        this.sequenceNumber = sequenceNumber;
        this.hostId = hostId;
        this.commandType = commandType;
        if (string.length() > BUFFERLENGTH) {
            this.commandLength = (byte) -1;
            return;
        } else {
            this.commandLength = (byte) string.length();
            try {
                byte[] b = string.getBytes("US-ASCII");
                for (int i = 0; i < this.commandLength; i++) {
                    this.byteBuf[i] = b[i];
                }
            } catch (Exception e) {

            }
        }
    }

    public Packet(ByteBuf b) {
        this.sequenceNumber = b.readInt();
        this.hostId = b.readShort();
        this.commandType = b.readByte();
        this.commandLength = b.readByte();

        for (int i = 0; i < this.commandLength; i++)
            this.byteBuf[i] = b.readByte();

    }

    int getSequenceNumber() {
        return sequenceNumber;
    }

    int getRemainSize() {
        return commandLength;
    }

    void stringToByte(String string, byte[] b) {
        try {
            b = string.getBytes("US-ASCII");

        } catch (Exception e) {

        }
    }

    String byteToString() {
        try {
            String string = new String(this.byteBuf, "US-ASCII");
            return string.substring(0, this.commandLength);
        } catch (Exception e) {
            return "Failed at Packet.byteToString";
        }

    }

    void messageToByte(ByteBuf out) {
        out.writeInt(this.sequenceNumber);
        out.writeShort(this.hostId);
        out.writeByte(this.commandType);
        out.writeByte(this.commandLength);
        for (int i = 0; i < BUFFERLENGTH; i++) {
            out.writeByte(this.byteBuf[i]);
        }

    }

    void print() {
        System.out.print("IN packet : " + this.sequenceNumber + " - "
                + this.commandLength);
        for (int i = 0; i < this.commandLength; i++)
            System.out.print(" ;" + this.byteBuf[i]);
        System.out.println();

    }
}
