package ru.miacomsoft.comportclass_002;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.io.UnsupportedEncodingException;

public class MainActivity extends AppCompatActivity {
    private final String TAG = "MainActivity";
    ArduinoMega2560 meg;
    TextView textView;
    EditText editText;
    EditText editTextNum;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = (TextView) findViewById(R.id.textView);
        editText = (EditText) findViewById(R.id.editTextTextPersonName);
        editTextNum = (EditText) findViewById(R.id.editTextNumberSigned);
        meg = new ArduinoMega2560(this) {
            @Override
            public void onUpdateData(byte[] arg0) {
                try {
                    Log.e(TAG, new String(arg0, "UTF-8"));
                    tvSet(textView, new String(arg0, "UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onUpdateString(String data) {
                Log.e(TAG, data);
            }

        };
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnStart:
                meg.setDeviceId(Integer.parseInt(editTextNum.getText().toString()));
                meg.connect();
                break;
            case R.id.btnSend:
                meg.send(editText.getText().toString());
                break;
            case R.id.btnStop:
                meg.stop();
                break;
            case R.id.btnGetListUsb:
                tvSet(textView, meg.getList());
                break;
        }
    }

    public void onClickDesibleKeybord(View view) {
        // дописать механизм уборки виртуально клавиатуры
    }


    private void tvSet(TextView tv, CharSequence text) {
        final TextView ftv = tv;
        final CharSequence ftext = text;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ftv.setText(ftext);
            }
        });
    }


}