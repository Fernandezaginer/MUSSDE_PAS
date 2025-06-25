package com.example.pas_proyecto;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;


public class MainActivity extends AppCompatActivity {

    private Button button;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        if(!Utilities.checkAndRequestPermissions(this)){
            Toast.makeText(MainActivity.this, "Permissions not granted", Toast.LENGTH_LONG).show();
            this.finishActivity(-1);
        }

        button = findViewById(R.id.button_log_in);

        button.setOnClickListener(v -> {
            Firebase.signIn(MainActivity.this, new GoogleSignInCallback() {
                @Override
                public void onSuccess(GoogleSignInAccount account) {
                    if(Firebase.get_username().equals("")){
                        Toast.makeText(MainActivity.this, Firebase.get_username(), Toast.LENGTH_LONG).show();
                        Firebase.set_username(account.getEmail());
                        Firebase.registerUserIfNew(account.getEmail());
                        open_main_screen();
                    }
                    else {
                        open_main_screen();
                    }
                }
                @Override
                public void onError(Exception e) {
                    Toast.makeText(MainActivity.this, "LogIN ERROR", Toast.LENGTH_LONG).show();
                }
            });
        });
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Firebase.handleActivityResult(requestCode, resultCode, data, this);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (!Utilities.handlePermissionResult(this, requestCode, grantResults)) {
            Toast.makeText(MainActivity.this, "Permissions not granted", Toast.LENGTH_LONG).show();
            this.finishActivity(-1);
        }
    }


    private void open_main_screen(){
        Intent intent = new Intent(MainActivity.this, MainScreen.class);
        startActivity(intent);
    }


}

