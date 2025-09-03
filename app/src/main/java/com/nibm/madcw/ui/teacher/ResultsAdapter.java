package com.nibm.madcw.ui.teacher;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.nibm.madcw.R;
import com.nibm.madcw.model.Submission;

import java.util.List;

public class ResultsAdapter extends RecyclerView.Adapter<ResultsAdapter.ResultViewHolder> {

    private Context context;
    private List<Submission> submissionList;
    private OnMarkSubmitListener listener;

    public interface OnMarkSubmitListener {
        void onMarkSubmitted(int submissionId, int marks);
    }

    public ResultsAdapter(Context context, List<Submission> submissionList, OnMarkSubmitListener listener) {
        this.context = context;
        this.submissionList = submissionList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ResultViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.result_item, parent, false);
        return new ResultViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ResultViewHolder holder, int position) {
        Submission submission = submissionList.get(position);

        holder.textStudentName.setText("Student: " + submission.studentName);
        holder.textAssignmentTitle.setText("Assignment: " + submission.assignmentTitle);

        if (submission.marks >= 0) {
            holder.editMarks.setText(String.valueOf(submission.marks));
        } else {
            holder.editMarks.setText("");
        }

        holder.buttonSubmit.setOnClickListener(v -> {
            String marksStr = holder.editMarks.getText().toString().trim();
            if (TextUtils.isEmpty(marksStr)) {
                Toast.makeText(context, "Enter marks", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                int marks = Integer.parseInt(marksStr);
                if (marks < 0 || marks > 100) {
                    Toast.makeText(context, "Marks must be 0-100", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (listener != null) {
                    listener.onMarkSubmitted(submission.id, marks);
                }

            } catch (NumberFormatException e) {
                Toast.makeText(context, "Invalid number", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return submissionList.size();
    }

    static class ResultViewHolder extends RecyclerView.ViewHolder {
        TextView textStudentName, textAssignmentTitle;
        EditText editMarks;
        Button buttonSubmit;

        public ResultViewHolder(@NonNull View itemView) {
            super(itemView);
            textStudentName = itemView.findViewById(R.id.textStudentName);
            textAssignmentTitle = itemView.findViewById(R.id.textAssignmentTitle);
            editMarks = itemView.findViewById(R.id.editMarks);
            buttonSubmit = itemView.findViewById(R.id.buttonSubmitMarks);
        }
    }
}

