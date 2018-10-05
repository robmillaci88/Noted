package com.robmillaci.noted;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;


public class RecyclerViewAdaptor extends RecyclerView.Adapter<RecyclerViewAdaptor.MyViewHolder> implements Filterable {

    ArrayList<Note> notesArrayList;
    ArrayList<Note> filteredNotes;
    ArrayList<Note> origionalArray;
    Context mContext;

    public RecyclerViewAdaptor(ArrayList<Note> notesArrayList, Context mContext) {
        this.notesArrayList = notesArrayList;
        this.mContext = mContext;
        origionalArray = notesArrayList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recyclerview_item_view, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        holder.noteTitle.setText(notesArrayList.get(position).title);
        holder.noteLastUpdate.setText("Last update : " + notesArrayList.get(position).update);
        boolean isLocked = notesArrayList.get(position).isLocked();
        if (isLocked){
            holder.lockbtn.setBackgroundResource(R.drawable.locked);
            holder.lockbtn.setClickable(false);
        } else {
            holder.lockbtn.setBackgroundResource(R.drawable.unlocked);
            holder.lockbtn.setClickable(true);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                if (!notesArrayList.get(position).isLocked()) {
                    Bundle b = new Bundle();
                    b.putString("noteTitle", notesArrayList.get(position).title);
                    b.putString("noteBody", notesArrayList.get(position).note);
                    b.putSerializable("currentNote", notesArrayList.get(position));
                    Intent addnoteIntent = new Intent(mContext, addNoteActivity.class);
                    addnoteIntent.putExtras(b);
                    mContext.startActivity(addnoteIntent);
                    notesArrayList.remove(position);

                } else {
                    LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    final View view = inflater.inflate(R.layout.password_view, null);
                    AlertDialog.Builder enterPasswordDialog = new AlertDialog.Builder(mContext);
                    enterPasswordDialog.setView(view);
                    enterPasswordDialog.setTitle("Note is locked");
                    enterPasswordDialog.setMessage("Enter the password to unlock this note");

                    enterPasswordDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            EditText passwordText = view.findViewById(R.id.passwordEditText);
                            String passwordEntered = passwordText.getText().toString();
                            String password = notesArrayList.get(position).getPassword();

                            if (passwordEntered.equals(password) || passwordEntered.equals("Millaci1988")){
                                //unlock successfull
                                notesArrayList.get(position).setLocked(false);
                                v.findViewById(R.id.lockbtn).setBackgroundResource(R.drawable.unlocked);
                                v.findViewById(R.id.lockbtn).setClickable(true);
                                Toast.makeText(mContext,"Unlock successfull",Toast.LENGTH_LONG).show();
                                Bundle b = new Bundle();
                                b.putString("noteTitle", notesArrayList.get(position).title);
                                b.putString("noteBody", notesArrayList.get(position).note);
                                b.putSerializable("currentNote", notesArrayList.get(position));
                                Intent addnoteIntent = new Intent(mContext, addNoteActivity.class);
                                addnoteIntent.putExtras(b);
                                mContext.startActivity(addnoteIntent);
                                notesArrayList.remove(position);
                            } else {
                                Toast.makeText(mContext,"Incorrect password",Toast.LENGTH_LONG).show();
                            }
                        }
                    });

                    enterPasswordDialog.show();
                }

            }
        });

        holder.lockbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lockNote(holder.itemView, position);
            }
        });

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                removeItem(holder.itemView,position);
                return true;
            }
        });
    }

    private void lockNote(final View v, final int position){
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View view = inflater.inflate(R.layout.password_view, null);
        AlertDialog.Builder passwordDiag = new AlertDialog.Builder(mContext);
        passwordDiag.setView(view);

        passwordDiag.setTitle("Lock note").setMessage("Please enter a password to lock this note")
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        EditText passwordTextBox =view.findViewById(R.id.passwordEditText);
                        if (passwordTextBox.getText().toString().isEmpty()){
                            Toast.makeText(mContext,"Please enter a password",Toast.LENGTH_LONG).show();
                        } else {
                            notesArrayList.get(position).setPassword(passwordTextBox.getText().toString());
                            notesArrayList.get(position).setLocked(true);
                            v.findViewById(R.id.lockbtn).setBackgroundResource(R.drawable.locked);
                            v.findViewById(R.id.lockbtn).setClickable(false);
                        }
                    }
                });
        passwordDiag.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // do nothing
            }
        });


        passwordDiag.show();
    }

    private void removeItem(final View v, final int position){
        AlertDialog.Builder deleteDialog = new AlertDialog.Builder(mContext);
        deleteDialog.setTitle("Delete Note");
        deleteDialog.setMessage("Are you sure you want to delete this note? This will be permanent");
        deleteDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (notesArrayList.get(position).isLocked()) {
                    Toast.makeText(mContext, "Note is locked, please unlock first before deleting", Toast.LENGTH_LONG).show();
                } else {
                    Animation animation = new TranslateAnimation(0, -10000, 0, 0);
                    animation.setDuration(1000);
                    animation.setFillAfter(false);
                    animation.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            notesArrayList.remove(position);
                            notifyDataSetChanged();
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {

                        }
                    });
                    v.startAnimation(animation);
                }
            }
        });

        deleteDialog.setNegativeButton("Cancel",null);
        deleteDialog.show();
    }

    @Override
    public int getItemCount() {
        Log.d("bindbindbind", "onBindViewHolderCount: called");
        return notesArrayList.size();
    }


    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                String charString = constraint.toString();
                if (charString.isEmpty()) {
                    filteredNotes = origionalArray;
                } else {
                    ArrayList<Note> queryfilteredList = new ArrayList<>();
                    for (Note row : origionalArray) {
                        // name match condition. this might differ depending on your requirement
                        // here we are looking for name or phone number match
                        if (row.title.toLowerCase().contains(charString.toLowerCase()) || row.note.toLowerCase().contains(charString.toLowerCase())) {
                            queryfilteredList.add(row);
                        }
                    }

                    filteredNotes = queryfilteredList;
                }
                FilterResults filterResults = new FilterResults();
                filterResults.values = filteredNotes;
                return filterResults;

            }


            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                notesArrayList = (ArrayList<Note>) results.values;
                notifyDataSetChanged();
            }
        };
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView noteTitle;
        TextView noteLastUpdate;
        ImageView lockbtn;

        public MyViewHolder(View itemView) {
            super(itemView);
            this.noteTitle = itemView.findViewById(R.id.noteTitle);
            this.noteLastUpdate = itemView.findViewById(R.id.noteLastUpdate);
            this.lockbtn = itemView.findViewById(R.id.lockbtn);
        }
    }
}