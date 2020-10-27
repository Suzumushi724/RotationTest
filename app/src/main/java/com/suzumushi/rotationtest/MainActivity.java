package com.suzumushi.rotationtest;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.graphics.Color;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;
import android.widget.Toast;

import com.hoho.android.usbserial.driver.CdcAcmSerialDriver;
import com.hoho.android.usbserial.driver.ProbeTable;
import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;

import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //ProbeTableで追加するデバイスの登録
        //mbedは使用対象外なのでここでベンダーIDとプロダクトIDを登録する必要がある
        ProbeTable customTable = new ProbeTable();
        customTable.addProduct(0x0d28, 0x0204, CdcAcmSerialDriver.class);
        UsbSerialProber prober = new UsbSerialProber(customTable);
        // Find all available drivers from attached devices.
        UsbManager manager = (UsbManager) getSystemService(Context.USB_SERVICE);
        List<UsbSerialDriver> availableDrivers = prober.findAllDrivers(manager);
        if (availableDrivers.isEmpty()) {
            Toast.makeText(MainActivity.this,"デバイスが見つかりません",Toast.LENGTH_LONG).show();
            return;
        }

        // Open a connection to the first available driver.
        UsbSerialDriver driver = availableDrivers.get(0);
        UsbDeviceConnection connection = manager.openDevice(driver.getDevice());
        if (connection == null) {
            // add UsbManager.requestPermission(driver.getDevice(), ..) handling here
            Toast.makeText(MainActivity.this,"デバイスに接続できません",Toast.LENGTH_LONG).show();
            return;
        }

        UsbSerialPort port = driver.getPorts().get(0); // Most devices have just one port (port 0)

        try {
            port.open(connection);
            port.setParameters(9600, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);
            TextView ConnectCheck = findViewById(R.id.connection);
            ConnectCheck.setText("connected");
            ConnectCheck.setTextColor(Color.GREEN);
            ReadData(port);

        }catch (Exception e){
            Toast.makeText(MainActivity.this,"接続を確立できません",Toast.LENGTH_LONG).show();
        }
    }

    public void  ReadData(final UsbSerialPort port){
        new Thread(new Runnable() {
            @Override
            public void run() { while(true){
                final byte[] RotBuff = new byte[10];
                try {
                    int RotNum = port.read(RotBuff,500);
                    if(RotNum > 0){
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    TextView RotView = findViewById(R.id.RotView);
                                    RotView.setText((new String(RotBuff)).substring(0,3));
                                }
                            });
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }
}