package com.abhishek.cambridgeappteachers.Adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
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

import com.abhishek.cambridgeappteachers.Models.NoticeBoardItem;
import com.abhishek.cambridgeappteachers.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.List;

public class NoticeBoardAdapter extends RecyclerView.Adapter<NoticeBoardAdapter.ViewHolder>{

    Context mContext;
    ProgressBar progressBar;

    List<NoticeBoardItem> noticeBoardItemList;
    FirebaseFirestore firestore;
    FirebaseStorage firebaseStorage;
    StorageReference storageReference;
    FirebaseAuth mAuth;

    private static final int GOOD = 1;
    private static final int BAD = -1;
    RelativeLayout root;

    public NoticeBoardAdapter(Context mContext, ProgressBar progressBar, List<NoticeBoardItem> noticeBoardItemList) {
        this.mContext = mContext;
        this.progressBar = progressBar;
        this.noticeBoardItemList = noticeBoardItemList;

        firestore = FirebaseFirestore.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference().child("Noticeboard");
        mAuth = FirebaseAuth.getInstance();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.notice_board_list, parent, false);
        root = view.findViewById(R.id.toast_root);
        return new NoticeBoardAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {

        final NoticeBoardItem notice = noticeBoardItemList.get(position);

        Log.d("\t\tITEM :: ", notice.getName() + " | " + notice.isIsImage() + " | " + mAuth.getCurrentUser().getUid() + " | " + notice.getSetBy());

        holder.tvFileName.setText(notice.getName());
        holder.tvFileDescription.setText(notice.getDescription());
        holder.tvFileAuthor.setText(notice.getAuthor());

        String semClass = notice.getBranch() + " " + notice.getSem();
        holder.tvClass.setText(semClass);

        if (!notice.isIsImage())
            holder.rlImageCropper.setVisibility(View.GONE);
        else {
            if (notice.getFileUrl() != null && !notice.getFileUrl().isEmpty())
            {
                Picasso.get().load(notice.getFileUrl()).placeholder(R.drawable.logo_loadings).into(holder.ivIfImage);
                holder.rlImageCropper.setVisibility(View.VISIBLE);
            }
        }

        switch (notice.getExtension()){

            case "pdf" : holder.ivFileType.setImageResource(R.drawable.logo_pdf);
                break;

            case "docx" : holder.ivFileType.setImageResource(R.drawable.logo_doc);
                break;

            case "png" : holder.ivFileType.setImageResource(R.drawable.logo_png);
                break;

        }

        if (!mAuth.getCurrentUser().getUid().equals(notice.getSetBy())){

            holder.spacer.setVisibility(View.GONE);
            holder.ibDeleteNotice.setEnabled(false);
            holder.ibDeleteNotice.setVisibility(View.GONE);
        }
        else {

            holder.spacer.setVisibility(View.VISIBLE);
            holder.ibDeleteNotice.setEnabled(true);
            holder.ibDeleteNotice.setVisibility(View.VISIBLE);

        }

        holder.rlGoToFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Go View File
                if (!notice.getFileUrl().isEmpty() && notice.getFileUrl() != null)
                    mContext.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(notice.getFileUrl())));
                else
                    showToast("Unable to open File", BAD, Toast.LENGTH_SHORT);
            }
        });

        holder.ivIfImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!notice.getFileUrl().isEmpty() && notice.getFileUrl() != null)
                    mContext.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(notice.getFileUrl())));
                else
                    showToast("Unable to open File", BAD, Toast.LENGTH_SHORT);
            }
        });


        holder.ibDeleteNotice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteFile(notice, position);
            }
        });

    }

    private void deleteFile(final NoticeBoardItem notice, final int position) {

        progressBar.setVisibility(View.VISIBLE);
        storageReference.child(notice.getFileId()).delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        firestore.collection("DEPARTMENT").document(notice.getBranch().toLowerCase() + "_branch")
                                .collection("CLASS").document(notice.getSem() + "_sem")
                                .collection("NOTICE_BOARD").document(notice.getFileId())
                                .delete()
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {

                                        if (task.isSuccessful()){
                                            showToast("Notice successfully deleted", GOOD, Toast.LENGTH_SHORT);
                                            noticeBoardItemList.remove(position);
                                            notifyDataSetChanged();
                                        }
                                        else {
                                            showToast("Error " + task.getException(), BAD, Toast.LENGTH_SHORT);
                                        }
                                        progressBar.setVisibility(View.INVISIBLE);
                                    }
                                });

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        showToast("Unable to delete " + e.getMessage(),BAD, Toast.LENGTH_SHORT);
                        Log.d("\t\tError delete notice", e.toString());
                        progressBar.setVisibility(View.INVISIBLE);
                    }
                });
    }

    @Override
    public int getItemCount() {
        return noticeBoardItemList.size();
    }



    public void filterList(List<NoticeBoardItem> filterList){
        noticeBoardItemList = filterList;
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        public RelativeLayout rlGoToFile, rlImageCropper;
        public ImageView ivFileType, ivIfImage;
        public TextView tvFileName, tvFileDescription, tvFileAuthor, tvClass;
        public ImageButton ibDeleteNotice;
        public View spacer;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            ivIfImage = itemView.findViewById(R.id.ivIfImage);
            rlImageCropper = itemView.findViewById(R.id.rlImageCropper);
            rlGoToFile = itemView.findViewById(R.id.rlGoToFile);
            ivFileType = itemView.findViewById(R.id.ivFileFormat);
            tvFileName = itemView.findViewById(R.id.tvNoticeName);
            tvFileDescription = itemView.findViewById(R.id.tvNoticeData);
            tvFileAuthor = itemView.findViewById(R.id.tvNoticeAuthor);
            ibDeleteNotice = itemView.findViewById(R.id.ibDeleteNotice);
            spacer = itemView.findViewById(R.id.spacer_notice);
            tvClass = itemView.findViewById(R.id.tvClassSem_Notice);

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
