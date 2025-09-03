package com.nibm.madcw.ui.admin;

import android.app.AlertDialog;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.nibm.madcw.R;
import com.nibm.madcw.data.TuitionDbHelper;
import com.nibm.madcw.model.Teacher;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TeacherAdapter extends RecyclerView.Adapter<TeacherAdapter.TeacherViewHolder> {

    public interface OnTeacherItemClickListener {
        void onViewProfileClicked(Teacher teacher);
        void onAssignClicked(Teacher teacher);
        void onDeleteClicked(Teacher teacher);
    }

    private Context context;
    private List<Teacher> teacherList;
    private OnTeacherItemClickListener listener;

    public TeacherAdapter(Context context, List<Teacher> teacherList, OnTeacherItemClickListener listener) {
        this.context = context;
        this.teacherList = teacherList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public TeacherViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.teacher_item, parent, false);
        return new TeacherViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TeacherViewHolder holder, int position) {
        Teacher teacher = teacherList.get(position);

        holder.textName.setText(teacher.getName());
        holder.textEmail.setText(teacher.getEmail());

        holder.buttonViewProfile.setOnClickListener(v -> {
            if (listener != null) {
                listener.onViewProfileClicked(teacher);
            }
        });

        holder.buttonAssign.setOnClickListener(v -> {
            if (listener != null) {
                listener.onAssignClicked(teacher);
            }
        });

        holder.buttonDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteClicked(teacher);
            }
        });
    }

    @Override
    public int getItemCount() {
        return teacherList.size();
    }

    static class TeacherViewHolder extends RecyclerView.ViewHolder {
        TextView textName, textEmail;
        Button buttonViewProfile, buttonAssign, buttonDelete;

        public TeacherViewHolder(@NonNull View itemView) {
            super(itemView);
            textName = itemView.findViewById(R.id.textTeacherName);
            textEmail = itemView.findViewById(R.id.textTeacherEmail);
            buttonViewProfile = itemView.findViewById(R.id.buttonViewProfile);
            buttonAssign = itemView.findViewById(R.id.buttonAssign);
            buttonDelete = itemView.findViewById(R.id.buttonDelete);

        }
    }
}
