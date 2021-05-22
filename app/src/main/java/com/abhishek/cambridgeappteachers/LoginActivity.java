package com.abhishek.cambridgeappteachers;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.abhishek.cambridgeappteachers.Models.Teacher;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {

    EditText etEmail, etPassword;
    Button btnLogin;
    TextView tvForgotPassword;

    ProgressBar progressBar;

    FirebaseAuth mAuth;
    FirebaseFirestore firestore_temporary;

    private static final int GOOD = 1;
    private static final int BAD = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etEmail = findViewById(R.id.etEmail_Login);
        etPassword = findViewById(R.id.etPassword_Login);
        btnLogin = findViewById(R.id.btnLogin_Login);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.INVISIBLE);

        mAuth = FirebaseAuth.getInstance();
        firestore_temporary = FirebaseFirestore.getInstance();


        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                progressBar.setVisibility(View.VISIBLE);
                String email = etEmail.getText().toString().trim();
                String password = etPassword.getText().toString().trim();

                if (email.isEmpty() || password.isEmpty())
                {
                    showToast("Enter all fields!!", BAD, Toast.LENGTH_SHORT);
                    progressBar.setVisibility(View.INVISIBLE);
                }
                else {

                    mAuth.signInWithEmailAndPassword(email, password)
                            .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                                @Override
                                public void onSuccess(AuthResult authResult) {

                                    /*if (mAuth.getCurrentUser().isEmailVerified())
                                    {*/

                                        if (true)//mAuth.getCurrentUser().getEmail().contains("@cambridge.edu.in") &&
                                                //!mAuth.getCurrentUser().getEmail().matches(".*\\d.*")) //Teacher email never contains a number
                                        {
                                            firestore_temporary.collection("TEACHER_LIST")
                                                    .document(mAuth.getCurrentUser().getUid())
                                                    .get()
                                                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                                        @Override
                                                        public void onSuccess(DocumentSnapshot documentSnapshot) {

                                                            Teacher teacher = documentSnapshot.toObject(Teacher.class);

                                                            if (teacher != null){

                                                                if (teacher.getBranch().size() == 0)
                                                                {
                                                                    showToast("New User!!", GOOD, Toast.LENGTH_SHORT);
                                                                    startActivity(new Intent(LoginActivity.this,
                                                                            EditTeacherActivity.class)
                                                                            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP));
                                                                    progressBar.setVisibility(View.INVISIBLE);
                                                                    finish();
                                                                }
                                                                else
                                                                {
                                                                    startActivity(new Intent(LoginActivity.this,
                                                                            MainTeacherActivity.class)
                                                                            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP));
                                                                    progressBar.setVisibility(View.INVISIBLE);
                                                                    finish();
                                                                }

                                                            }
                                                            else {

                                                                mAuth.signOut();
                                                                progressBar.setVisibility(View.INVISIBLE);
                                                                showToast("Error", BAD, Toast.LENGTH_SHORT);
                                                            }

                                                        }
                                                    })
                                                    .addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            showToast(e.getMessage(), BAD, Toast.LENGTH_SHORT);
                                                            progressBar.setVisibility(View.INVISIBLE);
                                                        }
                                                    });
                                        }
                                        else {
                                           showToast("Invalid email!!", BAD, Toast.LENGTH_SHORT);
                                        }

                                    /*}
                                    else{
                                        showToast("Please verify Email", BAD, Toast.LENGTH_SHORT)
                                        progressBar.setVisibility(View.INVISIBLE);
                                    }*/

                                }

                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    showToast(e.getMessage(), BAD, Toast.LENGTH_SHORT);
                                    progressBar.setVisibility(View.INVISIBLE);
                                }
                            });
                }
            }
        });


    }

    /**
     *Sends a link to the registered email to change the password.
     */
    public void forgotPassword(View view)
    {
        progressBar.setVisibility(View.VISIBLE);
        tvForgotPassword.setTextColor(ContextCompat.getColor(LoginActivity.this, R.color.colorGrey));

        etPassword.setVisibility(View.GONE);

        if (etEmail.getText().toString().isEmpty())
        {
            showToast("Please enter your email in the email field", BAD, Toast.LENGTH_SHORT);
            progressBar.setVisibility(View.INVISIBLE);
        }
        else {

            //Send Link to reset Password
            mAuth.sendPasswordResetEmail(etEmail.getText().toString().trim())
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(LoginActivity.this,
                                        "Please go to the email link we have sent you to reset your password!!", Toast.LENGTH_LONG).show();
                                progressBar.setVisibility(View.INVISIBLE);
                            }
                            else {
                                showToast("Failed!! Unregistered user!!", BAD, Toast.LENGTH_SHORT);
                                progressBar.setVisibility(View.INVISIBLE);
                            }
                        }
                    });

        }

    }

    /**
     *Redirects to <code>RegisterActivity</code> when clicked on <i>Not yet Registered</i>.
     */
    public void goToRegisterActivity(View view)
    {
        startActivity(new Intent(LoginActivity.this,
                com.abhishek.cambridgeappteachers.RegisterActivity.class));
        finish();
    }

    public void showToast(String text, int emoji, int duration){

        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.toast_layout, (ViewGroup) findViewById(R.id.toast_root));

        TextView toastText = layout.findViewById(R.id.toast_message);
        ImageView toastImage = layout.findViewById(R.id.toast_emoji);

        toastText.setText(text);
        if (emoji == GOOD){
            toastImage.setImageResource(R.drawable.ic_emoji_ok);
        }else {
            toastImage.setImageResource(R.drawable.ic_emoji_bad);
        }

        Toast toast = new Toast(getApplicationContext());
        toast.setDuration(duration);
        toast.setGravity(Gravity.BOTTOM, 0, 50);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(layout);
        toast.show();
    }

}