package com.hoho.android.usbserial.examples.esptool;

public class ESPTool {
    // W code
    public static int ESP_READ_REG = 0x0a;
    public static int OP_NONE = -1;
    public static int ESP_SYNC = 0x08;
    public static int DEFAULT_TIMEOUT = 3000;              // timeout for most flash operations
    public static int MEM_DEFAULT_TIMEOUT = 5000;              // timeout for most flash operations
    public static int SYNC_TIMEOUT = 100;
    public static int MEM_END_ROM_TIMEOUT = 50;            // special short timeout for ESP_MEM_END, as it may never respond

    // Initial state for the checksum routine
    public static int ESP_CHECKSUM_MAGIC = 0xef;

    public static int MD5_TIMEOUT_PER_MB = 8;               // timeout (per megabyte) for calculating md5sum

}
