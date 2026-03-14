package com.example.borrowhub.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.borrowhub.databinding.ActivityLoginBinding;
import com.example.borrowhub.viewmodel.AuthViewModel;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private AuthViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        setupObservers();
        setupListeners();
    }

    private void setupObservers() {
        viewModel.getIsLoading().observe(this, isLoading -> {
            if (isLoading) {
                binding.pbLoading.setVisibility(View.VISIBLE);
                binding.btnLogin.setEnabled(false);
                binding.btnLogin.setText("");
            } else {
                binding.pbLoading.setVisibility(View.GONE);
                binding.btnLogin.setEnabled(true);
                binding.btnLogin.setText(com.example.borrowhub.R.string.sign_in_button);
            }
        });

        viewModel.getErrorMessage().observe(this, errorMsg -> {
            if (errorMsg != null && !errorMsg.isEmpty()) {
                Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupListeners() {
        binding.btnLogin.setOnClickListener(v -> {
            String username = binding.etUsername.getText() != null ? binding.etUsername.getText().toString() : "";
            String password = binding.etPassword.getText() != null ? binding.etPassword.getText().toString() : "";
            
            viewModel.login(username, password).observe(this, isSuccess -> {
                if (Boolean.TRUE.equals(isSuccess)) {
                    Toast.makeText(this, com.example.borrowhub.R.string.login_success, Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(LoginActivity.this, DashboardActivity.class);
                    startActivity(intent);
                    finish();
                }
            });
        });
    }
}
