package com.mohamed_alami.multilimnote.classes;

import android.app.ListActivity;
import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.mohamed_alami.multilimnote.R;

import java.util.ArrayList;

public class ListeDeNote implements Parcelable {

    private ArrayList<Note> notes = new ArrayList<>();

    public ListeDeNote() {}

    protected ListeDeNote(Parcel in) {
        notes = in.createTypedArrayList(Note.CREATOR);
    }

    public static final Creator<ListeDeNote> CREATOR = new Creator<ListeDeNote>() {
        @Override
        public ListeDeNote createFromParcel(Parcel in) {
            return new ListeDeNote(in);
        }

        @Override
        public ListeDeNote[] newArray(int size) {
            return new ListeDeNote[size];
        }
    };

    public ArrayList<Note> getNotes() {
        return notes;
    }

    public void setNotes(ArrayList<Note> notes) {
        this.notes = notes;
    }

    @Override
    public String toString(){
        StringBuilder S = new StringBuilder();
        S.append("\n{\n");
        for (Note N:notes) {
            S.append(N.toString());
            S.append(",\n");
        }
        S.append('}');
        return S.toString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeTypedList(notes);
    }
}
