package com.mohamed_alami.multilimnote.classes;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;
import java.util.Random;

public class Note implements Parcelable {

    private String contentNote; // Contenue de la note (texte)
    private String createdBy; // uID de l'utilisateur qui a créé la note
    private Date createdDateNote; // Date de création
    private int idNote; // Identifiant de la note
    private ArrayList<Utilisateur> participantsNote; // Participants qui ont accès à la note
    private String titleNote; // Titre de la note  (titre)
    private String updatedBy; // uID du dernier utilisateur mettant la note à jour
    private Date updatedDateNote; // Date de la dernière mise à jour
    public static String defaultImage = "https://placehold.it/500x300"; // Image par défaut
    // On stock plus l'image (URL) dans la base de donnée, on récupére à partir de l'ID de la note
    //private String imageNote; // Image de la note (image)

    public Note() {
        idNote = Math.abs(new Random().nextInt());
        titleNote = "Note sans titre";
        //imageNote = defaultImage;
        participantsNote = new ArrayList<>();
        createdDateNote = new Date();
        updatedDateNote = new Date();
    }

    public Note(String contentNote, String createdBy, Date createdDateNote, int idNote, ArrayList<Utilisateur> participantsNote, String titleNote, String updatedBy, Date updatedDateNote) {
        this.contentNote = contentNote;
        this.createdBy = createdBy;
        this.createdDateNote = createdDateNote;
        this.idNote = idNote;
        this.participantsNote = participantsNote;
        this.titleNote = titleNote;
        this.updatedBy = updatedBy;
        this.updatedDateNote = updatedDateNote;
    }

    protected Note(Parcel in) {
        contentNote = in.readString();
        createdBy = in.readString();
        createdDateNote = new Date(in.readLong());
        idNote = in.readInt();
        participantsNote = in.createTypedArrayList(Utilisateur.CREATOR);
        titleNote = in.readString();
        updatedBy = in.readString();
        updatedDateNote = new Date(in.readLong());
    }

    public void newUser(Utilisateur utilisateur) {
        if (!this.participantsNote.contains(utilisateur)) {
            this.participantsNote.add(utilisateur);
            Log.d("-U", "Participant " + utilisateur.getFullNameUtilisateur() + "(" + utilisateur.getEmailUtilisateur() + ") ajouté !");
        }
        else
            Log.d("-U", "Participant " + utilisateur.getFullNameUtilisateur() + "(" + utilisateur.getEmailUtilisateur() + ") exist déjà !");
    }


    @Override
    public boolean equals(Object obj) {
        return (idNote == ((Note)obj).idNote);
    }

    @Override
    public String toString(){
        return idNote + " / " + titleNote + " / " + contentNote;
    }

    @Override
    public int hashCode() {
        return Objects.hash(contentNote,createdBy,createdDateNote,idNote,participantsNote,titleNote,updatedBy,updatedDateNote);
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(contentNote);
        parcel.writeString(createdBy);
        parcel.writeLong(createdDateNote.getTime());
        parcel.writeInt(idNote);
        parcel.writeTypedList(participantsNote);
        parcel.writeString(titleNote);
        parcel.writeString(updatedBy);
        parcel.writeLong(updatedDateNote.getTime());
    }

    public String getContentNote() {
        return contentNote;
    }

    public void setContentNote(String contentNote) {
        this.contentNote = contentNote;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public Date getCreatedDateNote() {
        return createdDateNote;
    }

    public void setCreatedDateNote(Date createdDateNote) {
        this.createdDateNote = createdDateNote;
    }

    public int getIdNote() {
        return idNote;
    }

    public void setIdNote(int idNote) {
        this.idNote = idNote;
    }

    public ArrayList<Utilisateur> getParticipantsNote() {
        return participantsNote;
    }

    public void setParticipantsNote(ArrayList<Utilisateur> participantsNote) {
        this.participantsNote = participantsNote;
    }

    public String getTitleNote() {
        return titleNote;
    }

    public void setTitleNote(String titleNote) {
        this.titleNote = titleNote;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public Date getUpdatedDateNote() {
        return updatedDateNote;
    }

    public void setUpdatedDateNote(Date updatedDateNote) {
        this.updatedDateNote = updatedDateNote;
    }

    public static final Creator<Note> CREATOR = new Creator<Note>() {
        @Override
        public Note createFromParcel(Parcel in) {
            return new Note(in);
        }

        @Override
        public Note[] newArray(int size) {
            return new Note[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }
}
