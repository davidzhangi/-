package com.david.blesample.operation;

import android.annotation.TargetApi;
import android.app.Activity;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.david.blesample.LinkEvent;
import com.david.blesample.R;
import com.david.blueconnection.BleManager;
import com.david.blueconnection.callback.BleNotifyCallback;
import com.david.blueconnection.callback.BleWriteCallback;
import com.david.blueconnection.data.BleDevice;
import com.david.blueconnection.exception.BleException;
import com.david.blueconnection.utils.HexUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class ServiceListFragment extends Fragment {

    private TextView txt_name, txt_mac, temperature_tv, info_tv;
    private ResultAdapter mResultAdapter;
    private String mac;


    @Subscribe
    public void onEventMainThread(LinkEvent event) {
        if (mac.equals(event.address)) {
            info_tv.setText("开始重连...");

            setCharacteristicNotification();
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_service_list, null);
        initView(v);
        initData();
        return v;
    }

    private void initView(View v) {
        txt_name = v.findViewById(R.id.txt_name);
        txt_mac = v.findViewById(R.id.txt_mac);
        temperature_tv = v.findViewById(R.id.temperature_tv);
        info_tv = v.findViewById(R.id.info_tv);

        mResultAdapter = new ResultAdapter(getActivity());
        ListView listView_device = v.findViewById(R.id.list_service);
        listView_device.setAdapter(mResultAdapter);
        listView_device.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                BluetoothGattService service = mResultAdapter.getItem(position);
//                ((OperationActivity) getActivity()).setBluetoothGattService(service);
//                ((OperationActivity) getActivity()).changePage(1);
            }
        });
    }

    private BleDevice bleDevice;
    private BluetoothGattCharacteristic f1;
    private BluetoothGattCharacteristic f2;
    private BluetoothGattCharacteristic f3;
    private BluetoothGattCharacteristic f4;
    private BluetoothGattCharacteristic f5;

    private void initData() {
        bleDevice = ((OperationActivity) getActivity()).getBleDevice();
        String name = bleDevice.getName();
        mac = bleDevice.getMac();
        BluetoothGatt gatt = BleManager.getInstance().getBluetoothGatt(bleDevice);


        BluetoothGattService service1 = gatt.getService(UUID.fromString("0000fff0-0000-1000-8000-00805f9b34fb"));
        if (service1 == null) {
            return;
        }
        Log.d("david", "---------------------------------------------");


        f1 = service1.getCharacteristic(UUID.fromString("0000fff1-0000-1000-8000-00805f9b34fb"));
        f2 = service1.getCharacteristic(UUID.fromString("0000fff2-0000-1000-8000-00805f9b34fb"));
        f3 = service1.getCharacteristic(UUID.fromString("0000fff3-0000-1000-8000-00805f9b34fb"));
        f4 = service1.getCharacteristic(UUID.fromString("0000fff4-0000-1000-8000-00805f9b34fb"));
        f5 = service1.getCharacteristic(UUID.fromString("0000fff5-0000-1000-8000-00805f9b34fb"));
        setCharacteristicNotification();


        txt_name.setText(getActivity().getString(R.string.name) + name);
        txt_mac.setText(getActivity().getString(R.string.mac) + mac);

        mResultAdapter.clear();
    /*    for (BluetoothGattService service : gatt.getServices()) {
            mResultAdapter.addResult(service);
        }*/
        mResultAdapter.addResult(service1);
        mResultAdapter.notifyDataSetChanged();
    }

    private void setCharacteristicNotification() {
        BleManager.getInstance().notify(
                bleDevice,
                f1.getService().getUuid().toString(),
                f1.getUuid().toString(),
                new BleNotifyCallback() {

                    @Override
                    public void onNotifySuccess() {
                        ((Activity) getContext()).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Log.d("david", "f1 => notify success:" + f1.getUuid().toString());

                                info_tv.setText("f1 => notify success:" + f1.getUuid().toString());


                                TimerTask task = new TimerTask() {
                                    @Override
                                    public void run() {
                                        authentication(0);
                                    }
                                };
                                Timer timer = new Timer();
                                timer.schedule(task, 500);
                            }
                        });
                    }

                    @Override
                    public void onNotifyFailure(final BleException exception) {
                        ((Activity) getContext()).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Log.d("david", "f1 => notifyFailure:" + exception.toString());


                                info_tv.setText("f1 => notifyFailure:" + exception.toString());
                            }
                        });
                    }

                    @Override
                    public void onCharacteristicChanged(final byte[] data) {
                        ((Activity) getContext()).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Log.d("david", "f1 => onCharacteristicChanged:" + HexUtil.formatHexString(data));
                                Log.d("david", "0位:" + data[0]);
                                Log.d("david", "1位:" + data[1]);
                                Log.d("david", "2位:" + data[2]);
                                Log.d("david", "3位:" + data[3]);
                                Log.d("david", "4位:" + data[4]);

                                if (data[0] == 0x20) {
                                    TimerTask task = new TimerTask() {
                                        @Override
                                        public void run() {
                                            authentication(1);
                                        }
                                    };
                                    Timer timer = new Timer();
                                    timer.schedule(task, 500);

                                } else if (data[0] == 0x21 && data[1] == 0) {

                                    TimerTask task = new TimerTask() {
                                        @Override
                                        public void run() {
                                            getTemperature();
                                        }
                                    };
                                    Timer timer = new Timer();
                                    timer.schedule(task, 500);

                                } else {
                                    setCharacteristicNotification();
                                }
                            }
                        });
                    }
                });
    }


    private void authentication(int i) {
        byte[] pair0 = {32, 7, 6, 5, 4, 3, 2, 1, 1, 1, 1, 1, 0, 0, 0};
        byte[] pair1 = {33, 7, 6, 5, 4, 3, 2, 1, -72, 34, 0, 0, 0, 0, 0};
        byte[] pair;
        if (i == 0) {
            pair = pair0;
        } else {
            pair = pair1;
        }


        BleManager.getInstance().write(
                bleDevice,
                f2.getService().getUuid().toString(),
                f2.getUuid().toString(),
                pair,
                new BleWriteCallback() {

                    @Override
                    public void onWriteSuccess(final int current, final int total, final byte[] justWrite) {
                        ((Activity) getContext()).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Log.d("david", "0位:" + justWrite[0]);
                                Log.d("david", "1位:" + justWrite[1]);
                                Log.d("david", "2位:" + justWrite[2]);
                                Log.d("david", "3位:" + justWrite[3]);

                                Log.d("david", "8位:" + justWrite[8]);
                                Log.d("david", "9位:" + justWrite[9]);
                                Log.d("david", "10位:" + justWrite[10]);
                                Log.d("david", "11位:" + justWrite[11]);

                                Log.d("david", "write success::"
                                        +
                                        "write success, current: " + current
                                        + " total: " + total
                                        + " justWrite: " + HexUtil.formatHexString(justWrite, true));


                                info_tv.setText("write success::"
                                        +
                                        "write success, current: " + current
                                        + " total: " + total
                                        + " justWrite: " + HexUtil.formatHexString(justWrite, true));
                            }
                        });
                    }

                    @Override
                    public void onWriteFailure(final BleException exception) {
                        ((Activity) getContext()).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Log.d("david", "onWriteFailure::" + exception.toString());


                                info_tv.setText("onWriteFailure::" + exception.toString());
                            }
                        });
                    }
                });
    }


    private void setTemperature() {
        byte[] pair = {0, 0, 0, 0, 0};

        BleManager.getInstance().write(
                bleDevice,
                f5.getService().getUuid().toString(),
                f5.getUuid().toString(),
                pair,
                new BleWriteCallback() {

                    @Override
                    public void onWriteSuccess(final int current, final int total, final byte[] justWrite) {
                        ((Activity) getContext()).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Log.d("david", "f5 => write success::"
                                        +
                                        "write success, current: " + current
                                        + " total: " + total
                                        + " justWrite: " + HexUtil.formatHexString(justWrite, true));


                            }
                        });
                    }

                    @Override
                    public void onWriteFailure(final BleException exception) {
                        ((Activity) getContext()).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Log.d("david", "f5 => onWriteFailure::" + exception.toString());
                            }
                        });
                    }
                });
    }

    private void getTemperature() {
        BleManager.getInstance().notify(
                bleDevice,
                f4.getService().getUuid().toString(),
                f4.getUuid().toString(), new BleNotifyCallback() {

                    @Override
                    public void onNotifySuccess() {
                        ((Activity) getContext()).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Log.d("david", "f4 => notify success:" + f4.getUuid().toString());

                                info_tv.setText("f4 => notify success:" + f4.getUuid().toString());
                            }
                        });
                    }

                    @Override
                    public void onNotifyFailure(final BleException exception) {
                        ((Activity) getContext()).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Log.d("david", "f4 => notifyFailure:" + exception.toString());


                                info_tv.setText("f4 => notifyFailure:" + exception.toString());
                            }
                        });
                    }

                    @Override
                    public void onCharacteristicChanged(final byte[] data) {
                        ((Activity) getContext()).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Log.d("david", "f4 => onCharacteristicChanged:" + HexUtil.formatHexString(data));

                                byte[] data = f4.getValue();
                                for (int i = 0; i < data.length; i++) {
                                    Log.d("david", i + "位:" + data[i]);
                                }


                                short aShort = getShort(data, 4);
                                double v = (aShort / 10.0);
                                Log.d("david", "温度：" + v);

                                info_tv.setText("温度更新次数：" + conut++);

                                if (v <= 25) {
                                    temperature_tv.setText("当前温度低于：24℃");
                                } else {
                                    temperature_tv.setText("当前温度：" + v + "℃");
                                }
                            }
                        });
                    }
                }
        );
    }

    int conut = 0;


    public static short getShort(byte[] b, int index) {
        return (short) (((b[index + 1] << 8) | b[index + 0] & 0xff));
    }

    private int getTime(byte[] characteristicValue) {
        byte[] targets = new byte[4];
        for (int i = 0; i < 4; i++) {
            targets[i] = characteristicValue[i];
        }
        return bytesToInt(targets, 0);
    }

    public static int bytesToInt(byte[] src, int offset) {
        int value;
        value = (int) ((src[offset] & 0xFF) | ((src[offset + 1] & 0xFF) << 8)
                | ((src[offset + 2] & 0xFF) << 16) | ((src[offset + 3] & 0xFF) << 24));
        return value;
    }


    private class ResultAdapter extends BaseAdapter {

        private Context context;
        private List<BluetoothGattService> bluetoothGattServices;

        ResultAdapter(Context context) {
            this.context = context;
            bluetoothGattServices = new ArrayList<>();
        }

        void addResult(BluetoothGattService service) {
            bluetoothGattServices.add(service);
        }

        void clear() {
            bluetoothGattServices.clear();
        }

        @Override
        public int getCount() {
            return bluetoothGattServices.size();
        }

        @Override
        public BluetoothGattService getItem(int position) {
            if (position > bluetoothGattServices.size())
                return null;
            return bluetoothGattServices.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView != null) {
                holder = (ViewHolder) convertView.getTag();
            } else {
                convertView = View.inflate(context, R.layout.adapter_service, null);
                holder = new ViewHolder();
                convertView.setTag(holder);
                holder.txt_title = convertView.findViewById(R.id.txt_title);
                holder.txt_uuid = convertView.findViewById(R.id.txt_uuid);
                holder.txt_type = convertView.findViewById(R.id.txt_type);
            }

            BluetoothGattService service = bluetoothGattServices.get(position);
            String uuid = service.getUuid().toString();

            holder.txt_title.setText(String.valueOf(getActivity().getString(R.string.service) + "(" + position + ")"));
            holder.txt_uuid.setText(uuid);
            holder.txt_type.setText(getActivity().getString(R.string.type));
            return convertView;
        }

        class ViewHolder {
            TextView txt_title;
            TextView txt_uuid;
            TextView txt_type;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }
}
