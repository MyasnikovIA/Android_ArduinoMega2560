package ru.miacomsoft.comportclass_002;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.widget.TextView;

import com.felhr.usbserial.UsbSerialDevice;
import com.felhr.usbserial.UsbSerialInterface;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
/*
http://independence-sys.net/main/?p=5421
https://github.com/mik3y/usb-serial-for-android
--------------------------------------------------------------------------------------------------
Manifest
    <application
               <activity
                ***
                ***
                ***
                    <!--  USB Serial (Arduino) -->
                    <intent-filter>
                        <action android:name="android.hardware.usb.action.USB_DEVICE_ATTACHED" />
                    </intent-filter>
                    <meta-data android:name="android.hardware.usb.action.USB_DEVICE_ATTACHED" android:resource="@xml/device_filter" />
                    <!--  ===================== -->
                ***
              </activity>
    </application>
    <!--  USB Serial (Arduino) -->
    <uses-feature android:name="android.hardware.usb.host" />
--------------------------------------------------------------------------------------------------
app\src\main\res\xml\device_filter.xml
    <resources>
        <usb-device vendor-id="9025" />
        <!-- Vendor ID для Arduino -->
    </resources>
--------------------------------------------------------------------------------------------------
   \app \ ****
   \libs \ usbserial.jar  -
 1) Создать папку
 2) скопировать библиотеку
 3) нажать ПКМ по usbserial.jar и установить как библиотеку ("Add As Library")
--------------------------------------------------------------------------------------------------
*/

/**
 *  Класс для обмена данными между ArduinoMega2560  и Android TV box
 *  тестировалось на H96max H96mini
 *  Работает с платами:  ESP8266, ArduinoMega2560, Arduino nano
 */
public class ArduinoMega2560 {
    private final String ACTION_USB_PERMISSION = "com.hariharan.arduinousb.USB_PERMISSION";
    private UsbManager usbManager;
    private UsbDevice device;
    private UsbSerialDevice serialPort = null;
    private UsbDeviceConnection connection;
    Context context;
    private int deviceId = -1;
    private int speed = 115200;

    public ArduinoMega2560(Context context, int deviceId) {
        this.context = context;
        this.deviceId = deviceId;
        usbManager = (UsbManager) context.getSystemService(context.USB_SERVICE);
    }

    public ArduinoMega2560(Context context) {
        this.context = context;
        usbManager = (UsbManager) context.getSystemService(context.USB_SERVICE);
    }


    public void onUpdateData(byte[] data) {
    }

    public void onUpdateString(String data) {
    }

    UsbSerialInterface.UsbReadCallback mCallback = new UsbSerialInterface.UsbReadCallback() {
        // Определение метода обратного вызова, который вызывается при приеме данных.
        StringBuffer sb = new StringBuffer();

        @Override
        public void onReceivedData(byte[] arg0) {
            onUpdateData(arg0);
            String data = null;
            try {
                data = new String(arg0, "UTF-8");
                if (data.indexOf("\n") != -1) {
                    onUpdateString(sb.toString());
                    sb.setLength(0);
                    return;
                }
                sb.append(data);
                // data.concat("/n");
                // textView.setText(data);
                // tvAppend(textView, data);
                // tvSet(textView, data);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
    };

    public String getList() {
        StringBuffer sb = new StringBuffer();
        HashMap<String, UsbDevice> usbDevices = usbManager.getDeviceList();
        if (!usbDevices.isEmpty()) {
            for (Map.Entry<String, UsbDevice> entry : usbDevices.entrySet()) {
                boolean keep = true;
                device = entry.getValue();
                int deviceVID = device.getVendorId();
                // if ((deviceVID != 6790) && (deviceVID != 4292)) continue;
                sb.append("\n =============== \n");
                sb.append("deviceVID ");
                sb.append(deviceVID);
                sb.append("\n getDeviceName : ");
                sb.append(device.getDeviceName());
                sb.append("\n getProductName : ");
                sb.append(device.getProductName());
                sb.append("\ngetSerialNumber : ");
                sb.append(device.getSerialNumber());
                sb.append("\ngetManufacturerName : ");
                sb.append(device.getManufacturerName());
                sb.append("\ngetDeviceProtocol : ");
                sb.append(device.getDeviceProtocol());
                sb.append("\ngetProductId : ");
                sb.append(device.getProductId());
                sb.append("\ngetVendorId : ");
                sb.append(device.getVendorId());
                sb.append("\ngetDeviceId : ");
                sb.append(device.getDeviceId());
                sb.append("\nhashCode : ");
                sb.append(device.hashCode());
                if (deviceVID == 6790) {
                    sb.append("- MEGA2560");
                }
                if (deviceVID == 4292) {
                    sb.append("- Arduino Nano");
                }
                sb.append("\n");
            }
        }
        return sb.toString();
    }

    public void connect() {
        HashMap<String, UsbDevice> usbDevices = usbManager.getDeviceList();
        int deviceVID = 0;
        if (!usbDevices.isEmpty()) {
            for (Map.Entry<String, UsbDevice> entry : usbDevices.entrySet()) {
                boolean keep = true;
                device = entry.getValue();
                deviceVID = device.getVendorId();
                // 3034 - ???
                // 6790 - Arduino Mega2560
                // 4292 - Arduino Arduino nano
                if ((deviceVID == 6790) || (deviceVID == 4292)) {   //Arduino Vendor ID
                    if ((deviceId > 0) && (deviceId != device.getDeviceId())) {
                        continue;
                    }
                    PendingIntent pi = PendingIntent.getBroadcast(context, 0, new Intent(ACTION_USB_PERMISSION), 0);
                    usbManager.requestPermission(device, pi);
                    keep = false;
                } else {
                    connection = null;
                    device = null;
                }
                if (!keep)
                    break;
            }
            connection = usbManager.openDevice(device);
            serialPort = UsbSerialDevice.createUsbSerialDevice(device, connection);
            try {
                serialPort.open();
                serialPort.setBaudRate(speed);
                serialPort.setDataBits(UsbSerialInterface.DATA_BITS_8);
                serialPort.setStopBits(UsbSerialInterface.STOP_BITS_1);
                serialPort.setParity(UsbSerialInterface.PARITY_NONE);
                serialPort.setFlowControl(UsbSerialInterface.FLOW_CONTROL_OFF);
                serialPort.read(mCallback);
            } catch (Exception e) {
                onUpdateData(("Error open device " + String.valueOf(deviceVID) + "\n" + e.getMessage()).getBytes());
            }
        }
    }

    public void send(String msg) {
        if (serialPort != null) {
            serialPort.write(msg.getBytes());
        }
    }

    public void stop() {
        if (serialPort != null) {
            serialPort.close();
        }
    }

    public void setDeviceId(int deviceId) {
        this.deviceId = deviceId;
    }

    public void setSpeed(int speed) {
        this.speed = deviceId;
    }

    private void tvSet(TextView tv, CharSequence text) {
        final TextView ftv = tv;
        final CharSequence ftext = text;
        //runOnUiThread(new Runnable() {
        //    @Override
        //    public void run() {
        //        ftv.setText(ftext);
        //    }
        //});
    }

}
