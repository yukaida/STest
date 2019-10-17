package com.example.stest;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;

import com.aill.androidserialport.SerialPort;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private Button button;
    private SerialPort serialPort;
    private InputStream inputStream;
    private Switch aSwitch_电热丝;
    private Switch aSwitch_风机;
    private Switch aSwitch_照明;
    private Switch aSwitch_转叉;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        /**
         * @param 1 串口路径
         * @param 2 波特率
         * @param 3 flags 给0就好
         */
        try {
            SerialPort.setSuPath("/system/xbin/su");
            serialPort = new SerialPort(new File("/dev/ttyS2"), 9600, 0);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            readFromSerialport();
        } catch (IOException e) {
            e.printStackTrace();
        }

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //从串口对象中获取输出流
                OutputStream outputStream = serialPort.getOutputStream();
                //需要写入的数据
                byte[] data_tosend = new byte[50];
                String HexCommand = "AA55080008";
                data_tosend = DataUtils.HexToByteArr(HexCommand);
                //写入数据
                try {
                    outputStream.write(data_tosend);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    outputStream.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                getButtonState();
            }
        });


        aSwitch_电热丝.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendToServer();
            }
        });aSwitch_转叉.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendToServer();
            }
        });aSwitch_照明.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendToServer();
            }
        });aSwitch_风机.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendToServer();
            }
        });
//          aSwitch_风机;
//        aSwitch_照明;
//        aSwitch_转叉;





    }

    //初始化界面
    private void initView() {
        button = findViewById(R.id.button);
        aSwitch_电热丝 = findViewById(R.id.switch_0);
        aSwitch_风机 = findViewById(R.id.switch_4);
        aSwitch_照明 = findViewById(R.id.switch_6);
        aSwitch_转叉 = findViewById(R.id.switch_7);
    }


    //读数据
    private void readFromSerialport() throws IOException {
        new Thread(new Runnable() {
            @Override
            public void run() {
                inputStream = serialPort.getInputStream();
                while (true) {
                    try {
                        if (inputStream.available() > 0) {
                            //当接收到数据时，sleep 500毫秒（sleep时间自己把握）
                            Thread.sleep(1000);
                            //sleep过后，再读取数据，基本上都是完整的数据
                            byte[] buffer = new byte[inputStream.available()];
                            int size = inputStream.read(buffer);
                            String HexString = DataUtils.ByteArrToHex(buffer);
                            Log.d(TAG, "run: get         " + HexString);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }



    //扫描按钮状态 返回字节数组指令
    private byte[] getButtonState(){

        String command = "AA55000100";
        int 电热丝=0x00;//0x80
        int 风机=0x00;//0x08
        int 照明=0x00;//0x02
        int 转叉=0x00;//0x01
        int result=0;
        if (aSwitch_电热丝.isChecked()) {
            电热丝 = 0x80;
        }
        if (aSwitch_风机.isChecked()) {
            风机 = 0x08;
        }
        if (aSwitch_照明.isChecked()) {
            照明 = 0x02;
        }
        if (aSwitch_转叉.isChecked()) {
            转叉=0x01;
        }
        result = 电热丝 | 风机 | 照明 | 转叉;

        byte b1 = (byte) result;//3号位数据

        byte[] data= DataUtils.HexToByteArr(command);
        for (int i = 0; i < data.length; i++) {
            Log.d(TAG, "getButtonState: get 原始指令"+data[i]);
        }


        data[2] = b1;

        int temp=0;
        for (int i = 2; i < data.length-1; i++) {
            temp += data[i];
        }
        data[4] = (byte)temp;


        Log.d(TAG, "getButtonState: get  控制位 "+b1);
        Log.d(TAG, "getButtonState: get   校验位"+data[4]);
        for (int i = 0; i < data.length; i++) {
            Log.d(TAG, "getButtonState: get  + "+data[i]);
        }
        return data;
    }
    //给服务器发送指令
    private void sendToServer(){
        OutputStream outputStream = serialPort.getOutputStream();
        byte[] data_tosend = getButtonState();
        try {
            outputStream.write(data_tosend);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
