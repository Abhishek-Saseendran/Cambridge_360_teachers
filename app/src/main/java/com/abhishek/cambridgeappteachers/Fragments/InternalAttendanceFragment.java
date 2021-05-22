package com.abhishek.cambridgeappteachers.Fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.abhishek.cambridgeappteachers.Adapters.InternalAttendanceAdapter;
import com.abhishek.cambridgeappteachers.Adapters.SubjectsEnrolledAdapter;
import com.abhishek.cambridgeappteachers.Models.Teacher;
import com.abhishek.cambridgeappteachers.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;


public class InternalAttendanceFragment extends Fragment {

    ProgressBar progressBar;
    RecyclerView rvInternalsAttendance;
    InternalAttendanceAdapter internalAttendanceAdapter;

    Teacher teacher;
    FirebaseAuth mAuth;
    FirebaseFirestore firestore;


    public InternalAttendanceFragment() {
        // Required empty public constructor
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
       View view = inflater.inflate(R.layout.fragment_internal_attendance, container, false);

        progressBar = view.findViewById(R.id.progressBar);
        progressBar.setVisibility(View.INVISIBLE);

        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        rvInternalsAttendance = view.findViewById(R.id.rvInternalAttendance);
        rvInternalsAttendance.setHasFixedSize(true);
        rvInternalsAttendance.setLayoutManager(new LinearLayoutManager(getContext()));

        getTeacher();

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

                            internalAttendanceAdapter = new InternalAttendanceAdapter(getContext(), teacher.getSubjectsHandlingNames());
                            rvInternalsAttendance.setAdapter(internalAttendanceAdapter);
                            internalAttendanceAdapter.notifyDataSetChanged();
                            progressBar.setVisibility(View.INVISIBLE);
                        }
                    }
                });

    }




}