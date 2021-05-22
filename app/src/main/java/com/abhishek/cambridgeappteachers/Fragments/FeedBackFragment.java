package com.abhishek.cambridgeappteachers.Fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.abhishek.cambridgeappteachers.Adapters.FeedBackAdapter;
import com.abhishek.cambridgeappteachers.Models.Feedbacks;
import com.abhishek.cambridgeappteachers.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class FeedBackFragment extends Fragment {

    ProgressBar progressBar;
    RecyclerView rvFeedbacks;
    FeedBackAdapter feedBackAdapter;

    List<Feedbacks> feedbacksList;
    FirebaseAuth mAuth;
    FirebaseFirestore firestore;

    private static final int GOOD = 1;
    private static final int BAD = -1;

    public FeedBackFragment() {
        // Required empty public constructor
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_feed_back, container, false);

        progressBar = view.findViewById(R.id.progressBar);
        progressBar.setVisibility(View.INVISIBLE);

        rvFeedbacks = view.findViewById(R.id.rvFeedbacks);
        rvFeedbacks.setHasFixedSize(true);
        rvFeedbacks.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.HORIZONTAL, false));

        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        feedbacksList = new ArrayList<>();

        getFeedbackList();

        return view;
    }

    private void getFeedbackList() {

        progressBar.setVisibility(View.VISIBLE);
        firestore.collection("TEACHER_LIST").document(mAuth.getCurrentUser().getUid())
                .collection("FEEDBACKS")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {

                        if (task.isSuccessful()){

                            QuerySnapshot querySnapshot = task.getResult();

                            if (querySnapshot != null){

                                feedbacksList = querySnapshot.toObjects(Feedbacks.class);

                                if (feedbacksList.size() != 0){

                                    feedBackAdapter = new FeedBackAdapter(getContext(), feedbacksList);
                                    rvFeedbacks.setAdapter(feedBackAdapter);
                                    feedBackAdapter.notifyDataSetChanged();

                                }
                                else {
                                    showToast("No subjects Enrolled", BAD, Toast.LENGTH_SHORT);
                                }

                            }
                            else {
                                showToast("No subjects Enrolled", BAD, Toast.LENGTH_SHORT);
                            }
                            progressBar.setVisibility(View.INVISIBLE);
                        }
                        else {
                            showToast("Error " + task.getException(), BAD, Toast.LENGTH_SHORT);
                        }

                    }
                });
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