package com.mohamed_alami.multilimnote.classes;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;
import android.view.View;
import com.mohamed_alami.multilimnote.activities.AfficheNoteActivity;
import com.mohamed_alami.multilimnote.activities.AfficheNoteActivityFragment;

public class AdapterTabsDeNote extends FragmentPagerAdapter{
    int count = 0;
    ListeDeNote listeDeNote;
    Note note;

    public AdapterTabsDeNote(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {

        note = listeDeNote.getNotes().get(position);

        Bundle bundle = new Bundle();

        if(note != null)
            bundle.putParcelable("note", note);

        Fragment fragment = new AfficheNoteActivityFragment();
        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public int getCount() { return count; }

    public void setCount(int count){
        this.count = count;
    }

    public void setListeDeNote(ListeDeNote listeDeNote) { this.listeDeNote = listeDeNote; }

}
