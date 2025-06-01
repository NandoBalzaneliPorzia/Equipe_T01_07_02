package com.example.aplicativo_gerente  // Define em qual pacote essa classe está

import android.os.Bundle  // Necessário para usar o onCreate
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity  // Superclasse para Activities modernas no Android
import kotlinx.coroutines.FlowPreview
import java.util.Calendar
import android.app.DatePickerDialog
import android.widget.DatePicker



class RelatorioActivity : AppCompatActivity() {  // Nome da Activity

    private lateinit var spinnerRisco: Spinner      // Componente para selecionar opções        // "private lateinit var" Declara uma variável que será inicializada depois,
    private lateinit var inputData: EditText        // Campo de entrada para a data de filtro   // dentro do onCreate
    private lateinit var btnExtrairRelatorio: Button  // Aciona a exportação do relatório
    private lateinit var btnVoltar: Button    // Botão de voltar
    private lateinit var txtPreview: TextView // Exibi a pré-visualização do relatório

    override fun onCreate(savedInstanceState: Bundle?) {  // Método obrigatório que executa assim que a tela abre
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_relatorio)  // Vincula o layout XML

        spinnerRisco = findViewById(R.id.spinnerRisco)
        inputData = findViewById(R.id.inputData)
        btnExtrairRelatorio = findViewById(R.id.btnExtrairRelatorio)
        btnVoltar = findViewById(R.id.botaoVoltar)
        txtPreview = findViewById(R.id.txtPreview)

        // Configura o Spinner
        val opcoes = arrayOf("Todos", "Leve", "Medio", "Grave")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, opcoes)
        spinnerRisco.adapter = adapter

        // Configura o DatePicker
        inputData.setOnClickListener {
            mostrarDatePicker()
        }

        btnVoltar.setOnClickListener {
            finish() // Fecha a tela e volta para a anterior
        }


        btnExtrairRelatorio.setOnClickListener {
            // Simulando geração de relatório
            txtPreview.text = "Relatório gerado com sucesso!"
            Toast.makeText(this, "Relatório exportado!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun mostrarDatePicker() {
        val calendario = Calendar.getInstance()
        val ano = calendario.get(Calendar.YEAR)
        val mes = calendario.get(Calendar.MONTH)
        val dia = calendario.get(Calendar.DAY_OF_MONTH)

        val datePicker = DatePickerDialog(this, { _, selectedAno, selectedMes, selectedDia ->
            val dataFormatada = String.format("%02d/%02d/%04d", selectedDia, selectedMes + 1, selectedAno)
            inputData.setText(dataFormatada)
        }, ano, mes, dia)

        datePicker.show()
    }
}