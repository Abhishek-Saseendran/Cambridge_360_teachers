package com.abhishek.cambridgeappteachers.Fragments;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.abhishek.cambridgeappteachers.MainTeacherActivity;
import com.abhishek.cambridgeappteachers.Models.Controls;
import com.abhishek.cambridgeappteachers.Models.Teacher;
import com.abhishek.cambridgeappteachers.R;
import com.google.android.gms.dynamic.IFragmentWrapper;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;


public class TeacherAdminFragment extends Fragment {

    SwitchMaterial switchAdmin;
    LinearLayout llAdminTrue;
    SwitchMaterial switchFeedback, switchEditPermissions;
    ImageButton ibAddSubjects, ibRemoveSubjects;
    ProgressBar progressBar;
    RelativeLayout rlAdmin, rlFeedback, rlAddSubs, rlRemoveSubs, rlEditPerm;

    FirebaseAuth mAuth;
    FirebaseFirestore firestore;

    Context mContext;

    Teacher teacher;
    Controls control;
    boolean start;

    private static final int GOOD = 1;
    private static final int BAD = -1;

    public TeacherAdminFragment(Context context) {
        // Required empty public constructor
        mContext = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_teacher_admin, container, false);

        switchAdmin = view.findViewById(R.id.switchAdmin);
        //switchAdmin.setChecked(false);

        llAdminTrue = view.findViewById(R.id.llAdminTrue);
        llAdminTrue.setVisibility(View.INVISIBLE);

        switchFeedback = view.findViewById(R.id.switchFeedback);
        switchEditPermissions = view.findViewById(R.id.switchEditPermission);
        ibAddSubjects = view.findViewById(R.id.ibAddSubjects);
        ibRemoveSubjects = view.findViewById(R.id.ibRemoveSubjects);

        rlAdmin = view.findViewById(R.id.rlAdmin);
        rlFeedback = view.findViewById(R.id.rlAdminFeedback);
        rlAddSubs = view.findViewById(R.id.rlAdminAddSubjects);
        rlRemoveSubs = view.findViewById(R.id.rlAdminRemoveSubjects);
        rlEditPerm = view.findViewById(R.id.rlAdminEditPermission);

        progressBar = view.findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);

        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        start = false;
        loadTeacher();
        getControls();

        rlAdmin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                switchAdmin.setChecked(!switchAdmin.isChecked());

            }
        });

        switchAdmin.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b)
                {
                    switchAdmin.setTrackTintList(ColorStateList.valueOf(ContextCompat.getColor(mContext, R.color.colorGreen)));
                }
                else {
                    switchAdmin.setTrackTintList(ColorStateList.valueOf(ContextCompat.getColor(mContext, R.color.colorRed)));
                }
                updateAdminRights(b);
                getControls();
            }
        });

        rlEditPerm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                switchEditPermissions.setChecked(!switchEditPermissions.isChecked());

            }
        });

        switchEditPermissions.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b)
                {
                    switchEditPermissions.setTrackTintList(ColorStateList.valueOf(ContextCompat.getColor(mContext, R.color.colorGreen)));
                }
                else {
                    switchEditPermissions.setTrackTintList(ColorStateList.valueOf(ContextCompat.getColor(mContext, R.color.colorRed)));
                }
                updateEditPermissions(b);
            }
        });

        rlFeedback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                switchFeedback.setChecked(!switchFeedback.isChecked());

            }
        });

        switchFeedback.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b)
                {
                    switchFeedback.setTrackTintList(ColorStateList.valueOf(ContextCompat.getColor(mContext, R.color.colorGreen)));
                }
                else {
                    switchFeedback.setTrackTintList(ColorStateList.valueOf(ContextCompat.getColor(mContext, R.color.colorRed)));
                }
                updateFeedbackEnabled(b);
            }
        });

        ibAddSubjects.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(mContext, com.abhishek.cambridgeappteachers.AddSubjectsActivity.class));
            }
        });

        rlAddSubs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(mContext, com.abhishek.cambridgeappteachers.AddSubjectsActivity.class));
            }
        });

        ibRemoveSubjects.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(mContext, com.abhishek.cambridgeappteachers.RemoveSubjectsActivity.class));
            }
        });

        rlRemoveSubs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(mContext, com.abhishek.cambridgeappteachers.RemoveSubjectsActivity.class));
            }
        });

        return view;
    }

    private void updateFeedbackEnabled(final boolean enableFeedback) {

        progressBar.setVisibility(View.VISIBLE);
        firestore.collection("ADMIN_OPTIONS").document("controls")
                .update("enableFeedback", enableFeedback)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful())
                        {
                            Log.d("Feedback Enabled", enableFeedback + "");
                        }
                        progressBar.setVisibility(View.INVISIBLE);
                    }
                });

    }

    private void updateEditPermissions(final boolean enableEditPermissions) {

        progressBar.setVisibility(View.VISIBLE);
        firestore.collection("ADMIN_OPTIONS").document("controls")
                .update("canEdit", enableEditPermissions)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful())
                        {
                            Log.d("Edit Enabled", enableEditPermissions + "");
                        }
                        progressBar.setVisibility(View.INVISIBLE);
                    }
                });

    }

    private void getControls() {

        firestore.collection("ADMIN_OPTIONS").document("controls")
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (error != null) return;

                        if (value != null){

                            control = value.toObject(Controls.class);
                            switchEditPermissions.setChecked(control.isCanEdit());
                            switchFeedback.setChecked(control.isEnableFeedback());
                        }
                    }
                });

    }

    private void updateAdminRights(final boolean isAdmin) {

        progressBar.setVisibility(View.VISIBLE);
        firestore.collection("TEACHER_LIST").document(mAuth.getCurrentUser().getUid())
                .update("isAdmin", isAdmin)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful())
                        {
                            if (isAdmin)
                            {
                                if (start){
                                    showToast("You are now an Administrator", GOOD, Toast.LENGTH_SHORT);
                                }
                                llAdminTrue.setVisibility(View.VISIBLE);
                            }
                            else {
                                showToast("You are now not an Administrator", GOOD, Toast.LENGTH_SHORT);
                                llAdminTrue.setVisibility(View.INVISIBLE);
                            }
                            start = true;
                        }
                        progressBar.setVisibility(View.INVISIBLE);
                    }
                });

    }

    private void loadTeacher() {

        firestore.collection("TEACHER_LIST").document(mAuth.getCurrentUser().getUid())
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful())
                        {
                            DocumentSnapshot documentSnapshot = task.getResult();
                            teacher = documentSnapshot.toObject(Teacher.class);

                            switchAdmin.setChecked(teacher.isIsAdmin());

                        }
                        progressBar.setVisibility(View.INVISIBLE);
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