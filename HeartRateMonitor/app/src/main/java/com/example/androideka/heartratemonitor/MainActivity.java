package com.example.androideka.heartratemonitor;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
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

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private BluetoothAdapter bluetooth = BluetoothAdapter.getDefaultAdapter();
    private InputStream inputStream;

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                Log.d("NAME", "" + device.getName());
                Log.d("ADDRESS", "" + device.getAddress());
                Toast.makeText(context, "" + device.getName(), Toast.LENGTH_LONG).show();
                if (device.getName() != null && device.getName().equals("HXM022879")) {
                    try {
                        bluetooth.cancelDiscovery();
                        long msb = 10;
                        long lsb = 11;
                        UUID uuid = new UUID(msb, lsb);

                        Method method = device.getClass().getMethod("setPin", byte[].class);
                        byte[] pin = {(byte) 1234};
                        method.invoke(device, pin);
                        device.getClass().getMethod("setPairingConfirmation",
                                boolean.class).invoke(device, true);
                        device.getClass().getMethod("cancelPairingUserInput").invoke(device);
                        BluetoothSocket socket = device.createRfcommSocketToServiceRecord(uuid);
                        socket = (BluetoothSocket) device.getClass().getMethod("createRfcommSocket",
                                new Class[]{int.class}).invoke(device,1);
                        Log.d("BLUETOOTH", "Connecting to socket.....");
                        socket.connect();
                        Log.d("BLUETOOTH", "Socket connected");
                        monitorHeartRate(socket);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                    } catch (NoSuchMethodException e) {
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
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

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

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

    public void monitorHeartRate(BluetoothSocket socket) throws IOException {
        Log.d("SUCCESS", "Successfully opened a socket to the heart rate monitor.");
        inputStream = socket.getInputStream();
        while(true)
        {socket 
            System.out.println("" + inputStream.read());
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
