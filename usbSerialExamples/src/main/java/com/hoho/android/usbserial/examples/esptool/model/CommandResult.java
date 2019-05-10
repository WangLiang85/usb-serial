package com.hoho.android.usbserial.examples.esptool.model;

public class CommandResult {
    public byte[] data;
    public int val;
    public boolean isInt = false;

    public CommandResult(int val, byte[] data) {
        this.val = val;
        this.data = data;
    }
}
