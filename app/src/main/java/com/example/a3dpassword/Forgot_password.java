package com.example.a3dpassword;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.*;
import android.view.*;
import android.widget.*;
import com.google.android.gms.tasks.*;
import com.google.android.material.textfield.*;
import com.google.firebase.auth.FirebaseAuth;
import java.util.regex.*;

public class Forgot_password extends AppCompatActivity {
    TextInputLayout user_input;
    TextInputEditText email;
    ProgressBar progressBar;
    Button forgot_btn;
    String strEmail;
    FirebaseAuth auth;
    private boolean exitApp = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        user_input = findViewById(R.id.emailLayout);
        email = findViewById(R.id.useremail);
        progressBar = findViewById(R.id.progressBar);
        auth = FirebaseAuth.getInstance();
        forgot_btn = findViewById(R.id.forgotButton);
        forgot_btn.setEnabled(false);
        int error_image = R.drawable.baseline_error_24;

        email.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String userInput = charSequence.toString();
                Pattern pattern = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
                Matcher matcher = pattern.matcher(userInput);
                boolean isEmailValid = matcher.find();
                if (isEmailValid) {
                    user_input.setHelperText("Valid Email");
                    user_input.setError("");
                    forgot_btn.setEnabled(true);
                } else {
                    user_input.setError("Invalid Email");
                    forgot_btn.setEnabled(false);
                }
            }
            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        forgot_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                strEmail = email.getText().toString().trim();
                if (!TextUtils.isEmpty(strEmail)) {
                    resetPassword();
                    progressBar.setVisibility(View.VISIBLE);
                } else {
                    user_input.setError("Email field can't be empty");
                }
            }
            private void resetPassword() {
                auth.sendPasswordResetEmail(strEmail).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        int image = R.drawable.baseline_email_send;
                        String message="Reset Password link has been sent to your registered Email";
                        showcustomToast(image,message);
                        progressBar.setVisibility(View.GONE);
                        Intent intent = new Intent(Forgot_password.this, Login.class);
                        startActivity(intent);
                        finish();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        String message = e.getMessage();
                        showcustomToast(error_image,message);
                        progressBar.setVisibility(View.GONE);
                    }
                });
            }
        });
    }
    private void showcustomToast(int image, String toastmessage) {
        Toast toast = new Toast(getApplicationContext());
        View view = getLayoutInflater().inflate(R.layout.custom_toast, (ViewGroup) findViewById(R.id.customtoastview));
        toast.setView(view);
        TextView message = view.findViewById(R.id.toastmessage);
        ImageView imageView = view.findViewById(R.id.toastimage);
        message.setText(toastmessage);
        imageView.setImageResource(image);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.show();
    }
}
