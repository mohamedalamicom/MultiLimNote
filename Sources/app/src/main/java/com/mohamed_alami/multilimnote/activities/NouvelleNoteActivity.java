package com.mohamed_alami.multilimnote.activities;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import com.facebook.internal.Utility;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.*;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mohamed_alami.multilimnote.R;
import com.mohamed_alami.multilimnote.classes.ImageManager;
import com.mohamed_alami.multilimnote.classes.Note;
import com.mohamed_alami.multilimnote.classes.Utilisateur;
import com.squareup.picasso.Picasso;

import java.io.*;
import java.util.ArrayList;
import java.util.Date;
public class NouvelleNoteActivity extends AppCompatActivity {

    Note note;
    Utilisateur utilisateur;

    EditText titreEditText;
    EditText participantsEditText;
    EditText contenueEditText;
    ImageView imageView;
    Button actionBtn;

    // Références de la base de donnée firebase
    DatabaseReference notesRef =  FirebaseDatabase.getInstance().getReference().child("Notes");

    // Si c'est une nouvelle note (True = Nouvelle note // False = Modification de note)
    boolean isNew;

    // Image
    Bitmap image = null;

    public final int GET_FROM_CAMERA = 1;
    public final int GET_FROM_GALLERY = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nouvelle_note);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setLogo(R.drawable.logo_toolbar);

        titreEditText = findViewById(R.id.title);
        participantsEditText = findViewById(R.id.participants);
        contenueEditText = findViewById(R.id.contenue);
        contenueEditText.setMovementMethod(new ScrollingMovementMethod());
        imageView = findViewById(R.id.image);
        actionBtn = findViewById(R.id.actionBtn);

        note = getIntent().getParcelableExtra("note");
        utilisateur = getIntent().getParcelableExtra("utilisateur");

        if (note == null){
            // Nouvelle note
            isNew = true;
            note = new Note();
            actionBar.setTitle(" Nouvelle Note");
            actionBtn.setText("Ajouter la note");
            imageView.setImageResource(R.drawable.placeholder);
            // On ajoute l'utilisateur comme participant
            participantsEditText.setText(utilisateur.getEmailUtilisateur()+";");
        } else {
            // Modification d'une note
            isNew = false;
            actionBar.setTitle(" Modification de la note");
            // Titre de la note
            titreEditText.setText(note.getTitleNote());

            // Charger les participants
            StringBuilder s = new StringBuilder();
            for(Utilisateur u : note.getParticipantsNote())
                s.append(u.getEmailUtilisateur() +";");
            participantsEditText.setText(s.toString());

            // Contenue de la note
            contenueEditText.setText(note.getContentNote());

            // Image de la note
            ImageManager.loadImageIntoViewFromNote(imageView, note);

            actionBtn.setText("Modifier la note");
        }

        // Séléctionner une image de la galerie ou caméra
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectImage();
            }
        });

        // Action d'ajout / modification
        actionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // SI la note est vide
                if(contenueEditText.getText().length() == 0)
                    Toast.makeText(NouvelleNoteActivity.this, "Impossible d'enregistrer une note sans contenu", Toast.LENGTH_SHORT).show();
                else {
                    // Titre
                    if (titreEditText.getText().length() > 0)
                        note.setTitleNote(titreEditText.getText().toString());

                    // Contenue
                    note.setContentNote(contenueEditText.getText().toString());

                    // Si c'est une nouvelle note
                    if(isNew) {
                        note.setCreatedBy(utilisateur.getIdUtilisateur()); // Qui a créé la note
                        note.setCreatedDateNote(new Date()); // Date de création
                    }

                    // L'utilisateur qui a mis à jour la note
                    note.setUpdatedBy(utilisateur.getIdUtilisateur());

                    // Date de la mise à jour
                    note.setUpdatedDateNote(new Date());

                    // Participants
                    if (participantsEditText.length() > 0) {

                        // On réinitialise la liste des participants
                        note.getParticipantsNote().clear();

                        // Ajouter l'utilisateur qui modifie/ajoute la note
                        note.newUser(utilisateur);

                        // On commence à ajouter les participants depuis les emails fournis
                        final String emails[] = participantsEditText.getText().toString().split(",|;");
                        for (int i=0; i < emails.length; i++) {
                            final int index = i;
                            try {
                                Query query = FirebaseDatabase.getInstance().getReference().child("Utilisateurs").orderByChild("emailUtilisateur").equalTo(emails[i].trim());
                                query.addListenerForSingleValueEvent(new ValueEventListener() {

                                    Utilisateur participant = new Utilisateur();

                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        if (dataSnapshot.exists()) {
                                            dataSnapshot = dataSnapshot.getChildren().iterator().next();
                                            participant.setSignedInUtilisateur(dataSnapshot.child("signedInUtilisateur").getValue(Date.class));
                                            participant.setCreatedDateUtilisateur(dataSnapshot.child("createdDateUtilisateur").getValue(Date.class));
                                            participant.setIdUtilisateur(dataSnapshot.child("idUtilisateur").getValue(String.class));
                                            participant.setProviderUtilisateur(dataSnapshot.child("providerUtilisateur").getValue(String.class));
                                            participant.setFullNameUtilisateur(dataSnapshot.child("fullNameUtilisateur").getValue(String.class));
                                            participant.setEmailUtilisateur(dataSnapshot.child("emailUtilisateur").getValue(String.class));
                                            note.newUser(participant);

                                            // Si c'est le dernier participant
                                            if (index == emails.length -1)
                                                nextStepInTheProcess();
                                        } else {
                                            Toast.makeText(NouvelleNoteActivity.this, "Email '"+ emails[index] +"' est introuvable", Toast.LENGTH_SHORT).show();
                                            Log.d("DEBUG-U", "Email non trouvé !");
                                        }

                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {
                                        Log.d("DEBUG-U", "(Cancelled) Email non trouvé !");
                                    }
                                });
                            } catch (Exception e){
                                Log.d("-U", e.getMessage());
                            }
                        }
                    } else
                        Toast.makeText(NouvelleNoteActivity.this, "Impossible d'enregistrer une note sans participants", Toast.LENGTH_SHORT).show();
                        //nextStepInTheProcess();

                    // Attention : On ne lance pas la demande du retour à l'activité précédente ici
                    // Tout simplement parce que le code ici est suseptible d'être éxecuté avant la fin de l'upload d'image
                }
            }
        });
    }

    private void nextStepInTheProcess(){
        try {
            // S'il y a une image
            if (image != null) {
                // Référence au storage firebase
                StorageReference storageRef = FirebaseStorage.getInstance().getReference();
                StorageReference NewImageRef = storageRef.child("images/note-"+String.valueOf(note.getIdNote())+".jpg");

                // Convertir image de bitmap à bytes
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                image.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                byte[] D = baos.toByteArray();

                // Uploader l'image
                UploadTask uploadTask = NewImageRef.putBytes(D);
                uploadTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(NouvelleNoteActivity.this, "Erreur: image n'a pas pu être enregistrée !", Toast.LENGTH_SHORT).show();
                        Log.e("DEBUG-IMG", e.getMessage());
                    }
                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // S'il y a une image, on envoie après l'upload de l'image
                        notesRef.child("Note-"+String.valueOf(note.getIdNote())).setValue(note);
                        // On revient à l'activité précédente
                        previousActivity();
                    }
                });
            } else {
                // S'il n'y a pas d'image on peut directement envoyé la note
                notesRef.child("Note-" + String.valueOf(note.getIdNote())).setValue(note);
                // Et revenir à l'activité précédente
                previousActivity();
            }
        } catch (Exception e) {
            Log.e("DEBUG-NOTE", e.getMessage());
        }
    }

    private void previousActivity(){
        // Retour
        Intent returnIntent = new Intent(NouvelleNoteActivity.this, MainActivity.class);
        startActivity(returnIntent);
    }

    private void selectImage() {
        final CharSequence[] items = { "Prendre une Photo", "Choisir de la Galerie", "Annuler" };
        AlertDialog.Builder builder = new AlertDialog.Builder(NouvelleNoteActivity.this);
        builder.setTitle("Séléctionner une Photo");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (items[item].equals("Prendre une Photo")) {
                    // Caméra
                    Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    if (cameraIntent.resolveActivity(getPackageManager()) != null)
                        startActivityForResult(cameraIntent, GET_FROM_CAMERA);
                } else if (items[item].equals("Choisir de la Galerie")) {
                    // Gallerie
                    Intent galleryIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI);
                    if (galleryIntent.resolveActivity(getPackageManager()) != null)
                        startActivityForResult(galleryIntent, GET_FROM_GALLERY);
                } else if (items[item].equals("Annuler")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) { // SI j'ai trouvé !
            if (requestCode == GET_FROM_CAMERA) { // Si l'enfant est la caméra
                Bundle extras = data.getExtras();
                image = (Bitmap) extras.get("data");
                imageView.setImageBitmap(image);
                Log.d("DEBUG-IMG", "Image prise de la camera");
            }
            if (requestCode == GET_FROM_GALLERY) {

                Uri uri = data.getData();

                try {
                    image = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                    imageView.setImageBitmap(image);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Log.d("DEBUG-IMG", "Image séléctonnée de la galerie");
            }
        } else {
            Log.e("DEBUG-IMG", "Aucune image séléctonnée");
        }

    }

}
