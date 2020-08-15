package com.georgehargreaves.aboutme

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import androidx.databinding.DataBindingUtil
import com.georgehargreaves.aboutme.R.string
import com.georgehargreaves.aboutme.databinding.ActivityMainBinding
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*

private const val IMAGE_CAPTURE_REQUEST = 0

class MainActivity : AppCompatActivity() {
    private lateinit var imm: InputMethodManager
    private lateinit var binding: ActivityMainBinding
    private val myName = MyName("Jimmie Cricket")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.myName = myName
        binding.bioDoneButton.setOnClickListener { setNickName() }
        if (this.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            binding.bioSetImageButton.apply {
                setOnClickListener { setUserImage() }
                visibility = View.VISIBLE
            }
        }

    }

    private fun setUserImage() {
        when (ContextCompat.checkSelfPermission(this, Manifest.permission_group.CAMERA)) {
            PermissionChecker.PERMISSION_DENIED -> checkRationale()
            PermissionChecker.PERMISSION_GRANTED -> sendCaptureIntent()
        }
    }

    private fun checkRationale() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (shouldShowRequestPermissionRationale(Manifest.permission_group.CAMERA)) {
                AlertDialog.Builder(this)
                    .setMessage("We need access to your camera to capture an image of you for your Bio, can we continue?")
                    .setPositiveButton("Grant Permission") { dialog, which ->
                        sendCaptureIntent()
                        dialog.dismiss()
                    }
                    .setNegativeButton("Deny") { dialog, which -> dialog.dismiss() }
                    .setNeutralButton("Cancel") { dialog, which -> dialog.cancel() }
                    .show()
            } else {
                AlertDialog.Builder(this)
                    .setMessage("We ae unable to capture an image due to current permissions settings. To change the permissions for this app, head to System Settings")
                    .setNeutralButton("Ok") { dialog, _ -> dialog.dismiss() }
                    .show()
            }
        } else {
            AlertDialog.Builder(this)
                .setMessage("We need access to your camera to capture an image of you for your Bio, can we continue?")
                .setPositiveButton("Grant Permission") { dialog, which ->
                    sendCaptureIntent()
                    dialog.dismiss()
                }
                .setNegativeButton("Deny") { dialog, which -> dialog.dismiss() }
                .setNeutralButton("Cancel") { dialog, which -> dialog.cancel() }
                .show()
        }
    }

    private fun sendCaptureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { captureIntent ->
            captureIntent.resolveActivity(packageManager)?.also {
                startActivityForResult(captureIntent, IMAGE_CAPTURE_REQUEST)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == IMAGE_CAPTURE_REQUEST) {
            when (resultCode) {
                Activity.RESULT_OK -> {
                    (data?.extras?.get("data") as? Bitmap)?.let { bitmap ->
                        binding.bioImage.setImageBitmap(bitmap)
                    } ?: showDataErrorSnackbar()
                }
                else -> showCancelSnackbar()
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun showDataErrorSnackbar() {
        Snackbar.make(
            binding.root,
            getString(string.image_capture_data_error),
            Snackbar.LENGTH_SHORT
        ).show()
    }

    private fun showCancelSnackbar() {
        Snackbar.make(
            binding.root,
            getString(string.image_capture_cancelled),
            Snackbar.LENGTH_SHORT
        ).show()
    }

    private fun setNickName() {
        binding.myName?.nickName = binding.bioUserNickname.text.toString()
        binding.invalidateAll()
        binding.bioNickNameDisplay.visibility = View.VISIBLE
        imm.hideSoftInputFromWindow(bio_nick_name_display.windowToken, 0)
    }
}
