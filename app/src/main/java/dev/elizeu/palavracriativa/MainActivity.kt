package dev.elizeu.palavracriativa

import android.content.ContentValues
import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.webkit.*
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var meuWebView: WebView
    private var imageFilePath: String? = null
    private lateinit var cameraActivityResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var galleryActivityResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var storagePermissionLauncher: ActivityResultLauncher<String>

    private var mFilePathCallback: ValueCallback<Array<Uri>>? = null
    private lateinit var fileChooserActivityResultLauncher: ActivityResultLauncher<Intent>

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        meuWebView = findViewById(R.id.meu_webview)
        val webSettings = meuWebView.settings
        webSettings.javaScriptEnabled = true
        webSettings.domStorageEnabled = true

        // Adicione esta linha para garantir que o JavaScript funcione corretamente.
        // Ele lida com caixas de diálogo e outras funções do navegador.
        // Inicializa o novo launcher para a seleção de arquivos
        fileChooserActivityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            var results: Array<Uri>? = null
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.dataString?.let {
                    results = arrayOf(Uri.parse(it))
                }
            }
            mFilePathCallback?.onReceiveValue(results)
            mFilePathCallback = null
        }

        meuWebView.webChromeClient = object : WebChromeClient() {
            override fun onShowFileChooser(
                webView: WebView,
                filePathCallback: ValueCallback<Array<Uri>>,
                fileChooserParams: FileChooserParams
            ): Boolean {
                // Guarda o callback para usar depois
                mFilePathCallback?.onReceiveValue(null)
                mFilePathCallback = filePathCallback

                // Cria a Intent para selecionar arquivos
                val intent = fileChooserParams.createIntent()
                intent.addCategory(Intent.CATEGORY_OPENABLE)

                // Lança o seletor de arquivos e obtém o resultado no launcher
                try {
                    fileChooserActivityResultLauncher.launch(intent)
                } catch (e: Exception) {
                    mFilePathCallback?.onReceiveValue(null)
                    mFilePathCallback = null
                    return false
                }
                return true
            }
        }

        // Registre os ActivityResultLaunchers para tratar os resultados das Intents.
        cameraActivityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                imageFilePath?.let { path ->
                    val uri = Uri.fromFile(File(path))
                    meuWebView.evaluateJavascript("javascript:onImageSelected('$uri')", null)
                    imageFilePath = null
                }
            } else {
                Toast.makeText(this, "Captura de imagem cancelada", Toast.LENGTH_SHORT).show()
            }
        }

        galleryActivityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.let { uri ->
                    val inputStream = contentResolver.openInputStream(uri)
                    val bytes = inputStream?.readBytes()
                    inputStream?.close()
                    if (bytes != null) {
                        val base64Image = Base64.encodeToString(bytes, Base64.DEFAULT)
                        meuWebView.evaluateJavascript("javascript:onImageSelected('data:image/jpeg;base64,$base64Image')", null)
                    }
                }
            } else {
                Toast.makeText(this, "Seleção de imagem da galeria cancelada", Toast.LENGTH_SHORT).show()
            }
        }

        storagePermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                Toast.makeText(this, "Permissão de armazenamento concedida", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Permissão de armazenamento negada", Toast.LENGTH_SHORT).show()
            }
        }

        // Adicione a interface JavaScript ao WebView para a comunicação bidirecional.
        meuWebView.addJavascriptInterface(WebAppInterface(this), "Android")

        // Carregue a URL do seu site.
        meuWebView.loadUrl("file:///android_asset/index.html")
    }

    inner class WebAppInterface(private val mContext: MainActivity) {
        @JavascriptInterface
        fun openCamera() {
            Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
                takePictureIntent.resolveActivity(packageManager)?.also {
                    val photoFile: File? = try {
                        createImageFile()
                    } catch (ex: IOException) {
                        null
                    }
                    photoFile?.also {
                        val photoURI: Uri = FileProvider.getUriForFile(
                            mContext,
                            "dev.elizeu.palavracriativa.fileprovider",
                            it
                        )
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                        cameraActivityResultLauncher.launch(takePictureIntent)
                    }
                }
            }
        }

        @JavascriptInterface
        fun openGallery() {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            galleryActivityResultLauncher.launch(intent)
        }

        @JavascriptInterface
        fun saveImage(base64Image: String) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q ||
                ContextCompat.checkSelfPermission(mContext, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                saveImageToGallery(base64Image)
            } else {
                storagePermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }

        @Throws(IOException::class)
        private fun createImageFile(): File {
            val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            return File.createTempFile(
                "JPEG_${timeStamp}_",
                ".jpg",
                storageDir
            ).apply {
                imageFilePath = absolutePath
            }
        }

        private fun saveImageToGallery(base64Image: String) {
            try {
                val imageDataBytes = Base64.decode(base64Image.substring(base64Image.indexOf(",") + 1), Base64.DEFAULT)
                val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                val fileName = "EditedImage_$timeStamp.jpg"
                val values = ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                    put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + File.separator + "YourAppName")
                        put(MediaStore.Images.Media.IS_PENDING, 1)
                    } else {
                        val directory = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "YourAppName")
                        directory.mkdirs()
                        val file = File(directory, fileName)
                        put(MediaStore.Images.Media.DATA, file.absolutePath)
                    }
                }

                val uri: Uri? = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                if (uri != null) {
                    contentResolver.openOutputStream(uri).use { outputStream ->
                        outputStream?.write(imageDataBytes)
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        values.clear()
                        values.put(MediaStore.Images.Media.IS_PENDING, 0)
                        contentResolver.update(uri, values, null, null)
                    }
                    runOnUiThread {
                        Toast.makeText(mContext, "Imagem salva na galeria!", Toast.LENGTH_LONG).show()
                        meuWebView.evaluateJavascript("javascript:onImageSaved(true, '$uri')", null)
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(mContext, "Erro ao salvar a imagem.", Toast.LENGTH_LONG).show()
                        meuWebView.evaluateJavascript("javascript:onImageSaved(false, null)", null)
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
                runOnUiThread {
                    Toast.makeText(mContext, "Erro ao salvar a imagem: ${e.message}", Toast.LENGTH_LONG).show()
                    meuWebView.evaluateJavascript("javascript:onImageSaved(false, null)", null)
                }
            }
        }
    }
}