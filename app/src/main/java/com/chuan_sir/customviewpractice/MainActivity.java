package com.chuan_sir.customviewpractice;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.chuan_sir.customviewpractice.widget.SlideLockView;

public class MainActivity extends AppCompatActivity {

    private SlideLockView slideLockView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        slideLockView=findViewById(R.id.slideView);
        slideLockView.setmOnLockListener(new SlideLockView.OnLockListener() {
            @Override
            public void onOpenLockSuccess() {
                Toast.makeText(MainActivity.this,"解锁成功~",Toast.LENGTH_SHORT).show();
            }
        });
    }
}
