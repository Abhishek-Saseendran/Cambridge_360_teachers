package com.abhishek.cambridgeappteachers.Fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.abhishek.cambridgeappteachers.Adapters.EnrollForSubjectsAdapter;
import com.abhishek.cambridgeappteachers.Adapters.NoticeBoardAdapter;
import com.abhishek.cambridgeappteachers.AddNoticeActivity;
import com.abhishek.cambridgeappteachers.EnrollForSubjectsActivity;
import com.abhishek.cambridgeappteachers.Models.NoticeBoardItem;
import com.abhishek.cambridgeappteachers.Models.StudentSubjectEnrolled;
import com.abhishek.cambridgeappteachers.Models.Subjects;
import com.abhishek.cambridgeappteachers.Models.Teacher;
import com.abhishek.cambridgeappteachers.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;


public class NoticeBoardFragment extends Fragment {

    Button btnAddNotice;
    ProgressBar progressBar;
    EditText etSearch;

    FirebaseAuth mAuth;
    FirebaseFirestore firestore;

    RecyclerView rvNoticeBoard;
    NoticeBoardAdapter noticeBoardAdapter;
    List<NoticeBoardItem> noticeBoardItemList;

    Teacher teacher;
    List<String> semesters;

    private static final int NOTICE_REQUEST = 4321;

    private static final int GOOD = 1;
    private static final int BAD = -1;

    public NoticeBoardFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_notice_board, container, false);

        progressBar = view.findViewById(R.id.progressBar);
        progressBar.setVisibility(View.INVISIBLE);

        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        getTeacher();

        btnAddNotice = view.findViewById(R.id.btnAddNotice);
        etSearch = view.findViewById(R.id.etSearchForNotices);
        rvNoticeBoard = view.findViewById(R.id.rvNoticeBoard);

        rvNoticeBoard.setHasFixedSize(true);
        rvNoticeBoard.setLayoutManager(new LinearLayoutManager(getContext()));

        etSearch.addTextChangedListener(new TextWatcher() {
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

        btnAddNotice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(new Intent(getContext(), com.abhishek.cambridgeappteachers.AddNoticeActivity.class), NOTICE_REQUEST);
            }
        });

        return  view;

    }

    private void filter(String text) {

        ArrayList<NoticeBoardItem> filterList = new ArrayList<>();

        for (NoticeBoardItem item : noticeBoardItemList){
            if (item.getName().toLowerCase().contains(text.toLowerCase()) || item.getBranch().toLowerCase().contains(text.toLowerCase())
                    || item.getSem().toLowerCase().contains(text.toLowerCase()) || item.getAuthor().toLowerCase().contains(text.toLowerCase())
                    || item.getDescription().toLowerCase().contains(text.toLowerCase())){
                filterList.add(item);
            }

        }
        noticeBoardAdapter.filterList(filterList);

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
                            noticeBoardItemList = new ArrayList<>();

                            getSemesters();

                        }
                       else {
                           progressBar.setVisibility(View.INVISIBLE);
                           showToast("Error " + task.getException(), BAD, Toast.LENGTH_SHORT);
                        }
                    }
                });


    }

    private void getSemesters() {

        noticeBoardItemList.clear();
        progressBar.setVisibility(View.VISIBLE);
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
                                        getNotices(branch, qds.getId());
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

    private void getNotices(String branch, String sem) {

        firestore.collection("DEPARTMENT").document(branch + "_branch")
                .collection("CLASS").document(sem)
                .collection("NOTICE_BOARD")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful() && task != null)
                        {
                            for (QueryDocumentSnapshot qds : task.getResult()){

                                Log.d("\t\t", "Inside Notices");

                                NoticeBoardItem item = qds.toObject(NoticeBoardItem.class);
                                noticeBoardItemList.add(item);

                                Log.d("\t\t", "Added notices :: " + item.getBranch() + item.getSem() + item.getName());
                            }

                            noticeBoardAdapter = new NoticeBoardAdapter(getContext(), progressBar, noticeBoardItemList);

                            rvNoticeBoard.setAdapter(noticeBoardAdapter);
                            noticeBoardAdapter.notifyDataSetChanged();

                            progressBar.setVisibility(View.INVISIBLE);
                        }
                        else {
                            progressBar.setVisibility(View.INVISIBLE);
                            Log.d("\t\t", "Inside Notices Failing!!" + task.getException());
                        }
                    }
                });


    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        //super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == NOTICE_REQUEST){
            if (resultCode == Activity.RESULT_CANCELED)
            {
                Log.d("\t\t","Wohoo Notifying Adapter!!");
                getSemesters();
            }
        }


    }

    public void showToast(String text, int emoji, int duration){

        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.toast_layout, (ViewGroup) getActivity().findViewById(R.id.toast_root));

        TextView toastText = layout.findViewById(R.id.toast_message);
        ImageView toastImage = layout.findViewById(R.id.toast_emoji);

        toastText.setText(text);
        if (emoji == GOOD){
            toastImage.setImageResource(R.drawable.ic_emoji_ok);
        }else {
            toastImage.setImageResource(R.drawable.ic_emoji_bad);
        }

        Toast toast = new Toast(getContext());
        toast.setDuration(duration);
        toast.setGravity(Gravity.BOTTOM, 0, 50);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(layout);
        toast.show();
    }

}