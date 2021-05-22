package com.abhishek.cambridgeappteachers;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.abhishek.cambridgeappteachers.Models.Subjects;
import com.abhishek.cambridgeappteachers.Models.Teacher;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Transaction;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableReference;
import com.google.firebase.functions.HttpsCallableResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EditTeacherActivity extends AppCompatActivity {

    Button btnSubmit;
    CheckBox cbBSC, cbCSE, cbISE, cbECE, cbEEE, cbCIV, cbMECH;

    TextView tvTeacherProfileName, tvTeacherID, tvTeacherEmail;
    TextView tvPhone, tvDOB;

    ProgressBar progressBar;

    Teacher teacher;
    List<String> branchList;

    FirebaseAuth mAuth;
    FirebaseFirestore firestore;

    private static final int GOOD = 1;
    private static final int BAD = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_teacher);

        btnSubmit = findViewById(R.id.btnSubmitTeacher_Edit);
        cbBSC = findViewById(R.id.cbBSC);
        cbCSE = findViewById(R.id.cbCSE);
        cbISE = findViewById(R.id.cbISE);
        cbECE = findViewById(R.id.cbECE);
        cbEEE = findViewById(R.id.cbEEE);
        cbCIV = findViewById(R.id.cbCIV);
        cbMECH = findViewById(R.id.cbMECH);

        tvTeacherProfileName = findViewById(R.id.tvTeacherProfileName_Edit);
        tvTeacherID = findViewById(R.id.tvTeacherProfileID_Edit);
        tvTeacherEmail = findViewById(R.id.tvTeacherProfileEmail_Edit);
        tvPhone = findViewById(R.id.tvTeacherProfilePhone_Edit);
        tvDOB = findViewById(R.id.tvTeacherProfileDOB_Edit);

        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);

        cbBSC.setChecked(false);
        cbCSE.setChecked(false);
        cbISE.setChecked(false);
        cbECE.setChecked(false);
        cbEEE.setChecked(false);
        cbCIV.setChecked(false);
        cbMECH.setChecked(false);

        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        loadProfile();

        branchList = new ArrayList<>();

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (branchList.size() != 0)
                {
                    deleteSubjectsHandling();
                    //deletePreviousTeacher();
                    updateTeacher();

                }
                else {
                    showToast("Select atleast one branch", BAD, Toast.LENGTH_SHORT);
                }
            }
        });

    }


    private void loadProfile() {

        firestore.collection("TEACHER_LIST").document(mAuth.getCurrentUser().getUid())
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful())
                        {
                            DocumentSnapshot documentSnapshot = task.getResult();

                            teacher = documentSnapshot.toObject(Teacher.class);

                            tvTeacherProfileName.setText(teacher.getName());
                            tvTeacherEmail.setText(teacher.getEmail());
                            tvTeacherID.setText(teacher.getId());
                            tvPhone.setText(teacher.getPhone());
                            tvDOB.setText(teacher.getDob());

                            progressBar.setVisibility(View.INVISIBLE);

                        }
                    }
                });
    }

    private void deleteSubjectsHandling() {

        if (teacher.getSubjectsHandlingNames().size() != 0){

            for (final HashMap<String, String> subject : teacher.getSubjectsHandlingNames()){

                final DocumentReference docRef = firestore.collection("DEPARTMENT").document(subject.get("branch").toLowerCase() + "_branch" )
                        .collection("CLASS").document(subject.get("sem").toLowerCase() + "_sem")
                        .collection("SUBJECTS").document(subject.get("subjectId"));

                firestore.runTransaction(new Transaction.Function<Void>() {
                    @Nullable
                    @Override
                    public Void apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {

                        DocumentSnapshot documentSnapshot = transaction.get(docRef);
                        final Subjects subjectDoc = documentSnapshot.toObject(Subjects.class);

                        if (subjectDoc != null){

                            subjectDoc.getHandledBy().get(subject.get("section")).remove(mAuth.getCurrentUser().getUid());

                            transaction.set(docRef, subjectDoc);

                        }
                        return null;
                    }
                })
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.d("\t\tRemoved :: ", subject.get("subjectName") + "  " +subject.get("section"));
                                progressBar.setVisibility(View.INVISIBLE);
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                showToast("Error " + e.toString(), BAD, Toast.LENGTH_SHORT);
                                progressBar.setVisibility(View.INVISIBLE);
                            }
                        });

            }

        }

    }

    public void updateTeacher()
    {
        progressBar.setVisibility(View.VISIBLE);
        Teacher newTeacher = new Teacher(teacher.getId(), teacher.getName(), branchList,
                teacher.getEmail(), teacher.getDob(), teacher.getPhone(), teacher.getImageUrl(), teacher.getGender(), teacher.isIsAdmin());

        firestore.collection("TEACHER_LIST").document(mAuth.getCurrentUser().getUid())
                .set(newTeacher)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        //deleteFeedBackCollection();

                        showToast("Updation done!!", GOOD, Toast.LENGTH_SHORT);
                        progressBar.setVisibility(View.INVISIBLE);
                        startActivity(new Intent(EditTeacherActivity.this,
                                com.abhishek.cambridgeappteachers.MainTeacherActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP));
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        showToast(e.getMessage(), BAD, Toast.LENGTH_SHORT);
                        progressBar.setVisibility(View.INVISIBLE);
                    }
                });
    }

    private void deleteFeedBackCollection() {

        String path = "/TEACHER_LIST/"+mAuth.getCurrentUser().getUid()+"/FEEDBACKS";
        Map<String, Object> data = new HashMap<>();
        data.put("path", path);

            HttpsCallableReference deleteFn =
                    FirebaseFunctions.getInstance().getHttpsCallable("recursiveDelete");
            deleteFn.call(data)
                    .addOnSuccessListener(new OnSuccessListener<HttpsCallableResult>() {
                        @Override
                        public void onSuccess(HttpsCallableResult httpsCallableResult) {
                            Log.d("Successfully deleted ::",  "FFEDBACKS " + mAuth.getCurrentUser().getUid());
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d("UNSUCCESFUL :: ",  "FEEDBACKS " + mAuth.getCurrentUser().getUid() + e.toString());
                        }
                    });

    }

    public void onCheckBoxClicked(View view)
    {
        // Is the view now checked?
        boolean checked = ((CheckBox) view).isChecked();

        // Check which checkbox was clicked
        switch(view.getId()) {
            case R.id.cbBSC:
            case R.id.cbCSE:
            case R.id.cbISE:
            case R.id.cbECE:
            case R.id.cbEEE:
            case R.id.cbCIV:
            case R.id.cbMECH:
                if (checked){
                    branchList.add(view.getTag().toString());
                }
                else{
                    branchList.remove(view.getTag().toString());
                }
                break;
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


}