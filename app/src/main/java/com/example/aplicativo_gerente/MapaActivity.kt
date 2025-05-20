package com.example.aplicativo_gerente

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MapaActivity : AppCompatActivity() {

    private lateinit var btnTodos: Button
    private lateinit var btnLeves: Button
    private lateinit var btnMedios: Button
    private lateinit var btnGraves: Button
    private lateinit var btnExportarMapa: Button
    private lateinit var btnVoltar: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mapa)

        btnTodos = findViewById(R.id.btnTodos)
        btnLeves = findViewById(R.id.btnLeves)
        btnMedios = findViewById(R.id.btnMedios)
        btnGraves = findViewById(R.id.btnGraves)
        btnExportarMapa = findViewById(R.id.btnExportarMapa)
        btnVoltar = findViewById(R.id.botaoVoltar)

        btnTodos.setOnClickListener {
            Toast.makeText(this, "Exibindo todos os riscos", Toast.LENGTH_SHORT).show()
        }

        btnLeves.setOnClickListener {
            Toast.makeText(this, "Exibindo riscos leves", Toast.LENGTH_SHORT).show()
        }

        btnMedios.setOnClickListener {
            Toast.makeText(this, "Exibindo riscos médios", Toast.LENGTH_SHORT).show()
        }

        btnGraves.setOnClickListener {
            Toast.makeText(this, "Exibindo riscos graves", Toast.LENGTH_SHORT).show()
        }

        btnExportarMapa.setOnClickListener {
            Toast.makeText(this, "Exportando mapa...", Toast.LENGTH_SHORT).show()
        }

        btnVoltar.setOnClickListener {
            finish()
        }
    }
}
