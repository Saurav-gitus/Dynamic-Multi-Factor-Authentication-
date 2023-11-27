package com.example.a3dpassword;

import androidx.appcompat.app.AppCompatActivity;
import android.content.*;
import android.os.Bundle;
import android.os.Handler;
import android.text.*;
import android.view.*;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import com.google.android.material.textfield.*;
import com.google.firebase.auth.FirebaseAuth;
import java.util.regex.*;

public class Register extends AppCompatActivity {
    TextInputLayout user_input, pass_input, email_input, phone_input;
    TextInputEditText username, pass, useremail, phoneno;
    ProgressBar progressBar;
    Button register_btn;
    TextView login_btn;
    private FirebaseAuth auth;
    boolean isUserValid = false, isUserEmailValid = false, isPasswordValid = false, isPhoneValid = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        auth = FirebaseAuth.getInstance();
        user_input = findViewById(R.id.UserNameLayout);
        email_input = findViewById(R.id.EmailInputLayout);
        phone_input = findViewById(R.id.UserphoneLayout);
        pass_input = findViewById(R.id.PassInputLayout);
        progressBar = findViewById(R.id.progressBar);
        username = findViewById(R.id.username);
        useremail = findViewById(R.id.email);
        phoneno = findViewById(R.id.phoneno);
        pass = findViewById(R.id.password);
        register_btn = findViewById(R.id.RegisterButton);
        login_btn = findViewById(R.id.signinText);
        login_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Register.this, Login.class));
                finish();
            }
        });
        username.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String usernameInput = charSequence.toString();
                int minLength = 5;
                int maxLength = 20;
                boolean isLengthValid = (usernameInput.length() >= minLength && usernameInput.length() <= maxLength);
                boolean isSpecialCharacterValid = !usernameInput.contains("@"); // Example: Check for the "@" symbol.
                if (isLengthValid && isSpecialCharacterValid) {
                    user_input.setError("");
                    isUserValid = true;
                } else {
                    // Combine error messages based on different validation rules
                    StringBuilder errorMessage = new StringBuilder();
                    if (!isLengthValid) {
                        errorMessage.append("Username must be between ").append(minLength).append(" and ").append(maxLength).append(" characters. ");
                        isUserValid = false;
                    }
                    if (!isSpecialCharacterValid) {
                        errorMessage.append("Username cannot contain '@' symbol.");
                        isUserValid = false;
                    }
                    user_input.setError(errorMessage.toString());
                    isUserValid = false;
                }
                checkInputValidity();
            }
            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
        useremail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String userInuput = charSequence.toString();
                Pattern pattern = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
                Matcher matcher = pattern.matcher(userInuput);
                boolean user_matcher = matcher.find();
                isUserEmailValid=user_matcher;
                if (user_matcher) {
                    email_input.setError("");
                } else {
                    email_input.setError("A valid email is required");
                    isUserEmailValid = false;
                }
                checkInputValidity();
            }
            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
        phoneno.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String phoneInput = charSequence.toString();
                // Remove any non-numeric characters
                String numericInput = phoneInput.replaceAll("[^0-9]", "");
                if (numericInput.length() == 10 && numericInput.matches("[6-9]\\d{9}")) {
                    phone_input.setHelperText("");
                    phone_input.setError("");
                    isPhoneValid = true; // Phone number is valid
                } else {
                    phone_input.setHelperText("Invalid Phone_no");
                    isPhoneValid = false;
                    // Provide a meaningful error message
                    if (numericInput.length() != 10) {
                        phone_input.setError("Phone number must have exactly 10 digits.");
                        isPhoneValid = false;
                    } else {
                        phone_input.setError("Only Indian Phone numbers allowed.");
                        isPhoneValid = false;
                    }
                    isPhoneValid = false; // Phone number is invalid
                }
                checkInputValidity();
            }
            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
        pass.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String passwordInput = charSequence.toString();
                Pattern uppercasePattern = Pattern.compile("[A-Z]");
                Pattern lowercasePattern = Pattern.compile("[a-z]");
                Pattern digitPattern = Pattern.compile("[0-9]");
                Pattern specialCharPattern = Pattern.compile("[^a-zA-Z0-9]");

                boolean hasUppercase = uppercasePattern.matcher(passwordInput).find();
                boolean hasLowercase = lowercasePattern.matcher(passwordInput).find();
                boolean hasDigit = digitPattern.matcher(passwordInput).find();
                boolean hasSpecialChar = specialCharPattern.matcher(passwordInput).find();

                // Check if the password is at least 8 characters long
                boolean isLengthValid = passwordInput.length() >= 8;

                isPasswordValid = hasUppercase && hasLowercase && hasDigit && hasSpecialChar && isLengthValid;

                // Check if the password contains the user's name
                String[] usernameInput = username.getText().toString().toLowerCase().split("\\s");//only first name
                String passwordLowerCase = passwordInput.toLowerCase();

                boolean containsName = passwordLowerCase.contains(usernameInput[0]);

                if (containsName) {
                    pass_input.setError("Password contains your name. Please choose a different password.");
                    isPasswordValid = false;
                } else if (isPasswordValid) {
                    pass_input.setHelperText("");
                    pass_input.setError("");
                } else if (!isPasswordValid) {
                    pass_input.setError("Password must include at least one uppercase letter, one lowercase letter, one number, and one special character, and be at least 8 characters long");
                    isPasswordValid = false;
                } else {
                    pass_input.setHelperText("Password must be at least 8 characters");
                    pass_input.setError("");
                    isPasswordValid = false;
                }
                checkInputValidity();
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
        register_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isUserValid && isUserEmailValid && isPhoneValid && isPasswordValid) {
                    String user = username.getText().toString();
                    String email = useremail.getText().toString();
                    String phone = phoneno.getText().toString();
                    String password = pass.getText().toString();

                    // Disable the register button and show a loading indicator
                    register_btn.setEnabled(false);
                    progressBar.setVisibility(View.VISIBLE);

                    // Start the ManageOTP activity with user data
                    Intent intent_manageOTP = new Intent(Register.this, ManageOTP.class);
                    intent_manageOTP.putExtra("username", user);
                    intent_manageOTP.putExtra("useremail", email);
                    intent_manageOTP.putExtra("phone", phone);
                    intent_manageOTP.putExtra("password", password);

                    startActivityForResult(intent_manageOTP,1);
                    progressBar.setVisibility(View.GONE);
                    register_btn.setEnabled(true);
                    // Hide the keyboard
                    hideKeyboard();
                } else {
                    int error_image = R.drawable.baseline_error_24;
                    String error_message = "Please fill in all required fields with valid data.";
                    showcustomToast(error_image,error_message);
                }
            }
        });
    }
    private void checkInputValidity() {
        boolean userValid = isUserValid;
        boolean useremailValid = isUserEmailValid;
        boolean phoneValid = isPhoneValid;
        boolean passValid = isPasswordValid;

        // Only enable the button when all fields are valid
        register_btn.setEnabled(userValid && useremailValid && phoneValid && passValid);
    }
    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
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
