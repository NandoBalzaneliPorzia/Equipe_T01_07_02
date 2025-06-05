package com.example.aplicativo_gerente

import android.app.DatePickerDialog
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class RelatorioActivity : AppCompatActivity() {

    private lateinit var spinnerRisco: Spinner
    private lateinit var inputData: EditText
    private lateinit var btnVisualizarRelatorio: Button
    private lateinit var btnVoltar: Button
    private lateinit var txtPreview: TextView
    private lateinit var btnExportarRelatorio: Button
    private lateinit var spinnerPeriodoFiltro: Spinner

    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_relatorio)

        spinnerRisco = findViewById(R.id.spinnerRisco)
        inputData = findViewById(R.id.inputData)
        btnVisualizarRelatorio = findViewById(R.id.btnVisualizarRelatorio)
        btnVoltar = findViewById(R.id.botaoVoltar)
        txtPreview = findViewById(R.id.txtPreview)
        btnExportarRelatorio = findViewById(R.id.btnExportarRelatorio)
        spinnerPeriodoFiltro = findViewById(R.id.spinnerPeriodoFiltro) // Inicializar

        val adapterRisco = ArrayAdapter.createFromResource(
            this,
            R.array.tipos_de_riscos,
            android.R.layout.simple_spinner_item
        )
        adapterRisco.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerRisco.adapter = adapterRisco

        ArrayAdapter.createFromResource(
            this,
            R.array.periodo_filtro_opcoes,
            android.R.layout.simple_spinner_item
        ).also { periodAdapter ->
            periodAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerPeriodoFiltro.adapter = periodAdapter
        }

        spinnerPeriodoFiltro.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedPeriod = parent.getItemAtPosition(position).toString()
                if (selectedPeriod == "Data específica") {
                    inputData.visibility = View.VISIBLE
                } else {
                    inputData.visibility = View.GONE
                    inputData.setText("") // Limpa data se outra opção for escolhida
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>) {
                inputData.visibility = View.GONE
            }
        }

        inputData.setOnClickListener {
            mostrarDatePicker()
        }

        btnVoltar.setOnClickListener {
            finish()
        }

        btnVisualizarRelatorio.setOnClickListener {
            buscarERenderizarRelatorio()
        }

        btnExportarRelatorio.setOnClickListener {
            val previewText = txtPreview.text.toString()
            if (previewText.isNotEmpty() && previewText != "Pré-visualização do relatório" && !previewText.startsWith("Nenhum risco encontrado")) {
                gerarPdfRelatorio(previewText)
            } else {
                Toast.makeText(this, "Visualize um relatório antes de exportar.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun mostrarDatePicker() {
        val calendario = Calendar.getInstance()
        val ano = calendario.get(Calendar.YEAR)
        val mes = calendario.get(Calendar.MONTH)
        val dia = calendario.get(Calendar.DAY_OF_MONTH)

        val datePicker = DatePickerDialog(this, { _, selectedAno, selectedMes, selectedDia ->
            val dataFormatada = String.format(Locale.getDefault(), "%02d/%02d/%04d", selectedDia, selectedMes + 1, selectedAno)
            inputData.setText(dataFormatada)
        }, ano, mes, dia)
        datePicker.show()
    }

    private fun parseDateString(dateStr: String): Date? {
        return try {
            dateFormat.parse(dateStr)
        } catch (e: Exception) {
            null
        }
    }

    private fun buscarERenderizarRelatorio() {
        val filtroTipoSelecionadoSpinner = spinnerRisco.selectedItem.toString()
        val selectedPeriodOption = spinnerPeriodoFiltro.selectedItem.toString()
        var filtroDataSelecionadaEspecifica = ""

        if (selectedPeriodOption.equals("Data específica")) {
            filtroDataSelecionadaEspecifica = inputData.text.toString().trim()
            if (filtroDataSelecionadaEspecifica.isEmpty()) {
                Toast.makeText(this, "Selecione uma data específica", Toast.LENGTH_SHORT).show()
                return
            }
        }

        val db = FirebaseFirestore.getInstance()
        db.collection("riscos")
            .get()
            .addOnSuccessListener { documentos ->
                val builder = StringBuilder()
                var riscoIndexGlobal = 0

                for (documento in documentos) {
                    val nivelRiscoOriginalFB = documento.getString("nivelRisco") ?: ""
                    val dataFBString = documento.getString("dataRegistro") ?: ""
                    val descricaoFB = documento.getString("descricao") ?: ""
                    val tituloFB = documento.getString("titulo") ?: ""

                    val nivelRiscoMapeadoParaComparacao = when (nivelRiscoOriginalFB.lowercase(Locale.getDefault())) {
                        "baixo" -> "Leve"   // Ex: se o spinner tem "Leve"
                        "medio" -> "Médio"  // Ex: se o spinner tem "Médio"
                        "médio" -> "Médio" // Para cobrir variações com/sem acento
                        "grave" -> "Grave"  // Ex: se o spinner tem "Grave"
                        "alto"  -> "Grave"  // Se "Alto" no DB significa "Grave"
                        else -> nivelRiscoOriginalFB
                    }

                    val condicaoNivel: Boolean
                    if (filtroTipoSelecionadoSpinner.equals("Todos", ignoreCase = true)) {
                        condicaoNivel = true
                    } else {
                        condicaoNivel = nivelRiscoMapeadoParaComparacao.equals(filtroTipoSelecionadoSpinner, ignoreCase = true)
                    }

                    val condicaoData: Boolean
                    val dataDocumentoDate = parseDateString(dataFBString)

                    when {
                        selectedPeriodOption.equals("Todas as datas") -> {
                            condicaoData = true
                        }
                        selectedPeriodOption.equals("Data específica") -> {
                            condicaoData = dataFBString == filtroDataSelecionadaEspecifica
                        }
                        selectedPeriodOption.equals("Últimos 7 dias") -> {
                            condicaoData = if (dataDocumentoDate != null) {
                                val startDate = Calendar.getInstance().apply {
                                    add(Calendar.DAY_OF_YEAR, -6)
                                    set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
                                }.time
                                !dataDocumentoDate.before(startDate)
                            } else false
                        }
                        selectedPeriodOption.equals("Últimos 30 dias") -> {
                            condicaoData = if (dataDocumentoDate != null) {
                                val startDate = Calendar.getInstance().apply {
                                    add(Calendar.DAY_OF_YEAR, -29)
                                    set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
                                }.time
                                !dataDocumentoDate.before(startDate)
                            } else false
                        }
                        else -> {
                            condicaoData = true
                        }
                    }

                    if (condicaoNivel && condicaoData) {
                        riscoIndexGlobal++
                        builder.appendLine("──────────────────────────────")
                        builder.appendLine("Risco $riscoIndexGlobal")
                        builder.appendLine("Título: $tituloFB")
                        builder.appendLine("Nível: $nivelRiscoOriginalFB")
                        builder.appendLine("Data: $dataFBString")
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

        if (textoRelatorio.isBlank() || textoRelatorio == "Pré-visualização do relatório" || textoRelatorio.startsWith("Nenhum risco encontrado")) {
            Toast.makeText(this, "Gere um relatório válido antes de exportar.", Toast.LENGTH_SHORT).show()
            return
        }

        val nomeArquivo = "relatorio_${System.currentTimeMillis()}.txt"
        val diretorio = getExternalFilesDir(null)
        if (diretorio == null) {
            Toast.makeText(this, "Não foi possível acessar o armazenamento.", Toast.LENGTH_SHORT).show()
            return
        }
        if (!diretorio.exists()) {
            diretorio.mkdirs()
        }
        val arquivo = File(diretorio, nomeArquivo)

        try {
            arquivo.writeText(textoRelatorio)
            Toast.makeText(this, "Relatório exportado em:\n${arquivo.absolutePath}", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Erro ao salvar o arquivo TXT.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun gerarPdfRelatorio(conteudo: String) {
        if (conteudo.isBlank() || conteudo == "Pré-visualização do relatório" || conteudo.startsWith("Nenhum risco encontrado")) {
            Toast.makeText(this, "Gere um relatório válido antes de exportar para PDF.", Toast.LENGTH_SHORT).show()
            return
        }

        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4
        var page = document.startPage(pageInfo)
        var canvas = page.canvas //

        val paintTitulo = Paint().apply {
            textSize = 18f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            color = Color.rgb(0, 0, 139)
            textAlign = Paint.Align.CENTER
        }
        val paintCorpo = Paint().apply {
            textSize = 10f
            color = Color.BLACK
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        }

        val xTitulo = pageInfo.pageWidth / 2f
        val topMarginPrimeiraPagina = 70f
        val marginGeral = 40f
        val lineHeight = 15f

        canvas.drawText("Relatório de Riscos - WorkSafe", xTitulo, marginGeral, paintTitulo)
        var yPos = topMarginPrimeiraPagina

        val linhas = conteudo.split("\n")

        for (linha in linhas) {

            if (yPos > pageInfo.pageHeight - marginGeral) {
                document.finishPage(page)
                page = document.startPage(pageInfo)
                canvas = page.canvas
                yPos = marginGeral
            }
            canvas.drawText(linha, marginGeral, yPos, paintCorpo)
            yPos += lineHeight
        }

        document.finishPage(page)

        try {
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val appFolder = File(downloadsDir, "WorkSafeReports")
            if (!appFolder.exists()) {
                appFolder.mkdirs()
            }

            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val arquivoPdf = File(appFolder, "Relatorio_Riscos_WorkSafe_$timestamp.pdf")

            FileOutputStream(arquivoPdf).use { outputStream ->
                document.writeTo(outputStream)
            }
            Toast.makeText(this, "PDF salvo em: ${arquivoPdf.absolutePath}", Toast.LENGTH_LONG).show()

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Erro ao gerar PDF: ${e.message}", Toast.LENGTH_SHORT).show()
        } finally {
            document.close()
        }
    }
}