package com.mohamed_alami.multilimnote.classes;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;
import java.util.Date;
import java.util.Objects;

public class Utilisateur implements Parcelable{

    private Date signedInUtilisateur;
    private Date createdDateUtilisateur;
    private String idUtilisateur; // uID utilisateur
    private String providerUtilisateur; // Facebook // Email // Google+
    private String fullNameUtilisateur; // Nom complet
    private String emailUtilisateur; // Email

    public static DatabaseReference getDatabaseReference() {
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference(); // Racine de la base de donnée
        DatabaseReference utilisateursRef = rootRef.child("Utilisateurs"); // Noeud utilisateurs
        return utilisateursRef;
    }

    public static FirebaseUser getCurrentUser() {
        return FirebaseAuth.getInstance().getCurrentUser();
    }

    public static boolean Connected(){
        return (getCurrentUser() != null);
    }

    @Override
    public int hashCode() {
        return Objects.hash(createdDateUtilisateur,emailUtilisateur,fullNameUtilisateur,idUtilisateur,providerUtilisateur,signedInUtilisateur);
    }

    @Override
    public boolean equals(Object obj) {
        return getEmailUtilisateur().equals(((Utilisateur)obj).getEmailUtilisateur());
    }

    public static Utilisateur getInstance() {
        boolean isRegistered = false;
        if (Connected()) {
            Utilisateur u = new Utilisateur(getCurrentUser().getUid(),
                    getCurrentUser().getProviderId(),
                    new Date(), // Date de création
                    new Date(), // Date de derniere connexion
                    getCurrentUser().getDisplayName(),
                    getCurrentUser().getEmail());

            getDatabaseReference().child("Utilisateur-"+u.idUtilisateur).setValue(u);

            return u;
        } else
            return null;
    }

    // TODO
    public static String getNameById(String id){
        return "{Utilisateur}";
    }

    public Utilisateur() {
        createdDateUtilisateur = new Date();
        signedInUtilisateur = new Date();
    }

    public Utilisateur(String idUtilisateur, String providerUtilisateur, Date createdDateUtilisateur, Date signedInUtilisateur, String fullNameUtilisateur, String emailUtilisateur) {
        this.idUtilisateur = idUtilisateur;
        this.providerUtilisateur = providerUtilisateur;
        this.createdDateUtilisateur = createdDateUtilisateur;
        this.signedInUtilisateur = signedInUtilisateur;
        this.fullNameUtilisateur = fullNameUtilisateur;
        this.emailUtilisateur = emailUtilisateur;
    }

    public Utilisateur(Parcel in) {
        idUtilisateur = in.readString();
        providerUtilisateur = in.readString();
        createdDateUtilisateur = new Date(in.readLong());
        signedInUtilisateur = new Date(in.readLong());
        fullNameUtilisateur = in.readString();
        emailUtilisateur = in.readString();
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(idUtilisateur);
        parcel.writeString(providerUtilisateur);
        parcel.writeLong(createdDateUtilisateur.getTime());
        parcel.writeLong(signedInUtilisateur.getTime());
        parcel.writeString(fullNameUtilisateur);
        parcel.writeString(emailUtilisateur);
    }

    // ---------------------------------------------------------- //

    public String getEmailUtilisateur() {
        return emailUtilisateur;
    }

    public void setEmailUtilisateur(String emailUtilisateur) {
        this.emailUtilisateur = emailUtilisateur;
    }

    public String getIdUtilisateur() {
        return idUtilisateur;
    }

    public void setIdUtilisateur(String idUtilisateur) {
        this.idUtilisateur = idUtilisateur;
    }

    public String getProviderUtilisateur() {
        return providerUtilisateur;
    }

    public void setProviderUtilisateur(String providerUtilisateur) {
        this.providerUtilisateur = providerUtilisateur;
    }

    public Date getCreatedDateUtilisateur() {
        return createdDateUtilisateur;
    }

    public void setCreatedDateUtilisateur(Date createdDateUtilisateur) {
        this.createdDateUtilisateur = createdDateUtilisateur;
    }

    public Date getSignedInUtilisateur() {
        return signedInUtilisateur;
    }

    public void setSignedInUtilisateur(Date signedInUtilisateur) {
        this.signedInUtilisateur = signedInUtilisateur;
    }

    public String getFullNameUtilisateur() {
        return fullNameUtilisateur;
    }

    public void setFullNameUtilisateur(String fullNameUtilisateur) {
        this.fullNameUtilisateur = fullNameUtilisateur;
    }


    // ------------------------------------------------------- //

    public static final Creator<Utilisateur> CREATOR = new Creator<Utilisateur>() {
        @Override
        public Utilisateur createFromParcel(Parcel in) {
            return new Utilisateur(in);
        }

        @Override
        public Utilisateur[] newArray(int size) {
            return new Utilisateur[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }
}
