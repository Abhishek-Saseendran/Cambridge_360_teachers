package com.abhishek.cambridgeappteachers.Fragments;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.abhishek.cambridgeappteachers.Models.Controls;
import com.abhishek.cambridgeappteachers.Models.Teacher;
import com.abhishek.cambridgeappteachers.R;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import de.hdodenhof.circleimageview.CircleImageView;


public class TeacherProfileFragment extends Fragment {

    CircleImageView civTeacherProfileImage;
    FloatingActionButton fabEditProfile;
    TextView tvName, tvEmail, tvID, tvClass;
    TextView tvPhone, tvDOB;
    ProgressBar progressBar;

    FirebaseAuth mAuth;
    FirebaseFirestore firestore;
    FirebaseStorage firebaseStorage;
    StorageTask uploadTask;
    StorageReference storageReference;
    private Uri imageUri;

    Teacher teacher;

    private static final int GOOD = 1;
    private static final int BAD = -1;

    public TeacherProfileFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_teacher_profile, container, false);

        civTeacherProfileImage = view.findViewById(R.id.civTeacherProfileImage_Profile);
        fabEditProfile = view.findViewById(R.id.fabEditProfile_Profile);
        fabEditProfile.setVisibility(View.INVISIBLE);

        tvName = view.findViewById(R.id.tvTeacherProfileName_Profile);
        tvEmail = view.findViewById(R.id.tvTeacherProfileEmail_Profile);
        tvID = view.findViewById(R.id.tvTeacherProfileID_Profile);
        tvPhone = view.findViewById(R.id.tvTeacherProfilePhone_Profile);
        tvDOB = view.findViewById(R.id.tvTeacherProfileDOB_Profile);
        progressBar = view.findViewById(R.id.progressBar);
        progressBar.setVisibility(View.INVISIBLE);

        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference().child("Uploads");

        firestore.collection("TEACHER_LIST")
                .document(mAuth.getCurrentUser().getUid())
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                        if (task.isSuccessful())
                        {
                            DocumentSnapshot documentSnapshot = task.getResult();
                            teacher = documentSnapshot.toObject(Teacher.class);


                            if (teacher.getImageUrl().isEmpty())
                            {
                                if (teacher.getGender().equals("male"))
                                {
                                    Picasso.get().load(R.drawable.ic_boy).into(civTeacherProfileImage);
                                }
                                else {
                                    Picasso.get().load(R.drawable.ic_girl).into(civTeacherProfileImage);
                                }
                            }
                            else {
                                Picasso.get().load(teacher.getImageUrl()).into(civTeacherProfileImage);
                            }
                            tvName.setText(teacher.getName());
                            tvEmail.setText(teacher.getEmail());
                            tvID.setText(teacher.getId());
                            tvPhone.setText(teacher.getPhone());
                            tvDOB.setText(teacher.getDob());

                        }

                    }
                });


        fabEditProfile.setEnabled(false);

        firestore.collection("ADMIN_OPTIONS")
                .document("controls")
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (error != null)
                        {
                            return;
                        }
                        if (value != null)
                        {
                            Controls control = value.toObject(Controls.class);
                            Log.d("\t\tCheck value :: ", String.valueOf(control.isCanEdit()));
                            fabEditProfile.setEnabled(control.isCanEdit());
                            if (control.isCanEdit())
                            {
                                fabEditProfile.setVisibility(View.VISIBLE);
                            }
                            else {
                                fabEditProfile.setVisibility(View.INVISIBLE);
                            }
                        }

                    }
                });



        fabEditProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getContext(),com.abhishek.cambridgeappteachers.EditTeacherActivity.class ));
            }
        });

        civTeacherProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CropImage.activity().setCropShape(CropImageView.CropShape.OVAL).start(getContext(), TeacherProfileFragment.this);
                progressBar.setVisibility(View.VISIBLE);
            }
        });

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == Activity.RESULT_OK)
        {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            imageUri = result.getUri();

            uploadImage();
        }
        else {
            showToast("Something went wrong", BAD, Toast.LENGTH_SHORT);
            progressBar.setVisibility(View.INVISIBLE);
        }
    }

    private void uploadImage() {

        if (imageUri != null)
        {
            final StorageReference fileRef = storageReference.child(mAuth.getCurrentUser().getUid() + ".jpeg");
            uploadTask = fileRef.putFile(imageUri);

            uploadTask.continueWithTask(new Continuation() {
                @Override
                public Object then(@NonNull Task task) throws Exception {
                    if (!task.isSuccessful())
                    {
                        throw task.getException();
                    }
                    return fileRef.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful())
                    {
                        Uri downloadUri = task.getResult();
                        String url = downloadUri.toString();

                        teacher.setImageUrl(url);
                        Picasso.get().load(url).into(civTeacherProfileImage);

                        setImage();

                        progressBar.setVisibility(View.INVISIBLE);
                    }
                    else {
                       showToast("Upload Failed", BAD, Toast.LENGTH_SHORT);
                    }
                }
            });
        }
        else {
            showToast("No Image selected", BAD, Toast.LENGTH_SHORT);
        }

    }

    private void setImage() {

        firestore.collection("TEACHER_LIST").document(mAuth.getCurrentUser().getUid())
                .update("imageUrl", teacher.getImageUrl())
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        showToast("Profile Updated", GOOD, Toast.LENGTH_SHORT);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        showToast("Error Updating!! " + e.getMessage().toString(), BAD, Toast.LENGTH_SHORT);
                        Log.d("Error :::: ", e.toString());
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