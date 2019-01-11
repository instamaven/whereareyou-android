package com.wlady.app.whereareyou.feature.helpers;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class FilestorageHelper {

    private static String TAG = "FilestorageHelper";

    public static void deleteFile(
            String uId,
            String fileUri,
            @NonNull OnSuccessListener<Void> callBack
    ) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        String[] parts = fileUri.split("/");
        if (parts.length > 0) {
            String fileToDelete = "images/" + uId + "/" + parts[parts.length - 1];
            storageRef.child(fileToDelete)
                    .delete()
                    .addOnSuccessListener(callBack)
                    .addOnFailureListener(e -> {
                        Log.d(TAG, e.getMessage());
                    });
        }
    }

    public static void uploadFile(
            String uId,
            Uri fileUri,
            @NonNull OnSuccessListener<UploadTask.TaskSnapshot> callBack,
            @NonNull OnFailureListener onFailure
    ) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        String avatarChildName = "images/" + uId + "/" + fileUri.getLastPathSegment();
        StorageReference avatarRef = storageRef.child(avatarChildName);
        StorageMetadata metaData = new StorageMetadata.Builder()
                .setContentType("image/jpg")
                .build();
        avatarRef.putFile(fileUri, metaData)
                .addOnFailureListener(onFailure)
                .addOnSuccessListener(callBack);
    }
}
