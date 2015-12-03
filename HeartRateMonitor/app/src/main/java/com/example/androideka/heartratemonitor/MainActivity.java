package com.example.androideka.heartratemonitor;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.UUID;

import zephyr.android.HxMBT.BTClient;
import zephyr.android.HxMBT.BTComms;
import zephyr.android.HxMBT.ConnectedEvent;
import zephyr.android.HxMBT.ConnectedListener;
import zephyr.android.HxMBT.ZephyrPacket;
import zephyr.android.HxMBT.ZephyrPacketArgs;
import zephyr.android.HxMBT.ZephyrProtocol;

public class MainActivity extends AppCompatActivity {

    private BluetoothAdapter bluetooth = BluetoothAdapter.getDefaultAdapter();
    private InputStream inputStream;
    private BluetoothSocket socket;
    private boolean connected = false;

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                Log.d("NAME", "" + device.getName());
                Log.d("ADDRESS", "" + device.getAddress());
                Toast.makeText(context, "" + device.getName(), Toast.LENGTH_LONG).show();
                if (device.getName() != null && device.getName().equals("HXM021780")) {
                    try {
                        bluetooth.cancelDiscovery();
                        byte[] pin = {1,2,3,4};
                        device.setPairingConfirmation(true);
                        device.setPin(pin);
                        device.getClass().getMethod("cancelPairingUserInput").invoke(device);
                        // Set port to 1, default is -1
                        socket = (BluetoothSocket) device.getClass()
                                .getMethod("createRfcommSocket", new Class[]{int.class})
                                .invoke(device,1);
                        Log.d("BLUETOOTH", "Connecting to socket.....");
                        socket.connect();
                        Log.d("BLUETOOTH", "Socket connected");
                        monitorHeartRate();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (bluetooth != null && !bluetooth.isEnabled()) {
            Intent enable = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enable, 42);
        }

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(receiver, filter);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 42) {
            Log.d("BLUETOOTH", "Bluetooth Enabled.");
        } else {
            Log.e("BLUETOOTH", "Bluetooth not enabled.");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void connect(View view) {
        if (bluetooth.startDiscovery()) {
            Log.d("BLUETOOTH", "Scanning for devices...");
        }
    }

    public void monitor(View view) throws Exception {
        monitorHeartRate();
    }

    public void monitorHeartRate() throws Exception {
        Log.d("SUCCESS", "Successfully opened a socket to the heart rate monitor.");
        inputStream = socket.getInputStream();
        ZephyrPacket packet = new ZephyrPacket();
        byte[] buffer = new byte[60];
        while(true)
        {
            packet.Serialize(buffer);
            ZephyrPacketArgs args = packet.Parse(buffer);
            Log.d("Bytes received: ", "" + args.getNumRvcdBytes());
            Log.d("Message ID: ", "" + args.getMsgID());
            Log.d("Status: ", "" + args.getStatus());
            Log.d("", "" + args.getBytes());
        }
    }

    @Override
    public void onResume()
    {
        super.onResume();
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(receiver, filter);
    }

    @Override
    public void onPause()
    {
        super.onPause();
        unregisterReceiver(receiver);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
