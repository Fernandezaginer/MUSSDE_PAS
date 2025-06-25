package com.example.pas_proyecto;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;


public class BT {


    public static boolean disconnected = false;
    public static boolean connected = false;

    static BluetoothAdapter bluetoothAdapter;

    static BluetoothSocket socket = null;
    static BluetoothDevice[] pairedDevices;


    @SuppressLint("MissingPermission")
    public static void connect_device(MainScreen activity){
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            Toast.makeText(activity, "Bluetooth no disponible o no activado", Toast.LENGTH_LONG).show();
            System.exit(-1);
        }

        Set<BluetoothDevice> dispositivos = bluetoothAdapter.getBondedDevices();
        if (dispositivos.size() == 0) {
            Toast.makeText(activity, "No hay dispositivos emparejados", Toast.LENGTH_LONG).show();
            System.exit(-1);
        }

        final String[] nombres = new String[dispositivos.size()];
        pairedDevices = new BluetoothDevice[dispositivos.size()];
        int i = 0;
        for (BluetoothDevice device : dispositivos) {
            nombres[i] = device.getName() + "\n" + device.getAddress();
            pairedDevices[i] = device;
            i++;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("Selecciona un dispositivo");
        builder.setItems(nombres, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                BluetoothDevice selectedDevice = pairedDevices[which];

                try {
                    bluetoothAdapter.cancelDiscovery();
                    socket = selectedDevice.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                start_testConnection(selectedDevice, activity);

            }
        });
        builder.create().show();
    }




    private static void start_testConnection(BluetoothDevice device, Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(activity, Manifest.permission.BLUETOOTH_CONNECT)
                    != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(activity, "Error BLUETOOTH_CONNECT", Toast.LENGTH_LONG).show();
                return;
            }
        }

        BluetoothSocket testSocket;
        try {
            UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
            testSocket = device.createRfcommSocketToServiceRecord(uuid);
            testSocket.connect();

            final OutputStream finalOutputStream = testSocket.getOutputStream();

            Handler handler = new Handler(Looper.getMainLooper());
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    try {
                        connected = true;
                        finalOutputStream.write("1".getBytes());
                        handler.postDelayed(this, 2000);
                    } catch (IOException e) {
                        e.printStackTrace();
                        disconnected = true;
                        Toast.makeText(activity, "Disconnected", Toast.LENGTH_SHORT).show();
                    }
                }
            };
            handler.post(runnable);

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(activity, "Connection failed", Toast.LENGTH_LONG).show();
        }
    }

}


