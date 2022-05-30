package com.example.testing

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.testing.ui.main.CanvasView
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream


class MainFragment(var uri: Uri) : Fragment() {

    var bitmap: Bitmap? = null

    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Toast.makeText(context, "adasd", Toast.LENGTH_LONG).show()
        bitmap = MediaStore.Images.Media.getBitmap(context?.contentResolver, uri)
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.main_fragment, container, false)
    }

    var x = false
    lateinit var  canvas: CanvasView
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bitmap?.let {
          canvas = view.findViewById<CanvasView>(R.id.canvas)
            canvas.setBitmap(it)
            view.findViewById<ImageButton>(R.id.bt).setOnClickListener {
               canvas.move = true
               canvas.scale = false
            }
        }
        view.findViewById<ImageButton>(R.id.sv).setOnClickListener {
            val endbitmap = canvas.getBitmap()
            saveToGallery(requireContext(), endbitmap, "my")
        }
        view.findViewById<ImageButton>(R.id.back).setOnClickListener{
            var intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, 3)
        }
        view.findViewById<ImageButton>(R.id.scale).setOnClickListener{
            canvas.scale = true
            canvas.move = false
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 3) {
            data?.let {
                it.data?.let {
                    canvas.setbackground(MediaStore.Images.Media.getBitmap(context?.contentResolver, it))
                }
            }
        }
    }

    fun saveToGallery(context: Context, bitmap: Bitmap, albumName: String) {
        val filename = "${System.currentTimeMillis()}.png"
        val write: (OutputStream) -> Boolean = {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                put(MediaStore.MediaColumns.RELATIVE_PATH, "${Environment.DIRECTORY_DCIM}/$albumName")
            }

            context.contentResolver.let {
                it.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)?.let { uri ->
                    it.openOutputStream(uri)?.let(write)
                }
            }
        } else {
            val imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString() + File.separator + albumName
            val file = File(imagesDir)
            if (!file.exists()) {
                file.mkdir()
            }
            val image = File(imagesDir, filename)
            write(FileOutputStream(image))
        }
    }

}