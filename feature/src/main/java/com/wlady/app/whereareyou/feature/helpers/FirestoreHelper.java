package com.wlady.app.whereareyou.feature.helpers;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;
import com.wlady.app.whereareyou.feature.App;
import com.wlady.app.whereareyou.feature.models.IDeviceModel;
import com.wlady.app.whereareyou.feature.models.IUserModel;
import com.wlady.app.whereareyou.feature.models.InviteModel;

public class FirestoreHelper {

    private static String TAG = "FirestoreHelper";

    public static void readUserDocument(String uId, @NonNull OnCompleteListener<DocumentSnapshot> callBack) {
        FirebaseFirestore.getInstance()
                .collection("users")
                .document(uId)
                .get()
                .addOnCompleteListener(callBack)
                .addOnFailureListener(e1 -> {
                    Log.d(TAG, e1.getMessage());
                });
    }

    public static void updateUser(IUserModel user) {
        FirebaseFirestore.getInstance()
                .collection("users")
                .document(user.getuId())
                .update(user.toMap())
                .addOnFailureListener(e1 -> {
                    Log.d(TAG, e1.getMessage());
                });
    }

    public static void addUser(IUserModel user) {
        FirebaseFirestore.getInstance()
                .collection("users")
                .document(user.getuId())
                .set(user.toMap())
                .addOnFailureListener(e1 -> {
                    Log.d(TAG, e1.getMessage());
                });
    }

    public static void deleteUser(IUserModel user) {
        FirebaseFirestore.getInstance()
                .collection("users")
                .document(user.getuId())
                .delete()
                .addOnFailureListener(e1 -> {
                    Log.d(TAG, e1.getMessage());
                });
    }

    public static void readInviteDocument(String uId, @NonNull OnCompleteListener<QuerySnapshot> callBack) {
        FirebaseFirestore.getInstance()
                .collection("invites")
                .document(uId)
                .collection("active")
                .get()
                .addOnCompleteListener(callBack);
    }

    public static void addInvite(InviteModel inviteModel, @NonNull OnSuccessListener<DocumentReference> callBack) {
        FirebaseFirestore.getInstance()
                .collection("invites")
                .document(inviteModel.from)
                .collection("active")
                .add(inviteModel.toMap())
                .addOnSuccessListener(callBack)
                .addOnFailureListener(e1 -> {
                    Log.d(TAG, e1.getMessage());
                });
    }

    public static void deleteInvite(String uId, String docId) {
        FirebaseFirestore.getInstance()
                .collection("invites")
                .document(uId)
                .collection("active")
                .document(docId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                })
                .addOnFailureListener(e1 -> {
                    Log.d(TAG, e1.getMessage());
                });
    }

    public static void updateContacts(IUserModel user, @NonNull OnSuccessListener<Void> callback) {
        FirebaseFirestore.getInstance()
                .collection("users")
                .document(user.getuId())
                .update("contacts", user.getContacts())
                .addOnSuccessListener(callback)
                .addOnFailureListener(e1 -> {
                    Log.d(TAG, e1.getMessage());
                });
    }

    public static void updateBlacklist(IUserModel user, @NonNull OnSuccessListener<Void> callback) {
        FirebaseFirestore.getInstance()
                .collection("users")
                .document(user.getuId())
                .update("blacklist", user.getBlacklist())
                .addOnSuccessListener(callback)
                .addOnFailureListener(e1 -> {
                    Log.d(TAG, e1.getMessage());
                });
    }

    public static void updateLocation(IUserModel user, @NonNull OnSuccessListener<Void> callback) {
        FirebaseFirestore.getInstance()
                .collection("users")
                .document(user.getuId())
                .update("location", user.getLocation().toMap())
                .addOnSuccessListener(callback)
                .addOnFailureListener(e1 -> {
                    Log.d(TAG, e1.getMessage());
                });
    }

    public static void updateInvites(String fromId, String inviteId, String toId) {
        FirebaseFirestore.getInstance()
                .collection("invites")
                .document(fromId)
                .collection("active")
                .document(inviteId)
                .update("to", toId)
                .addOnSuccessListener(aVoid -> {
                })
                .addOnFailureListener(e1 -> {
                    Log.d(TAG, e1.getMessage());
                });
    }

    public static ListenerRegistration invitesListener(String uId, @NonNull EventListener<QuerySnapshot> callback) {
        return FirebaseFirestore.getInstance()
                .collection("invites")
                .document(uId)
                .collection("active")
                .addSnapshotListener(callback);
    }

    public static ListenerRegistration userListener(String uId, @NonNull EventListener<DocumentSnapshot> callback) {
        return FirebaseFirestore.getInstance()
                .collection("users")
                .document(uId)
                .addSnapshotListener(callback);
    }

    public static void saveDevice(String uId, IDeviceModel deviceModel) {
        FirebaseFirestore.getInstance()
                .collection("users")
                .document(uId)
                .update("device", deviceModel.toMap())
                .addOnFailureListener(e1 -> {
                    Log.d(TAG, e1.getMessage());
                });
    }
}
