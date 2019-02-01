package com.mergilla.zildjian.bluetoothconnection;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class BluetoothController extends Thread{
    private final static String TAG="APP_DEBUG";
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothDevice device;
    private BluetoothSocket socket;
    private OutputStream outputStream;
    private InputStream inputStream;
    private byte _controllerData;
    private static UUID MY_UUID=UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static byte[] DEVICE_ADDRESS={0x20,0x16,0x12,0x21,0x42,0x56};
    private byte _motorState;
    private final byte VIBRATE_ON=0x69;
    private final byte VIBRATE_OFF=0x68;
    private boolean write=false;
    BluetoothController(){
        bluetoothAdapter=BluetoothAdapter.getDefaultAdapter();
        if(bluetoothAdapter==null){
            Log.d(TAG,"No Bluetooth Adapter");
            return;
        }
        _connect();
    }
    private void _connect(){
        device=null;
        socket=null;
        try{
            device=bluetoothAdapter.getRemoteDevice(DEVICE_ADDRESS);
            socket=device.createInsecureRfcommSocketToServiceRecord(MY_UUID);
        }
        catch(Exception e){
            Log.e(TAG,"Failed to get remote device/Socket creation failed",e);
            _connect();
        }
        try{
            socket.connect();
        }
        catch(Exception e){
            Log.e(TAG,"Failed to connecting to socket",e);
            _connect();
        }
        try{
            inputStream=socket.getInputStream();
            outputStream=socket.getOutputStream();
        }
        catch(Exception e){
            Log.e(TAG,"Failed creating Input/Output Stream.");
            _connect();
        }
    }
    public byte getControllerData(){
        return _controllerData;
    }
    @Override
    public void run(){
        while(true){
            try{
                int temp=(byte)inputStream.read();
                _controllerData=(byte)temp;
                if(write){
                    outputStream.write(_motorState);
                    write=false;
                }
            }
            catch(Exception e){
                Log.e(TAG,"Error reading from inputStream.",e);
                _connect();
            }
        }
    }
    public void vibrate(){
        if(_motorState!=VIBRATE_ON) write=true;
        _motorState = VIBRATE_ON;
    }
    public void stopVibration(){
        if(_motorState!=VIBRATE_OFF) write=true;
        _motorState = VIBRATE_OFF;
    }
    public void write(byte data){
        try{
            outputStream.write(data);
            Log.d("Write: "+TAG,"Data Written: "+data);
        }
        catch(Exception e){
            Log.e("Write: "+TAG,"Error writing", e);
        }
    }
}
