package com.example.a3dpassword;

import android.annotation.SuppressLint;
import android.content.*;
import android.os.Bundle;
import android.os.Handler;
import android.text.*;
import android.util.Log;
import android.view.*;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.*;
import com.google.android.gms.tasks.*;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.*;
import com.google.firebase.database.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
public class ManageOTP extends AppCompatActivity {
    private static final String TAG = "ManageOTP";
    private EditText otpField;
    private Button verifyButton;
    private LinearLayout resendOtpLayout;
    private ProgressBar progressBar;
    private TextView resendOtpTextView, textResendOtp;
    private String verificationId, enteredOTP, username, userEmail, password;
    private FirebaseAuth auth;
    private PhoneAuthProvider.ForceResendingToken resendToken;
    boolean fromloginpage=false, isloginsucessfully=false;
    String otp_send_phoneno;
    String login_useremail, login_password;
    int image = R.drawable.baseline_error_24;
    private int otpAttempts = 0;
    private static final int MAX_OTP_ATTEMPTS = 5; // Set the maximum number of attempts
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_otp);

        progressBar = findViewById(R.id.progressBar);
        otpField = findViewById(R.id.otp_textfield);
        otpField.setEnabled(false);
        resendOtpTextView = findViewById(R.id.resendotpTextview);
        verifyButton = findViewById(R.id.verifyButton);
        resendOtpLayout = findViewById(R.id.resendotplayout);
        textResendOtp = findViewById(R.id.textresendotp);

        auth = FirebaseAuth.getInstance();
        TextView phoneNoTextView = findViewById(R.id.phoneno);
        String isfromlogin = getIntent().getStringExtra("fromlogin");
        if(isfromlogin!=null){
            fromloginpage = true;
            otp_send_phoneno = getIntent().getStringExtra("login_phone");
            login_useremail = getIntent().getStringExtra("login_useremail");
            login_password = getIntent().getStringExtra("login_password");
            String formattedPhoneNo = "+91 xxxxxxx" + otp_send_phoneno.substring(otp_send_phoneno.length() - 3);
            phoneNoTextView.setText(formattedPhoneNo);
        }
        else{
            fromloginpage = false;
            username = getIntent().getStringExtra("username");
            userEmail = getIntent().getStringExtra("useremail");
            password = getIntent().getStringExtra("password");
            otp_send_phoneno = getIntent().getStringExtra("phone");
            if (otp_send_phoneno != null && otp_send_phoneno.length() >= 5) {
                String formattedPhoneNo = "+91 xxxxxxx" + otp_send_phoneno.substring(otp_send_phoneno.length() - 3);
                phoneNoTextView.setText(formattedPhoneNo);
            }
        }

        findViewById(R.id.clear).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                otpField.setText("");
                otpField.requestFocus();
            }
        });
        progressBar.setVisibility(View.VISIBLE);
        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(auth)
                .setPhoneNumber("+91" + otp_send_phoneno)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(this)
                .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    @Override
                    public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                        startResendTimer();
                        if(fromloginpage){
                            String message = "Login Successfully";
                            int image = R.drawable.baseline_login_24;
                            showcustomToast(image,message);
                            Intent intent = new Intent(ManageOTP.this, PIN.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        }else{
                            registerUser(username, userEmail, otp_send_phoneno, password);
                        }
                    }
                    @Override
                    public void onVerificationFailed(@NonNull FirebaseException e) {
                        if (e instanceof FirebaseAuthInvalidCredentialsException) {
                            Toast.makeText(ManageOTP.this, "Invalid request", Toast.LENGTH_SHORT).show();
                        } else if (e instanceof FirebaseTooManyRequestsException) {
                            Toast.makeText(ManageOTP.this, "The SMS quota for the project has been exceeded", Toast.LENGTH_SHORT).show();
                        } else if (e instanceof FirebaseAuthMissingActivityForRecaptchaException) {
                            Toast.makeText(ManageOTP.this, "reCAPTCHA verification attempted with null Activity", Toast.LENGTH_SHORT).show();
                        }
                        progressBar.setVisibility(View.GONE);
                        verifyButton.setEnabled(true);
                        otpAttempts = 0;
                    }
                    @Override
                    public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                        super.onCodeSent(s, forceResendingToken);
                        progressBar.setVisibility(View.GONE);
                        int image = R.drawable.baseline_otp_24;
                        String errormessage = "OTP sent";
                        showcustomToast(image,errormessage);
                        startResendTimer();
                        otpField.setEnabled(true);
                        otpField.requestFocus();
                        showKeyboard(otpField);
                        verificationId = s;
                        resendToken = forceResendingToken;
                    }
                })
                .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
        verifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                {
                    verifyPhoneNumber();
                }
            }
        });
        textResendOtp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resendVerificationCode(otp_send_phoneno);
            }
        });
        setupOtpField();
    }

    private void setupOtpField() {
        final EditText[] otpFields = new EditText[]{
                otpField
        };

        otpField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }
            @Override
            public void onTextChanged(CharSequence s, int i, int i1, int i2) {
            }
            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.length() == 6) {
                    hideKeyboard();
                    otpField.setEnabled(false);
                    findViewById(R.id.clear).setEnabled(false);
                    resendOtpLayout.setEnabled(false);
                    verifyPhoneNumber();
                }
            }
        });
    }
    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
    }
    private void showKeyboard(EditText editText) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
    }

    private void verifyPhoneNumber() {
        otpAttempts++;
        progressBar.setVisibility(View.VISIBLE);
        verifyButton.setEnabled(false);

        enteredOTP = otpField.getText().toString();

        if (!enteredOTP.trim().isEmpty()) {
            PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, enteredOTP);
            auth.signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    if (task.isSuccessful()) {
                        user.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(fromloginpage){
                                    auth.signInWithEmailAndPassword(login_useremail, login_password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                        @Override
                                        public void onComplete(@NonNull Task<AuthResult> task) {
                                            isloginsucessfully=true;
                                            if(auth.getCurrentUser().isEmailVerified()){
                                                Intent intent = new Intent(ManageOTP.this, PIN.class);
                                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                startActivity(intent);
                                                finish();
                                            }
                                            else{
                                                auth.getCurrentUser().sendEmailVerification();
                                                showAlertDialog();
                                                progressBar.setVisibility(View.GONE);
                                                verifyButton.setEnabled(true);
                                            }
                                        }
                                    });
                                }else{
                                    registerUser(username, userEmail, otp_send_phoneno, password);
                                }
                            }
                        });
                    } else {
                        String errormessage = "Invalid OTP";
                        if (otpAttempts <= MAX_OTP_ATTEMPTS){
                            showcustomToast(image,errormessage);
                        }
                        findViewById(R.id.clear).setEnabled(true);
                        otpField.setEnabled(true);
                        otpField.setText("");
                        otpField.requestFocus();
                        showKeyboard(otpField);
                        progressBar.setVisibility(View.GONE);
                        verifyButton.setEnabled(true);
                    }
                }
            });
        } else {
            String errormessage = "Please enter OTP";
            showcustomToast(image,errormessage);
            progressBar.setVisibility(View.GONE);
            verifyButton.setEnabled(true);
        }
        if (otpAttempts >= MAX_OTP_ATTEMPTS) {
            otpAttempts = 0; // Reset attempts
            showcustomToast(image,"Resending OTP");
            resendVerificationCode(otp_send_phoneno);
        }
    }

    private void showAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(ManageOTP.this);
        builder.setTitle("Email Not Verified");
        builder.setMessage("Please verify your email now. You can not login without email verification.");
        builder.setPositiveButton("Continue", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_APP_EMAIL);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                auth.signOut();
                Intent intent = new Intent(ManageOTP.this, Login.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
                dialog.dismiss();
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();
    }

    private void resendVerificationCode(String phoneNo) {
        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(auth)
                .setPhoneNumber("+91" + phoneNo)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(this)
                .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    @Override
                    public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                        String errormessage = "OTP Sent";
                        int image = R.drawable.baseline_otp_24;
                        showcustomToast(image,errormessage);
                        startResendTimer();
                    }
                    @Override
                    public void onVerificationFailed(@NonNull FirebaseException e) {
                        if (e instanceof FirebaseAuthInvalidCredentialsException) {
                            String errormessage = "Invalid request";
                            showcustomToast(image,errormessage);
                        } else if (e instanceof FirebaseTooManyRequestsException) {
                            String errormessage = "The SMS quota for the project has been exceeded";
                            showcustomToast(image,errormessage);
                        } else if (e instanceof FirebaseAuthMissingActivityForRecaptchaException) {
                            String errormessage = "reCAPTCHA verification attempted with null Activity";
                            showcustomToast(image,errormessage);
                        }
                        progressBar.setVisibility(View.GONE);
                        verifyButton.setEnabled(true);
                    }

                    @Override
                    public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                        String errormessage = "Send OTP";
                        int image = R.drawable.baseline_otp_24;
                        showcustomToast(image,errormessage);
                        super.onCodeSent(s, forceResendingToken);
                        verificationId = s;
                        resendToken = forceResendingToken;
                        startResendTimer();
                    }
                })
                .setForceResendingToken(resendToken)
                .build();

        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private void registerUser(String username, String useremail, String phoneNo, String password) {
        auth.createUserWithEmailAndPassword(useremail, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    FirebaseUser firebaseUser = auth.getCurrentUser();

                    ReadWriteUserDetails writeUserDetails = new ReadWriteUserDetails(username, phoneNo, useremail);
                    DatabaseReference referenceProfile = FirebaseDatabase.getInstance().getReference("Registered Users");
                    referenceProfile.child(firebaseUser.getUid()).setValue(writeUserDetails).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            if (task.isSuccessful()) {
                                firebaseUser.sendEmailVerification();
                                String errormessage = "Please Verify your Email";
                                showcustomToast(image,errormessage);
                                Intent intent = new Intent(ManageOTP.this, PIN.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                finish();
                            } else {
                                String errormessage = "User Registration Failed, Please try again";
                                showcustomToast(image,errormessage);
                                progressBar.setVisibility(View.GONE);
                                verifyButton.setEnabled(true);
                            }
                        }
                    });
                } else {
                    String errormessage = task.getException().getMessage();
                    showcustomToast(image,errormessage);
                    progressBar.setVisibility(View.GONE);
                    verifyButton.setEnabled(true);
                }
            }
        });
    }
    private void startResendTimer() {
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            long timeoutSeconds = 30L;

            @Override
            public void run() {
                timeoutSeconds--;

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        resendOtpLayout.setVisibility(View.GONE);
                        resendOtpTextView.setText("Resend OTP in " + timeoutSeconds + " seconds");

                        if (timeoutSeconds <= 0) {
                            timer.cancel();
                            resendOtpLayout.setVisibility(View.VISIBLE);
                            resendOtpTextView.setText("");
                        }
                    }
                });
            }
        }, 0, 1000);
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
    @Override
    protected void onStop() {
        super.onStop();
        if(auth.getCurrentUser() !=null && isloginsucessfully==false){
            auth.signOut();
        }
    }
}
