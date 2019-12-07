package com.officialakbarali.fabiz;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.officialakbarali.fabiz.blockPages.AppVersion;

public class ServiceResumeCheck {
    public ServiceResumeCheck(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        boolean appVersionProblem = sharedPreferences.getBoolean("version", false);
        if (appVersionProblem) {
            Intent versionIntent = new Intent(context, AppVersion.class);
            versionIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(versionIntent);
        } else {

            String userName = sharedPreferences.getString("my_username", null);
            String password = sharedPreferences.getString("my_password", null);

            if (userName == null || password == null) {
                Intent loginIntent = new Intent(context, LogIn.class);
                loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(loginIntent);
            }
        }
    }
}
