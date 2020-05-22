package com.example.calendar2;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.app.Activity;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.RadioGroup;
import android.widget.Switch;

import java.io.OutputStream;

public class SettingsActivity extends AppCompatActivity {

    private static final int NOTIFICATION_SOUND_REQUEST = 1515;
    private String chosenRingtone;
    boolean darkModeSwitchOn = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Button notifButton = findViewById(R.id.selectDefaultNotificationSoundButton);
        final Button remindButton = findViewById(R.id.selectDefaultRemindTimeButton);
        Switch darkModeSwitch = findViewById(R.id.darkModeSwitch);
        Button saveButton = findViewById(R.id.settingsSaveButton);
        notifButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION);
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Select Tone");
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, (Uri) null);
                startActivityForResult(intent, NOTIFICATION_SOUND_REQUEST);
            }
        });

        remindButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Dialog remindPopup = new Dialog(SettingsActivity.this);
                remindPopup.setContentView(R.layout.reminder_default_options_popup);

                remindPopup.findViewById(R.id.reminderDefaultOptionsSaveButton).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        RadioGroup radioGroup = remindPopup.findViewById(R.id.reminderDefaultOptionsRadioGroup);
                        SharedPreferences sharedPreferences = getSharedPreferences("MyPref",Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        String defaultOption = "";
                        switch(radioGroup.getCheckedRadioButtonId()){
                            case R.id.reminderDefaultOnStartRadio:
                                defaultOption = "0Min";
                                break;
                            case R.id.reminderDefault5minutesRadio:
                                defaultOption = "5Min";
                                break;
                            case R.id.reminderDefault15minutesRadio:
                                defaultOption = "15Min";
                                break;
                            case R.id.reminderDefault1HourRadio:
                                defaultOption = "1Hour";
                                break;
                            case R.id.reminderDefault1DayRadio:
                                defaultOption = "1Day";
                                break;
                                default:
                                    defaultOption = "0Min";
                                    break;
                        }
                        editor.putString("DefaultReminderOption",defaultOption);
                        editor.commit();
                        remindPopup.dismiss();
                    }
                });
                remindPopup.show();
            }
        });

        SharedPreferences sharedPreferences = getSharedPreferences("MyPref",Context.MODE_PRIVATE);
        darkModeSwitchOn = sharedPreferences.getBoolean("DarkMode",false);
        darkModeSwitch.setChecked(darkModeSwitchOn);


        darkModeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                    darkModeSwitchOn = true;
                }else{
                    getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                    darkModeSwitchOn = false;
                }
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences sharedPreferences = getSharedPreferences("MyPref",Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("DarkMode",darkModeSwitchOn);
                editor.commit();
                Intent intent = new Intent(SettingsActivity.this,MainActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (resultCode == Activity.RESULT_OK && requestCode == NOTIFICATION_SOUND_REQUEST) {
            Uri uri = intent.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
            if (uri != null) {
                SetNotificationSound(uri);
            }
        }
    }

    private void SetNotificationSound(Uri uri){

        SharedPreferences sharedPreferences = getSharedPreferences("MyPref", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString("uri", uri.toString());
        editor.commit();

        /*ContentValues values = new ContentValues();
        values.put(MediaStore.MediaColumns.DATA, uri.getPath());
        values.put(MediaStore.MediaColumns.TITLE, "Default Sound");
        values.put(MediaStore.MediaColumns.MIME_TYPE, "audio/ogg");
        values.put(MediaStore.MediaColumns.SIZE, uri.getPath().length());
        values.put(MediaStore.Audio.Media.ARTIST, getString(R.string.app_name));
        values.put(MediaStore.Audio.Media.IS_RINGTONE, false);
        values.put(MediaStore.Audio.Media.IS_NOTIFICATION, true);
        values.put(MediaStore.Audio.Media.IS_ALARM, false);
        values.put(MediaStore.Audio.Media.IS_MUSIC, false);

        Uri uris = MediaStore.Audio.Media.getContentUriForPath(uri.getPath());
        //Uri newUri = getContentResolver().insert(uris, values);

        RingtoneManager.setActualDefaultRingtoneUri(
                this,
                RingtoneManager.TYPE_NOTIFICATION,
                uris
        );*/
    }

}
