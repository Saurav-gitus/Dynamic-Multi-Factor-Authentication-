package com.example.a3dpassword;

import androidx.annotation.NonNull;
import android.annotation.SuppressLint;
import androidx.appcompat.app.*;
import android.content.*;
import android.os.Bundle;
import android.os.Handler;
import android.view.*;
import android.widget.*;
import com.google.firebase.auth.*;
import com.google.firebase.database.*;

public class Home extends AppCompatActivity {
    private FirebaseAuth auth;
    private DatabaseReference mDatabase;
    TextView show_user_name,show_user_email;
    Button logout;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        int error_image = R.drawable.baseline_error_24;
        auth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = auth.getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        show_user_name = findViewById(R.id.welcomtext);
        show_user_email = findViewById(R.id.show_usermail);
        logout = findViewById(R.id.Logout);
        logout.setVisibility(View.GONE);
        String userId = firebaseUser.getUid();
        mDatabase.child("Registered Users").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String username = dataSnapshot.child("Username").getValue(String.class);
                    String userEmail = dataSnapshot.child("Useremail").getValue(String.class);
                    if (username != null && userEmail != null) {
                        show_user_name.setText(username);
                        show_user_email.setText(userEmail);
                        logout.setVisibility(View.VISIBLE);
                    } else {
                        String message = "Failed to retrieve username or user email";
                        showcustomToast(error_image,message);
                    }
                } else {
                    String message = "User data not found";
                    showcustomToast(error_image,message);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle any errors that occur.
                String message = databaseError.getMessage();
                showcustomToast(error_image, message);
            }
        });
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logout_user_showAlertDialog();
            }
        });
    }
    private void logout_user_showAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(Home.this);
        builder.setTitle("Logout");
        builder.setMessage("Are you sure you want to logout?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                auth.signOut();
                Intent intent = new Intent(Home.this, Login.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
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