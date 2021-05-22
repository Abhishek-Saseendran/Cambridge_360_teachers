package com.abhishek.cambridgeappteachers;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.abhishek.cambridgeappteachers.Models.Subjects;
import com.abhishek.cambridgeappteachers.Models.Teacher;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;

public class AddSubjectsActivity extends AppCompatActivity {

    EditText etSubjectId, etSubjectName, etTotalMarks;
    Spinner spinBranch, spinSem;
    CheckBox cbIsElective;
    RadioGroup rgElective;
    RelativeLayout rlIsElective;
    Button btnSaveSubject;
    ProgressBar progressBar;

    int electiveSet = 0;
    boolean isElective = false;
    String subjectId = "";
    String subjectName = "";
    String branch = "";
    String sem = "";
    int totalMarks;

    ArrayAdapter<String> branchArray;
    ArrayAdapter<String> semesterArray;
    String[] secStrings;

    Subjects subject;
    Teacher teacher;
    FirebaseAuth mAuth;
    FirebaseFirestore firestore;

    private static final int GOOD = 1;
    private static final int BAD = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_subjects);

        etSubjectId = findViewById(R.id.etSubjectId_addSubject);
        etSubjectName = findViewById(R.id.etSubjectName_addSubject);
        etTotalMarks = findViewById(R.id.etSubjectTotalMarks_addSubject);
        spinBranch = findViewById(R.id.spinBranch_addSubject);
        spinSem = findViewById(R.id.spinSem_addSubject);
        cbIsElective= findViewById(R.id.cbIsElective_addSubject);
        rgElective = findViewById(R.id.rgElectiveSet_addSubject);
        btnSaveSubject = findViewById(R.id.btnSaveSubject_addSubject);

        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.INVISIBLE);

        rgElective.clearCheck();
        rlIsElective = findViewById(R.id.rlIsElectiveTrue);
        rlIsElective.setVisibility(View.INVISIBLE);

        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        cbIsElective.setChecked(false);

        getTeacher();

        semesterArray = new ArrayAdapter<>(AddSubjectsActivity.this, R.layout.spinner_item,
                getResources().getStringArray(R.array.sem_array));
        spinSem.setAdapter(semesterArray);

        spinBranch.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                branch = spinBranch.getSelectedItem().toString();
                Log.d("\t\tBranch :; ", branch);

                if (branch.equals("bsc")){
                    semesterArray = new ArrayAdapter<>(AddSubjectsActivity.this, R.layout.spinner_item,
                            getResources().getStringArray(R.array.sem_basic_science));
                    spinSem.setAdapter(semesterArray);
                    secStrings = getResources().getStringArray(R.array.section_basic_science);
                }
                else {
                    semesterArray = new ArrayAdapter<>(AddSubjectsActivity.this, R.layout.spinner_item,
                            getResources().getStringArray(R.array.sem_array));
                    spinSem.setAdapter(semesterArray);
                    secStrings = getResources().getStringArray(R.array.section_array);
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        spinSem.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                sem = spinSem.getSelectedItem().toString().trim();
                Log.d("\t\tSem ::", sem);

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        rgElective.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                RadioButton rb = (RadioButton) radioGroup.findViewById(i);

                if (null != rb) {
                    electiveSet = Integer.parseInt(rb.getTag().toString().trim());
                    Log.d("\t\tElective Set :: ", electiveSet + "");
                }
            }
        });

        btnSaveSubject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (etSubjectId.getText().toString().isEmpty() || etSubjectName.getText().toString().isEmpty()
                        || etTotalMarks.getText().toString().isEmpty()){
                    showToast("Enter all fields!!", BAD, Toast.LENGTH_SHORT);
                }
                else {

                    if((cbIsElective.isChecked() && electiveSet != 0) || (!cbIsElective.isChecked() && electiveSet == 0)){

                        subjectId = etSubjectId.getText().toString().trim();
                        subjectName = etSubjectName.getText().toString().trim();
                        totalMarks = Integer.parseInt(etTotalMarks.getText().toString().trim());
                        saveSubject();

                    }

                }
            }
        });


    }

    private void saveSubject() {

        progressBar.setVisibility(View.VISIBLE);
        HashMap<String, ArrayList<String>> tempHandledBy = new HashMap<>();
        HashMap<String, Integer> tempTotalAttendance = new HashMap<>();

        for (String sec : secStrings){

            tempHandledBy.put(sec, new ArrayList<String>());
            tempTotalAttendance.put(sec, 0);

        }

        subject = new Subjects(subjectId, subjectName, totalMarks,
                tempTotalAttendance, tempHandledBy, isElective, electiveSet, sem, branch);
        firestore.collection("DEPARTMENT").document(branch + "_branch")
                .collection("CLASS").document(sem + "_sem")
                .collection("SUBJECTS").document(subjectId)
                .set(subject)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful()){
                            showToast("Subject "+ subjectId +" Successflly added!!", GOOD, Toast.LENGTH_SHORT);

                            etSubjectId.setText("");
                            etSubjectName.setText("");
                            etTotalMarks.setText("");
                            cbIsElective.setChecked(false);
                            rgElective.clearCheck();
                        }
                        else {
                            showToast("Unable to add Subject" + task.getException(), BAD, Toast.LENGTH_SHORT);
                        }
                        progressBar.setVisibility(View.INVISIBLE);
                    }
                });

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

                            if (teacher != null){
                                branchArray = new ArrayAdapter<>(AddSubjectsActivity.this, R.layout.spinner_item, teacher.getBranch());
                                spinBranch.setAdapter(branchArray);
                            }
                        }
                        progressBar.setVisibility(View.INVISIBLE);
                    }
                });

    }


    public void onCheckIsElective(View view){

        isElective = ((CheckBox) view).isChecked();

        if (isElective){
            rlIsElective.setVisibility(View.VISIBLE);
            rgElective.clearCheck();
        }
        else {
            rlIsElective.setVisibility(View.INVISIBLE);
            electiveSet = 0;
            rgElective.clearCheck();
        }

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

        AddSubjectsActivity.this.finish();

    }
}