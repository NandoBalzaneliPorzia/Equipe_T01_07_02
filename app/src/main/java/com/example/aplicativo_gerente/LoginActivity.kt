package com.example.aplicativo_gerente

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.*

class LoginActivity : AppCompatActivity() {

    private lateinit var editEmail: EditText
    private lateinit var editSenha: EditText
    private lateinit var loginButton: Button
    private lateinit var esqueceuSenhaButton: Button
    private lateinit var progressBar: ProgressBar

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Referência aos elementos da interface
        editEmail = findViewById(R.id.editEmail)
        editSenha = findViewById(R.id.editSenha)
        loginButton = findViewById(R.id.loginButton)
        esqueceuSenhaButton = findViewById(R.id.esqueceuSenhaButton)
        progressBar = findViewById(R.id.progressBar)

        auth = FirebaseAuth.getInstance()

        loginButton.setOnClickListener {
            fazerLogin()
        }

        esqueceuSenhaButton.setOnClickListener {
            val email = editEmail.text.toString().trim()

            if (email.isEmpty()) {
                Toast.makeText(this, "Digite seu e-mail para recuperar a senha", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.sendPasswordResetEmail(email)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "E-mail de recuperação enviado", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Erro ao enviar e-mail: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }

    private fun fazerLogin() {
        val email = editEmail.text.toString().trim()
        val senha = editSenha.text.toString().trim()

        when {
            email.isEmpty() && senha.isEmpty() -> {
                Toast.makeText(this, "Preencha o e-mail e a senha", Toast.LENGTH_SHORT).show()
            }
            email.isEmpty() -> {
                Toast.makeText(this, "Preencha o campo de e-mail", Toast.LENGTH_SHORT).show()
            }
            senha.isEmpty() -> {
                Toast.makeText(this, "Preencha o campo de senha", Toast.LENGTH_SHORT).show()
            }
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                Toast.makeText(this, "Formato de e-mail inválido.", Toast.LENGTH_SHORT).show()
            }
            senha.length < 6 -> {
                Toast.makeText(this, "A senha deve ter no mínimo 6 caracteres.", Toast.LENGTH_SHORT).show()
            }
            else -> {
                // Ambos preenchidos e senha com tamanho válido
                progressBar.visibility = View.VISIBLE

                auth.signInWithEmailAndPassword(email, senha)
                    .addOnCompleteListener { task ->
                        progressBar.visibility = View.GONE
                        if (task.isSuccessful) {
                            Toast.makeText(this, "Login realizado com sucesso", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this, OpcoesActivity::class.java)
                            startActivity(intent)
                            finish()
                        } else {
                            val exception = task.exception
                            val errorMessage = when (exception) {
                                is FirebaseAuthInvalidUserException -> {
                                    "Usuário não encontrado. Verifique o e-mail."
                                }
                                is FirebaseAuthInvalidCredentialsException -> {
                                    when (exception.errorCode) {
                                        "ERROR_USER_NOT_FOUND" -> "Usuário não encontrado. Verifique o e-mail."
                                        "ERROR_WRONG_PASSWORD" -> "Senha incorreta. Tente novamente."
                                        "ERROR_INVALID_EMAIL" -> "Formato de e-mail inválido."
                                        else -> "Credenciais inválidas. Verifique seus dados."
                                    }
                                }
                                else -> {
                                    "Erro ao realizar login. Tente novamente."
                                }
                            }

                            Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        }
    }
}
