package com.example.phonenumberauthentication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseUser mCurrentUser;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    // UI Elements
    private EditText login_code_text;
    private EditText login_number_text;
    private Button login_btn;
    private ProgressBar login_progressBar;
    private TextView login_feedback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser();

        // Initialize UI Elements
        login_btn = findViewById(R.id.login_btn);
        login_code_text = findViewById(R.id.login_code_text);
        login_number_text = findViewById(R.id.login_number_text);
        login_progressBar = findViewById(R.id.login_progressBar);
        login_feedback = findViewById(R.id.login_feedback);

        login_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String country_code = login_code_text.getText().toString();
                String phone_number = login_number_text.getText().toString();
                String complete_phone_number = "+" + country_code + "" + phone_number;

                if (country_code.isEmpty() || phone_number.isEmpty()){
                    login_feedback.setText("Please Enter all The Values");
                    login_feedback.setVisibility(View.VISIBLE);
                }
                else {
                    login_progressBar.setVisibility(View.VISIBLE);
                    login_btn.setEnabled(false);
                    login_feedback.setVisibility(View.INVISIBLE);

                    PhoneAuthOptions options =
                            PhoneAuthOptions.newBuilder(mAuth)
                                    .setPhoneNumber(complete_phone_number)       // Phone number to verify
                                    .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                                    .setActivity(LoginActivity.this)                 // Activity (for callback binding)
                                    .setCallbacks(mCallbacks)          // OnVerificationStateChangedCallbacks
                                    .build();
                    PhoneAuthProvider.verifyPhoneNumber(options);
                }

            }
        });

        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                signInWithPhoneAuthCredential(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                login_feedback.setText("Verification Failed, please try again.");
                login_feedback.setVisibility(View.VISIBLE);
                login_progressBar.setVisibility(View.INVISIBLE);
                login_btn.setEnabled(true);
            }

            @Override
            public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(s, forceResendingToken);

                Intent optIntent = new Intent(LoginActivity.this, OtpActivity.class);
                optIntent.putExtra("AuthCredentials", s);
                startActivity(optIntent);
            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (mCurrentUser != null){
            sendUserToHome();
        }
    }

    private void sendUserToHome() {
        Intent homeIntent = new Intent(LoginActivity.this, MainActivity.class);
        homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(homeIntent);
        finish();
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            sendUserToHome();
                            // ...
                        } else {
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                // The verification code entered was invalid
                                login_feedback.setVisibility(View.VISIBLE);
                                login_feedback.setText("There was an error verifying OTP");
                            }
                        }
                        login_progressBar.setVisibility(View.INVISIBLE);
                        login_btn.setEnabled(true);
                    }
                });
    }
}