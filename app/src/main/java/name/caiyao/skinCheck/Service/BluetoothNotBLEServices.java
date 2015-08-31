package name.caiyao.skinCheck.Service;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import name.caiyao.skinCheck.Constants;

/**
 * Created by testhx on 15/8/30.
 */
public class BluetoothNotBLEServices {

    private static final String NAME_SECURE = "BluetoothSecure";
    private static final String NAME_INSECURE = "BluetoothInsecure";

    public static UUID MY_UUID_SECURE = UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");
    public static UUID MY_UUID_INSECURE = UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66");

    public static UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    public static final int STATE_NONE = 0;       // we're doing nothing
    public static final int STATE_LISTEN = 1;     // now listening for incoming connections
    public static final int STATE_CONNECTING = 2; // now initiating an outgoing connection
    public static final int STATE_CONNECTED = 3;  // now connected to a remote device


    private final BluetoothAdapter bluetoothAdapter;
    private final Handler handler;
    private int state;
    private AcceptThread secureAcceptThread;
    private ConnectThread connectThread;
    private ConnectedThread connectedThread;
    private AcceptThread inSecureAcceptThread;

    public BluetoothNotBLEServices(Context context,Handler handler){
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        state = STATE_NONE;
        this.handler = handler;
    }

    public synchronized void setState(int state){
        this.state = state;
        handler.obtainMessage(Constants.MESSAGE_STATE_CHANGE,-1).sendToTarget();
    }

    public synchronized int getState(){
        return state;
    }

    public synchronized void start(){

        if (connectThread != null){
            try {
                connectThread.cancel();
            } catch (IOException e) {
                e.printStackTrace();
            }
            connectThread = null;
        }

        if (connectedThread != null){
            connectedThread.cancel();
            connectedThread = null;
        }

        setState(STATE_LISTEN);

        if (secureAcceptThread == null){
            secureAcceptThread = new AcceptThread(true);
            secureAcceptThread.start();
        }
        if (inSecureAcceptThread == null){
            inSecureAcceptThread = new AcceptThread(false);
            inSecureAcceptThread.start();
        }
    }

    public synchronized void connect(BluetoothDevice device,boolean secure){

        if (state == STATE_CONNECTING){
            if (connectThread != null){
                try {
                    connectThread.cancel();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                connectThread = null;
            }
        }

        if (connectedThread != null){
            connectedThread.cancel();
            connectedThread = null;
        }

        connectThread = new ConnectThread(device,secure);
        connectThread.start();
        setState(STATE_CONNECTING);
    }

    public synchronized void connected(BluetoothSocket socket,BluetoothDevice device,final String socketType){

        if (connectThread != null){
            try {
                connectThread.cancel();
            } catch (IOException e) {
                e.printStackTrace();
            }
            connectThread = null;
        }

        if (connectedThread != null){
            connectedThread.cancel();
            connectedThread = null;
        }

        if (secureAcceptThread != null){
            secureAcceptThread.cancel();
            secureAcceptThread = null;
        }

        if (inSecureAcceptThread != null){
            inSecureAcceptThread.cancel();
            inSecureAcceptThread = null;
        }

        connectedThread = new ConnectedThread(socket,socketType);
        connectedThread.start();

        Message msg = handler.obtainMessage(Constants.MESSAGE_DEVICE_NAME);
        Bundle bundle = new Bundle();
        bundle.putString(Constants.DEVICE_NAME,device.getName());
        msg.setData(bundle);
        handler.sendMessage(msg);

        setState(STATE_CONNECTED);
    }

    public synchronized  void stop(){

        if (connectThread != null){
            try {
                connectThread.cancel();
            } catch (IOException e) {
                e.printStackTrace();
            }
            connectThread = null;
        }

        if (connectedThread != null){
            connectedThread.cancel();
            connectedThread = null;
        }

        if (secureAcceptThread != null){
            secureAcceptThread.cancel();
            secureAcceptThread = null;
        }

        if (inSecureAcceptThread != null){
            inSecureAcceptThread.cancel();
            inSecureAcceptThread = null;
        }

        setState(STATE_NONE);
    }

    public void write(byte[] out){
        ConnectedThread r;

        synchronized (this){
            if (state != STATE_CONNECTED) return;
            r = connectedThread;
        }
        r.write(out);
    }

    private void connectionFailed() {
        // Send a failure message back to the Activty
        Message msg = handler.obtainMessage(Constants.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(Constants.TOAST, "Unable to connect device");
        msg.setData(bundle);
        handler.sendMessage(msg);

        // Start the service over to restart listening mode
        BluetoothNotBLEServices.this.start();
    }

    /**
     * Indicate that the connection was lost and notify the UI Activity.
     */
    private void connectionLost() {
        // Send a failure message back to the Activity
        Message msg = handler.obtainMessage(Constants.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(Constants.TOAST, "Device connection was lost");
        msg.setData(bundle);
        handler.sendMessage(msg);

        // Start the service over to restart listening mode
        BluetoothNotBLEServices.this.start();
    }

    private class AcceptThread extends Thread {
        private final BluetoothServerSocket bluetoothServerSocket;
        private String socketType;

        public AcceptThread(boolean secure){
            BluetoothServerSocket tmp = null;
            socketType = secure ? "Secure" : "Insecure";

            try{
                if (secure){
                    tmp = bluetoothAdapter.listenUsingRfcommWithServiceRecord(NAME_SECURE, MY_UUID);
                }else{
                    tmp = bluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(NAME_INSECURE, MY_UUID);
                }
            }catch (Exception e){
                e.printStackTrace();
            }
            bluetoothServerSocket = tmp;
        }

        @Override
        public void run() {
            setName("AcceptThread"+socketType);

            BluetoothSocket socket = null;

            while(state != STATE_CONNECTED){
                try{
                    socket = bluetoothServerSocket.accept();
                }catch (IOException e){
                    e.printStackTrace();
                    break;
                }

                if (socket != null){
                    synchronized (BluetoothNotBLEServices.this){
                        switch (state){
                            case STATE_LISTEN:
                            case STATE_CONNECTING:
                                connected(socket,socket.getRemoteDevice(),socketType);
                                break;
                            case STATE_NONE:
                            case STATE_CONNECTED:
                                try {
                                    socket.close();
                                }catch (IOException e){
                                    e.printStackTrace();
                                }
                                break;
                        }
                    }
                }
            }
        }

        public void cancel(){
            try{
                bluetoothServerSocket.close();
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }


    public class ConnectThread extends Thread{
        private final BluetoothSocket socket;
        private final BluetoothDevice device;
        private String socketType;

        private ConnectThread(BluetoothDevice device,boolean secure){
            this.device = device;
            BluetoothSocket tmp = null;
            socketType = secure ? "Secure":"Insecure";

            try{
                if (secure){
                    tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
                }else {
                    tmp = device.createInsecureRfcommSocketToServiceRecord(MY_UUID);
                }
            }catch (IOException e){
                e.printStackTrace();
            }
            socket = tmp;
        }

        @Override
        public void run() {
            setName("ConnectThread" + socketType);

            bluetoothAdapter.cancelDiscovery();

            try{
                socket.connect();
            }catch (IOException e) {
                e.printStackTrace();
                connectionFailed();
                return;
            }

            synchronized (BluetoothNotBLEServices.this){
                connectThread = null;
            }
            connected(socket,device,socketType);
        }

        public void cancel() throws IOException {
            socket.close();
        }
    }

    public class ConnectedThread extends Thread{
        private final BluetoothSocket socket;
        private final InputStream inputStream;
        private final OutputStream outputStream;

        public ConnectedThread(BluetoothSocket socket,String socketType){
            this.socket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            }catch (IOException e){
                e.printStackTrace();
            }
            inputStream = tmpIn;
            outputStream = tmpOut;
        }

        @Override
        public void run() {
            byte[] buffer = new byte[1024];
            int bytes;

            while(!interrupted()){
                try {
                    bytes = inputStream.read(buffer);
                    Log.i("receive:",new String(buffer));
                    handler.obtainMessage(Constants.MESSAGE_READ,bytes,-1,buffer).sendToTarget();
                }catch (IOException e){
                    connectionLost();
                    BluetoothNotBLEServices.this.start();
                    break;
                }
            }
        }

        public void write(byte[] buffer){
            try{
                outputStream.write(buffer);
                Log.i("send:",new String(buffer));
                handler.obtainMessage(Constants.MESSAGE_WRITE,-1,-1,buffer).sendToTarget();
            }catch (IOException e){
                e.printStackTrace();
            }

        }

        public void cancel(){
            try {
                socket.close();
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }

}
