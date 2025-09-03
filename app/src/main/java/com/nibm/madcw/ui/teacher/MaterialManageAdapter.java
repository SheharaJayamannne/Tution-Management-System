package com.nibm.madcw.ui.teacher;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.nibm.madcw.R;
import com.nibm.madcw.data.TuitionDbHelper;
import com.nibm.madcw.model.Material;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.List;

public class MaterialManageAdapter extends RecyclerView.Adapter<MaterialManageAdapter.ViewHolder> {

    private Context context;
    private List<Material> materialList;
    private TuitionDbHelper dbHelper;

    public MaterialManageAdapter(Context context, List<Material> materialList) {
        this.context = context;
        this.materialList = materialList;
        this.dbHelper = new TuitionDbHelper(context);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.material_manage_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Material material = materialList.get(position);
        holder.textTitle.setText(material.getTitle());

        holder.buttonView.setOnClickListener(v -> {
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.parse(material.getFilePath()), "*/*");
                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                context.startActivity(Intent.createChooser(intent, "Open with"));
            } catch (Exception e) {
                Toast.makeText(context, "Unable to open file", Toast.LENGTH_SHORT).show();
            }
        });

        holder.buttonEdit.setOnClickListener(v -> {
            showEditDialog(material, position);
        });

        holder.buttonDelete.setOnClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle("Delete Material")
                    .setMessage("Are you sure you want to delete this material?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        SQLiteDatabase db = dbHelper.getWritableDatabase();
                        db.delete("materials", "title = ? AND file_path = ?",
                                new String[]{material.getTitle(), material.getFilePath()});
                        materialList.remove(position);
                        notifyItemRemoved(position);
                        Toast.makeText(context, "Material deleted", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        holder.buttonDownload.setOnClickListener(v -> {
            try {
                Uri uri = Uri.parse(material.getFilePath());
                InputStream inputStream = context.getContentResolver().openInputStream(uri);

                File downloadDir = context.getExternalFilesDir(null);
                File file = new File(downloadDir, material.getTitle().replace(" ", "_") + ".pdf"); // default to pdf

                FileOutputStream outputStream = new FileOutputStream(file);
                byte[] buffer = new byte[1024];
                int len;
                while ((len = inputStream.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, len);
                }

                inputStream.close();
                outputStream.close();

                Toast.makeText(context, "File downloaded to: " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(context, "Download failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showEditDialog(Material material, int position) {
        EditText input = new EditText(context);
        input.setText(material.getTitle());

        new AlertDialog.Builder(context)
                .setTitle("Edit Material Title")
                .setView(input)
                .setPositiveButton("Save", (dialog, which) -> {
                    String newTitle = input.getText().toString().trim();
                    if (!newTitle.isEmpty()) {
                        SQLiteDatabase db = dbHelper.getWritableDatabase();
                        db.execSQL("UPDATE materials SET title = ? WHERE title = ? AND file_path = ?",
                                new Object[]{newTitle, material.getTitle(), material.getFilePath()});
                        material.setTitle(newTitle);
                        notifyItemChanged(position);
                        Toast.makeText(context, "Title updated", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public int getItemCount() {
        return materialList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textTitle;
        ImageButton buttonView, buttonEdit, buttonDelete, buttonDownload;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textTitle = itemView.findViewById(R.id.textTitle);
            buttonView = itemView.findViewById(R.id.buttonView);
            buttonEdit = itemView.findViewById(R.id.buttonEdit);
            buttonDelete = itemView.findViewById(R.id.buttonDelete);
            buttonDownload = itemView.findViewById(R.id.buttonDownload);
        }
    }
}
