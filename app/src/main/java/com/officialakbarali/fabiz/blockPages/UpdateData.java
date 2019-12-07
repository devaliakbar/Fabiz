package com.officialakbarali.fabiz.blockPages;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

import com.officialakbarali.fabiz.R;
import com.officialakbarali.fabiz.ServiceResumeCheck;

public class UpdateData extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_data);
    }

    @Override
    protected void onResume() {
        super.onResume();
        new ServiceResumeCheck(this);
    }
}
