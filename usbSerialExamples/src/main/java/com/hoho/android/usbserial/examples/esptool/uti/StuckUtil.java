package com.hoho.android.usbserial.examples.esptool.uti;

import com.hoho.android.usbserial.examples.esptool.model.UnPackedData;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class StuckUtil {
    public static byte[] pack_I(int x) {
        ByteBuffer bb = ByteBuffer.allocate(4);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        bb.putInt(x);
        return bb.array();
    }

    public static byte[] pack_BB(int x, int y) {
        ByteBuffer bb = ByteBuffer.allocate(2);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        bb.put((byte) x);
        bb.put((byte) y);
        return bb.array();
    }


    public static byte[] pack_BBHI(byte x, byte y, int z, int chk) {
        ByteBuffer bb = ByteBuffer.allocate(8);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        bb.put(x);
        bb.put(y);
        bb.putShort((short) z);
        bb.putInt(chk);
        return bb.array();
    }

    public static byte[] pack_IIII(int x, int y, int z, int chk) {
        ByteBuffer bb = ByteBuffer.allocate(16);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        bb.putInt(x);
        bb.putInt(y);
        bb.putInt(z);
        bb.putInt(chk);
        return bb.array();
    }

    public static byte[] pack_II(int x, int y) {
        ByteBuffer bb = ByteBuffer.allocate(8);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        bb.putInt(x);
        bb.putInt(y);
        return bb.array();
    }

    public static UnPackedData unpack_BBHI(byte[] data) {
        UnPackedData result = new UnPackedData();
        ByteBuffer bb = ByteBuffer.wrap(data);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        result.resp = bb.get();
        result.op_ret = bb.get();
        result.len_ret = bb.getShort();
        result.val = bb.getInt();
        return result;
    }

    public static class BBBB {
        public byte magic;
        public byte _Unused;
        public byte flash_mode;
        public byte flash_size_freq;

    }

    public static BBBB unpack_BBBB(byte[] data) {
        BBBB result = new BBBB();
        ByteBuffer bb = ByteBuffer.wrap(data);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        result.magic = bb.get();
        result._Unused = bb.get();
        result.flash_mode = bb.get();
        result.flash_size_freq = bb.get();
        return result;
    }

    public static UnPackedData unpack_I(byte[] data) {
        UnPackedData result = new UnPackedData();
        ByteBuffer bb = ByteBuffer.wrap(data);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        result.resp = bb.get();
        result.op_ret = bb.get();
        result.len_ret = bb.getShort();
        result.val = bb.getInt();
        return result;
    }
}
