package com.abhishek.cambridgeappteachers;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.abhishek.cambridgeappteachers.Adapters.StudentListAdapter;
import com.abhishek.cambridgeappteachers.Models.Student;
import com.abhishek.cambridgeappteachers.Models.StudentSubjectEnrolled;
import com.abhishek.cambridgeappteachers.Models.Subjects;
import com.abhishek.cambridgeappteachers.Models.SubjectsEnrolled;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GradeInternalAttendanceActivity extends AppCompatActivity {

    ProgressBar progressBar;
    TextView tvSubjectName, tvSubjectClass, tvTotalClasses;
    ImageButton ibPlusOneMain, ibMinusOneMain;
    EditText etSearchForStudent;
    Button btnSaveGrades;

    RecyclerView rvGradeStudent;
    StudentListAdapter studentListAdapter;
    String branch, sem, section, subjectId, subjectName;
    Subjects subject;
    List<StudentSubjectEnrolled> studentList;

    FirebaseFirestore firestore;

    private static final int GOOD = 1;
    private static final int BAD = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grade_internal_attendance);

        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.INVISIBLE);

        tvSubjectName = findViewById(R.id.tvSubjectName_gradeActivity);
        tvSubjectClass = findViewById(R.id.tvSubjectClass_gradeActivity);
        tvTotalClasses = findViewById(R.id.tvTotalClasses_gradeActivity);
        ibPlusOneMain = findViewById(R.id.ibPlusOneMain);
        ibMinusOneMain = findViewById(R.id.ibMinusOneMain);
        etSearchForStudent = findViewById(R.id.etSearchForStudent);
        btnSaveGrades = findViewById(R.id.btnSaveGrades);
        rvGradeStudent = findViewById(R.id.rvGradeStudent);

        firestore = FirebaseFirestore.getInstance();
        rvGradeStudent.setHasFixedSize(true);
        rvGradeStudent.setLayoutManager(new LinearLayoutManager(this));

        Intent intent = getIntent();

        subjectId = intent.getStringExtra("subjectId");
        subjectName = intent.getStringExtra("subjectName");
        branch = intent.getStringExtra("branch");
        sem = intent.getStringExtra("sem");
        section = intent.getStringExtra("section");

        String fullSubjectName = subjectId + " : " + subjectName;
        String fullClassName = branch + " " + sem + " " + section;
        tvSubjectName.setText(fullSubjectName);
        tvSubjectClass.setText(fullClassName);

        studentList = new ArrayList<>();
        getSubject();


        etSearchForStudent.addTextChangedListener(new TextWatcher() {
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

        btnSaveGrades.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                boolean done = studentListAdapter.saveButtonClicked();
                getStudents();
                if (done){
                    showToast("Grades Saved", GOOD, Toast.LENGTH_SHORT);
                    saveSubject();
                }
                else {
                    showToast("There was some problem", BAD, Toast.LENGTH_SHORT);
                }

            }
        });


        ibPlusOneMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String number = tvTotalClasses.getText().toString().trim().replaceAll("[^0-9]", "");
                Log.d("\t\tNUMBER : ", number);
                int plusOneAtt = Integer.parseInt(number) + 1;
                number = "Total Classes : " + plusOneAtt;
                tvTotalClasses.setText(number);

                subject.getTotalAttendance().put(section, plusOneAtt);

            }
        });

        ibMinusOneMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String number = tvTotalClasses.getText().toString().trim().replaceAll("[^0-9]", "");
                Log.d("\t\tNUMBER : ", number);
                int minusOneAtt = Integer.parseInt(number) - 1;
                number = "Total Classes : " + minusOneAtt;
                tvTotalClasses.setText(number);

                subject.getTotalAttendance().put(section, minusOneAtt);

            }
        });

    }

    private void saveSubject() {

        progressBar.setVisibility(View.VISIBLE);
        firestore.collection("DEPARTMENT").document(branch + "_branch")
                .collection("CLASS").document(sem + "_sem")
                .collection("SUBJECTS").document(subjectId)
                .update("totalAttendance", subject.getTotalAttendance())
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            Log.d("\t\tUpdated attendance ::", subjectId);
                        }
                        else {
                            Log.d("\t\tUNSUCESSFUL ::", subjectId);
                        }
                        progressBar.setVisibility(View.INVISIBLE);
                    }
                });
    }


    private void filter(String text) {

        ArrayList<StudentSubjectEnrolled> filterList = new ArrayList<>();

        for (StudentSubjectEnrolled stud : studentList){
            if (stud.getName().toLowerCase().contains(text.toLowerCase()) || stud.getUsn().toLowerCase().contains(text.toLowerCase())){
                filterList.add(stud);
            }
            studentListAdapter.filterList(filterList);
        }
    }

    private void getSubject() {

        progressBar.setVisibility(View.VISIBLE);
        firestore.collection("DEPARTMENT").document(branch + "_branch")
                .collection("CLASS").document(sem + "_sem")
                .collection("SUBJECTS").document(subjectId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()){

                            DocumentSnapshot documentSnapshot = task.getResult();
                            subject = documentSnapshot.toObject(Subjects.class);
                            if (subject != null){

                                int totalClass = subject.getTotalAttendance().get(section);
                                String totalClasses = "Total Classes : " + totalClass ;
                                tvTotalClasses.setText(totalClasses);

                                getStudents();

                            }
                            else {

                                //progressBar.setVisibility(View.INVISIBLE);
                                showToast("There is no subject as such!!", BAD, Toast.LENGTH_SHORT);
                                btnSaveGrades.setEnabled(false);
                                btnSaveGrades.setVisibility(View.GONE);
                                progressBar.setVisibility(View.INVISIBLE);
                            }
                        }
                        else {

                            progressBar.setVisibility(View.INVISIBLE);
                            Log.d("\t\tNo subject :: ", task.getException().toString());
                        }
                    }
                });
    }

    private void getStudents() {

        studentList.clear();
        firestore.collection("DEPARTMENT").document(branch + "_branch")
                .collection("CLASS").document(sem + "_sem")
                .collection("SECTION").document(section)
                .collection("STUDENTS")
                .whereArrayContains("subjectEnrolledNames", subjectId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()){

                            QuerySnapshot qs = task.getResult();
                            if (qs != null){


                                List<StudentSubjectEnrolled> list = qs.toObjects(StudentSubjectEnrolled.class);

                                if (list.size() == 0){
                                    showToast("No student Available", BAD, Toast.LENGTH_SHORT);
                                    btnSaveGrades.setEnabled(false);
                                    btnSaveGrades.setVisibility(View.GONE);
                                }
                                else {
                                    for(StudentSubjectEnrolled student : list){

                                        studentList.add(student);
                                        studentListAdapter = new StudentListAdapter(GradeInternalAttendanceActivity.this, studentList,
                                                subjectId, branch, sem, section, subject, progressBar);
                                        rvGradeStudent.setAdapter(studentListAdapter);
                                        studentListAdapter.notifyDataSetChanged();

                                    }
                                }
                            }
                            progressBar.setVisibility(View.INVISIBLE);
                        }
                        else {
                            Log.d("\t\tError :: ", task.getException().toString());
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

        GradeInternalAttendanceActivity.this.finish();

    }
}