package com.abhishek.cambridgeappteachers;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.abhishek.cambridgeappteachers.Adapters.EnrollForSubjectsAdapter;
import com.abhishek.cambridgeappteachers.Adapters.SubjectsEnrolledAdapter;
import com.abhishek.cambridgeappteachers.Models.Subjects;
import com.abhishek.cambridgeappteachers.Models.Teacher;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import javax.security.auth.Subject;

public class EnrollForSubjectsActivity extends AppCompatActivity {

    ProgressBar progressBar;
    RecyclerView rvEnrollForSubjects;
    EditText etSearchSubjects;
    EnrollForSubjectsAdapter enrollForSubjectsAdapter;

    List<String> semesters;
    List<Subjects> subjectsList;

    Teacher teacher;
    FirebaseAuth mAuth;
    FirebaseFirestore firestore;

    private static final int GOOD = 1;
    private static final int BAD = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enroll_subjects);

        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.INVISIBLE);

        etSearchSubjects = findViewById(R.id.etSearchForSubjects);

        rvEnrollForSubjects = findViewById(R.id.rvEnrollForSubjects);
        rvEnrollForSubjects.setHasFixedSize(true);
        rvEnrollForSubjects.setLayoutManager(new LinearLayoutManager(this));

        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        getTeacher();

        etSearchSubjects.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                filter(editable.toString());
            }
        });

    }

    private void filter(String text) {

        ArrayList<Subjects> filteredList = new ArrayList<>();

        for(Subjects sub : subjectsList){

            if (sub.getSubjectName().toLowerCase().contains(text.toLowerCase()) || sub.getSubjectId().toLowerCase().contains(text.toLowerCase())){
                filteredList.add(sub);
            }

        }

        enrollForSubjectsAdapter.filterList(filteredList);

    }

    private void getTeacher() {

        progressBar.setVisibility(View.VISIBLE);
        firestore.collection("TEACHER_LIST").document(mAuth.getCurrentUser().getUid())
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful())
                        {
                            DocumentSnapshot documentSnapshot = task.getResult();
                            teacher = documentSnapshot.toObject(Teacher.class);

                            semesters = new ArrayList<>();
                            subjectsList = new ArrayList<>();
                            Log.d("\t\t", "Inside Teacher");

                            getSemesters();

                        }
                    }
                });

    }

    private void getSemesters() {

        if (teacher.getBranch().size() > 0){
            for(final String branch : teacher.getBranch())
            {
                firestore.collection("DEPARTMENT").document(branch + "_branch").collection("CLASS")
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful())
                                {
                                    for (QueryDocumentSnapshot qds : task.getResult()){

                                        Log.d("\t\t", "Inside Semesters" + qds.getId());
                                        getSubjects(branch, qds.getId());
                                    }
                                }
                            }
                        });
            }
        }
        else {
            showToast("You are not handling any branch", BAD, Toast.LENGTH_SHORT);
            progressBar.setVisibility(View.INVISIBLE);
        }

    }

    private void getSubjects(String branch, String sem) {

        firestore.collection("DEPARTMENT").document(branch + "_branch")
                .collection("CLASS").document(sem)
                .collection("SUBJECTS")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful() && task != null)
                        {
                            for (QueryDocumentSnapshot qds : task.getResult()){

                                Log.d("\t\t", "Inside Subjects");

                                Subjects subject = qds.toObject(Subjects.class);
                                subjectsList.add(subject);

                                Log.d("\t\t", "Added subjects :: " + subject.getBranch() + subject.getSem() + subject.getSubjectName());
                            }

                            enrollForSubjectsAdapter = new EnrollForSubjectsAdapter(EnrollForSubjectsActivity.this,
                                    teacher.getBranch(), teacher, subjectsList, progressBar);
                            rvEnrollForSubjects.setAdapter(enrollForSubjectsAdapter);
                            enrollForSubjectsAdapter.notifyDataSetChanged();
                            progressBar.setVisibility(View.INVISIBLE);
                        }
                        else {
                            progressBar.setVisibility(View.INVISIBLE);
                            Log.d("\t\t", "Inside Subjects Failing!!" + task.getException());
                        }
                    }
                });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        Intent intent = new Intent();
        setResult(RESULT_CANCELED, intent);
        EnrollForSubjectsActivity.this.finish();

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