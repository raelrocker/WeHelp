package com.wehelp.wehelp;

import android.app.Application;
import android.os.AsyncTask;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.text.Text;
import com.wehelp.wehelp.adapters.MyEventAdapter;
import com.wehelp.wehelp.adapters.ParticipatingEventAdapter;
import com.wehelp.wehelp.classes.Event;
import com.wehelp.wehelp.classes.WeHelpApp;
import com.wehelp.wehelp.controllers.EventController;

import java.util.ArrayList;

import javax.inject.Inject;

public class MyEventsActivity extends AppCompatActivity {

    @Inject
    EventController eventController;
    int userId;

    @Inject
    Application application;

    public ArrayAdapter<Event> eventArrayAdapter;
    public ArrayList<Event> eventList = new ArrayList<Event>();
    ListView listView;
    View footer; //lazy load
    private SwipeRefreshLayout swipeRefreshLayout;
    RelativeLayout loadingPanel;
    RelativeLayout noEventsPanel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_events);



        loadingPanel = (RelativeLayout) findViewById(R.id.loadingPanel);
        noEventsPanel = (RelativeLayout) findViewById(R.id.no_events_panel);
        assert noEventsPanel != null;
        noEventsPanel.setVisibility(View.GONE);
        setTitle("Eventos que criei");

        ((WeHelpApp) getApplication()).getNetComponent().inject(this);

        listView = (ListView)findViewById(R.id.myevents_listview);
        eventArrayAdapter = new MyEventAdapter(this,eventList);
        listView.setAdapter(eventArrayAdapter);
        swipeRefreshLayout = (SwipeRefreshLayout)findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new ListMyEventsTask().execute();

            }
        });

        loadingPanel.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        super.onResume();
        eventList.clear();
        eventArrayAdapter.notifyDataSetChanged();
        new ListMyEventsTask().execute();

    }

    private class ListMyEventsTask extends AsyncTask<Void, Void, ArrayList<Event>> {
        @Override
        protected void onPreExecute() {
            loadingPanel.setVisibility(View.VISIBLE);
            eventArrayAdapter.clear();
        }

        @Override
        protected ArrayList<Event> doInBackground(Void... params) {

            eventController.getMyEvents(((WeHelpApp)application).getUser().getId());
            while (eventController.getListEvents() == null && !eventController.errorService){}
            if (eventController.errorService) {
                return null;
            }

            ArrayList<Event> listEvents = eventController.getListEvents();

            eventController.setListEvents(null);
            System.out.println("listEvents: "+listEvents);

            return listEvents;
        }

        protected void onPostExecute(ArrayList<Event> events) {
            if (events == null) {
                loadingPanel.setVisibility(View.GONE);
                Toast.makeText(getApplicationContext(), eventController.errorMessages.toString(), Toast.LENGTH_LONG).show();
            } else {

                if(events.size() > 0) {
                    eventArrayAdapter.clear();
                    eventList.addAll(events);
                    noEventsPanel.setVisibility(View.GONE);
                    eventArrayAdapter.notifyDataSetChanged();
                } else {
                    noEventsPanel.setVisibility(View.VISIBLE);
                }

                loadingPanel.setVisibility(View.GONE);
                swipeRefreshLayout.setRefreshing(false);
            }
            // remover loader
        }
    }
}
