package com.example.calendar2;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.icu.text.SimpleDateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.TextView;


import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class EventAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    ArrayList<Event> eventList;
    LayoutInflater inflater;
    OnCalendarEventListener onCalendarEventListener;
    Context context;
    SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd");

    private DBController dbController;
    private SQLiteDatabase sqLiteDatabase;

    public EventAdapter(Context context, ArrayList<Event> eventList, OnCalendarEventListener onCalendarEventListener){
        inflater = LayoutInflater.from(context);
        this.eventList = eventList;
        this.onCalendarEventListener = onCalendarEventListener;
        this.context = context;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        if(viewType == 0){
            View view = inflater.inflate(R.layout.item_event_card, parent, false);
            EventViewHolder holder = new EventViewHolder(view, onCalendarEventListener);
            return holder;
        }
        else if(viewType == 1){
            View view = inflater.inflate(R.layout.item_group_header, parent, false);
            GroupViewHolder holder = new GroupViewHolder(view);
            return holder;
        }
        View view = inflater.inflate(R.layout.item_event_card, parent, false);
        EventViewHolder holder = new EventViewHolder(view, onCalendarEventListener);
        return holder;

    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final Event selectedEvent = eventList.get(position);
        if(holder instanceof GroupViewHolder){
            ((GroupViewHolder) holder).setData(selectedEvent.getEventName());
        }
        else if(holder instanceof EventViewHolder){
            ((EventViewHolder) holder).setData(selectedEvent, position);
            ((EventViewHolder) holder).isDoneCheckBox.setChecked(selectedEvent.isDone());
            ((EventViewHolder) holder).isDoneCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    try{
                        dbController = new DBController(context, "CalendarDB" , null, 1);
                        sqLiteDatabase = dbController.getWritableDatabase();
                    }catch(Exception e){
                        e.printStackTrace();
                    }

                    int checkInt;
                    if(isChecked)
                        checkInt = 1;
                    else
                        checkInt = 0;

                    String query = "UPDATE EventCalendar SET IsDone = " + checkInt + " WHERE EventID = " + selectedEvent.getEventID();
                    sqLiteDatabase.execSQL(query);
                }
            });
            ((EventViewHolder) holder).editButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent editEventIntent = new Intent(context, AddEventActivity.class);
                    editEventIntent.putExtra("ISEDIT", true);
                    editEventIntent.putExtra("EVENTID", selectedEvent.getEventID());
                    context.startActivity(editEventIntent);
                }
            });
            ((EventViewHolder) holder).deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final Dialog deletePopup = new Dialog(context);
                    deletePopup.setContentView(R.layout.item_delete_popup);
                    deletePopup.show();

                    deletePopup.findViewById(R.id.deleteOnlyThisEventButton).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            try{
                                dbController = new DBController(context, "CalendarDB" , null, 1);
                                sqLiteDatabase = dbController.getWritableDatabase();
                                String query = "DELETE FROM EventCalendar WHERE EventID = " + selectedEvent.getEventID();
                                sqLiteDatabase.execSQL(query);
                            }catch(Exception e){
                                e.printStackTrace();
                            }
                            deletePopup.hide();
                            ((MainActivity)context).ReadDatabase();
                        }
                    });
                    deletePopup.findViewById(R.id.deleteThisAndRecurringEventsButton).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            try{
                                dbController = new DBController(context, "CalendarDB" , null, 1);
                                sqLiteDatabase = dbController.getWritableDatabase();
                                String query = "DELETE FROM EventCalendar WHERE OriginEventID = " + selectedEvent.getOriginEventID();
                                sqLiteDatabase.execSQL(query);
                            }catch(Exception e){
                                e.printStackTrace();
                            }
                            deletePopup.hide();
                            ((MainActivity)context).ReadDatabase();
                        }
                    });
                }
            });
        }
    }

    @Override
    public int getItemViewType(int position) {
        return eventList.get(position).getViewType();
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }

    class GroupViewHolder extends RecyclerView.ViewHolder{

        TextView groupHeader;

        public GroupViewHolder( View itemView) {
            super(itemView);
            groupHeader = itemView.findViewById(R.id.groupHeaderText);
        }

        public void setData(String headerText){
            this.groupHeader.setText(headerText);
        }
    }


    class EventViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        TextView eventName;
        CheckBox isDoneCheckBox;
        ImageButton editButton, deleteButton;
        OnCalendarEventListener onCalendarEventListener;

        public EventViewHolder(View itemView, OnCalendarEventListener onCalendarEventListener){
            super(itemView);
            eventName = itemView.findViewById(R.id.eventItemText);
            isDoneCheckBox = itemView.findViewById(R.id.eventItemIsDoneCheckBox);
            editButton = itemView.findViewById(R.id.eventListEditButton);
            deleteButton = itemView.findViewById(R.id.eventListDeleteButton);
        }

        public void setData(Event selectedEvent, int position){
            this.eventName.setText(selectedEvent.getEventName() + "(" + selectedEvent.getStartDateTime() + ")");
            this.isDoneCheckBox.setChecked(selectedEvent.isDone());
        }

        @Override
        public void onClick(View v){

        }
    }




    public interface OnCalendarEventListener{
        void OnCalendarEventClick(int position);
    }

}
