package com.mohamed_alami.multilimnote.classes;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.mohamed_alami.multilimnote.R;

import java.util.ArrayList;
import java.util.Locale;

import org.ocpsoft.prettytime.PrettyTime;

public class AdapterListeDeNote extends ArrayAdapter<Note> {

    public AdapterListeDeNote(Context context, ArrayList<Note> notes) {
        super(context, R.layout.fragment_liste_de_note, notes);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        Note note = getItem(position);
        LayoutInflater layoutInflater = LayoutInflater.from(getContext());
        View view = layoutInflater.inflate(R.layout.fragment_liste_de_note, parent, false);

        TextView titleTextView = view.findViewById(R.id.titleNote);
        TextView infoTextView = view.findViewById(R.id.infoNote);
        final ImageView imageView = view.findViewById(R.id.imageNote);

        titleTextView.setText(note.getTitleNote());
        PrettyTime p = new PrettyTime(new Locale("fr"));
        String timeago = p.format(note.getUpdatedDateNote());
        String createdago = p.format(note.getCreatedDateNote());
        infoTextView.setText("Editée " + timeago + " / Créée " + createdago);

        ImageManager.loadImageIntoViewFromNote(imageView, note);

        return view;
    }
}
