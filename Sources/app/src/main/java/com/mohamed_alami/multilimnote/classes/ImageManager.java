package com.mohamed_alami.multilimnote.classes;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.ImageView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.squareup.picasso.Picasso;

public class ImageManager {
    public static void loadImageIntoViewFromNote(final ImageView imageView, Note note){
        Log.d("DEBUG-IMG", "Checking if image is already there..");
        if(imageView.getDrawable() == null) {
            FirebaseStorage.getInstance().getReference()
                    .child("images/note-" + String.valueOf(note.getIdNote()) + ".jpg").getDownloadUrl()
                    .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                              @Override
                                              public void onSuccess(Uri uri) {
                                                  try {
                                                      Log.d("DEBUG-IMG", "Trying to pull " + uri.toString());
                                                      Picasso.with(imageView.getContext()).load(uri.toString()).into(imageView);
                                                  } catch (Exception e) {
                                                      e.printStackTrace();
                                                  }
                                              }
                                          }
                    ).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Picasso.with(imageView.getContext()).load(Note.defaultImage).into(imageView);
                    Log.d("DEBUG-IMG", "Failed to pull the image. Default image used instead!");
                }
            });
        }
        Log.d("DEBUG-IMG", "Finished setting image !");
    }
}
