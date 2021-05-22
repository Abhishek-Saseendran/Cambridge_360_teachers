package com.abhishek.cambridgeappteachers.Adapters;

import android.content.Context;
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
import androidx.recyclerview.widget.RecyclerView;

import com.abhishek.cambridgeappteachers.Models.Subjects;
import com.abhishek.cambridgeappteachers.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class DeleteSubjectsAdapter extends RecyclerView.Adapter<DeleteSubjectsAdapter.ViewHolder>{

    ProgressBar progressBar;
    Context mContext;
    List<Subjects> subjectsList;

    FirebaseFirestore firestore;

    private static final int GOOD = 1;
    private static final int BAD = -1;
    RelativeLayout root;

    public DeleteSubjectsAdapter(Context mContext, List<Subjects> subjectsList, ProgressBar progressBar) {
        this.mContext = mContext;
        this.subjectsList = subjectsList;
        this.progressBar = progressBar;
        firestore = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.delete_subjects_list, parent, false);
        root = view.findViewById(R.id.toast_root);
        return new DeleteSubjectsAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        final Subjects subject = subjectsList.get(position);

        holder.tvSubjectId.setText(subject.getSubjectId());
        holder.tvSubjectName.setText(subject.getSubjectName());
        holder.tvBranch.setText(subject.getBranch());
        holder.tvSem.setText(subject.getSem());

        holder.ibDeleteSubject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                deleteSubject(subject);

            }
        });

    }

    private void deleteSubject(final Subjects subject) {

        progressBar.setVisibility(View.VISIBLE);
        firestore.collection("DEPARTMENT").document(subject.getBranch() + "_branch")
                .collection("CLASS").document(subject.getSem() + "_sem")
                .collection("SUBJECTS").document(subject.getSubjectId())
                .delete()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            showToast("Successfully deleted " + subject.getSubjectName(), GOOD, Toast.LENGTH_SHORT);

                            subjectsList.remove(subject);
                            notifyDataSetChanged();
                        }
                        else {
                            showToast("Unsuccessful " + task.getException(), BAD, Toast.LENGTH_SHORT);
                        }
                        progressBar.setVisibility(View.INVISIBLE);
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

        public TextView tvSubjectId, tvSubjectName, tvBranch, tvSem;
        public ImageButton ibDeleteSubject;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvSubjectId = itemView.findViewById(R.id.tvSubjectCode_deleteSubjectList);
            tvSubjectName = itemView.findViewById(R.id.tvSubjectName_deleteSubjectList);
            tvBranch = itemView.findViewById(R.id.tvBranch_deleteSubjectList);
            tvSem = itemView.findViewById(R.id.tvSem_deleteSubjectList);
            ibDeleteSubject = itemView.findViewById(R.id.ibDeleteSubject_deleteSubjectList);

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
