package name.caiyao.skinCheck.UI;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import name.caiyao.skinCheck.Constants;
import name.caiyao.skinCheck.R;
import name.caiyao.skinCheck.Service.BluetoothNotBLEServices;

public class BluetoothListActivity extends Activity {

    private ListView lv_bluetooth,lv_message;
    private EditText et_blue_send;
    private Button btn_blue_send;
    private MenuItem action_scan;

    private BluetoothAdapter bluetoothAdapter;
    private List<BluetoothDevice> deviceList;
    private ArrayAdapter<String> listAdapter;
    private BluetoothDevice device = null;
    private DeviceAdapter deviceAdapter;
    private static final int REQUEST_ENABLE_BT = 2;

    private BluetoothNotBLEServices bluetoothService = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_list);

        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
        if (bluetoothAdapter == null){
            Toast.makeText(this,"蓝牙异常错误",Toast.LENGTH_SHORT).show();
            finish();
        }
        deviceList = new ArrayList<>();
        deviceAdapter = new DeviceAdapter(this, deviceList);

        service_init();
        scanDevice();

        et_blue_send = (EditText) findViewById(R.id.et_blue_send);
        btn_blue_send = (Button) findViewById(R.id.btn_blue_send);
        lv_bluetooth = (ListView) findViewById(R.id.lv_bluetooth);
        lv_message = (ListView) findViewById(R.id.lv_message);
        listAdapter = new ArrayAdapter<>(this, R.layout.message_detail);
        lv_message.setAdapter(listAdapter);
        lv_message.setDivider(null);
        lv_bluetooth.setAdapter(deviceAdapter);
        btn_blue_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    String message = et_blue_send.getText().toString();
                    byte[] bytes;
                    try {
                        bytes = message.getBytes("UTF-8");
                        bluetoothService.write(bytes);
                        lv_message.smoothScrollToPosition(listAdapter.getCount() - 1);
                        et_blue_send.setText("");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
            }
        });
        lv_bluetooth.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                bluetoothAdapter.cancelDiscovery();
                device = deviceList.get(position);
                bluetoothService.connect(device,true);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // If it's already paired, skip it, because it's been listed already
                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    deviceList.add(device);
                    deviceAdapter.notifyDataSetChanged();
                }
                // When discovery is finished, change the Activity title
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {

            }else if(BluetoothDevice.ACTION_UUID.equals(action)){
                device.fetchUuidsWithSdp();
            }
        }
    };

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Constants.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BluetoothNotBLEServices.STATE_CONNECTED:
                            listAdapter.add("hasConnected");
                            break;
                        case BluetoothNotBLEServices.STATE_CONNECTING:
                            listAdapter.add("isConnecting");
                            break;
                        case BluetoothNotBLEServices.STATE_LISTEN:
                        case BluetoothNotBLEServices.STATE_NONE:
                            break;
                    }
                    break;
                case Constants.MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;
                    // construct a string from the buffer
                    String writeMessage = new String(writeBuf);
                    listAdapter.add("send:  " + writeMessage);
                    break;
                case Constants.MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    // construct a string from the valid bytes in the buffer
                    String readMessage = new String(readBuf);
                    listAdapter.add( "receive:  " + readMessage);
                    lv_message.smoothScrollToPosition(listAdapter.getCount());
                    break;
                case Constants.MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    String devices = msg.getData().getString(Constants.DEVICE_NAME);
                        Toast.makeText(BluetoothListActivity.this, "Connected to "
                                + devices, Toast.LENGTH_SHORT).show();
                    break;
                case Constants.MESSAGE_TOAST:
                        Toast.makeText(BluetoothListActivity.this, msg.getData().getString(Constants.TOAST),
                                Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };


    private void service_init() {
        bluetoothService = new BluetoothNotBLEServices(this,mHandler);
        // Register for broadcasts when a device is discovered
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        this.registerReceiver(mReceiver, filter);

        // Register for broadcasts when discovery has finished
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        this.registerReceiver(mReceiver, filter);

        filter = new IntentFilter(BluetoothDevice.ACTION_UUID);
        this.registerReceiver(mReceiver,filter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (bluetoothService != null) bluetoothService.stop();
        unregisterReceiver(mReceiver);
    }

    class DeviceAdapter extends BaseAdapter {
        Context context;
        List<BluetoothDevice> devices;
        LayoutInflater inflater;

        public DeviceAdapter(Context context, List<BluetoothDevice> devices) {
            this.context = context;
            inflater = LayoutInflater.from(context);
            this.devices = devices;
        }

        @Override
        public int getCount() {
            return devices.size();
        }

        @Override
        public Object getItem(int position) {
            return devices.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewGroup viewGroup;
            if (convertView != null){
                viewGroup = (ViewGroup) convertView;
            }else {
                viewGroup = (ViewGroup) inflater.inflate(R.layout.device_element,null);
            }
            BluetoothDevice device = devices.get(position);
            final TextView tvadd = ((TextView) viewGroup.findViewById(R.id.address));
            final TextView tvname = ((TextView) viewGroup.findViewById(R.id.name));
            final TextView tvpaired = (TextView) viewGroup.findViewById(R.id.paired);
            final TextView tvrssi = (TextView) viewGroup.findViewById(R.id.rssi);
            tvname.setText(device.getName());
            tvadd.setText(device.getAddress());
            if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
                tvname.setTextColor(Color.WHITE);
                tvadd.setTextColor(Color.WHITE);
                tvpaired.setTextColor(Color.GRAY);
                tvpaired.setVisibility(View.VISIBLE);
                tvpaired.setText("已配对");
                tvrssi.setVisibility(View.VISIBLE);
                tvrssi.setTextColor(Color.WHITE);

            } else {
                tvname.setTextColor(Color.WHITE);
                tvadd.setTextColor(Color.WHITE);
                tvpaired.setVisibility(View.GONE);
                tvrssi.setVisibility(View.VISIBLE);
                tvrssi.setTextColor(Color.WHITE);
            }
            return viewGroup;
        }
    }

    private void scanDevice() {
        // Indicate scanning in the title
        setProgressBarIndeterminateVisibility(true);

        // If we're already discovering, stop it
        if (bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
        }

        // Request discover from BluetoothAdapter
        bluetoothAdapter.startDiscovery();
        Set<BluetoothDevice> bluetoothDevices = bluetoothAdapter.getBondedDevices();
        if (bluetoothDevices != null && bluetoothDevices.size() != 0){
            deviceList.addAll(bluetoothDevices);
            deviceAdapter.notifyDataSetChanged();
        }
    }

    private void showMessage(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_bluetooth_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id){
            case R.id.action_scan:
                scanDevice();
                item.setTitle("Scaning");
                break;
            case R.id.action_settings:
                return true;
            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }
}
