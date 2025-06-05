package com.example.aplicativo_gerente

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton // Importar MaterialButton
import com.google.firebase.auth.FirebaseAuth

class OpcoesActivity : AppCompatActivity() {

    private lateinit var btnMapaRisco: MaterialButton
    private lateinit var btnGerarRelatorios: MaterialButton
    private lateinit var btnLogout: MaterialButton

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_opcoes)

        auth = FirebaseAuth.getInstance()

        btnMapaRisco = findViewById(R.id.btnMapaRisco_xml)
        btnGerarRelatorios = findViewById(R.id.btnGerarRelatorios_xml)
        btnLogout = findViewById(R.id.botaoLogout)

        btnMapaRisco.setOnClickListener {
            startActivity(Intent(this, MapaActivity::class.java))
        }

        btnGerarRelatorios.setOnClickListener {
            startActivity(Intent(this, RelatorioActivity::class.java))
        }

        btnLogout.setOnClickListener {
            auth.signOut()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
}