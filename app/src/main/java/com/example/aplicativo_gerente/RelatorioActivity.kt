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
import com.google.firebase.firestore.FirebaseFirestore
import android.graphics.pdf.PdfDocument
import android.os.Environment
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.Color


class RelatorioActivity : AppCompatActivity() {  // Nome da Activity

    private lateinit var spinnerRisco: Spinner      // Componente para selecionar opções        // "private lateinit var" Declara uma variável que será inicializada depois,
    private lateinit var inputData: EditText        // Campo de entrada para a data de filtro   // dentro do onCreate
    private lateinit var btnVisualizarRelatorio: Button  // Aciona a exportação do relatório
    private lateinit var btnVoltar: Button    // Botão de voltar
    private lateinit var txtPreview: TextView // Exibi a pré-visualização do relatório
    private lateinit var btnExportarRelatorio: Button


    override fun onCreate(savedInstanceState: Bundle?) {  // Método obrigatório que executa assim que a tela abre
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_relatorio)  // Vincula o layout XML

        spinnerRisco = findViewById(R.id.spinnerRisco)
        inputData = findViewById(R.id.inputData)
        btnVisualizarRelatorio = findViewById(R.id.btnVisualizarRelatorio)
        btnVoltar = findViewById(R.id.botaoVoltar)
        txtPreview = findViewById(R.id.txtPreview)
        btnExportarRelatorio = findViewById(R.id.btnExportarRelatorio)

        // Cria o adapter a partir do array de strings
        val adapter = ArrayAdapter.createFromResource(
            this,
            R.array.tipos_de_riscos,
            android.R.layout.simple_spinner_item
        )

        // Define o layout para o dropdown
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        // Associa o adapter ao Spinner
        spinnerRisco.adapter = adapter

        // Configura o DatePicker
        inputData.setOnClickListener {
            mostrarDatePicker()
        }

        btnVoltar.setOnClickListener {
            finish() // Fecha a tela e volta para a anterior
        }


        btnVisualizarRelatorio.setOnClickListener {
            buscarERenderizarRelatorio()
        }


        btnExportarRelatorio.setOnClickListener {
            if (txtPreview.text.isNotEmpty()) {
                gerarPdfRelatorio(txtPreview.text.toString())
            } else {
                buscarERenderizarRelatorio()
                gerarPdfRelatorio(txtPreview.text.toString())
            }
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

    private fun buscarERenderizarRelatorio() {
        // Nota: Havia uma dupla declaração de tipoSelecionado e dataSelecionada no código original.
        // Usei as que estavam fora do loop Firestore.
        val filtroTipoSelecionadoSpinner = spinnerRisco.selectedItem.toString() // Ex: "Todos", "Leve", "Médio", "Grave"
        val filtroDataSelecionada = inputData.text.toString().trim()

        if (filtroDataSelecionada.isEmpty()) {
            Toast.makeText(this, "Selecione uma data", Toast.LENGTH_SHORT).show()
            return
        }

        val db = FirebaseFirestore.getInstance()
        db.collection("riscos")
            .get()
            .addOnSuccessListener { documentos ->
                val builder = StringBuilder()
                var riscoIndexGlobal = 0 // Para numerar os riscos que passam no filtro

                for (documento in documentos) {
                    // Ler da estrutura da "foto 1"
                    val nivelRiscoOriginalFB = documento.getString("nivelRisco") ?: "" // Ex: "Baixo"
                    val dataFB = documento.getString("dataRegistro") ?: ""       // Ex: "02/06/2025"
                    val descricaoFB = documento.getString("descricao") ?: ""
                    val tituloFB = documento.getString("titulo") ?: ""
                    // O campo "localizacao" não existe na "foto 1".
                    // Se precisar dele, adicione ao Firebase e leia aqui.
                    // val localFB = documento.getString("localizacao") ?: ""

                    // Mapear o valor de nivelRiscoOriginalFB para ser comparável com o filtroTipoSelecionadoSpinner
                    // Ex: Firebase "Baixo" -> App (Spinner) "Leve"
                    val nivelRiscoMapeadoParaComparacao = when (nivelRiscoOriginalFB.lowercase(Locale.getDefault())) {
                        "baixo" -> "Leve"   // Ajuste "Leve" para o valor exato no seu spinner array
                        "medio" -> "Médio"  // Ajuste "Médio" para o valor exato no seu spinner array
                        "médio" -> "Médio"
                        "grave" -> "Grave"  // Ajuste "Grave" para o valor exato no seu spinner array
                        "alto"  -> "Grave"
                        else -> nivelRiscoOriginalFB // Fallback, pode não corresponder se o spinner for diferente
                    }

                    val condicaoNivel = filtroTipoSelecionadoSpinner.equals("Todos", ignoreCase = true) ||
                            nivelRiscoMapeadoParaComparacao.equals(filtroTipoSelecionadoSpinner, ignoreCase = true)

                    // Certifique-se que o formato da data no Firebase (dataFB)
                    // é o mesmo que o selecionado no input (filtroDataSelecionada)
                    val condicaoData = dataFB == filtroDataSelecionada

                    if (condicaoNivel && condicaoData) {
                        riscoIndexGlobal++
                        builder.appendLine("──────────────────────────────")
                        builder.appendLine("Risco $riscoIndexGlobal")
                        builder.appendLine("Título: $tituloFB")
                        builder.appendLine("Nível: $nivelRiscoOriginalFB") // Exibe o valor original do Firebase
                        // builder.appendLine("Local: $localFB") // Removido pois "localizacao" não está na foto 1
                        builder.appendLine("Data: $dataFB")
                        builder.appendLine("Descrição:")
                        builder.appendLine("$descricaoFB")
                        builder.appendLine("──────────────────────────────\n")
                    }
                }

                txtPreview.text = if (builder.isNotEmpty()) {
                    builder.toString()
                } else {
                    "Nenhum risco encontrado com os filtros selecionados"
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Erro ao carregar os dados: ${it.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun exportarRelatorioParaTxt() {
        val textoRelatorio = txtPreview.text.toString()

        if (textoRelatorio.isBlank()) {
            Toast.makeText(this, "Gere um relatório antes de exportar.", Toast.LENGTH_SHORT).show()
            return
        }

        val nomeArquivo = "relatorio_${System.currentTimeMillis()}.txt"
        val diretorio = getExternalFilesDir(null)
        val arquivo = java.io.File(diretorio, nomeArquivo)

        try {
            arquivo.writeText(textoRelatorio)
            Toast.makeText(this, "Relatório exportado em:\n${arquivo.absolutePath}", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Erro ao salvar o arquivo.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun gerarPdfRelatorio(conteudo: String) {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        val paintTitulo = Paint().apply {
            textSize = 16f
            typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
            color = Color.BLUE
            textAlign = Paint.Align.CENTER
        }

        val paintCorpo = Paint().apply {
            textSize = 12f
            color = Color.BLACK
        }

        // Título centralizado
        canvas.drawText("Relatório de Riscos - WorkSafe", 595 / 2f, 50f, paintTitulo)

        // Conteúdo do relatório
        val linhas = conteudo.split("\n")
        var y = 80f

        for (linha in linhas) {
            if (y > 800) break
            canvas.drawText(linha, 40f, y, paintCorpo)
            y += 20f
        }

        document.finishPage(page)

        try {
            val pasta = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).path)
            if (!pasta.exists()) pasta.mkdirs()

            val timestamp = SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(Date())
            val arquivoPdf = File(pasta, "relatorio_riscos_$timestamp.pdf")

            val outputStream = FileOutputStream(arquivoPdf)
            document.writeTo(outputStream)
            document.close()
            outputStream.close()

            Toast.makeText(this, "PDF gerado em:\n${arquivoPdf.absolutePath}", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Erro ao gerar PDF", Toast.LENGTH_SHORT).show()
        }
    }


}