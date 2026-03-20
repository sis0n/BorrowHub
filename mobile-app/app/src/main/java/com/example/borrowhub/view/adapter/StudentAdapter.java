package com.example.borrowhub.view.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.borrowhub.R;
import com.example.borrowhub.data.local.entity.StudentEntity;

import java.util.ArrayList;
import java.util.List;

public class StudentAdapter extends RecyclerView.Adapter<StudentAdapter.StudentViewHolder> {

    public interface StudentActionListener {
        void onEditStudent(StudentEntity student);
        void onDeleteStudent(StudentEntity student);
    }

    private final StudentActionListener listener;
    private List<StudentEntity> students = new ArrayList<>();

    public StudentAdapter(StudentActionListener listener) {
        this.listener = listener;
    }

    public void setStudents(List<StudentEntity> students) {
        this.students = students == null ? new ArrayList<>() : new ArrayList<>(students);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public StudentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_student_row, parent, false);
        return new StudentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StudentViewHolder holder, int position) {
        holder.bind(students.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return students.size();
    }

    static class StudentViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvInitial;
        private final TextView tvStudentName;
        private final TextView tvStudentNumber;
        private final TextView tvCourse;
        private final ImageButton btnEdit;
        private final ImageButton btnDelete;

        StudentViewHolder(@NonNull View itemView) {
            super(itemView);
            tvInitial = itemView.findViewById(R.id.tvInitial);
            tvStudentName = itemView.findViewById(R.id.tvStudentName);
            tvStudentNumber = itemView.findViewById(R.id.tvStudentNumber);
            tvCourse = itemView.findViewById(R.id.tvCourse);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }

        void bind(StudentEntity student, StudentActionListener listener) {
            String name = student.name != null ? student.name : "";
            String studentNumber = student.studentNumber != null ? student.studentNumber : "";
            String course = student.course != null ? student.course : "";

            tvStudentName.setText(name);
            tvStudentNumber.setText(studentNumber);
            tvCourse.setText(course);

            // Set first letter of name as the avatar initial
            tvInitial.setText(name.isEmpty() ? "?" : String.valueOf(name.charAt(0)).toUpperCase());

            tvInitial.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.student_avatar_text));

            btnEdit.setOnClickListener(v -> listener.onEditStudent(student));
            btnDelete.setOnClickListener(v -> listener.onDeleteStudent(student));
        }
    }
}
