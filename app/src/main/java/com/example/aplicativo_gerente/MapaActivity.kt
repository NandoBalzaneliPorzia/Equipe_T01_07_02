package com.example.aplicativo_gerente

import android.content.ContentValues
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*
import android.graphics.Bitmap
import android.graphics.Canvas
import java.io.File
import java.io.FileOutputStream
import android.graphics.Color
import android.graphics.pdf.PdfDocument
import android.view.View
import android.widget.*
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.content.ContextCompat
import com.google.firebase.firestore.FirebaseFirestore

class MapaActivity : AppCompatActivity() {

    // Referência dos botões
    private lateinit var btnTodos: Button
    private lateinit var btnLeves: Button
    private lateinit var btnMedios: Button
    private lateinit var btnGraves: Button
    private lateinit var btnExportarMapa: Button
    private lateinit var btnVoltar: Button

    // Referência ao mapa
    private lateinit var mapaContainer: FrameLayout
    private lateinit var imagemMapa: ImageView

    // Firestore
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mapa)

        // Inicializando o Firestore
        db = FirebaseFirestore.getInstance()

        // Referência aos botões
        btnTodos = findViewById(R.id.btnTodos)
        btnLeves = findViewById(R.id.btnLeves)
        btnMedios = findViewById(R.id.btnMedios)
        btnGraves = findViewById(R.id.btnGraves)
        btnExportarMapa = findViewById(R.id.btnExportarMapa)
        btnVoltar = findViewById(R.id.botaoVoltar)

        // Referência ao mapa
        mapaContainer = findViewById(R.id.mapaContainer)
        imagemMapa = findViewById(R.id.imagemMapa)

        btnTodos.setOnClickListener {
            carregarRiscos("todos")
        }
        btnLeves.setOnClickListener {
            carregarRiscos("leve")
        }
        btnMedios.setOnClickListener {
            carregarRiscos("medio")
        }
        btnGraves.setOnClickListener {
            carregarRiscos("grave")
        }

        btnExportarMapa.setOnClickListener {
            mostrarDialogoExportacao()
        }

        btnVoltar.setOnClickListener {
            finish()
        }
    }

    private fun carregarRiscos(filtro: String){
        // Limpa qualquer bolinha anterior no mapa
        mapaContainer.removeViews(1, mapaContainer.childCount - 1)

        // Faz a busca na coleção "riscos" no bd
        db.collection("riscos")
            .get()
            .addOnSuccessListener { documentos ->
                for (documento in documentos) {
                    val tipo = documento.getString("nivel") ?: ""  // nível = grave, leve, médio

                    val coordenadas = documento.get("coordenadas") as? Map<*, *>
                    val lat = coordenadas?.get("latitude") as? Double
                    val lon = coordenadas?.get("longitude") as? Double

                    if (lat != null && lon != null) {
                        val (x, y) = normalizarCoordenadas(lat, lon)

                        val dadosValidos = tipo in listOf("leve", "medio", "grave") &&
                                x in 0.0..1.0 && y in 0.0..1.0

                        if (dadosValidos && (filtro == "todos" || filtro.equals(tipo, ignoreCase = true))) {
                            adicionarBolinha(x, y, tipo)

                        }
                    }
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Erro ao carregar os riscos", Toast.LENGTH_SHORT).show()
            }
    }

    fun normalizarCoordenadas(latitude: Double, longitude: Double): Pair<Double, Double> {
        val latMin = -22.8382 // ficou mais baixo
        val latMax = -22.8351 // ficou mais alto
        val lonMin = -47.0783 // mais à esquerda
        val lonMax = -47.0759 // mais à direita

        val y = 1.0 - ((latitude - latMin) / (latMax - latMin))
        val x = (longitude - lonMin) / (lonMax - lonMin)

        return Pair(x, y)
    }



    private fun adicionarBolinha(x: Double, y: Double, tipo:String) {
        val bolinha = View(this)

        // Tamanho da bolinha
        val tamanho = 50

        val params = FrameLayout.LayoutParams(tamanho, tamanho)
        // Converter as coordenadas proporcionais para pixels
        params.leftMargin = (x * mapaContainer.width).toInt() - tamanho / 2
        params.topMargin = (y * mapaContainer.height).toInt() - tamanho / 2

        bolinha.layoutParams = params

// Define a cor da bolinha de acordo com o tipo de risco (mantendo o shape)
        val drawable = ContextCompat.getDrawable(this, R.drawable.bolinha_shape)?.mutate()
        val wrappedDrawable = DrawableCompat.wrap(drawable!!)

        val cor = when (tipo.lowercase()) {
            "leve" -> Color.parseColor("#4CAF50") // Verde
            "medio" -> Color.parseColor("#FF9800") // Laranja
            "grave" -> Color.parseColor("#F44336") // Vermelho
            else -> Color.GRAY
        }

        DrawableCompat.setTint(wrappedDrawable, cor)
        bolinha.background = wrappedDrawable

        // Adicionar a bolinha no mapa (dentro do FrameLayout)
        mapaContainer.addView(bolinha)
    }

    private fun mostrarDialogoExportacao() {
        val opcoes = arrayOf("Imagem (PNG)", "PDF")

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Escolha o formato de exportação")
            .setItems(opcoes) { _, which ->
                when (which) {
                    0 -> exportarMapaComoImagem() // Imagem
                    1 -> exportarMapaComoPDF()    // PDF
                    2 -> exportarMapaParaGaleria() // Salvar na galeria
                }
            }
        builder.create().show()
    }

    private fun exportarMapaComoImagem() {
        if (mapaContainer.width == 0 || mapaContainer.height == 0) {
            Toast.makeText(this, "O mapa ainda não está pronto para exportar.", Toast.LENGTH_SHORT).show()
            return
        }

        val bitmap = Bitmap.createBitmap(
            mapaContainer.width,
            mapaContainer.height,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        mapaContainer.draw(canvas)

        try {
            val caminho = getExternalFilesDir(null)?.absolutePath
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val arquivo = File(caminho, "mapa_riscos_$timestamp.png")

            val outputStream = FileOutputStream(arquivo)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            outputStream.flush()
            outputStream.close()

            Toast.makeText(this, "Mapa exportado em: ${arquivo.absolutePath}", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Erro ao exportar o mapa", Toast.LENGTH_SHORT).show()
        }
    }

    private fun exportarMapaComoPDF() {
        if (mapaContainer.width == 0 || mapaContainer.height == 0) {
            Toast.makeText(this, "O mapa ainda não está pronto para exportar.", Toast.LENGTH_SHORT).show()
            return
        }

        val bitmap = Bitmap.createBitmap(
            mapaContainer.width,
            mapaContainer.height,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        mapaContainer.draw(canvas)

        try {
            val caminho = getExternalFilesDir(null)?.absolutePath
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val arquivoPDF = File(caminho, "Mapa_riscos_$timestamp.pdf")

            val document = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(
                bitmap.width, bitmap.height, 1
            ).create()

            val page = document.startPage(pageInfo)
            val pdfCanvas = page.canvas

            pdfCanvas.drawBitmap(bitmap, 0f, 0f, null)

            document.finishPage(page)

            val outputStream = FileOutputStream(arquivoPDF)
            document.writeTo(outputStream)
            document.close()

            outputStream.close()

            Toast.makeText(this, "PDF salvo em: ${arquivoPDF.absolutePath}", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Erro ao exportar PDF", Toast.LENGTH_SHORT).show()
        }
    }

    private fun exportarMapaParaGaleria() {
        if (mapaContainer.width == 0 || mapaContainer.height == 0) {
            Toast.makeText(this, "O mapa ainda não está pronto para exportar.", Toast.LENGTH_SHORT).show()
            return
        }

        val bitmap = Bitmap.createBitmap(
            mapaContainer.width,
            mapaContainer.height,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        mapaContainer.draw(canvas)

        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val filename = "mapa_riscos_$timestamp.png"
        var outputStream: OutputStream? = null

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val resolver = contentResolver
                val contentValues = ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, filename)
                    put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                    put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/WorkSafe")
                    put(MediaStore.Images.Media.IS_PENDING, 1)
                }

                val imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

                if (imageUri != null) {
                    outputStream = resolver.openOutputStream(imageUri)
                }

                contentValues.clear()
                contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
                resolver.update(imageUri!!, contentValues, null, null)

            } else {
                val imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString() + "/WorkSafe"
                val file = File(imagesDir)
                if (!file.exists()) {
                    file.mkdirs()
                }
                val image = File(file, filename)
                outputStream = FileOutputStream(image)
            }

            outputStream?.use {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
                Toast.makeText(this, "Mapa exportado para Galeria/WorkSafe", Toast.LENGTH_LONG).show()
            }

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Erro ao exportar mapa", Toast.LENGTH_SHORT).show()
        }
    }
}