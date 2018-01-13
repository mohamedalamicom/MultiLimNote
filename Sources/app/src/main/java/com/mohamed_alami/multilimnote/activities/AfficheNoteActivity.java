package com.mohamed_alami.multilimnote.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.FirebaseDatabase;
import com.mohamed_alami.multilimnote.R;
import com.mohamed_alami.multilimnote.classes.*;
import org.ocpsoft.prettytime.PrettyTime;

import java.util.Locale;

public class AfficheNoteActivity extends AppCompatActivity {

    Utilisateur utilisateur;
    ListeDeNote listeDeNote;
    int indexNote;
    static Note note;
    ViewPager viewPager;
    AdapterTabsDeNote adapterTabsDeNote;

    public void refreshTitle(){
        getSupportActionBar().setTitle(" "+note.getTitleNote());
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Base
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_affiche_note);

        // ToolBar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Initialisation des varriables
        try {
            utilisateur = getIntent().getParcelableExtra("utilisateur");
            listeDeNote = getIntent().getParcelableExtra("notes");
            indexNote = getIntent().getIntExtra("index", 0);
            note = listeDeNote.getNotes().get(indexNote);
            refreshTitle();
        } catch (Exception e) {
            Log.e("DEBUGNOTEF", "Erreur (AfficheNoteActivity ligne 44) : " + e.getMessage());
        }

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Modifier une note
                Intent intent = new Intent(AfficheNoteActivity.this, NouvelleNoteActivity.class);
                intent.putExtra("note", note);
                intent.putExtra("utilisateur", utilisateur);
                startActivity(intent);
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        viewPager = findViewById(R.id.mainPager);
        adapterTabsDeNote = new AdapterTabsDeNote(getSupportFragmentManager());
        if(listeDeNote != null) {

            adapterTabsDeNote.setListeDeNote(listeDeNote);
            adapterTabsDeNote.setCount(listeDeNote.getNotes().size());

            viewPager.setAdapter(adapterTabsDeNote);
            viewPager.setCurrentItem(indexNote);

            viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                }

                @Override
                public void onPageSelected(int position) {
                    note = listeDeNote.getNotes().get(viewPager.getCurrentItem());
                    refreshTitle();
                    Log.d("TABS", "New Page ! New Note !" + note.getTitleNote() + "/" + viewPager.getCurrentItem());
                }

                @Override
                public void onPageScrollStateChanged(int state) {

                }
            });
        } else {
            Toast.makeText(this, "Liste de note vide", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(AfficheNoteActivity.this, MainActivity.class));
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_afficher_note, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent = new Intent(AfficheNoteActivity.this, NouvelleNoteActivity.class);
        switch (item.getItemId()){
            case R.id.action_delete:
                new AlertDialog.Builder(this)
                        .setTitle("Supprimer une note")
                        .setMessage("Supprimer une note est permanant et pour tous les participants. Vous confirmez la suppression ?")
                        .setPositiveButton("Confirmer", new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                FirebaseDatabase.getInstance().getReference().child("Notes").child("Note-"+note.getIdNote()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Intent intent = new Intent(AfficheNoteActivity.this, MainActivity.class);
                                        startActivity(intent);
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(AfficheNoteActivity.this, "Erreur lors de la suppression de la note !", Toast.LENGTH_LONG).show();
                                        Log.e("DEBUG-NOTE", e.getMessage());
                                    }
                                });
                            }
                        })
                        .setNegativeButton("Annuler", null)
                        .show();
                break;
            case R.id.action_edit:
                intent.putExtra("note", note);
                intent.putExtra("utilisateur", utilisateur);
                startActivity(intent);
                break;
            case R.id.action_new:
                intent.putExtra("utilisateur", utilisateur);
                startActivity(intent);
                break;
            case R.id.action_return:
                intent = new Intent(AfficheNoteActivity.this, MainActivity.class);
                startActivity(intent);
                break;
            case R.id.action_logout:
                AuthUI.getInstance().signOut(this).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Intent intent = new Intent(AfficheNoteActivity.this, MainActivity.class);
                        startActivity(intent);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(AfficheNoteActivity.this, "Un problème est survenu lors de la déconnexion !", Toast.LENGTH_LONG).show();
                        Log.e("DEBUG-USER", e.getMessage());
                    }
                });
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}