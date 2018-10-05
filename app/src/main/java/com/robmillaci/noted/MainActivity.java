package com.robmillaci.noted;

import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RadioButton;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    RecyclerView notesRecyclerView;
    RecyclerViewAdaptor mAdaptor;
    ArrayList<Note> notesList;
    SearchView mSearchView;
    View.OnClickListener checkBoxListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("OnCreateMain", "onCreate: called");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        //Restore shared preferences
        SharedPreferences appSharedPrefs = getSharedPreferences("myPrefs", MODE_PRIVATE);
        Gson gson = new Gson();
        String json = appSharedPrefs.getString("notes", "");
        Type type = new TypeToken<List<Note>>() {
        }.getType();
        ArrayList<Note> notes = gson.fromJson(json, type);

        if (!json.equals("")) {
            Note.setNoteObjects(notes);
        }
        //end of restore shared preferences

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), addNoteActivity.class));
            }
        });


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        notesRecyclerView = findViewById(R.id.notesRecyclerView);

        createRecyclerView();
    }


    private void createRecyclerView() {
        notesList = Note.getNoteObjects();

        mAdaptor = new RecyclerViewAdaptor(notesList, this);
        notesRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

        DividerItemDecoration itemDecorator = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        itemDecorator.setDrawable(ContextCompat.getDrawable(this, R.drawable.divider));

        notesRecyclerView.addItemDecoration(itemDecorator);
        notesRecyclerView.setAdapter(mAdaptor);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        mSearchView = (SearchView) MenuItemCompat.getActionView(searchItem);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        if (mSearchView != null) {
            mSearchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
            mSearchView.setIconifiedByDefault(true);
            mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    if (mAdaptor != null) {
                        mAdaptor.getFilter().filter(newText);
                    }
                    return false;
                }
            });

            return true;
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case R.id.sort:
                final ArrayList<RadioButton> radioButtons = new ArrayList<>();

                LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View v = inflater.inflate(R.layout.sort_diag_view, null);
                AlertDialog.Builder sortDiagBuilder = new AlertDialog.Builder(this);
                sortDiagBuilder.setView(v);

                sortDiagBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        for (RadioButton r : radioButtons) {
                            if (r.isChecked()) {
                                Log.d("SORT", "onClick: called sort");
                                String tag = (String) r.getTag();
                                int tagInt = Integer.valueOf(tag);
                                sort(tagInt);
                            }
                        }
                    }
                });
                AlertDialog sortDialog = sortDiagBuilder.create();
                sortDialog.show();

                RadioButton sortAZ = sortDialog.findViewById(R.id.sortAZ);
                RadioButton sortZA = sortDialog.findViewById(R.id.sortZA);
                RadioButton sortDateOldNew = sortDialog.findViewById(R.id.sortDateOldNew);
                RadioButton sortDateNewOld = sortDialog.findViewById(R.id.sortDateNewOld);

                radioButtons.add(sortAZ);
                radioButtons.add(sortZA);
                radioButtons.add(sortDateOldNew);
                radioButtons.add(sortDateNewOld);

                checkBoxListener = new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int id = v.getId();
                        for (RadioButton c : radioButtons) {
                            if (c.getId() != id) {
                                c.setChecked(false);
                            }
                        }
                    }
                };

                for (RadioButton c : radioButtons) {
                    c.setOnClickListener(checkBoxListener);
                }


        }

        return super.onOptionsItemSelected(item);
    }

    private void sort(int id) {
        switch (id) {

            case 1:
                Collections.sort(notesList, new Comparator<Note>() {
                    @Override
                    public int compare(Note o1, Note o2) {
                        return o1.title.compareTo(o2.title);
                    }
                });

                mAdaptor.notifyDataSetChanged();
                break;

            case 2:
                Collections.sort(notesList, new Comparator<Note>() {
                    @Override
                    public int compare(Note o1, Note o2) {
                        return o2.title.compareTo(o1.title);
                    }
                });
                mAdaptor.notifyDataSetChanged();
                break;
            case 3:
                Collections.sort(notesList, new Comparator<Note>() {
                    @Override
                    public int compare(Note o1, Note o2) {
                        return o1.note.compareTo(o2.update);
                    }
                });
                mAdaptor.notifyDataSetChanged();
                break;
            case 4:
                Collections.sort(notesList, new Comparator<Note>() {
                    @Override
                    public int compare(Note o1, Note o2) {
                        return o2.update.compareTo(o1.update);
                    }
                });
                mAdaptor.notifyDataSetChanged();
                break;


        }


        for (Note n : notesList) {
            Log.d("COMPARE", "compare: " + n.title + " " + n.update);
        }


    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        String id = item.getTitle().toString();
        switch (id) {
            case "Settings":
                Toast.makeText(this,"Coming soon ...",Toast.LENGTH_LONG).show();
                break;

            case "Help":
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                        "mailto","MillApp1@hotmail.com", null));
                startActivity(Intent.createChooser(emailIntent,"Send email...."));
                break;
        }


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return false;
    }

    @Override
    protected void onResume() {
        mAdaptor.notifyDataSetChanged();
        super.onResume();

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        saveToSharePrefs();
    }

    private void saveToSharePrefs() {
        SharedPreferences.Editor sharedPrefEditor = getSharedPreferences("myPrefs", MODE_PRIVATE).edit();
        Gson gson = new Gson();
        String json = gson.toJson(notesList);
        sharedPrefEditor.putString("notes", json);
        sharedPrefEditor.commit();
    }
}


