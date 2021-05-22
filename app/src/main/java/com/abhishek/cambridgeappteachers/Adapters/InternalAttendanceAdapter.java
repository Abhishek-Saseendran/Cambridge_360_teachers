package com.abhishek.cambridgeappteachers.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.abhishek.cambridgeappteachers.Models.Subjects;
import com.abhishek.cambridgeappteachers.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;

public class InternalAttendanceAdapter extends RecyclerView.Adapter<InternalAttendanceAdapter.ViewHolder> {

    Context mContext;
    List<HashMap<String, String>> subjectEnrolled;

    private static final int GOOD = 1;
    private static final int BAD = -1;

    public InternalAttendanceAdapter(Context mContext, List<HashMap<String, String>> subjectEnrolled) {
        this.mContext = mContext;
        this.subjectEnrolled = subjectEnrolled;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.select_subject_to_grade_list, parent, false);
        return new InternalAttendanceAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        final HashMap<String, String> subject = subjectEnrolled.get(position);

        holder.tvSubjectCode.setText(subject.get("subjectId"));
        holder.tvSubjectName.setText(subject.get("subjectName"));

        String classHandle = subject.get("branch").toUpperCase() + " " + subject.get("sem") + " " + subject.get("section");
        holder.tvSubjectClass.setText(classHandle);

        holder.ibProceedToGrade.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(mContext, com.abhishek.cambridgeappteachers.GradeInternalAttendanceActivity.class);
                intent.putExtra("branch", subject.get("branch"));
                intent.putExtra("sem", subject.get("sem"));
                intent.putExtra("section", subject.get("section"));
                intent.putExtra("subjectId", subject.get("subjectId"));
                intent.putExtra("subjectName", subject.get("subjectName"));
                mContext.startActivity(intent);

            }
        });

        holder.rlSelectSubject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(mContext, com.abhishek.cambridgeappteachers.GradeInternalAttendanceActivity.class);
                intent.putExtra("branch", subject.get("branch"));
                intent.putExtra("sem", subject.get("sem"));
                intent.putExtra("section", subject.get("section"));
                intent.putExtra("subjectId", subject.get("subjectId"));
                intent.putExtra("subjectName", subject.get("subjectName"));
                mContext.startActivity(intent);

            }
        });

    }



    @Override
    public int getItemCount() {
        return subjectEnrolled.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        public TextView tvSubjectCode, tvSubjectName, tvSubjectClass;
        public ImageButton ibProceedToGrade;
        public RelativeLayout rlSelectSubject;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvSubjectCode = itemView.findViewById(R.id.tvSubjectCode_internalAttendanceList);
            tvSubjectName = itemView.findViewById(R.id.tvSubjectName_internalAttendanceList);
            tvSubjectClass = itemView.findViewById(R.id.tvSubjectClass_internalAttendanceList);
            ibProceedToGrade = itemView.findViewById(R.id.ibProceedToGrade_internalAttendanceList);
            rlSelectSubject = itemView.findViewById(R.id.rlSelectSubject_internalAttendanceList);

        }
    }

}
