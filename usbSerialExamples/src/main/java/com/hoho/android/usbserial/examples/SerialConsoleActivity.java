/* Copyright 2011-2013 Google Inc.
 * Copyright 2013 mike wakerly <opensource@hoho.com>
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301,
 * USA.
 *
 * Project home page: https://github.com/mik3y/usb-serial-for-android
 */

package com.hoho.android.usbserial.examples;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.examples.esptool.ESPTool;
import com.hoho.android.usbserial.examples.esptool.model.CommandResult;
import com.hoho.android.usbserial.examples.esptool.model.FatalError;
import com.hoho.android.usbserial.examples.esptool.uti.ArrayUtil;
import com.hoho.android.usbserial.examples.esptool.uti.StuckUtil;
import com.hoho.android.usbserial.util.SerialInputOutputManager;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Monitors a single {@link UsbSerialPort} instance, showing all data
 * received.
 *
 * @author mike wakerly (opensource@hoho.com)
 */
public class SerialConsoleActivity extends Activity {

    private final String TAG = SerialConsoleActivity.class.getSimpleName();

    /**
     * Driver instance, passed in statically via
     * {@link #show(Context, UsbSerialPort)}.
     * <p>
     * <p/>
     * This is a devious hack; it'd be cleaner to re-create the driver using
     * arguments passed in with the {@link #startActivity(Intent)} intent. We
     * can get away with it because both activities will run in the same
     * process, and this is a simple demo.
     */
    private static UsbSerialPort sPort = null;

    private TextView mTitleTextView;
    private TextView mDumpTextView;
    private ScrollView mScrollView;


    private final ExecutorService mExecutor = Executors.newSingleThreadExecutor();

    private SerialInputOutputManager mSerialIoManager;

    private final SerialInputOutputManager.Listener mListener =
            new SerialInputOutputManager.Listener() {

                @Override
                public void onRunError(Exception e) {
                    Log.d(TAG, "Runner stopped.");
                }

                @Override
                public void onNewData(final byte[] data) {
                    SerialConsoleActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            SerialConsoleActivity.this.updateReceivedData(data);
                        }
                    });
                }
            };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.serial_console);
        mTitleTextView = findViewById(R.id.demoTitle);
        mDumpTextView = findViewById(R.id.consoleText);
        mScrollView = findViewById(R.id.demoScroller);


        findViewById(R.id.buttonClear).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                try {
                    mDumpTextView.setText("");
                } catch (Exception ex) {
                    Log.e(TAG, ex.getMessage());
                }
            }
        });
        findViewById(R.id.buttonSync).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                try {
                    if (sPort == null) {
                        Toast.makeText(SerialConsoleActivity.this, "Please connect usb", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    sync();
                } catch (Exception ex) {
                    Log.e(TAG, ex.getMessage());
                }
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopIoManager();
        if (sPort != null) {
            try {
                sPort.close();
            } catch (IOException e) {
                // Ignore.
            }
            sPort = null;
        }
        finish();
    }


    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "Resumed, port=" + sPort);
        if (sPort == null) {
            mTitleTextView.setText("No serial device.");
        } else {
            final UsbManager usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);

            UsbDeviceConnection connection = usbManager.openDevice(sPort.getDriver().getDevice());
            if (connection == null) {
                mTitleTextView.setText("Opening device failed");
                return;
            }

            try {
                sPort.open(connection);
                sPort.setParameters(74880, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);

            } catch (IOException e) {
                Log.e(TAG, "Error setting up device: " + e.getMessage(), e);
                mTitleTextView.setText("Error opening device: " + e.getMessage());
                try {
                    sPort.close();
                } catch (IOException e2) {
                    // Ignore.
                }
                sPort = null;
                return;
            }
            mTitleTextView.setText("Serial device: " + sPort.getClass().getSimpleName());
        }
        onDeviceStateChange();
    }

    private void stopIoManager() {
        if (mSerialIoManager != null) {
            Log.i(TAG, "Stopping io manager ..");
            mSerialIoManager.stop();
            mSerialIoManager = null;
        }
    }

    private void startIoManager() {
        if (sPort != null) {
            Log.i(TAG, "Starting io manager ..");
            mSerialIoManager = new SerialInputOutputManager(sPort, mListener);
            mExecutor.submit(mSerialIoManager);
        }
    }

    private void onDeviceStateChange() {
        stopIoManager();
        startIoManager();
    }

    // https://github.com/espressif/esptool/wiki/Serial-Protocol
    // 36 bytes: 0x07 0x07 0x12 0x20, followed by 32 x 0x55

    // https://github.com/espressif/esptool/blob/master/esptool.py#L392 (Python code url)
    // Converted to android from python
    public CommandResult sync() {

        byte[] syncByte = new byte[36];
        syncByte[0] = 0x07;
        syncByte[1] = 0x07;
        syncByte[2] = 0x12;
        syncByte[3] = 0x20;
        for (int i = 0; i < 32; i++) {
            syncByte[4 + i] = 0x55;
        }

        return this.command(ESPTool.ESP_SYNC, syncByte, 0, false, ESPTool.SYNC_TIMEOUT);

//        for (int i = 0; i < 7; i++)
//            this.command(ESPTool.OP_NONE, new byte[]{}, 0, true, ESPTool.DEFAULT_TIMEOUT);
    }

    public CommandResult command(int op, byte[] data, int chk, boolean wait_response, int timeout) {
        try {
            if (op != ESPTool.OP_NONE) {
                byte[] pkt = ArrayUtil.concat(StuckUtil.pack_BBHI((byte) 0x00, (byte) op, data.length, chk & 0xFF), data);
                write(pkt, timeout);
            }
        } finally {

        }

        throw new FatalError("Response doesn't match request");
    }

    public int write(byte[] packet, int timeout) {

        int writeSize = 0;
        if (sPort != null) {

            byte[] list1 = ArrayUtil.replace(packet, new byte[]{(byte) 0xdb}, new byte[]{(byte) 0xdb, (byte) 0xdd});

            byte[] list2 = ArrayUtil.replace(list1, new byte[]{(byte) 0xC0}, new byte[]{(byte) 0xdb, (byte) 0xdc});
            byte[] buf = ArrayUtil.concat(new byte[]{(byte) 0xc0}, list2);
            byte[] buf2 = ArrayUtil.concat(buf, new byte[]{(byte) 0xc0});


            try {
                sPort.write(buf2, timeout);
            } catch (Exception ex) {
                Log.e(TAG, ex.getMessage());
            }

            try {
                Thread.sleep(timeout);
            } catch (Exception ex) {
                Log.e(TAG, ex.getMessage());
            }
        }
        return writeSize;
    }

    private void updateReceivedData(byte[] data) {
        final String message = ArrayUtil.toUTF8(data) + "\n";
        mDumpTextView.append(message);
        mScrollView.smoothScrollTo(0, mDumpTextView.getBottom());

    }

    /**
     * Starts the activity, using the supplied driver instance.
     *
     * @param context
     * @param port
     */
    static void show(Context context, UsbSerialPort port) {
        sPort = port;
        final Intent intent = new Intent(context, SerialConsoleActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NO_HISTORY);
        context.startActivity(intent);

    }

}
