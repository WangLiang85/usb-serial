package com.hoho.android.usbserial.examples.esptool.uti;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class ArrayUtil {
    public static byte[] concat(byte[] a, byte[] b) {
        if (a == null) {
            a = new byte[]{};
        }
        byte[] r = new byte[a.length + b.length];
        System.arraycopy(a, 0, r, 0, a.length);
        System.arraycopy(b, 0, r, a.length, b.length);
        return r;
    }

    public static byte[] copy(byte[] a) {
        byte[] r = new byte[a.length];
        System.arraycopy(a, 0, r, 0, a.length);
        return r;
    }

    public static byte[] subArray(byte[] a, int firstLength) {
        firstLength = Math.min(firstLength, a.length);
        byte[] r = new byte[firstLength];
        System.arraycopy(a, 0, r, 0, firstLength);
        return r;
    }

    public static byte[] subArray(byte[] a, int firstIndex, int toIndex) {
        toIndex = Math.min(toIndex, a.length);

        byte[] r = new byte[toIndex - firstIndex];
        if (toIndex - firstIndex >= 0)
            System.arraycopy(a, firstIndex, r, 0, toIndex - firstIndex);
        return r;
    }

    public static byte[] subFromArray(byte[] a, int fromIndex) {
        fromIndex = Math.max(0, fromIndex);
        if (fromIndex >= a.length) return new byte[]{};

        byte[] r = new byte[a.length - fromIndex];
        System.arraycopy(a, fromIndex, r, 0, a.length - fromIndex);
        return r;
    }

    public static String fromByte(byte[] a) {
        StringBuilder dis = new StringBuilder();
        for (byte anA : a) {
            dis.append(String.format("%02X", anA)).append(",");
        }
        return dis.toString();
    }

    public static String toUTF8(byte[] a) {
        try {
            String s = new String(a, "UTF-8");
            return s + "\n" + fromByte(a);
        } catch (Exception ex) {
            Log.e("ArrayUtil_TAG", ex.getMessage());
        }

        return fromByte(a);
    }

    public static String fromByteN(byte[] a, int len) {
        String dis = "";
        for (int i = 0; i < len; i++) {
            dis = dis + (a[i] + ",");
        }
        return dis;
    }

    public static boolean compare(byte[] a, byte[] b) {
        if (b.length < a.length) return false;

        for (int i = 0; i < a.length; i++) {
            if (a[i] != b[i]) return false;
        }

        return true;
    }

    public static String bytesToString(byte[] bytes) {
        StringBuilder dis = new StringBuilder();
        for (byte anA : bytes) {
            dis.append(String.format("%02X", anA));
        }
        return dis.toString();
    }

    public static byte[] stringToBytes(String str) {
        List<Byte> bytes = new ArrayList<>();
        for (int i = 0; i < str.length() / 2; i++) {
            String sub = str.substring(i * 2, i * 2 + 2);
            bytes.add((byte) Integer.parseInt(sub, 16));
        }
        byte[] result = new byte[bytes.size()];
        for (int i = 0; i < bytes.size(); i++) {
            result[i] = bytes.get(i);
        }
        return result;
    }

    public static byte[] replaceAll(byte[] array, byte[] rep, byte[] with) {
        String str_array = bytesToString(array);
        String str_rep = bytesToString(rep);
        String str_with = bytesToString(with);
        Log.d("debug", "str_array " + str_array);
        String str_result = str_array.replace(str_rep, str_with);
        Log.d("debug", "str_result " + str_result);
        byte[] result = stringToBytes(str_result);
        return result;
    }

    public static byte[] replace(byte[] a, byte[] rep, byte[] with) {

        List<Byte> bytes = new ArrayList<>();
        for (int i = 0; i < a.length; i++) {
            byte[] sub = ArrayUtil.subArray(a, i, i + rep.length);
            if (compare(sub, rep)) {
                for (int j = 0; j < with.length; j++) {
                    bytes.add(with[j]);
                }
                i = i + rep.length - 1;
            } else {
                bytes.add(a[i]);
            }
        }

        byte[] result = new byte[bytes.size()];
        for (int i = 0; i < bytes.size(); i++) {
            result[i] = bytes.get(i);
        }


        return result;
    }

    public static String convertToMac(byte[] array) {
        StringBuilder mac = new StringBuilder();
        for (int i = 0; i < array.length; i++) {
            mac.append(String.format("%02X", array[i]));
        }
        return mac.toString();
    }

    public static byte[] pad_to(byte[] data, int alignment, byte pad_character) {
        // "" " Pad to the next alignment boundary " ""
        int pad_mod = data.length % alignment;
        if (pad_mod != 0) {
//            data += pad_character * (alignment - pad_mod)
            data = ArrayUtil.concat(data, new byte[]{(byte) (pad_character * alignment - pad_mod)});
        }
        return data;
    }
}
