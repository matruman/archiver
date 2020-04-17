package com.archiver.pack;

import com.archiver.Main;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class ParallelCompressor implements Runnable {

    private SynchronizedIO synchronizedIO;
    private Overlap[] overlaps;
    private boolean[] busy;
    private byte[] buff;
    private ByteBuffer buffer;
    private int lengthIn;
    private int lengthOut;
    private int id;
    private int prev;

    public ParallelCompressor(SynchronizedIO synchronizedIO, int id, int prev) {

        this.id = id;
        this.prev = prev;
        this.synchronizedIO = synchronizedIO;
        this.buffer = ByteBuffer.allocate(Main.UNPACK_BUFF_SIZE);
        buff = new byte[Main.BUFF_SIZE];
        overlaps = new Overlap[Main.BUFF_SIZE];
        busy = new boolean[Main.BUFF_SIZE];
    }

    @Override
    public void run() {


        while ((lengthIn = synchronizedIO.read(buff, id, prev)) > 0) {

            getOverlaps();
            setNewPositions(overlaps);
            output();
            Arrays.fill(buff, (byte) 0);
            Arrays.fill(overlaps, null);
            Arrays.fill(busy, false);
            buffer.position(0);
        }
    }

    private void getOverlaps() {

        int i = 0;
        while (i < lengthIn - 2 * Main.MIN_OVERLAP)
        {
            if (busy[i]) {
                i = getEmpty(i);
                continue;
            }
            int j = i + 127;
            int length = 0;
            int tmp;
            while (j < lengthIn - Main.MIN_OVERLAP) {
                if (busy[j]) {
                    j = getEmpty(j);
                    continue;
                }
                if ((tmp = getOverlap(i, j)) >= Main.MIN_OVERLAP) {
                    if (tmp > length) {
                        if (length != 0)
                            overlaps[i].setLength(tmp);
                        length = tmp;
                    }
                    if (overlaps[i] == null)
                        overlaps[i] = new Overlap(i, length, -1, true);
                    setBusy(overlaps[j] = new Overlap(j, tmp, i, false));
                    j += tmp;
                    continue;
                }
                j++;
            }
            if (overlaps[i] != null) {
                setBusy(overlaps[i]);
                i += overlaps[i].getLength();
            }
            i++;
        }
    }

    private void output() {

        buffer.putInt(lengthOut);

        for(int i = 0; i < lengthIn; i++) {

            if (overlaps[i] != null) {
                if (overlaps[i].isOrigin()) {
                    buffer.put((byte)overlaps[i].getLength());
                    buffer.put(buff, overlaps[i].getPosition(), overlaps[i].getLength());
                    i += overlaps[i].getLength() - 1;
                }
                else {
                    buffer.put((byte)(-overlaps[i].getLength()));
                    buffer.putShort((short)overlaps[overlaps[i].getAddr()].getNewPosition());
                    i += overlaps[i].getLength() - 1;
                }
            }
            else {
                i += insertEmptyBytes(buffer, i) - 1;
            }
        }
        synchronizedIO.write(buffer, id, prev);
    }

    public int insertEmptyBytes(ByteBuffer buffer, int start) {

        int count;
        int tmp;

        count = 0;
        while (start + count < lengthIn && overlaps[start + count] == null)
            count++;

        int blocks = count % Main.MAX_BYTE > 0 ? count / Main.MAX_BYTE + 1 : count / Main.MAX_BYTE;

        while (blocks > 1)
        {
            buffer.put((byte)Main.MAX_BYTE);
            buffer.put(buff, start, Main.MAX_BYTE);
            start += Main.MAX_BYTE;
            blocks--;
        }
        if (blocks == 1)
        {
            tmp = count % Main.MAX_BYTE > 0 ? count % Main.MAX_BYTE : Main.MAX_BYTE;
            buffer.put((byte) (tmp));
            buffer.put(buff, start, tmp);
        }
        return count;
    }

    private void setNewPositions(Overlap[] overlaps) {

        int shift = 0;
        int tmp;

        for (int i = 0; i < lengthIn; i++) {
            if (overlaps[i] == null) {
                tmp = getEmptyLength(i, overlaps);
                i += tmp - 1;
                shift += tmp % Main.MAX_BYTE > 0 ? tmp / Main.MAX_BYTE + 1 : tmp / Main.MAX_BYTE;
                shift += tmp;
            }
            else if (overlaps[i] != null && !overlaps[i].isOrigin()) {
                shift += 1 + Main.LENGTH_ADDRESS;
                i =  overlaps[i].getEnd() - 1;
            }
            else if (overlaps[i] != null && overlaps[i].isOrigin()) {
                overlaps[i].setNewPosition(shift + 1);
                shift += 1 + (overlaps[i].getLength());
                i = overlaps[i].getEnd() - 1;
            }
            else {
                System.out.println("ERROR");
            }
        }
        lengthOut = shift;
    }

    private int getEmptyLength(int start, Overlap[] overlaps) {

        int i = 0;

        while (i + start < lengthIn && overlaps[i + start] == null) {
            ++i;
        }
        return i;
    }

    private void setBusy(Overlap overlap) {

        for (int i = overlap.getPosition(); i < overlap.getEnd(); ++i)
            busy[i] = true;
    }

    private int getEmpty(int start) {

        int i = start;
        while (i < lengthIn && busy[i])
            i++;
        return i;
    }

    private int getOverlap(int origin, int pos) {

        int i = 0;
        while (i < 127 && i + origin < lengthIn
                && i + pos < lengthIn && !busy[origin + i] && !busy[pos + i]
                && buff[origin + i] == buff[pos + i])
            i++;
        return i;
    }
}
