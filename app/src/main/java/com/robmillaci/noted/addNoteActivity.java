package com.robmillaci.noted;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Toast;

import java.util.Calendar;

public class addNoteActivity extends AppCompatActivity {
    EditText noteTitle;
    EditText noteBody;
    Note currentNoteReference;
    CoordinatorLayout thisView;
    boolean normalSave = false;
    private ShareActionProvider mShareActionProvider;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_note);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        noteTitle = findViewById(R.id.noteTitle);
        noteBody = findViewById(R.id.noteBody);
        thisView = findViewById(R.id.coordinatorLayout);


        Bundle intentBundle = getIntent().getExtras();
        if (intentBundle != null) {
            noteTitle.setText(intentBundle.getString("noteTitle"));
            noteBody.setText(intentBundle.getString("noteBody"));
            currentNoteReference = (Note) intentBundle.getSerializable("currentNote");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.addnotemenu, menu);

        MenuItem item = menu.findItem(R.id.share);
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);

        return true;
    }

    private void setShareIntent(Intent shareIntent) {
        if (mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(shareIntent);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.share:
                String title = noteTitle.getText().toString();
                String body = noteBody.getText().toString();
                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.putExtra(Intent.EXTRA_TEXT, "Sharing a note from Noted App" + "\n" + title + "\n" + body);
                shareIntent.setType("text/plain");
                setShareIntent(shareIntent);
                break;
            case android.R.id.home:
               onBackPressed();

            break;
            case R.id.save:
                if (currentNoteReference != null && noteBody.getText().toString().equals(currentNoteReference.note)
                        && noteTitle.getText().toString().equals(currentNoteReference.title)) {
                    saveNote(false, true);
                } else {
                    saveNote(true, true);
                }
                break;
            case R.id.undo:
                if (currentNoteReference != null && (!noteTitle.getText().toString().equals(currentNoteReference.title)
                        || !noteBody.getText().toString().equals(currentNoteReference.note))) {
                    AlertDialog.Builder alertDiagBuiler = new AlertDialog.Builder(this);
                    alertDiagBuiler.setTitle("Undo Changes");
                    alertDiagBuiler.setMessage("Are you sure you want to undo your changes?");
                    alertDiagBuiler.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String oldNoteTitle = currentNoteReference.title;
                            String oldNoteBody = currentNoteReference.note;

                            noteTitle.setText(oldNoteTitle);
                            noteTitle.setSelection(noteTitle.getText().length());
                            noteBody.setText(oldNoteBody);
                            noteBody.setSelection(noteBody.getText().length());
                        }
                    });
                    alertDiagBuiler.create();
                    alertDiagBuiler.show();
                } else {
                    Toast.makeText(this, "Nothing to undo", Toast.LENGTH_LONG).show();
                }
        }

        return true;
    }

    private void saveNote(boolean newNote, boolean displayToast) {
        if (!noteTitle.getText().toString().isEmpty()) {
            if (newNote) {
                String date = java.text.DateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime());
                new Note(noteTitle.getText().toString(), noteBody.getText().toString(), date);
            } else {
                new Note(noteTitle.getText().toString(), noteBody.getText().toString(), currentNoteReference.update);
            }
            normalSave = true;
            if (displayToast) {
                Toast.makeText(this, "Note Saved", Toast.LENGTH_LONG).show();
                finish();
            }
        } else {
            Toast.makeText(this, "Please enter a note title", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onBackPressed() {
        if (!normalSave) {
            Log.d("backbackback", "onBackPressed: got here");
            doYouWantToSave();

        } else {
            Log.d("backbackback", "onBackPressed: got to else");
            normalSave = false;
            finish();
        }
    }

    private void doYouWantToSave() {
        if (currentNoteReference == null) {
            displaySaveDialog();
        } else {
            if (noteTitle.getText().toString().isEmpty()) {
                Toast.makeText(this, "Please enter a note title", Toast.LENGTH_LONG).show();
            } else {
                if ((!currentNoteReference.title.equals(noteTitle.getText().toString()))
                        || !currentNoteReference.note.equals(noteBody.getText().toString())) {
                    displaySaveDialog();
                } else {
                    normalSave = true;
                    saveNote(false, false);
                    onBackPressed();
                }
            }
        }
    }

    private void displaySaveDialog() {
        AlertDialog.Builder alertDiagBuiler = new AlertDialog.Builder(this);
        alertDiagBuiler.setTitle("Save Changes");
        alertDiagBuiler.setMessage("Do you want to save your changes?");

        alertDiagBuiler.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (currentNoteReference == null) {//new note
                    saveNote(true, true);

                } else {
                    saveNote(false, true);
                }
            }
        });

        alertDiagBuiler.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });

        alertDiagBuiler.create();
        alertDiagBuiler.show();
    }
}
