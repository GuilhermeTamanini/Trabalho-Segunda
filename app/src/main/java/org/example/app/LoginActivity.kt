package org.example.app

import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import org.example.app.databinding.ActivityLoginBinding


class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text?.toString()?.trim().orEmpty()
            val password = binding.etPassword.text?.toString().orEmpty()

            val error = validateLogin(email, password)
            showToast(error ?: "Login successful (demo)")
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun validateLogin(email: String, password: String): String? {
        return when {
            email.isEmpty() -> "Please enter email"
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> "Enter a valid email"
            password.isEmpty() -> "Please enter password"
            password.length < 6 -> "Password must be at least 6 characters"
            else -> null
        }
    }
}
