package org.donntu.itt.dehax.bluetoothmanager;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
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
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static final int ENABLE_BLUETOOTH = 1;

    private BluetoothAdapter mBluetoothAdapter;

    private ArrayList<BluetoothDevice> mDeviceList = new ArrayList<>();

    private TextView mBluetoothName;
    private TextView mBluetoothStatus;
    private ListView mDeviceListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mBluetoothAdapter.isEnabled()) {
                    if (!mBluetoothAdapter.isDiscovering()) {
                        mDeviceList.clear();
                    }

                    mBluetoothAdapter.startDiscovery();

                    Snackbar.make(view, "Started discovering Bluetooth devices...", Snackbar.LENGTH_LONG)
                            .setAction("Action", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if (mBluetoothAdapter.isEnabled()) {
                                        mBluetoothAdapter.cancelDiscovery();
                                    }
                                }
                            }).show();
                }
            }
        });

        mBluetoothName = findViewById(R.id.bluetoothName);
        mBluetoothStatus = findViewById(R.id.bluetoothStatus);
        mDeviceListView = findViewById(R.id.devicesListView);

//        mDeviceListView.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, mDeviceList));

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        initBluetooth();
    }

    private void initBluetooth() {
        BroadcastReceiver bluetoothStateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String prevStateExtra = BluetoothAdapter.EXTRA_PREVIOUS_STATE;
                String stateExtra = BluetoothAdapter.EXTRA_STATE;
                int state = intent.getIntExtra(stateExtra, 1);
                int previousState = intent.getIntExtra(prevStateExtra, 1);

                String tt = "";

                switch (state) {
                    case BluetoothAdapter.STATE_TURNING_ON:
                        tt = "Bluetooth turning on";
                        break;
                    case BluetoothAdapter.STATE_ON:
                        tt = "Bluetooth on";
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        tt = "Bluetooth turning off";
                        break;
                    case BluetoothAdapter.STATE_OFF:
                        tt = "Bluetooth off";
                        break;
                    default:
                        break;
                }

                mBluetoothStatus.setText(tt);

                Log.d("DEHAX", tt);
            }
        };

        String actionStateChanged = BluetoothAdapter.ACTION_STATE_CHANGED;
        registerReceiver(bluetoothStateReceiver, new IntentFilter(actionStateChanged));

        BroadcastReceiver discoveryMonitor = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();

                assert action != null;
                switch (action) {
                    case BluetoothAdapter.ACTION_DISCOVERY_STARTED:
                        Log.d("DEHAX", "Discovery started...");
                        break;
                    case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
                        Log.d("DEHAX", "Discovery complete.");
                        break;
                    default:
                        break;
                }
            }
        };

        registerReceiver(discoveryMonitor, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED));
        registerReceiver(discoveryMonitor, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED));

        BroadcastReceiver discoveryResult = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String remoteDeviceName = intent.getStringExtra(BluetoothDevice.EXTRA_NAME);

                BluetoothDevice remoteDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                mDeviceList.add(remoteDevice);
                updateBluetoothDevicesList();

                Log.d("DEHAX", "Discovered " + remoteDeviceName);
            }
        };

        registerReceiver(discoveryResult, new IntentFilter(BluetoothDevice.ACTION_FOUND));

        if (!mBluetoothAdapter.isEnabled()) {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, ENABLE_BLUETOOTH);
        } else {
            initBluetoothUI();
        }
    }

    private void updateBluetoothDevicesList() {
        ArrayList<String> listData = new ArrayList<>(mDeviceList.size());

        for (BluetoothDevice device : mDeviceList) {
            listData.add(device.getName() + " (" + device.getAddress() + ")");
        }

        mDeviceListView.setAdapter(new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_list_item_1, listData));
    }

    private void initBluetoothUI() {
        if (mBluetoothAdapter.isEnabled()) {
            mBluetoothName.setText(mBluetoothAdapter.getName());
            mBluetoothStatus.setText("Bluetooth on");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ENABLE_BLUETOOTH) {
            if (resultCode == RESULT_OK) {
                initBluetoothUI();
            }
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
}
