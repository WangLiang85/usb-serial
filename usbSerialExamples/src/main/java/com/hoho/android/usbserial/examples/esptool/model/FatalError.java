package com.hoho.android.usbserial.examples.esptool.model;

public  class FatalError extends RuntimeException {
    public FatalError(String msg) {
        super(msg);
    }
}