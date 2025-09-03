package com.nibm.madcw.ui.student;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.nibm.madcw.R;
import com.nibm.madcw.model.Submission;

import java.util.List;

public class ResultsViewAdapter extends RecyclerView.Adapter<ResultsViewAdapter.ViewHolder> {

    private final List<Submission> resultsList;

    public ResultsViewAdapter(List<Submission> resultsList) {
        this.resultsList = resultsList;
    }

    @NonNull
    @Override
    public ResultsViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.results_item_student, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ResultsViewAdapter.ViewHolder holder, int position) {
        Submission submission = resultsList.get(position);

        holder.textAssignment.setText(submission.getAssignmentTitle());
        if (submission.getMarks() >= 0) {
            holder.textMarks.setText("Marks: " + submission.getMarks());
        } else {
            holder.textMarks.setText("Marks: Pending");
        }
    }

    @Override
    public int getItemCount() {
        return resultsList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textAssignment, textMarks;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textAssignment = itemView.findViewById(R.id.textAssignmentTitleStudent);
            textMarks = itemView.findViewById(R.id.textMarksStudent);
        }
    }
}

