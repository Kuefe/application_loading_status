package com.udacity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.udacity.databinding.ActivityDetailBinding
import kotlinx.android.synthetic.main.activity_detail.*
import kotlinx.android.synthetic.main.content_detail.*
import timber.log.Timber


class DetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(R.layout.activity_detail)
        setSupportActionBar(toolbar)

        // getting the bundle back from the MainActivity
        val bundle = intent.extras

        // Status form the download
        var status: Boolean? = null
        // Default Value false
        status = bundle!!.getBoolean("status", false)
        if (status) status_text.text = getString(R.string.success) else status_text.text = getString(
                    R.string.failed)

        // filename of the download
        var fileName: String? = null
        // Default value empty string
        fileName = bundle.getString("fileName", "")
        filename_text.text = fileName

        ok_button.setOnClickListener { finish() }
    }

}
