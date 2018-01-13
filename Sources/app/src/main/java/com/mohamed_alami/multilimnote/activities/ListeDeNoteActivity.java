package com.mohamed_alami.multilimnote.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.*;
import android.widget.*;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.*;
import com.mohamed_alami.multilimnote.R;
import com.mohamed_alami.multilimnote.classes.*;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class ListeDeNoteActivity extends AppCompatActivity {

    // Elements de cette activité
    TextView nonotestxt; // Text au cas où il n'a pas de notes
    ListView noteslistview; // Liste View des notes

    // Liste de Note
    ListeDeNote listeDeNote;
    AdapterListeDeNote adapter;

    // Références de la base de donnée
    DatabaseReference notesRef =  FirebaseDatabase.getInstance().getReference().child("Notes");

    // Utilisateur
    Utilisateur utilisateur;

    // Nombre de Notes courrant
    int nbNoteCurr;

    // Est-ce un premier chargement
    boolean isFirst = true;

    // Paramètre de trie de la liste
    SharedPreferences preferences;
    String sortby = "updatedDateNote"; // Par défaut

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_liste_de_note);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setLogo(R.drawable.logo_toolbar);
        try {
            utilisateur = getIntent().getParcelableExtra("utilisateur");
            if(utilisateur != null)
                getSupportActionBar().setTitle(" Notes de "+utilisateur.getFullNameUtilisateur());
        } catch (Exception e) {
            Log.e("DEBUGNOTE", "Erreur : " + e.getMessage());
        }

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent nouvelleNoteIntent = new Intent(ListeDeNoteActivity.this, NouvelleNoteActivity.class);
                nouvelleNoteIntent.putExtra("utilisateur", utilisateur);
                startActivity(nouvelleNoteIntent);
            }
        });

        // Instantiation des éléments de cette activité
        nonotestxt = findViewById(R.id.noNoteText);
        noteslistview = findViewById(R.id.notesListView);

        // Liste des notes
        listeDeNote = new ListeDeNote();
        adapter = new AdapterListeDeNote(this, listeDeNote.getNotes());
        noteslistview.setAdapter(adapter);

        noteslistview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                afficherNote(i);
            }
        });

        noteslistview.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, final int i, long l) {

                final CharSequence[] items = { "Afficher",  "Modifier", "Supprimer", "Annuler" };
                AlertDialog.Builder builder = new AlertDialog.Builder(ListeDeNoteActivity.this);
                builder.setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int item) {
                        switch (items[item].toString()) {
                            case "Afficher":
                                afficherNote(i);
                                break;
                            case "Modifier":
                                Intent intent = new Intent(ListeDeNoteActivity.this, NouvelleNoteActivity.class);
                                intent.putExtra("note", listeDeNote.getNotes().get(i));
                                intent.putExtra("utilisateur", utilisateur);
                                startActivity(intent);
                                break;
                            case "Supprimer":
                                new AlertDialog.Builder(ListeDeNoteActivity.this)
                                        .setTitle("Supprimer une note")
                                        .setMessage("Supprimer une note est permanant et pour tous les participants. Vous confirmez la suppression ?")
                                        .setPositiveButton("Confirmer", new DialogInterface.OnClickListener()
                                        {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                FirebaseDatabase.getInstance().getReference().child("Notes").child("Note-"+listeDeNote.getNotes().get(i).getIdNote()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        Intent intent = new Intent(ListeDeNoteActivity.this, MainActivity.class);
                                                        startActivity(intent);
                                                    }
                                                }).addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Toast.makeText(ListeDeNoteActivity.this, "Erreur lors de la suppression de la note !", Toast.LENGTH_LONG).show();
                                                        Log.e("DEBUG-NOTE", e.getMessage());
                                                    }
                                                });
                                            }
                                        })
                                        .setNegativeButton("Annuler", null)
                                        .show();
                                break;
                            default:
                                dialog.dismiss();
                                break;
                        }
                    }
                });
                builder.show();

                return false;
            }
        });

        // Nombre courrant d'item dans la liste
        nbNoteCurr = listeDeNote.getNotes().size();

        // Recuperer préférences sauvegardé (pour le trie)
        preferences = getSharedPreferences("preferences", 0);
        sortby = preferences.getString("sortby", sortby); // Par défaut "updatedDateNote" sinon la valeur stockée
    }

    void afficherNote(int i){
        Intent noteIntent =  new Intent(ListeDeNoteActivity.this, AfficheNoteActivity.class);
        noteIntent.putExtra("utilisateur", utilisateur);
        noteIntent.putExtra("notes", listeDeNote);
        noteIntent.putExtra("index", i);
        Log.d("DEBUG--NOTE", "Sending list : "+listeDeNote.toString());
        Log.d("DEBUG--NOTE", "Sending index : "+String.valueOf(i));
        startActivity(noteIntent);
    }

    @Override
    protected void onStart() {
        super.onStart();
        notesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Vider la liste avant de mettre à jour
                adapter.clear();
                Log.d("DEBUGNOTE", "Clearing the list for new entries !");

                // Liste des participants de chaque note
                ArrayList<Utilisateur> participantsNote = new ArrayList<>();
                participantsNote.clear();
                Log.d("DEBUGNOTE", "Clearing particpants for new entries!");
                Utilisateur u;

                // Préparons la Note
                Note note = null;

                // Participant à la note
                boolean isParticipant;

                // Parcourir toutes les notes de la base de donnée
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    note = new Note();
                    // On assume qu'il n'est pas participant dans cette note
                    isParticipant = false;
                    // On vérifie s'il participe
                    try {
                        // Vérifier que la note est remplie
                        if(snapshot.getValue() != null) {
                            Log.d("DEBUGNOTE", "Got the note '"+snapshot.child("titleNote").getValue().toString()+"' from the database");
                            Log.d("DEBUGNOTE", "Checking ownership for : "+utilisateur.getEmailUtilisateur());
                            // Parcourir les participants de la note
                            for (DataSnapshot participant : snapshot.child("participantsNote").getChildren())
                                for(DataSnapshot sn: participant.getChildren()) { // date // email...
                                    Log.d("CURIOUS", sn.getKey().toString());
                                    if (sn.getKey().equals("emailUtilisateur")) {
                                        u = new Utilisateur();
                                        u.setEmailUtilisateur(sn.getValue().toString());
                                        Log.d("DEBUGNOTE", "Participant : "+u.getEmailUtilisateur());
                                        if(utilisateur.equals(u)) {
                                            isParticipant = true;
                                            Log.d("DEBUGNOTE", "The note '"+snapshot.child("titleNote").getValue().toString()+"' can be edited by "+utilisateur.getFullNameUtilisateur());
                                        } else {
                                            Log.d("DEBUGNOTE", utilisateur.getEmailUtilisateur()+" doesn't match "+u.getEmailUtilisateur());
                                        }
                                        participantsNote.add(u); // Au cas où on découvre qu'il édite la note plus tard
                                    }
                            }
                        }
                    } catch (Exception e) {
                        Log.e("DEBUGNOTE", "Erreur : " + e.getMessage());
                    }

                    if (isParticipant) {
                        // Récup la note de la base de donnée à un objet
                        try {
                            note.setIdNote(Integer.valueOf(snapshot.child("idNote").getValue().toString()));
                            note.setContentNote(snapshot.child("contentNote").getValue().toString());
                            note.setCreatedBy(snapshot.child("createdBy").getValue().toString());
                            note.setTitleNote(snapshot.child("titleNote").getValue().toString());
                            note.setUpdatedBy(snapshot.child("updatedBy").getValue().toString());
                            note.setCreatedDateNote((Date)snapshot.child("createdDateNote").getValue(Date.class));
                            note.setUpdatedDateNote((Date)snapshot.child("updatedDateNote").getValue(Date.class));

                            ArrayList<Utilisateur> p = new ArrayList<>();
                            for (DataSnapshot participant: snapshot.child("participantsNote").getChildren())
                                p.add(participant.getValue(Utilisateur.class));
                            note.setParticipantsNote(p);

                            Log.d("DEBUGNOTE", '['+note.toString()+']'+ " The note is ready to be added to the list");

                            if (!listeDeNote.getNotes().contains(note)) {
                                // Ajouter la note à la liste
                                listeDeNote.getNotes().add(note);
                                // Trier la liste
                                sortNotes(sortby);
                                // Actualiser
                                adapter.notifyDataSetChanged();
                                Log.d("DEBUGNOTE", '['+note.toString()+']' + " The note was added successfully");
                                Log.d("DEBUGNOTE","Current Elements of the array : "+ listeDeNote.toString());
                                Log.d("DEBUGNOTE","---------");
                            } else
                                Log.d("DEBUGNOTE", '['+note.toString()+']' + " The note already exists in the list");

                        } catch (Exception e) {
                            Log.e("DEBUGNOTE", "Erreur : " + e.getMessage());
                            e.printStackTrace();
                        }
                    }
                    else{
                        Log.d("DEBUGNOTE", "The note '"+snapshot.child("titleNote").getValue().toString()+" is not editable by the current user");
                        participantsNote.clear();
                    }
                }

                int nbNoteNew = listeDeNote.getNotes().size(); // Nouveau nombre de notes courrant

                if (nbNoteNew > 0) {
                    // S'il a des notes : cacher le text et afficher la liste
                    nonotestxt.setVisibility(View.INVISIBLE);
                    noteslistview.setVisibility(View.VISIBLE);
                    // Si on a plus d'éléments qu'on avait
                    if(isFirst) {
                        isFirst = false;
                    } else if (nbNoteCurr < nbNoteNew) {
                        // Une note a été ajoutée
                        Toast.makeText(ListeDeNoteActivity.this, "Une nouvelle note vient d'être ajoutée !", Toast.LENGTH_SHORT).show();
                    } else if(nbNoteCurr > nbNoteNew) {
                        // Une note a été supprimée
                        Toast.makeText(ListeDeNoteActivity.this, "Une note vient d'être supprimée !", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // S'il n'y a pas de note : cacher la liste et afficher le texte
                    nonotestxt.setVisibility(View.VISIBLE);
                    noteslistview.setVisibility(View.INVISIBLE);
                }
                // A la fin, le nombre courrant égale au nouveau nombre des notes
                nbNoteCurr = nbNoteNew;
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // En cas de problème de connexion avec la base de donnée
                Log.e("ERROR-List",databaseError.getMessage());
                Snackbar.make(findViewById(R.id.fab), "Un probleme s'est produit lors de la synchronisation !", Snackbar.LENGTH_INDEFINITE).show();
            }
        });
    }

    public void sortNotes(final String s){

        Collections.sort(listeDeNote.getNotes(), new Comparator<Note>() {
            public int compare(Note o1, Note o2) {
                switch (s) {
                    case "titleNoteAZ":
                        if (o1.getTitleNote() == null || o2.getTitleNote() == null)
                            return 0;
                        return o1.getTitleNote().toUpperCase().compareTo(o2.getTitleNote().toUpperCase());

                    case "titleNoteZA":
                        if (o1.getTitleNote() == null || o2.getTitleNote() == null)
                            return 0;
                        return o1.getTitleNote().toUpperCase().compareTo(o2.getTitleNote().toUpperCase());

                    case "updatedDateNote":
                        if (o1.getUpdatedDateNote() == null || o2.getUpdatedDateNote() == null)
                            return 0;
                        return o1.getUpdatedDateNote().compareTo(o2.getUpdatedDateNote());

                    case "createdDateNote":
                        if (o1.getCreatedDateNote() == null || o2.getCreatedDateNote() == null)
                            return 0;
                        return o1.getCreatedDateNote().compareTo(o2.getCreatedDateNote());

                    default:
                        return 0;
                }
            }
        });
        if(!s.equals("titleNoteAZ"))
            Collections.reverse(listeDeNote.getNotes());
        adapter.notifyDataSetChanged();
        Log.d("SORT","Sorting by "+s);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_list_de_note, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.action_logout) {
            AuthUI.getInstance().signOut(this).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Intent signintent = new Intent(ListeDeNoteActivity.this, MainActivity.class);
                    startActivity(signintent);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e("DEBUG-USER", e.getMessage());
                    Toast.makeText(ListeDeNoteActivity.this, "Un problème est survenu lors de la déconnexion !", Toast.LENGTH_LONG).show();
                }
            });
        } else if(item.getItemId() == R.id.action_sortby){
            final CharSequence[] items = { "Trie par Titre (A - Z)", "Trie par Titre (Z - A)",  "Trie par Date de Création", "Trie par Date de Modification", "Annuler" };
            AlertDialog.Builder builder = new AlertDialog.Builder(ListeDeNoteActivity.this);
            builder.setTitle("Trier la liste de notes");
            builder.setItems(items, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int item) {

                    String s = sortby;

                    switch (items[item].toString()) {
                        case "Trie par Titre (A - Z)":
                            s = "titleNoteAZ";
                            break;
                        case "Trie par Titre (Z - A)":
                            s = "titleNoteZA";
                            break;
                        case "Trie par Date de Création":
                            s = "createdDateNote";
                            break;
                        case "Trie par Date de Modification":
                            s = "updatedDateNote";
                            break;
                        default:
                            dialog.dismiss();
                            break;
                    }
                    if(!items[item].toString().equals("Annuler"))
                        Toast.makeText(ListeDeNoteActivity.this, items[item].toString(), Toast.LENGTH_SHORT).show();
                    preferences.edit().putString("sortby", s).apply();
                    sortNotes(s);
                }
            });
            builder.show();
        }
        return super.onOptionsItemSelected(item);
    }
}
