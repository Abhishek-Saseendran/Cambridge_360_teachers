package com.abhishek.cambridgeappteachers.Adapters;

import android.content.Context;
import android.content.res.ColorStateList;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.abhishek.cambridgeappteachers.Models.Subjects;
import com.abhishek.cambridgeappteachers.Models.Teacher;
import com.abhishek.cambridgeappteachers.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;

public class SubjectsEnrolledAdapter extends RecyclerView.Adapter<SubjectsEnrolledAdapter.ViewHolder> {

    Context mContext;
    List<HashMap<String, String>> subjectEnrolled;
    FirebaseAuth mAuth;
    FirebaseFirestore firestore;

    ProgressBar progressBar;
    Teacher teacher;

    private static final int GOOD = 1;
    private static final int BAD = -1;
    RelativeLayout root;

    public SubjectsEnrolledAdapter(Context mContext, List<HashMap<String, String>> subjectEnrolled, ProgressBar progressBar) {
        this.mContext = mContext;
        this.subjectEnrolled = subjectEnrolled;
        this.progressBar = progressBar;
        progressBar.setVisibility(View.INVISIBLE);
        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.subjects_enrolled_list, parent, false);
        root= view.findViewById(R.id.toast_root);
        return new SubjectsEnrolledAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {

        final HashMap<String, String> subject = subjectEnrolled.get(position);
        holder.tvSubjectName.setText(subject.get("subjectName"));
        holder.tvSubjectCode.setText(subject.get("subjectId"));

        String classHandle = subject.get("branch").toUpperCase() + " " + subject.get("sem") + " " + subject.get("section");
        holder.tvSubjectClass.setText(classHandle);


        holder.ibCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("\t\tSubject selected ::", holder.tvSubjectName.getText().toString());


                    progressBar.setVisibility(View.VISIBLE);

                    //String subjectName = subjectEnrolled.get(position).get("subjectName");
                    subjectEnrolled.remove(position);

                    deleteHandlingFromSubject(subject);
                    deleteSubject(subject);

            }
        });
    }

    private void deleteHandlingFromSubject(final HashMap<String, String> subject) {

        firestore.collection("DEPARTMENT").document(subject.get("branch").toLowerCase() + "_branch" )
                .collection("CLASS").document(subject.get("sem").toLowerCase() + "_sem")
                .collection("SUBJECTS").document(subject.get("subjectId"))
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()){

                            DocumentSnapshot documentSnapshot = task.getResult();
                            final Subjects subjectDoc = documentSnapshot.toObject(Subjects.class);

                            if (subjectDoc != null){

                                subjectDoc.getHandledBy().get(subject.get("section")).remove(mAuth.getCurrentUser().getUid());

                                firestore.collection("DEPARTMENT").document(subject.get("branch").toLowerCase() + "_branch" )
                                        .collection("CLASS").document(subject.get("sem").toLowerCase() + "_sem")
                                        .collection("SUBJECTS").document(subject.get("subjectId"))
                                        .set(subjectDoc)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()){
                                                    Log.d("\t\tSUBJECTS modified ::", subjectDoc.getSubjectName());
                                                }
                                            }
                                        });

                            }

                        }
                        else {

                            showToast("" + task.getException(), BAD, Toast.LENGTH_SHORT);
                            Log.d("\t\tError Deleting", task.getException().toString());
                        }
                    }
                });
    }


    private void deleteSubject(final HashMap<String, String> subject) {

        progressBar.setVisibility(View.VISIBLE);

        firestore.collection("TEACHER_LIST").document(mAuth.getCurrentUser().getUid())
                .update("subjectsHandlingNames", subjectEnrolled)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful())
                        {

                            getTeacherToDeleteFeedBack(subject);

                            showToast("Successfully deleted " + subject.get("subjectName"), GOOD, Toast.LENGTH_SHORT);
                            notifyDataSetChanged();
                        }
                        else {
                            showToast("Unsuccessful deleted " + subject.get("subjectName"), BAD, Toast.LENGTH_SHORT);
                        }
                        progressBar.setVisibility(View.INVISIBLE);
                    }
                });

    }

    private void getTeacherToDeleteFeedBack(final HashMap<String, String> subject) {

        final boolean[] canDeleteFeedback = {true};
        firestore.collection("TEACHER_LIST").document(mAuth.getCurrentUser().getUid())
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()){

                            DocumentSnapshot documentSnapshot = task.getResult();
                            teacher = documentSnapshot.toObject(Teacher.class);

                            for(HashMap<String, String> temp : teacher.getSubjectsHandlingNames())
                            {

                                if (temp.get("subjectId").equals(subject.get("subjectId")))
                                    canDeleteFeedback[0] = false;
                            }

                            if (canDeleteFeedback[0]){

                                firestore.collection("TEACHER_LIST").document(mAuth.getCurrentUser().getUid())
                                        .collection("FEEDBACKS").document(subject.get("subjectId"))
                                        .delete()
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()){
                                                    Log.d("\t\tFeedback Deleted :: ", subject.get("subjectId"));
                                                }
                                            }
                                        });

                                }
                        }
                    }
                });
    }

    @Override
    public int getItemCount() {
        return subjectEnrolled.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder{

        public TextView tvSubjectCode, tvSubjectName, tvSubjectClass;
        public ImageButton ibCancelButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvSubjectName = itemView.findViewById(R.id.tvSubject_subjectEnrolledItem);
            tvSubjectCode = itemView.findViewById(R.id.tvCode_subjectEnrolledItem);
            tvSubjectClass = itemView.findViewById(R.id.tvClass_subjectEnrolledItem);
            ibCancelButton = itemView.findViewById(R.id.ibCancelButton);

        }
    }

    public void showToast(String text, int emoji, int duration){

        LayoutInflater inflater = LayoutInflater.from(mContext);
        View layout = inflater.inflate(R.layout.toast_layout, (ViewGroup) root);

        TextView toastText = layout.findViewById(R.id.toast_message);
        ImageView toastImage = layout.findViewById(R.id.toast_emoji);

        toastText.setText(text);
        if (emoji == GOOD){
            toastImage.setImageResource(R.drawable.ic_emoji_ok);
        }else {
            toastImage.setImageResource(R.drawable.ic_emoji_bad);
        }

        Toast toast = new Toast(mContext);
        toast.setDuration(duration);
        toast.setGravity(Gravity.BOTTOM, 0, 50);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(layout);
        toast.show();
    }

}
