package com.mohamed_alami.multilimnote.activities;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.mohamed_alami.multilimnote.R;
import com.mohamed_alami.multilimnote.classes.ImageManager;
import com.mohamed_alami.multilimnote.classes.ListeDeNote;
import com.mohamed_alami.multilimnote.classes.Note;
import com.mohamed_alami.multilimnote.classes.Utilisateur;
import org.ocpsoft.prettytime.PrettyTime;

import java.util.Locale;


public class AfficheNoteActivityFragment extends Fragment {

    public AfficheNoteActivityFragment() { }


    @Override
    public void setArguments(Bundle args) {
        super.setArguments(args);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_affiche_note, container, false);
        TextView titleTextView = view.findViewById(R.id.titleNote);
        TextView infoTextView = view.findViewById(R.id.infoNote);
        ImageView imageView = view.findViewById(R.id.imageNote);
        TextView contentTextView = view.findViewById(R.id.contentNote);
        TextView participantsNoteTextView = view.findViewById(R.id.participantsNote);

        Bundle bundle = getArguments();
        Note note = bundle.getParcelable("note");

        try {
            if(note != null) {

                // Titre
                titleTextView.setText(note.getTitleNote());

                // Derniere mise à jour
                PrettyTime p = new PrettyTime(new Locale("fr"));
                String timeago = p.format(note.getUpdatedDateNote());
                String createdago = p.format(note.getCreatedDateNote());
                infoTextView.setText("Editée " + timeago + " / Créée " + createdago);

                // Participants
                if(note.getParticipantsNote().size() > 1)
                    participantsNoteTextView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.users, 0, 0, 0);
                else
                    participantsNoteTextView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.user, 0, 0, 0);

                for(Utilisateur u: note.getParticipantsNote())
                    participantsNoteTextView.append(u.getFullNameUtilisateur()+" <"+u.getEmailUtilisateur()+">\n");

                ImageManager.loadImageIntoViewFromNote(imageView, note);

                contentTextView.setText(note.getContentNote());
            }
        } catch (Exception e) {
            Log.e("DEBUGNOTE", e.getMessage());
        }

        return view;
    }
}
