package com.example.calendar2;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.icu.text.SimpleDateFormat;
import android.media.Image;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.ToggleButton;

import com.google.android.material.snackbar.Snackbar;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map;

public class AddEventActivity extends AppCompatActivity implements View.OnClickListener{

    int PLACE_PICKER_ACTIVITY = 1245;

    private DBController dbController;
    private SQLiteDatabase sqLiteDatabase;

    private EditText eventNameText;
    private EditText locationText;
    private EditText phoneText;
    private EditText descriptionText;

    private ImageButton placePickerButton;

    private TextView startDateText;
    private TextView startTimeText;
    private ImageButton startDatePicker;
    private ImageButton startTimePicker;

    private TextView untilDateText;
    private TextView untilTimeText;

    private TextView reminderDateText;
    private TextView reminderTimeText;

    private Button customizeRecurringButton;

    private TextView endDateText;
    private TextView endTimeText;
    private ImageButton endDatePicker;
    private ImageButton endTimePicker;

    private LinearLayout recurringContainer;

    private RadioGroup recurringOptionsRadioGroup;
    private RadioButton dailyRadioButton;
    private RadioButton weeklyRadioButton;
    private RadioButton monthlyRadioButton;
    private RadioButton yearlyRadioButton;

    private CheckBox recurringEventCheckBox;

    private LinearLayout recurringDaysContainer;

    private Button reminderOptionsButton;

    private Button saveEventButton;

    String date = "";
    String startDate = "";
    String startTime = "";
    String endDate = "";
    String endTime = "";
    String untilDate = "";
    String untilTime = "";
    String reminderDate = "";
    String reminderTime = "";
    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
    SimpleDateFormat tf = new SimpleDateFormat("HH:mm");
    SimpleDateFormat dtf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    private Dialog customizePopup;

    String recurringDays;
    private String duration = "forever";
    int everyInt = 1;

    private boolean isEditMode;
    private int editID;
    private String[] sBeforeEdit;
    private String[] eBeforeEdit;

    private Map<Integer, Boolean> reminderOpts = new HashMap<>();
    private int foreverMax = 100;


    private String address;
    private Double latitude;
    private Double longtitude;

    private int alarmRequestCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_event);

        MakeUIReferences();
        customizePopup = new Dialog(this);

        try{
            dbController = new DBController(this, "CalendarDB" , null, 1);
            sqLiteDatabase = dbController.getWritableDatabase();
        }catch(Exception e){
            e.printStackTrace();
        }

        if(getIntent().hasExtra("ISEDIT")){
            isEditMode = true;
            if(getIntent().hasExtra("EVENTID")){

                editID = getIntent().getIntExtra("EVENTID",0);
                String query = "SELECT EventID, OriginEventID, Event, StartDateTime, EndDateTime, Location, Phone, Description FROM EventCalendar WHERE EventID = " + editID;
                Cursor cursor = sqLiteDatabase.rawQuery(query,null);
                cursor.moveToFirst();
                eventNameText.setText(cursor.getString(2));

                String sDT = cursor.getString(3);
                String eDT = cursor.getString(4);
                sBeforeEdit = sDT.split(" ");
                eBeforeEdit = eDT.split(" ");
                startDateText.setText(sBeforeEdit[0]);
                startTimeText.setText(sBeforeEdit[1]);
                endDateText.setText(eBeforeEdit[0]);
                endTimeText.setText(eBeforeEdit[1]);
                startDate = sBeforeEdit[0];
                startTime = sBeforeEdit[1];
                endDate = eBeforeEdit[0];
                endTime = eBeforeEdit[1];

                recurringContainer.setVisibility(LinearLayout.GONE);

                locationText.setText(cursor.getString(5));
                phoneText.setText(cursor.getString(6));
                descriptionText.setText(cursor.getString(7));
            }
        }

        if(getIntent().hasExtra("DATE")){
            date = getIntent().getStringExtra("DATE");
            startDateText.setText(date);
            startDate = date;
            startTime = "00:00";
            startTimeText.setText(startTime);

            endDateText.setText(date);
            endDate = date;
            endTime = "00:00";
            endTimeText.setText(endTime);

            untilTime = "00:00";
            untilDate = endDate;

        }
        reminderDate = startDate;
        reminderTime = startTime;

        recurringEventCheckBox.setChecked(false);
        ToggleRecurringOptions(false);
        customizeRecurringButton.setEnabled(false);

        startDatePicker.setOnClickListener(this);
        startTimePicker.setOnClickListener(this);
        endDatePicker.setOnClickListener(this);
        endTimePicker.setOnClickListener(this);

        recurringEventCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    ToggleRecurringOptions(true);
                    customizeRecurringButton.setEnabled(true);
                }
                else{
                    ToggleRecurringOptions(false);
                    customizeRecurringButton.setEnabled(false);
                }
            }
        });

        saveEventButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {
                    Date s = dtf.parse(startDate + " " + startTime);
                    Date e = dtf.parse(endDate + " " + endTime);
                    if(s.after(e)){
                        ShowAlert();
                        return;
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }


                if(isEditMode){
                    final Dialog updatePopup = new Dialog(AddEventActivity.this);
                    updatePopup.setContentView(R.layout.item_update_select_popup);
                    updatePopup.findViewById(R.id.updateOnlyThisEventButton).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            EditEvent(false);
                            updatePopup.hide();
                            Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                            intent.putExtra("SnackBarMessage",R.string.eventEditSuccess);
                            startActivity(intent);
                            finish();
                        }
                    });
                    updatePopup.findViewById(R.id.updateThisAndRecurringEventsButton).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            EditEvent(true);
                            updatePopup.hide();
                            Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                            intent.putExtra("SnackBarMessage",R.string.eventEditSuccess);
                            startActivity(intent);
                            finish();
                        }
                    });
                    updatePopup.show();
                }
                else{
                    SaveEvent();
                    Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                    intent.putExtra("SnackBarMessage",R.string.eventSaveSuccess);
                    startActivity(intent);
                    finish();
                }
            }
        });

        reminderOptionsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Dialog reminderPopup = new Dialog(AddEventActivity.this);
                reminderPopup.setContentView(R.layout.reminder_options_popup);

                reminderPopup.findViewById(R.id.remindDatePicker).setOnClickListener(AddEventActivity.this);
                reminderPopup.findViewById(R.id.remindTimePicker).setOnClickListener(AddEventActivity.this);
                reminderDateText = reminderPopup.findViewById(R.id.addEventRemindDateText);
                reminderTimeText = reminderPopup.findViewById(R.id.addEventRemindTimeText);
                reminderDateText.setText(startDate);
                reminderTimeText.setText(startTime);

                CheckBox customCB = reminderPopup.findViewById(R.id.customReminderDateTimeCheckBox);
                reminderPopup.findViewById(R.id.customReminderOptionDateTimeContainer).setVisibility(LinearLayout.GONE);
                customCB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if(isChecked){
                            reminderPopup.findViewById(R.id.customReminderOptionDateTimeContainer).setVisibility(LinearLayout.VISIBLE);
                        }else{
                            reminderPopup.findViewById(R.id.customReminderOptionDateTimeContainer).setVisibility(LinearLayout.GONE);
                        }
                    }
                });
                reminderPopup.findViewById(R.id.reminderOptionsSaveButton).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        LinearLayout cont = reminderPopup.findViewById(R.id.reminderOptionCheckBoxContainer);

                        for(int i = 0; i < cont.getChildCount(); i++){
                            if(((CheckBox)cont.getChildAt(i)).isChecked())
                                reminderOpts.put(cont.getChildAt(i).getId(), true);
                            else
                                reminderOpts.put(cont.getChildAt(i).getId(), false);
                        }
                        reminderPopup.hide();
                    }
                });

                reminderPopup.show();
            }
        });

        customizeRecurringButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                customizePopup.setContentView(R.layout.recurring_customize_popup);

                RadioButton foreverRadioButton = customizePopup.findViewById(R.id.foreverRadioButton);
                RadioButton untilRadioButton = customizePopup.findViewById(R.id.untilRadioButton);

                foreverRadioButton.setChecked(true);

                final LinearLayout untilDateTimeContainer = customizePopup.findViewById(R.id.untilDateLinearLayout);
                untilDateTimeContainer.setVisibility(LinearLayout.GONE);


                RadioGroup foreverUntilRadioGroup = customizePopup.findViewById(R.id.foreverUntilRadioGroup);
                foreverUntilRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(RadioGroup group, int checkedId) {
                        if(checkedId == R.id.foreverRadioButton){
                            untilDateTimeContainer.setVisibility(LinearLayout.GONE);

                        }else{
                            untilDateTimeContainer.setVisibility(LinearLayout.VISIBLE);
                        }
                    }
                });

                int recurID = recurringOptionsRadioGroup.getCheckedRadioButtonId();

                final EditText everyIntEditText = customizePopup.findViewById(R.id.everyIntText);
                TextView everyText = customizePopup.findViewById(R.id.everyDayWeekMonthYearText);
                if(recurID == R.id.dailyRadioButton) everyText.setText("Days");
                else if(recurID == R.id.weeklyRadioButton) everyText.setText("Weeks");
                else if(recurID == R.id.monthlyRadioButton) everyText.setText("Months");
                else if(recurID == R.id.yearlyRadioButton) everyText.setText("Years");

                recurringDaysContainer = customizePopup.findViewById(R.id.recurringDaysToggleContainer);
                ToggleButton mondayToggleButton = customizePopup.findViewById(R.id.mondayToggleButton);
                ToggleButton tuesdayToggleButton = customizePopup.findViewById(R.id.tuesdayToggleButton);
                ToggleButton wednesdayToggleButton = customizePopup.findViewById(R.id.wednesdayToggleButton);
                ToggleButton thursdayToggleButton = customizePopup.findViewById(R.id.thursdayToggleButton);
                ToggleButton fridayToggleButton = customizePopup.findViewById(R.id.fridayToggleButton);
                ToggleButton saturdayToggleButton = customizePopup.findViewById(R.id.saturdayToggleButton);
                ToggleButton sundayToggleButton = customizePopup.findViewById(R.id.sundayToggleButton);

                int dayOfWeek = 0;
                try{
                    Date parsedDate = df.parse(startDate);
                    Calendar c = Calendar.getInstance();
                    c.setTime(parsedDate);
                    dayOfWeek = c.get(Calendar.DAY_OF_WEEK);
                }
                catch(ParseException e){ e.printStackTrace();}

                if(weeklyRadioButton.isChecked()){
                    recurringDaysContainer.setVisibility(LinearLayout.VISIBLE);
                    switch(dayOfWeek){
                        case 1:
                            sundayToggleButton.setChecked(true);
                            sundayToggleButton.setEnabled(false);
                            break;
                        case 2:
                            mondayToggleButton.setChecked(true);
                            mondayToggleButton.setEnabled(false);
                            break;
                        case 3:
                            tuesdayToggleButton.setChecked(true);
                            tuesdayToggleButton.setEnabled(false);
                            break;
                        case 4:
                            wednesdayToggleButton.setChecked(true);
                            wednesdayToggleButton.setEnabled(false);
                            break;
                        case 5:
                            thursdayToggleButton.setChecked(true);
                            thursdayToggleButton.setEnabled(false);
                            break;
                        case 6:
                            fridayToggleButton.setChecked(true);
                            fridayToggleButton.setEnabled(false);
                            break;
                        case 7:
                            saturdayToggleButton.setChecked(true);
                            saturdayToggleButton.setEnabled(false);
                            break;
                    }
                }
                else{
                    recurringDaysContainer.setVisibility(LinearLayout.GONE);
                }

                untilDateText = customizePopup.findViewById(R.id.addEventUntilDateText);
                untilTimeText = customizePopup.findViewById(R.id.addEventUntilTimeText);
                ImageButton untilDatePicker = customizePopup.findViewById(R.id.untilDatePicker);
                ImageButton untilTimePicker = customizePopup.findViewById(R.id.untilTimePicker);

                untilDateText.setText(untilDate);
                untilTimeText.setText(untilTime);

                untilDatePicker.setOnClickListener(AddEventActivity.this);
                untilTimePicker.setOnClickListener(AddEventActivity.this);

                Button saveButton = customizePopup.findViewById(R.id.customizeRecurringSaveButton);
                saveButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        RadioGroup foreverUntilRadioGroup = customizePopup.findViewById(R.id.foreverUntilRadioGroup);
                        int radioID = foreverUntilRadioGroup.getCheckedRadioButtonId();
                        try{
                             everyInt = Integer.parseInt(everyIntEditText.getText().toString());
                        }catch(Exception e){e.printStackTrace();}

                        if(weeklyRadioButton.isChecked()){
                            recurringDays = identifyRecurringDays();
                        }

                        if(radioID == R.id.foreverRadioButton){
                            duration = "forever";
                        }else if(radioID == R.id.untilRadioButton){
                            duration = "until";
                        }
                        customizePopup.hide();
                    }
                });

                customizePopup.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                customizePopup.show();
            }
        });

        placePickerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AddEventActivity.this, PlacePickerActivity.class);
                intent.putExtra("Location", locationText.getText().toString());
                startActivityForResult(intent, PLACE_PICKER_ACTIVITY);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PLACE_PICKER_ACTIVITY) {
            if(resultCode == Activity.RESULT_OK){
                address = data.getStringExtra("address");
                latitude = data.getDoubleExtra("latitude",0);
                longtitude = data.getDoubleExtra("longtitude",0);
                locationText.setText(address + " Longtitude:" + longtitude + " Latitude:" + latitude);
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                //Write your code if there's no result
            }
        }
    }

    private void EditEvent(boolean updateRecurringEvents){
        ContentValues contentValues = new ContentValues();
        contentValues.put("Event", eventNameText.getText().toString());
        contentValues.put("Location", locationText.getText().toString());
        contentValues.put("Phone", phoneText.getText().toString());
        contentValues.put("Description", descriptionText.getText().toString());
        String reminderDateTimesString = MakeReminderDateTimesString();

        String[] reminderDateTimesArray = null;
        if(reminderDateTimesString != null){
            contentValues.put("ReminderDateTimes",reminderDateTimesString);
            reminderDateTimesArray = reminderDateTimesString.split(",");
        }

        SharedPreferences sharedPref = getSharedPreferences("MyPref",Context.MODE_PRIVATE);
        AlarmManager alarmManager = (AlarmManager)this.getSystemService(Context.ALARM_SERVICE);


        if(!updateRecurringEvents){

            contentValues.put("StartDateTime", startDate + " " + startTime);
            contentValues.put("EndDateTime", endDate + " " + endTime);


            if(reminderDateTimesString != null){
                for (int i = 0; i< reminderDateTimesArray.length;i++){
                    int rCode = sharedPref.getInt("alarmRequestCode" + editID + reminderDateTimesArray[i],0);
                    Intent intent = new Intent(this, AlarmReceiver.class);
                    PendingIntent alarmIntent = PendingIntent.getBroadcast(this, rCode,intent,PendingIntent.FLAG_UPDATE_CURRENT);
                    alarmManager.cancel(alarmIntent);
                }
                try {
                    Date curDate = dtf.parse(startDate + " " + startTime);
                    SetAlarm(reminderDateTimesString, curDate, editID);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }




            sqLiteDatabase.update("EventCalendar", contentValues, "EventID = ?",new String[] { String.valueOf(editID) });
        }else{
            try{
                Date beforeEditStartDate = dtf.parse(sBeforeEdit[0] + " " + sBeforeEdit[1]);
                Date beforeEditEndDate = dtf.parse(eBeforeEdit[0] + " " + eBeforeEdit[1]);
                Date afterEditStartDate = dtf.parse(startDate + " " + startTime);
                Date afterEditEndDate = dtf.parse(endDate + " " + endTime);

                long difS = afterEditStartDate.getTime() - beforeEditStartDate.getTime();
                long difE = afterEditEndDate.getTime() - beforeEditEndDate.getTime();

                String query = "SELECT OriginEventID FROM EventCalendar WHERE EventID = " + editID;
                Cursor cursor = sqLiteDatabase.rawQuery(query,null);
                cursor.moveToFirst();
                int originId = cursor.getInt(0);
                query = "SELECT EventID, StartDateTime, EndDateTime FROM EventCalendar WHERE OriginEventID = " + originId;
                cursor = sqLiteDatabase.rawQuery(query,null);
                cursor.moveToFirst();
                while(!cursor.isAfterLast()){
                    Calendar c = Calendar.getInstance();
                    Date startDT = dtf.parse(cursor.getString(1));
                    Date endDT = dtf.parse(cursor.getString(2));
                    c.setTimeInMillis(startDT.getTime() + difS);
                    Date curDate = c.getTime();
                    contentValues.put("StartDateTime", dtf.format(c.getTime()));
                    c.setTimeInMillis(endDT.getTime() + difE);
                    contentValues.put("EndDateTime", dtf.format(c.getTime()));

                    if(reminderDateTimesString != null){
                        for (int i = 0; i< reminderDateTimesArray.length;i++){
                            int rCode = sharedPref.getInt("alarmRequestCode" + cursor.getInt(0) + reminderDateTimesArray[i],0);
                            Intent intent = new Intent(this, AlarmReceiver.class);
                            PendingIntent alarmIntent = PendingIntent.getBroadcast(this, rCode,intent,PendingIntent.FLAG_UPDATE_CURRENT);
                            alarmManager.cancel(alarmIntent);
                        }
                        SetAlarm(reminderDateTimesString,curDate,cursor.getInt(0));
                    }

                    sqLiteDatabase.update("EventCalendar", contentValues, "EventID = ?",new String[] { String.valueOf(cursor.getInt(0))});


                    cursor.moveToNext();
                }
            }catch(ParseException e ){ e.printStackTrace(); }
        }

    }

    private void SaveEvent(){
        ContentValues contentValues = new ContentValues();
        contentValues.put("StartDateTime", startDate + " " + startTime);
        contentValues.put("EndDateTime", endDate + " " + endTime);
        contentValues.put("Event", eventNameText.getText().toString());
        contentValues.put("Location", locationText.getText().toString());
        contentValues.put("Phone", phoneText.getText().toString());
        contentValues.put("Description", descriptionText.getText().toString());
        contentValues.put("IsDone", 0);
        String reminderDateTimesString = MakeReminderDateTimesString();
        contentValues.put("ReminderDateTimes",reminderDateTimesString);
        Date curDate = null;



        sqLiteDatabase.insert("EventCalendar",null, contentValues);
        String query = "SELECT last_insert_rowid() FROM EventCalendar";
        Cursor cursor = sqLiteDatabase.rawQuery(query,null);
        cursor.moveToFirst();
        query = "UPDATE EventCalendar SET OriginEventID = " + cursor.getInt(0) + " WHERE EventID = " + cursor.getInt(0);
        sqLiteDatabase.execSQL(query);

        try {
            curDate = dtf.parse(startDate + " " + startTime);
            SetAlarm(reminderDateTimesString,curDate,cursor.getInt(0));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if(recurringEventCheckBox.isChecked()){
            SaveRecurringEvents();
        }


    }
    private String MakeReminderDateTimesString(){
        String reminderDateTimes = "";
        if(reminderOpts.size() > 0){
            for(Integer id : reminderOpts.keySet()){
                if(reminderOpts.get(id) == true){
                    reminderDateTimes += GetReminderIDString(id) + ",";
                }
            }
        }else{
            if(isEditMode){
                return null;
            }else{
                reminderDateTimes += "default";
            }
        }
        return reminderDateTimes;
    }

    private String GetReminderIDString(int id){
        switch (id){
            case R.id.before5MinutesCheckBox:
                return "5Min";
            case R.id.before15MinutesCheckBox:
                return "15Min";
            case R.id.before1HourCheckBox:
                return "1Hour";
            case R.id.before1DayCheckBox:
                return "1Day";
            case R.id.onStartTimeCheckBox:
                return "0Min";
            case R.id.customReminderDateTimeCheckBox:
                try{
                    Date s = dtf.parse(startDate + " " + startTime);
                    Date r = dtf.parse(reminderDate + " " + reminderTime);
                    long dif = s.getTime() - r.getTime();
                    return String.valueOf(dif);
                }catch(ParseException e){e.printStackTrace();}
            default:
                return "";
        }
    }

    private void SaveRecurringEvents(){
        int offset = 1;
        int recurringDayInd = 0;
        int dayOfWeek = 0;
        Date curDate = null;
        String query = "Select EventID From EventCalendar Where Event = '" + eventNameText.getText().toString() + "'";
        Cursor cursor = sqLiteDatabase.rawQuery(query, null);
        cursor.moveToFirst();

        Calendar c = Calendar.getInstance();
        c.setFirstDayOfWeek(Calendar.MONDAY);
        try{
            curDate = dtf.parse(startDate + " " + startTime);
            c.setTime(curDate);
            dayOfWeek = c.get(Calendar.DAY_OF_WEEK);
        }
        catch(ParseException e){e.printStackTrace();}

        ArrayList<Integer> recurringDaysInWeek = new ArrayList<>();
        if(dailyRadioButton.isChecked()){
            offset = Calendar.DAY_OF_MONTH;
        }
        else if(weeklyRadioButton.isChecked()){
            offset = Calendar.WEEK_OF_MONTH;

            for (int i=0; i<recurringDays.length();i++){
                if(i == dayOfWeek-2 || (dayOfWeek == 1 && i == 6)) continue;
                if(recurringDays.charAt(i) == '1'){
                    if(i == 6)
                        recurringDaysInWeek.add(1);
                    else
                        recurringDaysInWeek.add(i+2);
                }
            }
        }
        else if(monthlyRadioButton.isChecked()){
            offset = Calendar.MONTH;
        }
        else if(yearlyRadioButton.isChecked()){
            offset = Calendar.YEAR;
        }

        try {

            Date eDate = null;
            if(duration == "forever"){
                c.setTime(curDate);
                c.add(Calendar.YEAR,100);
                eDate = c.getTime();
            }
            else if(duration == "until"){
                eDate = dtf.parse(untilDate + " " + untilTime);
            }
            c.setTime(curDate);

            if(weeklyRadioButton.isChecked() && recurringDaysInWeek.size()>0 && dayOfWeek != 1){
                for(int i = 0; i < recurringDaysInWeek.size();i++){
                    if(recurringDaysInWeek.get(i) > dayOfWeek || (recurringDaysInWeek.get(i) == 1)){
                        recurringDayInd = i;
                        break;
                    }
                }
                c.set(Calendar.DAY_OF_WEEK,recurringDaysInWeek.get(recurringDayInd));
            }
            else{
                c.add(offset,everyInt);
            }
            curDate = c.getTime();

            Date s = dtf.parse(startDate + " " + startTime);
            Date e = dtf.parse(endDate + " " + endTime);
            long dif = e.getTime() - s.getTime();

            int maxCount = 0;
            recurringDayInd++;
            while(curDate.before(eDate) && maxCount < foreverMax){
                ContentValues contentValues = new ContentValues();
                Date cd = curDate;
                contentValues.put("StartDateTime", dtf.format(cd));
                Calendar ce = Calendar.getInstance();
                ce.setTimeInMillis(cd.getTime() + dif);
                cd = ce.getTime();
                contentValues.put("EndDateTime", dtf.format(cd));
                contentValues.put("Event", eventNameText.getText().toString());
                contentValues.put("Location", locationText.getText().toString());
                contentValues.put("Phone", phoneText.getText().toString());
                contentValues.put("Description", descriptionText.getText().toString());
                contentValues.put("IsDone", 0);
                contentValues.put("OriginEventID", cursor.getInt(0));
                String reminderDateTimesString = MakeReminderDateTimesString();
                contentValues.put("ReminderDateTimes", reminderDateTimesString);

                sqLiteDatabase.insert("EventCalendar",null, contentValues);

                String query2 = "SELECT last_insert_rowid() FROM EventCalendar";
                Cursor cursor2 = sqLiteDatabase.rawQuery(query2,null);
                cursor2.moveToFirst();
                SetAlarm(reminderDateTimesString,curDate,cursor2.getInt(0));

                if(weeklyRadioButton.isChecked() && recurringDaysInWeek.size()>0){
                    if(recurringDayInd >= recurringDaysInWeek.size()){
                        recurringDayInd = 0;
                        c.add(offset,everyInt);
                        c.set(Calendar.DAY_OF_WEEK,dayOfWeek);
                    }
                    else{
                        c.set(Calendar.DAY_OF_WEEK, recurringDaysInWeek.get(recurringDayInd));
                        recurringDayInd++;
                    }
                }
                else{
                    c.add(offset,everyInt);
                }
                curDate = c.getTime();
                maxCount++;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String identifyRecurringDays(){
        String recurringDays = "";
        for (int i = 0; i < recurringDaysContainer.getChildCount(); i++){
            ToggleButton t = (ToggleButton) recurringDaysContainer.getChildAt(i);
            if(t.isChecked()){
                recurringDays += "1";
            }
            else{
                recurringDays += "0";
            }
        }

        return recurringDays;
    }

    private void ToggleRecurringOptions(boolean isEnabled){
        for(int i = 0; i < recurringOptionsRadioGroup.getChildCount(); i++){
            recurringOptionsRadioGroup.getChildAt(i).setEnabled(isEnabled);
        }
    }

    private void MakeUIReferences() {
        eventNameText = findViewById(R.id.eventNameText);
        locationText = findViewById(R.id.locationText);
        phoneText = findViewById(R.id.phoneText);
        descriptionText = findViewById(R.id.descriptionText);

        startDateText = findViewById(R.id.addEventStartDateText);
        startTimeText = findViewById(R.id.addEventStartTimeText);
        startDatePicker = findViewById(R.id.startDatePicker);
        startTimePicker = findViewById(R.id.startTimePicker);

        customizeRecurringButton = findViewById(R.id.recurringCustomizeButton);

        endDateText = findViewById(R.id.addEventEndDateText);
        endTimeText = findViewById(R.id.addEventEndTimeText);
        endDatePicker = findViewById(R.id.endDatePicker);
        endTimePicker = findViewById(R.id.endTimePicker);

        recurringContainer = findViewById(R.id.addEventRecurringContainer);

        recurringOptionsRadioGroup = findViewById(R.id.recurringOptionsRadioGroup);
        dailyRadioButton = findViewById(R.id.dailyRadioButton);
        weeklyRadioButton = findViewById(R.id.weeklyRadioButton);
        monthlyRadioButton = findViewById(R.id.monthlyRadioButton);
        yearlyRadioButton =  findViewById(R.id.yearlyRadioButton);


        recurringEventCheckBox = findViewById(R.id.recurringEventCheckBox);

        reminderOptionsButton = findViewById(R.id.addEventReminderOptionsButton);

        saveEventButton = findViewById(R.id.saveEventButton);


        placePickerButton = findViewById(R.id.addEventPlacePickerButton);
    }

    private void ShowAlert(){
        AlertDialog.Builder builder = new AlertDialog.Builder(AddEventActivity.this);
        builder.setTitle("Invalid Date");
        builder.setMessage("End or Until date must be after start date");
        builder.setNeutralButton("Okay", null);
        builder.show();
    }


    int clickedPickerID = 0;

    @Override
    public void onClick(View v) {
        final Calendar c = Calendar.getInstance();
        int mYear = c.get(Calendar.YEAR);
        int mMonth = c.get(Calendar.MONTH);
        int mDay = c.get(Calendar.DAY_OF_MONTH);
        int mHour = c.get(Calendar.HOUR_OF_DAY);
        int mMinute = c.get(Calendar.MINUTE);

        DatePickerDialog datePickerDialog = new DatePickerDialog(AddEventActivity.this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year,
                                          int monthOfYear, int dayOfMonth) {
                        String d = "";
                        String month = "";
                        if(monthOfYear < 9)
                            month += "0";
                        month += (monthOfYear+1);
                        String day = "";
                        if(dayOfMonth < 10)
                            day += "0";
                        day += dayOfMonth;

                        d = year + "-" + month + "-" + day;
                        ChangeDate(d);

                    }
                }, mYear, mMonth, mDay);

        TimePickerDialog timePickerDialog = new TimePickerDialog(AddEventActivity.this,
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay,
                                          int minute) {
                        String t = "";
                        String hour = "";
                        if(hourOfDay < 10)
                            hour += "0";
                        hour += hourOfDay;
                        String sminute = "";
                        if(minute < 10)
                            sminute += "0";
                        sminute += minute;

                        t = hour + ":" + sminute;
                        ChangeTime(t);
                    }
                }, mHour, mMinute, true);


        switch(v.getId()){
            case R.id.startDatePicker:
                datePickerDialog.show();
                clickedPickerID = R.id.startDatePicker;
                break;
            case R.id.endDatePicker:
                datePickerDialog.show();
                clickedPickerID = R.id.endDatePicker;
                break;
            case R.id.untilDatePicker:
                datePickerDialog.show();
                clickedPickerID = R.id.untilDatePicker;
                break;
            case R.id.remindDatePicker:
                datePickerDialog.show();
                clickedPickerID = R.id.remindDatePicker;
                break;
            case R.id.startTimePicker:
                timePickerDialog.show();
                clickedPickerID = R.id.startTimePicker;
                break;
            case R.id.endTimePicker:
                timePickerDialog.show();
                clickedPickerID = R.id.endTimePicker;
                break;
            case R.id.untilTimePicker:
                timePickerDialog.show();
                clickedPickerID = R.id.untilTimePicker;
                break;
            case R.id.remindTimePicker:
                timePickerDialog.show();
                clickedPickerID = R.id.remindTimePicker;
                break;
        }
    }

    private void ChangeDate(String d){
        switch(clickedPickerID){
            case R.id.startDatePicker:
                try {
                    Date newStart = dtf.parse(d + " " + startTime);
                    Date end = dtf.parse(endDate + " " + endTime);
                    //if(newStart.after(end)){
                    //    ShowAlert();
                    //}
                    //else{
                        startDate = d;
                        startDateText.setText(startDate);
                    //}
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.endDatePicker:
                try {
                    Date start = dtf.parse(startDate + " " + startTime);
                    Date newEnd = dtf.parse(d + " " + endTime);
                    //if(start.after(newEnd)){
                    //    ShowAlert();
                    //}
                    //else{
                        endDate = d;
                        endDateText.setText(endDate);
                    //}
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.untilDatePicker:
                try {
                    Date start = dtf.parse(startDate + " " + startTime);
                    Date newUntil = dtf.parse(d + " " + untilTime);
                    if(start.after(newUntil)){
                        ShowAlert();
                    }
                    else{
                        untilDate = d;
                        untilDateText.setText(untilDate);
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.remindDatePicker:
                reminderDate = d;
                reminderDateText.setText(reminderDate);
                break;
        }
    }

    private void ChangeTime(String t){
        switch(clickedPickerID){
            case R.id.startTimePicker:
                try {
                    Date newStart = dtf.parse(startDate + " " + t);
                    Date end = dtf.parse(endDate + " " + endTime);
                    //if(newStart.after(end)){
                    //    ShowAlert();
                    //}
                    //else{
                        startTime = t;
                        startTimeText.setText(startTime);
                    //}
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.endTimePicker:
                try {
                    Date start = dtf.parse(startDate + " " + startTime);
                    Date newEnd = dtf.parse(endDate + " " + t);
                    //if(start.after(newEnd)){
                    //    ShowAlert();
                    //}
                    //else{
                        endTime = t;
                        endTimeText.setText(endTime);
                    //}
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.untilTimePicker:
                try {
                    Date start = dtf.parse(startDate + " " + startTime);
                    Date newUntil = dtf.parse(untilDate + " " + t);
                    if(start.after(newUntil)){
                        ShowAlert();
                    }
                    else{
                        untilTime = t;
                        untilTimeText.setText(untilTime);
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.remindTimePicker:
                reminderTime = t;
                reminderTimeText.setText(reminderTime);
                break;
        }
    }


    private ArrayList<PendingIntent> pendingIntents = new ArrayList<>();

    private void SetAlarm(String reminderString, Date curDate, int eventID){
        AlarmManager alarmMgr = (AlarmManager)getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        String[] reminderStringArray =  reminderString.split(",");

        SharedPreferences sharedPref = getSharedPreferences("MyPref", Context.MODE_PRIVATE);
        int rCode = sharedPref.getInt("alarmRequestCode", 0);

        for(int i=0; i< reminderStringArray.length; i++){
            long millisBefore = GetReminderOffsetFromString(reminderStringArray[i],curDate);
            Intent intent = new Intent(getApplicationContext(), AlarmReceiver.class);
            intent.putExtra("eventName", eventNameText.getText().toString());
            intent.putExtra("eventDescription", descriptionText.getText().toString());
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putInt("alarmRequestCode" + eventID + reminderStringArray[i], rCode);
            editor.commit();
            PendingIntent alarmIntent = PendingIntent.getBroadcast(getApplicationContext(), rCode++, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            Calendar c = Calendar.getInstance();
            long curMillis = curDate.getTime();
            c.setTimeInMillis(curMillis - millisBefore);

            alarmMgr.set(AlarmManager.RTC_WAKEUP,c.getTimeInMillis(),alarmIntent);

            pendingIntents.add(alarmIntent);
        }

        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt("alarmRequestCode", rCode);
        editor.commit();




    }

    private long GetReminderOffsetFromString(String reminder,Date curDate){
        int millisInMin = 60 * 1000;
        switch(reminder){
            case "5Min":
                return 5 * millisInMin;
            case "15Min":
                return 15 * millisInMin;
            case "1Hour":
                return 60 * millisInMin;
            case "1Day":
                return 60 * 24 * millisInMin;
            case "0Min":
                return 0;
            case "default":
                SharedPreferences sharedPreferences = getSharedPreferences("MyPref",Context.MODE_PRIVATE);
                String defaultReminder = sharedPreferences.getString("DefaultReminderOption","0Min");
                return GetReminderOffsetFromString(defaultReminder,curDate);
            default:
                return Long.parseLong(reminder);
        }
    }



}
