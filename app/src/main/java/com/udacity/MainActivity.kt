package com.udacity

import android.Manifest
import android.app.DownloadManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.*
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.android.synthetic.main.content_main.view.*
import timber.log.Timber
import timber.log.Timber.DebugTree


class MainActivity : AppCompatActivity() {

    private var downloadID: Long = 0

    private lateinit var notificationManager: NotificationManager

    private var url = ""
    private var fileName = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        Timber.plant(DebugTree())

        // Call create channel
        createChannel(
            getString(R.string.download_notification_channel_id),
            getString(R.string.download_notification_channel_name)
        )

        registerReceiver(receiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))

        // initialized notificationManager
        notificationManager = ContextCompat.getSystemService(
            this,
            NotificationManager::class.java
        ) as NotificationManager

        // check permission
        isStoragePermissionGranted()

        custom_button.setOnClickListener {
            // if no radio button is selected then toast message
            if (url == "") {
                Toast.makeText(
                    applicationContext,
                    R.string.no_file_selected,
                    Toast.LENGTH_SHORT
                )
                    .show()
            } else {
                download()
            }
        }
    }

    // https://stackoverflow.com/questions/33162152/storage-permission-error-in-marshmallow
    fun isStoragePermissionGranted(): Boolean {
        Timber.i("isStoragePermissionGranted")
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED
            ) {
                true
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    1
                )
                false
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            Timber.i("Permission is granted")
            true
        }
    }


    // This function is called when the user accepts or decline the permission.
    // Request Code is used to check which permission called this function.
    // This request code is provided when the user is prompt for permission.
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        Timber.i("onRequestPermissionsResult")
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Timber.i("Permission is granted")
        } else {
            // deactivate button when permission is not granted
            custom_button.isClickable = false
            permissionDialog()
        }
    }

    private fun permissionDialog() {
        Timber.i("permissionDialog")
        val builder = AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.no_permission))
        builder.setMessage(R.string.reason_for_permission)
        builder.apply {
            setPositiveButton(
                getString(R.string.okay),
                DialogInterface.OnClickListener { dialog, which -> })
        }
        builder.show()
    }

    // send the notification
    private fun sendNotifcation(status: Boolean) {

        notificationManager.sendNotification(
            status,
            fileName,
            this
        )
    }

    // broadcastReceiver end of the download
    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            Timber.i("onReceive")
            val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            if (id != null) {
                val status: Boolean = checkStatus(id)
                sendNotifcation(status)
            }
        }
    }

    // download the selected file
    private fun download() {
        Timber.i("download")
        val request =
            DownloadManager.Request(Uri.parse(url))
                .setTitle(getString(R.string.app_name))
                .setDescription(getString(R.string.app_description))
                .setRequiresCharging(false)
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true)
                .setDestinationInExternalPublicDir(
                    Environment.DIRECTORY_DOWNLOADS,
                    "master.zip"
                )

        val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        downloadID =
            downloadManager.enqueue(request)// enqueue puts the download request in the queue.
    }

    // Check status
    private fun checkStatus(downloadId: Long): Boolean {
        Timber.i("validDownload")
        val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        //Verify if download is a success
        val cursor: Cursor =
            downloadManager.query(DownloadManager.Query().setFilterById(downloadId))
        if (cursor.moveToFirst()) {
            val status: Int = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))
            if (status == DownloadManager.STATUS_SUCCESSFUL) return true
        }
        return false
    }

    // handle the radioButton
    fun onRadioButtonClicked(view: View) {
        Timber.i("onRadioButtonClicked")

        if (view is RadioButton) {
            // Is the button now checked?
            val checked = view.isChecked

            // Check which radio button was clicked
            when (view.getId()) {
                R.id.radio_glide ->
                    if (checked) {
                        url = "https://github.com/bumptech/glide/archive/refs/heads/master.zip"
                        fileName = getString(R.string.glide)
                    }
                R.id.radio_udacity ->
                    if (checked) {
                        url =
                            "https://github.com/udacity/nd940-c3-advanced-android-programming-project-starter/archive/refs/heads/master.zip"
                        fileName = getString(R.string.udacity)
                    }
                R.id.radio_retrofit ->
                    if (checked) {
                        url = "https://github.com/square/retrofit/archive/refs/heads/master.zip"
                        fileName = getString(R.string.retrofit)
                    }
            }
        }
    }

    // create a channel for the notification
    private fun createChannel(channelId: String, channelName: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create the NotificationChannel
            val descriptionText = getString(R.string.notification_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, channelName, importance)
            channel.description = descriptionText
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }


}
