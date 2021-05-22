package com.abhishek.cambridgeappteachers.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.abhishek.cambridgeappteachers.Models.Feedbacks;
import com.abhishek.cambridgeappteachers.R;

import java.util.List;

public class FeedBackAdapter extends RecyclerView.Adapter<FeedBackAdapter.ViewHolder>{

    Context mContext;
    List<Feedbacks> feedbacksList;

    public FeedBackAdapter(Context mContext, List<Feedbacks> feedbacksList) {
        this.mContext = mContext;
        this.feedbacksList = feedbacksList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.feedback_list, parent, false);
        return new FeedBackAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Feedbacks feedback = feedbacksList.get(position);

        holder.tvSubjectId.setText(feedback.getSubjectId());
        holder.tvSubjectName.setText(feedback.getSubjectName());

        if (feedback.getNo_of_feedbacks() != 0){
            holder.rbQuestion1.setRating((float) (feedback.getFeedbackScore1() / feedback.getNo_of_feedbacks()));
            holder.rbQuestion2.setRating((float) (feedback.getFeedbackScore2() / feedback.getNo_of_feedbacks()));
            holder.rbQuestion3.setRating((float) (feedback.getFeedbackScore3() / feedback.getNo_of_feedbacks()));
            holder.rbQuestion4.setRating((float) (feedback.getFeedbackScore4() / feedback.getNo_of_feedbacks()));
            holder.rbQuestion5.setRating((float) (feedback.getFeedbackScore5() / feedback.getNo_of_feedbacks()));
            holder.rbQuestion6.setRating((float) (feedback.getFeedbackScore6() / feedback.getNo_of_feedbacks()));
            holder.rbQuestion7.setRating((float) (feedback.getFeedbackScore7() / feedback.getNo_of_feedbacks()));
            holder.rbQuestion8.setRating((float) (feedback.getFeedbackScore8() / feedback.getNo_of_feedbacks()));
            holder.rbQuestion9.setRating((float) (feedback.getFeedbackScore9() / feedback.getNo_of_feedbacks()));
            holder.rbQuestion10.setRating((float) (feedback.getFeedbackScore10() / feedback.getNo_of_feedbacks()));
        }
        else {
            holder.rbQuestion1.setRating(0);
            holder.rbQuestion2.setRating(0);
            holder.rbQuestion3.setRating(0);
            holder.rbQuestion4.setRating(0);
            holder.rbQuestion5.setRating(0);
            holder.rbQuestion6.setRating(0);
            holder.rbQuestion7.setRating(0);
            holder.rbQuestion8.setRating(0);
            holder.rbQuestion9.setRating(0);
            holder.rbQuestion10.setRating(0);
        }

        String totalNumber = "/" + ((int)feedback.getNo_of_feedbacks() );

        String score = ((float) feedback.getFeedbackScore1()) + totalNumber ;
        holder.tvScore1.setText(score);
        score = ((float) feedback.getFeedbackScore2()) + totalNumber ;
        holder.tvScore2.setText(score);
        score = ((float) feedback.getFeedbackScore3()) + totalNumber ;
        holder.tvScore3.setText(score);
        score = ((float) feedback.getFeedbackScore4()) + totalNumber ;
        holder.tvScore4.setText(score);
        score = ((float) feedback.getFeedbackScore5()) + totalNumber ;
        holder.tvScore5.setText(score);
        score = ((float) feedback.getFeedbackScore6()) + totalNumber ;
        holder.tvScore6.setText(score);
        score = ((float) feedback.getFeedbackScore7()) + totalNumber ;
        holder.tvScore7.setText(score);
        score = ((float) feedback.getFeedbackScore8()) + totalNumber ;
        holder.tvScore8.setText(score);
        score = ((float) feedback.getFeedbackScore9()) + totalNumber ;
        holder.tvScore9.setText(score);
        score = ((float) feedback.getFeedbackScore10()) + totalNumber ;
        holder.tvScore10.setText(score);


    }

    @Override
    public int getItemCount() {
        return feedbacksList.size();
    }

    @Override
    public int getItemViewType(int position) {
        //return super.getItemViewType(position);
        return position;
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        public TextView tvSubjectId, tvSubjectName;
        public TextView tvScore1, tvScore2, tvScore3, tvScore4, tvScore5, tvScore6, tvScore7, tvScore8, tvScore9, tvScore10;
        public RatingBar rbQuestion1, rbQuestion2, rbQuestion3, rbQuestion4, rbQuestion5,
                            rbQuestion6, rbQuestion7, rbQuestion8, rbQuestion9, rbQuestion10;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvSubjectId = itemView.findViewById(R.id.tvSubjectId_feedbackList);
            tvSubjectName = itemView.findViewById(R.id.tvSubjectName_feedbackList);
            tvScore1 = itemView.findViewById(R.id.tvScore1);
            tvScore2 = itemView.findViewById(R.id.tvScore2);
            tvScore3 = itemView.findViewById(R.id.tvScore3);
            tvScore4 = itemView.findViewById(R.id.tvScore4);
            tvScore5 = itemView.findViewById(R.id.tvScore5);
            tvScore6 = itemView.findViewById(R.id.tvScore6);
            tvScore7 = itemView.findViewById(R.id.tvScore7);
            tvScore8 = itemView.findViewById(R.id.tvScore8);
            tvScore9 = itemView.findViewById(R.id.tvScore9);
            tvScore10 = itemView.findViewById(R.id.tvScore10);
            rbQuestion1 = itemView.findViewById(R.id.ratBarQuestion1);
            rbQuestion2 = itemView.findViewById(R.id.ratBarQuestion2);
            rbQuestion3 = itemView.findViewById(R.id.ratBarQuestion3);
            rbQuestion4 = itemView.findViewById(R.id.ratBarQuestion4);
            rbQuestion5 = itemView.findViewById(R.id.ratBarQuestion5);
            rbQuestion6 = itemView.findViewById(R.id.ratBarQuestion6);
            rbQuestion7 = itemView.findViewById(R.id.ratBarQuestion7);
            rbQuestion8 = itemView.findViewById(R.id.ratBarQuestion8);
            rbQuestion9 = itemView.findViewById(R.id.ratBarQuestion9);
            rbQuestion10 = itemView.findViewById(R.id.ratBarQuestion10);
        }
    }

}
