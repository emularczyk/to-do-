package com.example.todo;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

public class Directory implements Parcelable {
    private String name;
    private final ArrayList<String> notes;

    public Directory(String name) {
        this.name = name;
        this.notes = new ArrayList<>();
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<String> getNotes() {
        return notes;
    }

    public void addNote(String note) {
        notes.add(note);
    }

    protected Directory(Parcel in) {
        name = in.readString();
        notes = in.createStringArrayList();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeStringList(notes);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<Directory> CREATOR = new Parcelable.Creator<>() {
        @Override
        public Directory createFromParcel(Parcel in) {
            return new Directory(in);
        }

        @Override
        public Directory[] newArray(int size) {
            return new Directory[size];
        }
    };
}