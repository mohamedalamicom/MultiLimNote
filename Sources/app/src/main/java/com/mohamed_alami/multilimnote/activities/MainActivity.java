package com.mohamed_alami.multilimnote.activities;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.firebase.ui.auth.ResultCodes;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;
import com.mohamed_alami.multilimnote.R;
import com.mohamed_alami.multilimnote.classes.Utilisateur;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    // Elements de cette activité
    private TextView loggedTxt;
    private Button logOutBtn;
    private Button userBtn;

    // Identifiant de resultat de l'ActivityResult
    private static final int ID_SIGN = 99;

    // Utilisateur
    Utilisateur utilisateur;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Elements de l'activité
        loggedTxt = findViewById(R.id.loggedtextView);
        userBtn = findViewById(R.id.userBtn);
        logOutBtn = findViewById(R.id.logoutBtn);

        // Demande de connexion
        connect();
    }

    public void connect() {
        // Si l'utilisateur n'est pas connecté
        if(!Utilisateur.Connected()) {
            // Liste des providers
            List<AuthUI.IdpConfig> providers = Arrays.asList(
                    new AuthUI.IdpConfig.Builder(AuthUI.FACEBOOK_PROVIDER).build(),
                    new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build(),
                    new AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build()
                    );
            // Lancement de l'activité de demande de connexion
            startActivityForResult(
                    AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            .setAvailableProviders(providers)
                            .setTheme(R.style.LoginTheme)
                            .setLogo(R.drawable.logo)
                            .build(),
                    ID_SIGN);
        } else
            afterConnect();
    }

    public void afterConnect() {
        // Si l'utilisateur est connecté
        if(Utilisateur.Connected()) {
            // Instancier l'Utilisateur
            utilisateur = Utilisateur.getInstance();

            // Passage à l'activité ListeDeNoteActivity
            goToListeDeNote();

            // Rendre visible les éléments cette activité
            logOutBtn.setVisibility(View.VISIBLE);
            loggedTxt.setVisibility(View.VISIBLE);
            userBtn.setVisibility(View.VISIBLE);
            userBtn.setText(utilisateur.getFullNameUtilisateur());

            // Action sur les boutons
            userBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    goToListeDeNote();
                }
            });
            logOutBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    disconnect();
                }
            });
        }
    }

    public void disconnect(){
        AuthUI.getInstance()
                .signOut(this )
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(MainActivity.this, "Vous vous êtes déconnecté", Toast.LENGTH_SHORT).show();

                        // Rendre invisible les éléments
                        logOutBtn.setVisibility(View.INVISIBLE);
                        loggedTxt.setVisibility(View.INVISIBLE);
                        userBtn.setVisibility(View.INVISIBLE);

                        // Redemander de se connecter
                        connect();
                    }
                });
    }

    protected void goToListeDeNote() {
        Intent listDeNoteIntent = new Intent(MainActivity.this, ListeDeNoteActivity.class);
        listDeNoteIntent.putExtra("utilisateur", utilisateur);
        startActivity(listDeNoteIntent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ID_SIGN) {
            if (resultCode == RESULT_OK) {
                afterConnect();
                Toast.makeText(this, "Connecté en tant que : "+utilisateur.getFullNameUtilisateur(), Toast.LENGTH_SHORT).show();
            } else {
                connect();
                Toast.makeText(this, "Connexion échouée. Merci de réessayer", Toast.LENGTH_SHORT).show();
            }

        }
    }
}
