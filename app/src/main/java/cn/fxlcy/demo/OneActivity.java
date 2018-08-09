package cn.fxlcy.demo;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import cn.fxlcy.anno.LaunchMode;
import cn.fxlcy.anno.LaunchModeValues;

/**
 * Created by fxlcy on 18-8-8.
 */
@LaunchMode(LaunchModeValues.SINGLE_INSTANCE)
public class OneActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Toast.makeText(this, "hhhhhhhhhhh", Toast.LENGTH_SHORT).show();
    }
}
