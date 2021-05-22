package com.abhishek.cambridgeappteachers.Fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;

import com.abhishek.cambridgeappteachers.Adapters.SubjectsEnrolledAdapter;
import com.abhishek.cambridgeappteachers.EnrollForSubjectsActivity;
import com.abhishek.cambridgeappteachers.Models.Teacher;
import com.abhishek.cambridgeappteachers.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;


public class TeacherSubjectsFragment extends Fragment {

    ProgressBar progressBar;
    RecyclerView rvSubjectsEnrolled;
    SubjectsEnrolledAdapter subjectsEnrolledAdapter;

    private static int REQUEST_CODE_1289 = 1289;

    Button btnEnrollSubjects;

    Teacher teacher;
    FirebaseAuth mAuth;
    FirebaseFirestore firestore;

    public TeacherSubjectsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_teacher_subjects, container, false);

        progressBar = view.findViewById(R.id.progressBar);
        progressBar.setVisibility(View.INVISIBLE);

        rvSubjectsEnrolled = view.findViewById(R.id.rvSubjectsEnrolled);
        btnEnrollSubjects = view.findViewById(R.id.btnEnrollSubjects);

        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        rvSubjectsEnrolled.setHasFixedSize(true);
        rvSubjectsEnrolled.setLayoutManager(new LinearLayoutManager(getContext()));

        getTeacher();

        btnEnrollSubjects.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(new Intent(getContext(), com.abhishek.cambridgeappteachers.EnrollForSubjectsActivity.class), REQUEST_CODE_1289);
            }
        });

        return view;
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

                            subjectsEnrolledAdapter = new SubjectsEnrolledAdapter(getContext(), teacher.getSubjectsHandlingNames(), progressBar);
                            rvSubjectsEnrolled.setAdapter(subjectsEnrolledAdapter);
                            subjectsEnrolledAdapter.notifyDataSetChanged();
                            progressBar.setVisibility(View.INVISIBLE);
                        }
                    }
                });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        //super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_1289){
            if (resultCode == Activity.RESULT_CANCELED)
            {
                Log.d("\t\t","Wohoo Notifying Adapter!!");
//                subjectsEnrolledAdapter.notifyDataSetChanged();
                getTeacher();
            }
        }

    }



}