package com.example.aplicativo_gerente

import android.content.Intent  // Permite navegar entre as telas
import android.os.Bundle       // Carrega os dados ao criar a Activity
import android.widget.Button   // Para usar os botões da interface
import androidx.appcompat.app.AppCompatActivity  // Classe base para activities modernas no Android

class OpcoesActivity : AppCompatActivity() {

    // Declara 4 váriáveis para botões de interface
    private lateinit var btnCadastrarFuncionario: Button
    private lateinit var btnMapaRisco: Button
    private lateinit var btnGerarRelatorios: Button
    private lateinit var btnVoltar: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_opcoes)

        btnCadastrarFuncionario = findViewById(R.id.primeiraOpcao)
        btnMapaRisco = findViewById(R.id.segundaOpcao)
        btnGerarRelatorios = findViewById(R.id.terceiraOpcao)
        btnVoltar = findViewById(R.id.botaoVoltar)

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

        btnVoltar.setOnClickListener {
            finish()
        }
    }
}

