package com.mergilla.zildjian.bluetoothconnection;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import static android.support.v4.app.ActivityCompat.startActivityForResult;
import static android.support.v4.content.ContextCompat.startActivity;

public class BluetoothService {

    private static final String TAG = "MY_APP_DEBUG_TAG";
    private Handler mHandler; // handler that gets info from BluetoothService service

    // Defines several constants used when transmitting messages between the
    // service and the UI.
    private interface MessageConstants {
        public static final int MESSAGE_READ = 0;
        public static final int MESSAGE_WRITE = 1;
        public static final int MESSAGE_TOAST = 2;

        // ... (Add other message types here as needed.)
    }
    private BluetoothDevice device=null;
    private final static BluetoothAdapter bluetoothAdapter=BluetoothAdapter.getDefaultAdapter();
    private BluetoothSocket socket=null;
    public ConnectedThread serviceThread=null;
    UUID uuid=UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    public void startThread(){
        Log.d(BluetoothService.TAG,"Starting Bluetooth Service Thread");
        Log.d(TAG,"Checkpoint: Query default bluetooth adapter : "+bluetoothAdapter.getName());
        if(bluetoothAdapter==null){
            Log.d(TAG,"No Bluetooth Adapter");
        }
        else{
            Log.d(TAG,"Bluetooth Adapter Address: "+bluetoothAdapter.getAddress());
            for (BluetoothDevice d: bluetoothAdapter.getBondedDevices()
                 ) {
                Log.d(TAG,"Bluetooth Device Name: "+d.getName());
                Log.d(TAG,"Bluetooth Device Address: "+d.getAddress());
            }
        }
        byte[] address={0x20,0x16,0x12,0x21,0x42,0x56};
        device=bluetoothAdapter.getRemoteDevice(address);
        try {
            socket=device.createInsecureRfcommSocketToServiceRecord(uuid);
            bluetoothAdapter.getDefaultAdapter().cancelDiscovery();

            socket.connect();
            Log.d(TAG,"Successfully Connected");
        } catch (IOException e) {
            Log.e(BluetoothService.TAG,"Error connecting socket",e);
        }
        if(socket!=null){
            serviceThread=new ConnectedThread(socket);
            serviceThread.start();
            Log.d(BluetoothService.TAG,"Connected Thread Started");
        }
        else Log.d(BluetoothService.TAG,"Unable to initialize ConnectedThread socket is null");
    }
    public void write(byte[] bytes){
        serviceThread.write(bytes);
    }
    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        private byte[] mmBuffer; // mmBuffer store for the stream

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            // Get the input and output streams; using temp objects because
            // member streams are final.
            try {
                tmpIn = socket.getInputStream();
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when creating input stream", e);
            }
            try {
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when creating output stream", e);
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            Log.d(BluetoothService.TAG,"Thread Started");
            //mmBuffer = new byte[1024];
            int numBytes; // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs.
            while (true) {
                try {
                    // Read from the InputStream.
                    String out="";
                    char c;
                    while((c=(char)mmInStream.read())!=(char)10){
                        out+=c;
                    }
                    //numBytes = mmInStream.read(mmBuffer);
                    // Send the obtained bytes to the UI activity.
                    //Log.d(TAG,"Bytes Received : "+numBytes);
                    Log.d(BluetoothService.TAG,new String(out));
                } catch (IOException e) {
                    Log.e(TAG, "Input stream was disconnected", e);
                    break;
                }
            }
        }
        // Call this from the main activity to send data to the remote device.
        public void write(byte[] bytes) {
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when sending data", e);

                // Send a failure message back to the activity.
                Message writeErrorMsg =
                        mHandler.obtainMessage(MessageConstants.MESSAGE_TOAST);
                Bundle bundle = new Bundle();
                bundle.putString("toast",
                        "Couldn't send data to the other device");
                writeErrorMsg.setData(bundle);
                mHandler.sendMessage(writeErrorMsg);
            }
        }

        // Call this method from the main activity to shut down the connection.
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the connect socket", e);
            }
        }
    }
}
