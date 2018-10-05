package com.robmillaci.noted;

import android.util.Log;

import java.io.Serializable;
import java.util.ArrayList;

public class Note implements Serializable {
    String title;
    String note;
    String update;
    private static ArrayList<Note> noteObjects = new ArrayList<>();
    private static final long serialVersionUID = 1;
    private String password;
    private boolean locked;


    public Note(String title, String note, String update) {
        Log.d("bindbindbind", "Note: created");
        this.title = title;
        this.note = note;
        this.update = update;
        noteObjects.add(this);


    }

    public static ArrayList<Note> getNoteObjects() {
        return noteObjects;
    }

    public static void setNoteObjects(ArrayList<Note> noteObjects) {
        Log.d("FUCKFUCK", "setNoteObjects: notes set");
        Note.noteObjects = noteObjects;
    }

    public void setPassword(String password){
        this.password = password;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public String getPassword() {
        return password;
    }
}



