package com.example.testing

import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.photo).setOnClickListener {
            var intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, 4)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 4) {
            data?.let {
                it.data?.let {
                    supportFragmentManager.beginTransaction()
                        .add(R.id.container, MainFragment(it), "main").commit()
                }
            }
        }
        if (requestCode == 3) {
            data?.let {
                it.data?.let {
                    supportFragmentManager.findFragmentByTag("main")?.onActivityResult(requestCode, resultCode, data)
                }
            }
        }
    }
}
