package com.example.aplicativo_gerente

import android.content.Intent  // Permite navegar entre as telas
import android.os.Bundle       // Carrega os dados ao criar a Activity
import android.widget.Button   // Para usar os botões da interface
import androidx.appcompat.app.AppCompatActivity  // Classe base para activities modernas no Android
import com.google.firebase.auth.FirebaseAuth

class OpcoesActivity : AppCompatActivity() {

    // Declara 4 váriáveis para botões de interface
    private lateinit var btnCadastrarFuncionario: Button
    private lateinit var btnMapaRisco: Button
    private lateinit var btnGerarRelatorios: Button
    private lateinit var btnLogout: Button

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_opcoes)

        // Inicializa Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Referência aos botões
        btnCadastrarFuncionario = findViewById(R.id.primeiraOpcao)
        btnMapaRisco = findViewById(R.id.segundaOpcao)
        btnGerarRelatorios = findViewById(R.id.terceiraOpcao)
        btnLogout = findViewById(R.id.botaoLogout)

        btnCadastrarFuncionario.setOnClickListener {
            // TODO: criar a tela ou funcionalidade de cadastro se quiser
            // startActivity(Intent(this, CadastroActivity::class.java))
        }

        btnMapaRisco.setOnClickListener {
            startActivity(Intent(this, MapaActivity::class.java))
        }

        btnGerarRelatorios.setOnClickListener {
            startActivity(Intent(this, RelatorioActivity::class.java))
        }

        btnLogout.setOnClickListener {
            val intent = Intent(this,LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
}

