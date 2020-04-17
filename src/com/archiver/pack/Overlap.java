package com.archiver.pack;

public class Overlap {

    private int position;
    private int length;
    private int end;
    private int addr;
    private boolean origin;
    private int newPosition;

    public Overlap(int position, int length, int addr, boolean origin)
    {
        this.position = position;
        this.length = length;
        this.end = position + length;
        this.addr = addr;
        this.origin = origin;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
        this.end = this.position + length;
    }

    public int getAddr() {
        return addr;
    }

    public int getPosition() {
        return position;
    }

    public int getEnd() {
        return end;
    }

    public int getNewPosition() {
        return newPosition;
    }

    public void setNewPosition(int newPosition) {
        this.newPosition = newPosition;
    }

    public boolean isOrigin() {
        return origin;
    }
}
