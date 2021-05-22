package com.abhishek.cambridgeappteachers;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.abhishek.cambridgeappteachers.Models.NoticeBoardItem;
import com.abhishek.cambridgeappteachers.Models.Teacher;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

public class AddNoticeActivity extends AppCompatActivity {

    Button btnSelectFile, btnUpload;
    TextView tvFile;
    EditText etFileName, etFileDescription;
    Spinner spinBranch, spinSem;
    RadioGroup rgFileType;
    ProgressBar progressBar;

    Teacher teacher;
    FirebaseAuth mAuth;
    FirebaseFirestore firestore;
    FirebaseStorage storage;
    StorageReference storageReference;
    StorageTask uploadTask;
    Uri fileURI;

    ArrayAdapter<String> branchArray;
    ArrayAdapter<String> semesterArray;
    String branch = "";
    String sem = "";
    String fileTypeString = "";
    String fileExtension = "";
    NoticeBoardItem noticeBoardItem;

    private static final int REQUEST_PERMISSION_NUMBER = 1289;
    private static final int REQUEST_FILE_NUMBER = 1234;

    private static final int GOOD = 1;
    private static final int BAD = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_notice);

        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.INVISIBLE);

        rgFileType = findViewById(R.id.rgFileType);
        rgFileType.clearCheck();
        etFileName = findViewById(R.id.etFilename);
        etFileDescription = findViewById(R.id.etFileDescription);
        tvFile = findViewById(R.id.tvFileName);
        btnSelectFile = findViewById(R.id.btnSelectFile);
        btnUpload = findViewById(R.id.btnUpload);

        spinBranch = findViewById(R.id.spinBranch_addNotice);
        spinSem = findViewById(R.id.spinSem_addNotice);

        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference().child("Noticeboard");

        getTeacher();

        semesterArray = new ArrayAdapter<>(AddNoticeActivity.this, R.layout.spinner_item,
                getResources().getStringArray(R.array.sem_array));
        spinSem.setAdapter(semesterArray);

        spinBranch.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                branch = spinBranch.getSelectedItem().toString();
                Log.d("\t\tBranch Notice:; ", branch);

                if (branch.equals("bsc")){
                    semesterArray = new ArrayAdapter<>(AddNoticeActivity.this, R.layout.spinner_item,
                            getResources().getStringArray(R.array.sem_basic_science));
                    spinSem.setAdapter(semesterArray);
                }
                else {
                    semesterArray = new ArrayAdapter<>(AddNoticeActivity.this, R.layout.spinner_item,
                            getResources().getStringArray(R.array.sem_array));
                    spinSem.setAdapter(semesterArray);
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
                Log.d("\t\tSem Notice::", sem);

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        rgFileType.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                RadioButton rb = (RadioButton) radioGroup.findViewById(i);

                if (null != rb) {
                    fileTypeString = rb.getTag().toString().trim();
                    Log.d("\t\tFile Type :: ", fileTypeString + "");
                }
            }
        });


        btnSelectFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (ContextCompat.checkSelfPermission(AddNoticeActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)
                        == PackageManager.PERMISSION_GRANTED)
                {
                    if (!fileTypeString.isEmpty())
                        getFileToUpload();
                    else
                        showToast("Select the type of file", BAD, Toast.LENGTH_SHORT);
                }
                else {
                    ActivityCompat.requestPermissions(AddNoticeActivity.this,
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_PERMISSION_NUMBER);
                }

            }
        });

        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (etFileDescription.getText().toString().isEmpty() || etFileName.getText().toString().isEmpty() || branch.isEmpty()
                        || sem.isEmpty() || fileTypeString.isEmpty())
                    showToast("Enter all Fields", BAD, Toast.LENGTH_SHORT);
                else {

                    if (fileURI != null){
                        uploadFile(fileURI);
                    }
                    else
                        showToast("Please select a file", BAD, Toast.LENGTH_SHORT);

                }

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
                                branchArray = new ArrayAdapter<>(AddNoticeActivity.this, R.layout.spinner_item, teacher.getBranch());
                                spinBranch.setAdapter(branchArray);
                            }
                        }
                        progressBar.setVisibility(View.INVISIBLE);
                    }
                });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_PERMISSION_NUMBER && grantResults[0] == PackageManager.PERMISSION_GRANTED){

            if (!fileTypeString.isEmpty())
                getFileToUpload();
            else
                showToast("Select the type of file", BAD, Toast.LENGTH_SHORT);
        }
        else {
            showToast("Please grant permission to upload File", BAD, Toast.LENGTH_SHORT);
        }

    }

    private void getFileToUpload() {

        progressBar.setVisibility(View.VISIBLE);
        Intent intent = new Intent();

        switch (fileTypeString){
            case "pdf" :
                intent.setType("application/pdf");
                fileExtension = "pdf";
                break;

            case "doc" :
                intent.setType("*/*");
                String[] mimetypes = {"application/vnd.openxmlformats-officedocument.wordprocessingml.document", "application/msword"};
                intent.putExtra(Intent.EXTRA_MIME_TYPES, mimetypes);
                fileExtension = "docx";
                break;

            case "image" :
                intent.setType("image/*");
                fileExtension = "png";
                break;
        }

        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, REQUEST_FILE_NUMBER);
        progressBar.setVisibility(View.INVISIBLE);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_FILE_NUMBER && resultCode == RESULT_OK && data != null){

            fileURI = data.getData();
            if (fileURI != null){

                Log.d("\t\tFileURI :: ", fileURI.toString());
                tvFile.setText(fileURI.getLastPathSegment());
            }
        }
        else
            showToast("Error, Please Select a file", BAD, Toast.LENGTH_SHORT);

    }

    private void uploadFile(Uri fileURI) {

        progressBar.setVisibility(View.VISIBLE);
        final String fileId = mAuth.getCurrentUser().getUid() + "_" + System.currentTimeMillis() + "." + fileExtension;

        final StorageReference ref = storageReference.child(fileId);
        uploadTask = ref.putFile(fileURI);

        Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    progressBar.setVisibility(View.INVISIBLE);
                    throw task.getException();
                }

                // Continue with the task to get the download URL
                return ref.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    Uri downloadUri = task.getResult();

                    Log.d("\t\tURI", downloadUri.toString());

                    boolean isImage = false;
                    if (fileTypeString.equals("image"))
                        isImage = true;

                    noticeBoardItem = new NoticeBoardItem(fileId, etFileName.getText().toString().trim(),
                            etFileDescription.getText().toString().trim(), downloadUri.toString(),
                            fileExtension, isImage, mAuth.getCurrentUser().getUid(), teacher.getName(), branch, sem);
                    setToFirestore();

                } else {
                    // Handle failures
                    // ...
                    Log.d("\t\t ERROR UPLOAD", task.getException().toString());
                    showToast("", BAD, Toast.LENGTH_SHORT);
                    progressBar.setVisibility(View.INVISIBLE);
                }
            }
        });

    }

    private void setToFirestore() {

        firestore.collection("DEPARTMENT").document(branch.toLowerCase() + "_branch")
                .collection("CLASS").document(sem + "_sem")
                .collection("NOTICE_BOARD").document(noticeBoardItem.getFileId())
                .set(noticeBoardItem)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful()){
                            showToast("Notice added to " + branch.toUpperCase() + " " + sem, GOOD, Toast.LENGTH_SHORT);
                            etFileName.setText("");
                            etFileDescription.setText("");
                            tvFile.setText("");
                        }
                        else {
                            showToast("Error " + task.getException(), BAD, Toast.LENGTH_SHORT);
                        }
                        progressBar.setVisibility(View.INVISIBLE);
                    }
                });
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();

        Intent intent = new Intent();
        setResult(RESULT_CANCELED, intent);
        AddNoticeActivity.this.finish();
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