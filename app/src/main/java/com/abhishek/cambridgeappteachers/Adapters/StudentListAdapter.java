package com.abhishek.cambridgeappteachers.Adapters;

import android.content.Context;
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
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.abhishek.cambridgeappteachers.Models.StudentSubjectEnrolled;
import com.abhishek.cambridgeappteachers.Models.Subjects;
import com.abhishek.cambridgeappteachers.Models.SubjectsEnrolled;
import com.abhishek.cambridgeappteachers.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class StudentListAdapter extends RecyclerView.Adapter<StudentListAdapter.ViewHolder>{

    Context mContext;
    List<StudentSubjectEnrolled> studentList;

    Subjects subject;
    String subjectId;
    String branch, sem, section;
    ProgressBar progressBar;
    SubjectsEnrolled subjectEnrolled;
    FirebaseFirestore firestore;

    HashMap<String, SubjectsEnrolled> resultList;

    private static final int GOOD = 1;
    private static final int BAD = -1;
    RelativeLayout root;

    public StudentListAdapter(Context mContext, List<StudentSubjectEnrolled> studentList, String subjectId,
                              String branch, String sem, String section, Subjects subject, ProgressBar progressBar) {
        this.mContext = mContext;
        this.studentList = studentList;
        this.subject = subject;
        this.subjectId = subjectId;
        this.branch = branch;
        this.sem = sem;
        this.section = section;
        this.progressBar = progressBar;

        resultList = new HashMap<>();
        firestore = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
       View view = LayoutInflater.from(mContext).inflate(R.layout.student_grading_list, parent, false);
       root = view.findViewById(R.id.toast_root);
       return new StudentListAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {

        final StudentSubjectEnrolled student = studentList.get(position);

        holder.tvStudentUSN.setText(student.getUsn());
        holder.tvStudentName.setText(student.getName());

        if (student.getImageUrl().isEmpty()){
            if (student.getGender().equals("male")){
                Picasso.get().load(R.drawable.ic_boy).into(holder.civStudentImage);
            }
            else {
                Picasso.get().load(R.drawable.ic_girl).into(holder.civStudentImage);
            }
        }
        else {
            Picasso.get().load(student.getImageUrl()).into(holder.civStudentImage);
        }

        progressBar.setVisibility(View.VISIBLE);
        firestore.collection("DEPARTMENT").document(branch + "_branch")
                .collection("CLASS").document(sem + "_sem")
                .collection("SECTION").document(section)
                .collection("STUDENTS").document(student.getUsn())
                .collection("SUBJECTS_ENROLLED").document(subjectId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()){

                            DocumentSnapshot snapshot = task.getResult();
                            subjectEnrolled = snapshot.toObject(SubjectsEnrolled.class);

                            if (subjectEnrolled != null){

                                holder.etInt1.setText(String.valueOf(subjectEnrolled.getInternal1()));
                                holder.etInt2.setText(String.valueOf(subjectEnrolled.getInternal2()));
                                holder.etInt3.setText(String.valueOf(subjectEnrolled.getInternal3()));
                                holder.etAttendance.setText(String.valueOf(subjectEnrolled.getAttendance()));

                                setEditTextColor(holder.etInt1, holder.etInt2, holder.etInt3, holder.etAttendance);

                            }
                        }
                        progressBar.setVisibility(View.INVISIBLE);
                    }
                });

        holder.rlHeaders.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                double att = Double.parseDouble(holder.etAttendance.getText().toString()) + 1 ;
                holder.etAttendance.setText(String.valueOf(att));
                showToast("Added attendance of " + student.getName() + " by 1", GOOD, Toast.LENGTH_SHORT);

            }
        });

        holder.rlHeaders.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {

                double att = Double.parseDouble(holder.etAttendance.getText().toString()) - 1 ;
                holder.etAttendance.setText(String.valueOf(att));
                showToast("Reduced attendance of " + student.getName() + " by 1", BAD, Toast.LENGTH_SHORT);
                return true;

            }
        });


        holder.etInt1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

                if (Float.parseFloat(editable.toString()) > subject.getTotalMarks()){
                    showToast("The marks entered is greater than the total marks!!", BAD, Toast.LENGTH_SHORT);
                    holder.etInt1.setTextColor(ContextCompat.getColor(mContext, R.color.colorAccent));

                }

                setResultList(student.getUsn(), editable.toString(), holder.etInt2.getText().toString(),
                        holder.etInt3.getText().toString(), holder.etAttendance.getText().toString());
            }
        });

        holder.etInt2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

                if (Float.parseFloat(editable.toString()) > subject.getTotalMarks()){
                    showToast("The marks entered is greater than the total marks!!", BAD, Toast.LENGTH_SHORT);
                    holder.etInt2.setTextColor(ContextCompat.getColor(mContext, R.color.colorAccent));

                }

                setResultList(student.getUsn(), holder.etInt1.getText().toString(), editable.toString(),
                        holder.etInt3.getText().toString(), holder.etAttendance.getText().toString());
            }
        });

        holder.etInt3.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

                if (Float.parseFloat(editable.toString()) > subject.getTotalMarks()){
                    showToast("The marks entered is greater than the total marks!!", BAD, Toast.LENGTH_SHORT);
                    holder.etInt3.setTextColor(ContextCompat.getColor(mContext, R.color.colorAccent));

                }

                setResultList(student.getUsn(), holder.etInt1.getText().toString(), holder.etInt2.getText().toString(),
                        editable.toString(), holder.etAttendance.getText().toString());
            }
        });

        holder.etAttendance.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

                setResultList(student.getUsn(), holder.etInt1.getText().toString(), holder.etInt2.getText().toString(),
                        holder.etInt3.getText().toString(), editable.toString());
            }
        });


    }


    public void setResultList(String usn, String intOne, String intTwo, String intThree, String attendance){

        if (!usn.isEmpty() && !intOne.isEmpty() && !intTwo.isEmpty() && !intThree.isEmpty() && !attendance.isEmpty()){
            float int1 = Float.parseFloat(intOne);
            float int2 = Float.parseFloat(intTwo);
            float int3 = Float.parseFloat(intThree);
            float att = Float.parseFloat(attendance);

            SubjectsEnrolled obj = new SubjectsEnrolled(subject.getSubjectId(), subject.getSubjectName(),
                    int1, int2, int3, att);

            resultList.put(usn, obj);
        }
        Log.d("\t\tResultList ::", usn + "\t" + intOne + "\t" + intTwo + "\t" + intThree + "\t" + attendance + "\t" + resultList.size());

    }


    public boolean saveButtonClicked(){

        final boolean[] done = {true};

        for (final HashMap.Entry<String, SubjectsEnrolled> temp : resultList.entrySet()){

            progressBar.setVisibility(View.VISIBLE);
                firestore.collection("DEPARTMENT").document(branch + "_branch")
                        .collection("CLASS").document(sem + "_sem")
                        .collection("SECTION").document(section)
                        .collection("STUDENTS").document(temp.getKey())
                        .collection("SUBJECTS_ENROLLED").document(subjectId)
                        .set(temp.getValue())
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()){
                                    Log.d("\t\tSuccess Save :: ", temp.getKey() + "\t" + subjectId);
                                }
                                else {
                                    Log.d("\t\tUNSUCEESSFUL :: ", temp.getKey() + "\t" + subjectId);
                                    done[0] = false;
                                }
                                progressBar.setVisibility(View.INVISIBLE);
                            }
                        });

        }

        return done[0];

    }



    private void setEditTextColor(EditText etInt1, EditText etInt2, EditText etInt3, EditText etAttendance) {

        if (subject.getTotalMarks() != 0){

            double percent = Float.parseFloat(etInt1.getText().toString()) / subject.getTotalMarks() * 100;
            if (percent < 35){
                etInt1.setTextColor(ContextCompat.getColor(mContext, R.color.colorRed));
            }
            else if (percent > 80){
                etInt1.setTextColor(ContextCompat.getColor(mContext, R.color.colorGreen));
            }

            percent = Float.parseFloat(etInt2.getText().toString()) / subject.getTotalMarks() * 100;
            if (percent < 35){
                etInt2.setTextColor(ContextCompat.getColor(mContext, R.color.colorRed));
            }
            else if (percent > 80){
                etInt2.setTextColor(ContextCompat.getColor(mContext, R.color.colorGreen));
            }

            percent = Float.parseFloat(etInt3.getText().toString()) / subject.getTotalMarks() * 100;
            if (percent < 35){
                etInt3.setTextColor(ContextCompat.getColor(mContext, R.color.colorRed));
            }
            else if (percent > 80){
                etInt3.setTextColor(ContextCompat.getColor(mContext, R.color.colorGreen));
            }

        }

        if (subject.getTotalAttendance().get(section) != 0){

            double percent = Float.parseFloat(etAttendance.getText().toString()) / subject.getTotalAttendance().get(section) * 100;
            if (percent < 85){
                etAttendance.setTextColor(ContextCompat.getColor(mContext, R.color.colorRed));
            }
            else if (percent > 90){
                etAttendance.setTextColor(ContextCompat.getColor(mContext, R.color.colorGreen));
            }

        }

    }

    @Override
    public int getItemCount() {
        return studentList.size();
    }

    public void filterList(ArrayList<StudentSubjectEnrolled> filterList){

        studentList = filterList;
        notifyDataSetChanged();

    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        public CircleImageView civStudentImage;
        public TextView tvStudentUSN, tvStudentName;
        public RelativeLayout rlHeaders;
        public EditText etInt1, etInt2, etInt3, etAttendance;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            civStudentImage = itemView.findViewById(R.id.civStudentImage);
            tvStudentUSN = itemView.findViewById(R.id.tvStudentUSN);
            tvStudentName = itemView.findViewById(R.id.tvStudentName);
            rlHeaders = itemView.findViewById(R.id.rlHeaders_gradeList);
            etInt1 = itemView.findViewById(R.id.etInt1);
            etInt2 = itemView.findViewById(R.id.etInt2);
            etInt3 = itemView.findViewById(R.id.etInt3);
            etAttendance = itemView.findViewById(R.id.etAttendance);

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
