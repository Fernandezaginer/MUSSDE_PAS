package com.example.pas_proyecto;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

public interface GoogleSignInCallback {
    void onSuccess(GoogleSignInAccount account);
    void onError(Exception e);
}