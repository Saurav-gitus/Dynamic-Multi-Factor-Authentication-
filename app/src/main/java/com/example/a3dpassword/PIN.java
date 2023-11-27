package com.example.a3dpassword;

import androidx.annotation.NonNull;
import androidx.appcompat.app.*;
import android.annotation.SuppressLint;
import android.content.*;
import android.os.Bundle;
import android.os.Handler;
import android.text.*;
import android.view.*;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import com.google.android.gms.tasks.*;
import com.google.firebase.auth.*;
import com.google.firebase.database.*;

public class PIN extends AppCompatActivity {

    private EditText PIN_input1, PIN_input2, PIN_input3, PIN_input4;
    TextView pinedittext,atempt_left;
    private ProgressBar progressBar;
    private String PIN,re_enterPIN;
    private boolean re_enter_pin=false;
    private FirebaseAuth auth;
    private DatabaseReference mDatabase;
    boolean is_pin_reg =false;
    int wrong_pin_count=3;
    int error_image = R.drawable.baseline_error_24;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pin);

        progressBar = findViewById(R.id.progressBar);
        PIN_input1 = findViewById(R.id.pinInputField1);
        PIN_input2 = findViewById(R.id.pinInputField2);
        PIN_input3 = findViewById(R.id.pinInputField3);
        PIN_input4 = findViewById(R.id.pinInputField4);
        pinedittext = findViewById(R.id.Pin_edittext);
        atempt_left = findViewById(R.id.atemptleft);
        auth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        mDatabase.child("Registered Users").child(currentUser.getUid()).child("PIN").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (task.isSuccessful()) {
                    String pin = task.getResult().getValue(String.class);
                    if (pin != null) {
                        is_pin_reg = true;
                        atempt_left.setVisibility(View.VISIBLE);
                        pinedittext.setText("Enter PIN");
                    } else {
                        is_pin_reg = false;
                        atempt_left.setVisibility(View.GONE);
                        pinedittext.setText("Create PIN");
                    }
                }
            }
        }) ;
        PIN_input1.requestFocus();
        setupPinInputFields();

    }
    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
    }
    private void showKeyboard(EditText editText) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
    }
    private void setupPinInputFields() {
        final EditText[] pinFields = new EditText[]{
                PIN_input1, PIN_input2, PIN_input3, PIN_input4
        };

        for (int i = 0; i < pinFields.length; i++) {
            final int currentIndex = i;
            pinFields[i].addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                }

                @Override
                public void onTextChanged(CharSequence s, int i, int i1, int i2) {
                    // Check for backspace
                    if (i1 == 1 && i2 == 0 && currentIndex > 0) {
                        pinFields[currentIndex].setText(""); // Clear the current field
                        pinFields[currentIndex - 1].requestFocus(); // Move focus to the previous field
                    } else if (i1 == 0 && i2 == 1 && s.length() > 0 && currentIndex < pinFields.length - 1) {
                        pinFields[currentIndex + 1].requestFocus(); // Move focus to the next field
                    } else if (allFieldsFilled(pinFields)) {
                        hideKeyboard();
                        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                        if (is_pin_reg) {
                            retrievePIN(currentUser);
                            progressBar.setVisibility(View.VISIBLE);
                        } else {
                            // All PIN fields are filled, show the toast message
                            if(re_enter_pin){
                                re_enter_pin=false;
                                re_enterPIN = get_PIN();
                                if(PIN.equals(re_enterPIN)){
                                    progressBar.setVisibility(View.VISIBLE);
                                    savePINToFirebase(PIN);
                                }
                                else{
                                    String error_message = "Incorrect Pin ";
                                    showcustomToast(error_image,error_message);
                                    re_enter_pin = false;
                                    clearPIN_input();
                                    pinedittext.setText("Create PIN");
                                }
                            }
                            else{
                                re_enter_pin=true;
                                PIN = get_PIN();
                                progressBar.setVisibility(View.GONE);
                                pinedittext.setText("Re-enter PIN");
                                clearPIN_input();
                            }
                        }
                    }
                }

                @Override
                public void afterTextChanged(Editable editable) {
                    // Change background color when a number is entered
                    if (editable.length() > 0) {
                        pinFields[currentIndex].setBackgroundResource(R.drawable.filled_circle_background);
                    } else {
                        pinFields[currentIndex].setBackgroundResource(R.drawable.empty_circle_background);
                    }
                    PIN_input4.setBackgroundResource(R.drawable.empty_circle_background);

                }
            });

            // Add an OnKeyListener to handle backspace key press
            pinFields[i].setOnKeyListener(new View.OnKeyListener() {
                @Override
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_DEL) {
                        if (currentIndex > 0) {
                            pinFields[currentIndex].setText(""); // Clear the current field
                            pinFields[currentIndex - 1].setText(""); // Clear the previous field
                            pinFields[currentIndex - 1].requestFocus(); // Move focus to the previous field
                            return true;
                        }
                    }
                    return false;
                }
            });
        }
    }

    private boolean allFieldsFilled(EditText[] pinFields) {
        for (EditText field : pinFields) {
            if (field.getText().toString().isEmpty()) {
                return false;
            }
        }
        return true;
    }
    private String get_PIN() {
        return PIN_input1.getText().toString() +
                PIN_input2.getText().toString() +
                PIN_input3.getText().toString() +
                PIN_input4.getText().toString();
    }
    private void clearPIN_input() {
        PIN_input1.setText("");
        PIN_input2.setText("");
        PIN_input3.setText("");
        PIN_input4.setText("");
        PIN_input4.setBackgroundResource(R.drawable.empty_circle_background);
        PIN_input1.requestFocus();
        showKeyboard(PIN_input1);
    }
    private void retrievePIN(FirebaseUser firebaseUser) {
        if (firebaseUser != null) {
            String userId = firebaseUser.getUid();
            mDatabase.child("Registered Users").child(userId).child("PIN").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DataSnapshot> task) {
                    if (task.isSuccessful()) {
                        String pin = task.getResult().getValue(String.class);
                        String user_enter_pin=get_PIN();
                        if (user_enter_pin.equals(pin)) {
                            // Navigate to the ManageOTP activity and pass the phone number and PIN
                            String errormessage = "Login Successfully";
                            int image = R.drawable.baseline_login_24;
                            showcustomToast(image,errormessage);
                            Intent intent = new Intent(PIN.this, Home.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        } else {
                            if(wrong_pin_count>0) {
                                atempt_left.setText(wrong_pin_count + " attempt left");
                                wrong_pin_count--;
                            }else{
                                FirebaseAuth.getInstance().signOut();
                                showAlertDialog();
                            }
                            clearPIN_input();
                        }
                    } else {
                        String error_message = task.getException().getMessage();
                        showcustomToast(error_image,error_message);
                    }
                    progressBar.setVisibility(View.GONE);
                }
            });
        }
    }
    private void savePINToFirebase(String pin) {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            String userId = firebaseUser.getUid();
            DatabaseReference userRef = mDatabase.child("Registered Users").child(userId);
            userRef.child("PIN").setValue(pin).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Intent intent = new Intent(PIN.this, Home.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    } else {
                        String error_message = task.getException().getMessage();
                        showcustomToast(error_image,error_message);
                    }
                }
            });
        }
    }
    private void showAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(PIN.this);
        builder.setTitle("incorrect PIN attempts");
        builder.setMessage("You've reached the maximum number of incorrect PIN attempts.");
        builder.setPositiveButton("Continue", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(PIN.this, Login.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();

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
