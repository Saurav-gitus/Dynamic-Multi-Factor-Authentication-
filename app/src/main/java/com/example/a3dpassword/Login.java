package com.example.a3dpassword;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.*;
import android.os.Bundle;
import android.os.Handler;
import android.text.*;
import android.view.*;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.*;
import com.google.firebase.database.*;
import java.util.regex.*;
public class Login extends AppCompatActivity {
    TextInputLayout user_input, pass_input;
    TextInputEditText user, pass;
    ProgressBar progressBar;
    Button lgnbtn;
    TextView register, forgot_password;
    private FirebaseAuth auth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        int error_image = R.drawable.baseline_error_24;
        mDatabase = FirebaseDatabase.getInstance().getReference();
        auth = FirebaseAuth.getInstance();
        user_input = findViewById(R.id.UserInputLayout);
        pass_input = findViewById(R.id.PassInputLayout);
        user = findViewById(R.id.username);
        pass = findViewById(R.id.password);
        lgnbtn = findViewById(R.id.loginButton);
        progressBar = findViewById(R.id.progressBar);
        String r_email = getIntent().getStringExtra("email");
        user.setText(r_email);
        forgot_password = findViewById(R.id.forgotpassword);
        forgot_password.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Login.this, Forgot_password.class));
            }
        });
        register = findViewById(R.id.signupText);
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Login.this, Register.class));
                finish();
            }
        });
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            // User is already logged in, redirect to Home activity
            Intent intent = new Intent(Login.this, PIN.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        } else {
            user.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                }
                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    String userInput = charSequence.toString();
                    Pattern pattern = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
                    Matcher matcher = pattern.matcher(userInput);
                    boolean isEmailValid = matcher.find();
                    lgnbtn.setEnabled(isEmailValid);
                    if (!isEmailValid) {
                        user_input.setError("A valid email is required");
                    } else {
                        user_input.setError("");
                    }
                }
                @Override
                public void afterTextChanged(Editable editable) {
                }
            });
            lgnbtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String email = user.getText().toString();
                    String password = pass.getText().toString();
                    if (password.isEmpty()) {
                        pass_input.setError("Password can't be empty");
                    } else {
                        hideKeyboard();
                        progressBar.setVisibility(View.VISIBLE);
                        loginUser(email, password);
                    }
                }

                private void hideKeyboard() {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                }

                private void loginUser(String email, String password) {
                    auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(Login.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                FirebaseUser firebaseUser = auth.getCurrentUser();
                                retrievePhoneNumber(firebaseUser);
                            } else {
                                String errorMessage = task.getException().getMessage();
                                pass.setText("");
                                showcustomToast(error_image,errorMessage);
                            }
                            progressBar.setVisibility(View.GONE);
                        }

                        private void retrievePhoneNumber(FirebaseUser firebaseUser) {
                            if (firebaseUser != null) {
                                String userId = firebaseUser.getUid();
                                mDatabase.child("Registered Users").child(userId).child("Phoneno").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DataSnapshot> task) {
                                        if (task.isSuccessful()) {
                                            String transfer = "t";
                                            String phoneNumber = task.getResult().getValue(String.class);
                                            if(phoneNumber!=null){
//                                                Navigate to the ManageOTP activity and pass the phone number
                                                Intent intent = new Intent(Login.this, ManageOTP.class);
                                                intent.putExtra("login_useremail", user.getText().toString());
                                                intent.putExtra("login_password", pass.getText().toString());
                                                intent.putExtra("fromlogin", transfer);
                                                intent.putExtra("login_phone", phoneNumber);
                                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                startActivity(intent);
                                            }
                                            else{
                                                String message = "Invalid User";
                                                showcustomToast(error_image,message);
                                            }
                                        } else {
                                            String errorMessage = task.getException().getMessage();
                                            FirebaseAuth.getInstance().signOut();
                                            showcustomToast(error_image,errorMessage);
                                        }
                                        progressBar.setVisibility(View.GONE);
                                    }
                                });
                            }
                        }
                    });
                }
            });
        }
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
