package com.example.todo;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.UUID;

public class Directory implements Parcelable {
    private String id;
    private String name;
    private final ArrayList<String> notes;


    public Directory(String id, String name) {
        this.id = id;
        this.name = name;
        this.notes = new ArrayList<>();
    }

    public Directory(String name) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.notes = new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) { // (optional)
        this.id = id;
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

    // Parcelable stuff
    protected Directory(Parcel in) {
        id = in.readString();
        name = in.readString();
        notes = in.createStringArrayList();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
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
