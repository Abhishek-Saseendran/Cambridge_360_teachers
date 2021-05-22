package com.abhishek.cambridgeappteachers.Adapters;

import android.content.Context;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.abhishek.cambridgeappteachers.Models.Feedbacks;
import com.abhishek.cambridgeappteachers.Models.Subjects;
import com.abhishek.cambridgeappteachers.Models.Teacher;
import com.abhishek.cambridgeappteachers.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EnrollForSubjectsAdapter extends RecyclerView.Adapter<EnrollForSubjectsAdapter.ViewHolder> {

    Context mContext;
    List<String> branches;
    Teacher teacher;
    List<Subjects> subjectsList;
    ArrayAdapter<String> sectionArray;

    ProgressBar progressBar;

    FirebaseAuth mAuth;
    FirebaseFirestore firestore;

    private static final int GOOD = 1;
    private static final int BAD = -1;
    RelativeLayout root;

    public EnrollForSubjectsAdapter(Context mContext, List<String> branches, Teacher teacher, List<Subjects> subjectsList, ProgressBar progressBar) {
        this.mContext = mContext;
        this.branches = branches;
        this.teacher = teacher;
        this.subjectsList = subjectsList;
        this.progressBar = progressBar;
        sectionArray = new ArrayAdapter<>(mContext, R.layout.spinner_item, mContext.getResources().getStringArray(R.array.section_array));

        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.enroll_for_subjects_list, parent, false);
        root = view.findViewById(R.id.toast_root);
        return new EnrollForSubjectsAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {

        final Subjects subject = subjectsList.get(position);
        final String[] sec = {""};

        holder.tvSubjectCode.setText(subject.getSubjectId());
        holder.tvSubjectName.setText(subject.getSubjectName());
        holder.tvBranch.setText(subject.getBranch());
        holder.tvSem.setText(subject.getSem());

        if (subject.getBranch().equals("bsc")){
            sectionArray = new ArrayAdapter<>(mContext, R.layout.spinner_item, mContext.getResources().getStringArray(R.array.section_basic_science));
            holder.spinSection.setAdapter(sectionArray);
        }
        else {
            sectionArray = new ArrayAdapter<>(mContext, R.layout.spinner_item, mContext.getResources().getStringArray(R.array.section_array));
            holder.spinSection.setAdapter(sectionArray);
        }

        holder.spinSection.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                sec[0] = holder.spinSection.getSelectedItem().toString().trim();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        holder.ibEnrollForSubject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("\t\tEnrolling for", subject.getSubjectName());
                progressBar.setVisibility(View.VISIBLE);
                enrollSubject(subject, sec[0]);
            }
        });

    }

    private void enrollSubject(final Subjects subject, final String sec) {

        ArrayList<String> tempArray;

        if (subject.getHandledBy().containsKey(sec))
        {
            tempArray = subject.getHandledBy().get(sec);
        }
        else {
            tempArray = new ArrayList<>();
        }
        if (!tempArray.contains(mAuth.getCurrentUser().getUid())){
            tempArray.add(mAuth.getCurrentUser().getUid());
        }
        Map<String, ArrayList<String>> tempMap = subject.getHandledBy();
        tempMap.put(sec, tempArray);
        subject.setHandledBy(tempMap);

        firestore.collection("DEPARTMENT").document(subject.getBranch() + "_branch")
                 .collection("CLASS").document(subject.getSem() + "_sem")
                 .collection("SUBJECTS").document(subject.getSubjectId())
                .set(subject)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful()){

                            List<HashMap<String,String>> subHandleList = teacher.getSubjectsHandlingNames();

                            HashMap<String, String> tempMapInner = new HashMap<String, String>() {
                                {
                                    put("subjectId", subject.getSubjectId());
                                    put("subjectName", subject.getSubjectName());
                                    put("sem", subject.getSem());
                                    put("branch", subject.getBranch());
                                    put("section", sec);
                                }
                            };

                            if (!subHandleList.contains(tempMapInner)){
                                subHandleList.add(tempMapInner);

                            }
                            teacher.setSubjectsHandlingNames(subHandleList);

                            firestore.collection("TEACHER_LIST").document(mAuth.getCurrentUser().getUid())
                                    .set(teacher)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> taskInner) {

                                            if (taskInner.isSuccessful()){
                                                setFeedBackCollection(subject);
                                                //Toast.makeText(mContext, "SuccessFull Added Subject " + subject.getSubjectName(), Toast.LENGTH_SHORT).show();
                                                showToast( "SuccessFull Added Subject " + subject.getSubjectName(), GOOD, Toast.LENGTH_SHORT);
                                            }
                                            else{
                                                //Toast.makeText(mContext, "UnsuccessFull Added Subject " + subject.getSubjectName(), Toast.LENGTH_SHORT).show();
                                                showToast("UnsuccessFull Added Subject " + subject.getSubjectName(), BAD, Toast.LENGTH_SHORT);
                                                Log.d("\t\tUnsuccess :: ", taskInner.getException().toString());
                                            }
                                            progressBar.setVisibility(View.INVISIBLE);
                                        }
                                    });
                        }
                        else {
                            Log.d("\t\tUnsuccess :: ", task.getException().toString());
                        }
                        progressBar.setVisibility(View.INVISIBLE);

                    }
                });
    }

    private void setFeedBackCollection(final Subjects subject) {

        Feedbacks feedback = new Feedbacks(subject.getSubjectId(), subject.getSubjectName());

        firestore.collection("TEACHER_LIST").document(mAuth.getCurrentUser().getUid())
                .collection("FEEDBACKS").document(subject.getSubjectId())
                .set(feedback)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            Log.d("\t\tFeedback set :", "" + subject.getSubjectId());
                        }
                    }
                });
    }


    public void filterList(ArrayList<Subjects> filterList){

        subjectsList = filterList;
        notifyDataSetChanged();

    }


    @Override
    public int getItemCount() {
        return subjectsList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        public TextView tvSubjectCode, tvSubjectName, tvBranch, tvSem;
        public Spinner spinSection;
        public ImageButton ibEnrollForSubject;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvSubjectCode = itemView.findViewById(R.id.tvSubjectCode_enrollForSubjectList);
            tvSubjectName = itemView.findViewById(R.id.tvSubjectName_enrollForSubjectList);
            tvBranch = itemView.findViewById(R.id.tvBranch_enrollForSubjectList);
            tvSem = itemView.findViewById(R.id.tvSem_enrollForSubjectList);
            spinSection = itemView.findViewById(R.id.spinSection_enrollForSubjectList);
            ibEnrollForSubject = itemView.findViewById(R.id.ibEnrollForSubject_enrollForSubjectList);

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
