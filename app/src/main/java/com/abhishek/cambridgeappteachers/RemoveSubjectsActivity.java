package com.abhishek.cambridgeappteachers;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

import com.abhishek.cambridgeappteachers.Adapters.DeleteSubjectsAdapter;
import com.abhishek.cambridgeappteachers.Adapters.EnrollForSubjectsAdapter;
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

public class RemoveSubjectsActivity extends AppCompatActivity {

    ProgressBar progressBar;
    EditText etSearchSubjects;
    RecyclerView rvDeleteSubjects;
    DeleteSubjectsAdapter deleteSubjectsAdapter;

    FirebaseAuth mAuth;
    FirebaseFirestore firestore;

    Teacher teacher;
    List<String> semesters;
    List<Subjects> subjectsList;

    private static final int GOOD = 1;
    private static final int BAD = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remove_subjects);

        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.INVISIBLE);

        etSearchSubjects = findViewById(R.id.etSearchForSubject_deleteSubject);
        rvDeleteSubjects = findViewById(R.id.rvDeleteSubjects);
        rvDeleteSubjects.setHasFixedSize(true);
        rvDeleteSubjects.setLayoutManager(new LinearLayoutManager(RemoveSubjectsActivity.this));

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

        deleteSubjectsAdapter.filterList(filteredList);

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

                            deleteSubjectsAdapter = new DeleteSubjectsAdapter(RemoveSubjectsActivity.this, subjectsList,
                                    progressBar);
                            rvDeleteSubjects.setAdapter(deleteSubjectsAdapter);
                            deleteSubjectsAdapter.notifyDataSetChanged();
                            progressBar.setVisibility(View.INVISIBLE);
                        }
                        else {
                            progressBar.setVisibility(View.INVISIBLE);
                            Log.d("\t\t", "Inside Subjects Failing!!" + task.getException());
                        }
                    }
                });
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        RemoveSubjectsActivity.this.finish();
    }
}