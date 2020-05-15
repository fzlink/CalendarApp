package com.example.calendar2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.icu.text.DateFormatSymbols;
import android.icu.text.SimpleDateFormat;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.LinearLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.DateFormat;
import java.text.ParseException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements EventAdapter.OnCalendarEventListener{

    private DBController dbController;
    private SQLiteDatabase sqLiteDatabase;

    private CalendarView calendarView;
    String selectedDate;

    private Button dailyButton;
    private Button weeklyButton;
    private Button monthlyButton;
    private Button yearlyButton;

    private FloatingActionButton addEventFloatingButton;
    private RecyclerView eventRecyclerView;

    String groupType = "daily";
    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
    SimpleDateFormat tf = new SimpleDateFormat("HH:mm");
    SimpleDateFormat dtf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    ZoneId defaultZoneId = ZoneId.systemDefault();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MakeUIReferences();

        dailyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                groupType = "daily";
                ReadDatabase();
            }
        });
        weeklyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                groupType = "weekly";
                ReadDatabase();
            }
        });
        monthlyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                groupType = "monthly";
                ReadDatabase();
            }
        });
        yearlyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                groupType = "yearly";
                ReadDatabase();
            }
        });

        try{
            dbController = new DBController(this, "CalendarDB" , null, 1);
            sqLiteDatabase = dbController.getWritableDatabase();
            sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS EventCalendar(EventID INTEGER PRIMARY KEY, StartDateTime TEXT, EndDateTime TEXT, ReminderDateTimes TEXT, Event TEXT, Location TEXT, Phone TEXT, Description TEXT, IsDone BOOLEAN, OriginEventID Integer)");
        }catch(Exception e){
            e.printStackTrace();
        }

        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
                String sMonth;
                String sDay;
                if(month+1 < 10)
                    sMonth = "0"+(month+1);
                else
                    sMonth = Integer.toString(month+1);
                if(dayOfMonth < 10)
                    sDay = "0"+dayOfMonth;
                else
                    sDay = Integer.toString(dayOfMonth);

                selectedDate = Integer.toString(year) + "-" + sMonth + "-" + sDay;
                ReadDatabase();
            }
        });

        addEventFloatingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,AddEventActivity.class);
                intent.putExtra("DATE", selectedDate);
                startActivity(intent);
            }
        });

        //sqLiteDatabase.execSQL("DROP TABLE IF EXISTS EventCalendar");
        //sqLiteDatabase.execSQL("delete from EventCalendar");

        Calendar calendar = Calendar.getInstance();

        Date d = calendar.getTime();
        selectedDate = df.format(d);
        ReadDatabase();

    }

    private void MakeUIReferences() {
        calendarView = findViewById(R.id.calendarView);

        dailyButton = findViewById(R.id.dailyButton);
        weeklyButton = findViewById(R.id.weeklyButton);
        monthlyButton = findViewById(R.id.monthlyButton);
        yearlyButton = findViewById(R.id.yearlyButton);

        addEventFloatingButton = findViewById(R.id.addEventFloatingButton);

        eventRecyclerView = findViewById(R.id.eventsRecyclerView);
    }

    public void ReadDatabase(){

        ArrayList<Event> eventList = new ArrayList<>();
        Calendar c = Calendar.getInstance();
        c.setFirstDayOfWeek(Calendar.MONDAY);
        String start = df.format(FindStartDate()) + " 00:00";
        String end = df.format(FindFinishDate()) + " 00:00";

        String query = "SELECT EventID, StartDateTime, EndDateTime, Event, IsDone, OriginEventID, Description, Location, Phone FROM EventCalendar WHERE StartDateTime BETWEEN '" + start + "' AND '" + end + "'";

        Cursor cursor = sqLiteDatabase.rawQuery(query,null);
        cursor.moveToFirst();

        while(!cursor.isAfterLast()){
            Event event = new Event();
            event.setEventID(cursor.getInt(0));
            event.setOriginEventID(cursor.getInt(5));
            event.setEventName(cursor.getString(3));
            event.setDone(cursor.getInt(4) > 0);

            event.setStartDateTime(cursor.getString(1));
            event.setEndDateTime(cursor.getString(2));
            event.setViewType(0);
            event.setDescription(cursor.getString(6));
            event.setLocation(cursor.getString(7));
            event.setPhone(cursor.getString(8));
            eventList.add(event);
            cursor.moveToNext();
        }


        if(groupType != "daily"){
            Collections.sort(eventList);
            try{
                eventList = AddHeaders(eventList, df.parse(start), df.parse(end));
            }catch(ParseException e){ e.printStackTrace(); }

        }


        EventAdapter eventAdapter = new EventAdapter(this,eventList,this);
        eventRecyclerView.setAdapter(eventAdapter);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        eventRecyclerView.setLayoutManager(linearLayoutManager);

    }

    private ArrayList<Event> AddHeaders(ArrayList<Event> eventList, Date startDate, Date finishDate){
        ArrayList<Event> eventsWithHeaders = new ArrayList<>();
        if(groupType == "weekly"){
            Calendar c = Calendar.getInstance();
            c.setTime(startDate);
            int j = 0;
            for (int i = 0; i< 7;i++){
                c.add(Calendar.DAY_OF_MONTH,i>0?1:0);
                Event event = new Event();
                event.setViewType(1);
                event.setEventName(df.format(c.getTime()));
                eventsWithHeaders.add(event);
                try{
                    while(j < eventList.size() && df.parse(eventList.get(j).getStartDateTime()).equals(c.getTime())){
                        eventsWithHeaders.add(eventList.get(j));
                        j++;
                    }
                }catch(ParseException e){ e.printStackTrace(); }

            }
        }
        else if(groupType == "monthly"){
            Calendar c = Calendar.getInstance();
            c.setTime(startDate);
            Date finishWeek;
            Date startWeek;
            int j = 0;
            for (int i = 0; i<4; i++){
                startWeek = c.getTime();
                c.add(Calendar.DAY_OF_MONTH,6);
                finishWeek = c.getTime();
                c.add(Calendar.DAY_OF_MONTH,1);
                Event event = new Event();
                event.setViewType(1);
                event.setEventName(df.format(startWeek) + " " + df.format(finishWeek));
                eventsWithHeaders.add(event);
                try{
                    while(j < eventList.size() && df.parse(eventList.get(j).getStartDateTime()).before(c.getTime())){
                        eventsWithHeaders.add(eventList.get(j));
                        j++;
                    }
                }catch(ParseException e){ e.printStackTrace(); }

            }
        }
        else if(groupType == "yearly"){
            Calendar c = Calendar.getInstance();
            c.setTime(startDate);
            Date finishMonth;
            Date startMonth;
            int j = 0;
            for (int i=0; i<12; i++){
                startMonth = c.getTime();
                Event event = new Event();
                event.setViewType(1);
                event.setEventName(new SimpleDateFormat("MMMM - yyyy").format(startMonth));
                eventsWithHeaders.add(event);
                c.add(Calendar.MONTH,1);
                finishMonth = c.getTime();
                try{
                    while(j < eventList.size() && df.parse(eventList.get(j).getStartDateTime()).before(finishMonth)){
                        eventsWithHeaders.add(eventList.get(j));
                        j++;
                    }
                }catch(ParseException e){ e.printStackTrace(); }

            }
        }

        return eventsWithHeaders;
    }

    private Date FindStartDate(){
        Date date;
        try{
            final DayOfWeek firstDayOfWeek = DayOfWeek.MONDAY;

            switch (groupType){
                case "daily":
                    date = df.parse(selectedDate);
                    return date;
                case "weekly":
                    LocalDate ld = LocalDate.parse(selectedDate,DateTimeFormatter.ofPattern("yyyy-MM-dd")).with(TemporalAdjusters.previousOrSame(firstDayOfWeek)); // first day
                    date = Date.from(ld.atStartOfDay(defaultZoneId).toInstant());
                    return date;
                case "monthly":
                    LocalDate firstDayofMonth = LocalDate.parse(selectedDate, DateTimeFormatter.ofPattern("yyyy-MM-dd")).with(TemporalAdjusters.firstDayOfMonth());
                    date = Date.from(firstDayofMonth.atStartOfDay(defaultZoneId).toInstant());
                    return date;
                case "yearly":
                    LocalDate firstDayofYear = LocalDate.parse(selectedDate, DateTimeFormatter.ofPattern("yyyy-MM-dd")).with(TemporalAdjusters.firstDayOfYear());
                    date = Date.from(firstDayofYear.atStartOfDay(defaultZoneId).toInstant());
                    return date;
                default:
                    return df.parse(selectedDate);
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }

    private Date FindFinishDate(){
        Date date;
        try{
            final DayOfWeek firstDayOfWeek = DayOfWeek.MONDAY;
            final DayOfWeek lastDayOfWeek = DayOfWeek.SUNDAY;

            switch (groupType){
                case "daily":
                    date = df.parse(selectedDate);
                    return date;
                case "weekly":
                    LocalDate ld = LocalDate.parse(selectedDate,DateTimeFormatter.ofPattern("yyyy-MM-dd")).with(TemporalAdjusters.nextOrSame(lastDayOfWeek));
                    date = Date.from(ld.atStartOfDay(defaultZoneId).toInstant());
                    return date;
                case "monthly":
                    LocalDate lastDayofMonth = LocalDate.parse(selectedDate, DateTimeFormatter.ofPattern("yyyy-MM-dd")).with(TemporalAdjusters.lastDayOfMonth());
                    date = Date.from(lastDayofMonth.atStartOfDay(defaultZoneId).toInstant());
                    return date;
                case "yearly":
                    LocalDate lastDayofYear = LocalDate.parse(selectedDate, DateTimeFormatter.ofPattern("yyyy-MM-dd")).with(TemporalAdjusters.lastDayOfYear());
                    date = Date.from(lastDayofYear.atStartOfDay(defaultZoneId).toInstant());
                    return date;
                default:
                    return df.parse(selectedDate);
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void OnCalendarEventClick(int position) {

    }
}
