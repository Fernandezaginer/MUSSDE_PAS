package com.example.pas_proyecto;

import androidx.annotation.NonNull;

import android.app.Activity;
import android.content.Intent;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;






public class Firebase {


    static String ID_TOKEN = "305571595398-cnv3s0aubjjbh9e5vd35h41g5el688du.apps.googleusercontent.com";

    private static String username = new String("");
    private static final int RC_SIGN_IN = 9001;
    private static FirebaseAuth mAuth;
    private static GoogleSignInClient mGoogleSignInClient;
    private static GoogleSignInCallback callback;

    public static void set_username(String user){
        username = user;
    }

    public static String get_username(){
        return username;
    }


    public static void signIn(Activity activity, GoogleSignInCallback cb) {
        mAuth = FirebaseAuth.getInstance();
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(ID_TOKEN)
            .requestEmail()
            .build();
        mGoogleSignInClient = GoogleSignIn.getClient(activity, gso);
        callback = cb;
        activity.startActivityForResult(mGoogleSignInClient.getSignInIntent(), RC_SIGN_IN);
    }



    private void signOut(Activity activity) {
        mAuth.signOut();
        mGoogleSignInClient.signOut().addOnCompleteListener(activity,
            new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    Toast.makeText(activity, "Signed out", Toast.LENGTH_SHORT).show();
                }
            });
    }


    public static void handleActivityResult(int requestCode, int resultCode, Intent data, Activity activity) {
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                set_username(account.getEmail());
                if (callback != null) {
                    callback.onSuccess(account);
                }
            } catch (ApiException e) {
                if (callback != null) {
                    callback.onError(e);
                }
            }
        }
    }







    // ------------- REAL TIME DATABASE ----------------------

    static String DB_URI = "https://pas-proyecto-default-rtdb.europe-west1.firebasedatabase.app/";

    static String DB_NAME = "USERS";


    public static void registerUserIfNew(String username) {
        DatabaseReference usersRef = FirebaseDatabase.getInstance(DB_URI)
                .getReference(DB_NAME)
                .child("users");

        String safeUsername = username.replace(".", "_").replace("@", "_at_");

        usersRef.child(safeUsername).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    Map<String, Object> userData = new HashMap<>();
                    userData.put("email", username);
                    userData.put("lost_objects", 0);

                    usersRef.child(safeUsername).setValue(userData)
                            .addOnSuccessListener(aVoid -> System.out.println("User registered: " + username))
                            .addOnFailureListener(e -> System.err.println("Failed to register: " + e.getMessage()));
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
            }
        });
    }


    public static void incrementLostObjects(String username) {
        DatabaseReference usersRef = FirebaseDatabase.getInstance(DB_URI).getReference(DB_NAME).child("users");

        String safeUsername = username.replace(".", "_").replace("@", "_at_");

        DatabaseReference userRef = usersRef.child(safeUsername).child("lost_objects");

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                Long currentCount = snapshot.getValue(Long.class);
                long newCount = (currentCount != null ? currentCount : 0) + 1;
                userRef.setValue(newCount);
            }

            @Override
            public void onCancelled(DatabaseError error) {
            }
        });
    }


    public interface UsersCallback {
        void onResult(Map<String, Long> userData);
    }

    public static void getAllUsersAndLostObjects(UsersCallback callback) {
        DatabaseReference usersRef = FirebaseDatabase.getInstance(DB_URI).getReference(DB_NAME).child("users");

        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                Map<String, Long> result = new HashMap<>();
                for (DataSnapshot userSnap : snapshot.getChildren()) {
                    String username = userSnap.child("email").getValue(String.class);
                    Long lostObjects = userSnap.child("lost_objects").getValue(Long.class);
                    result.put(username, lostObjects != null ? lostObjects : 0L);
                }
                callback.onResult(result);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                callback.onResult(new HashMap<>());
            }
        });
    }
}

