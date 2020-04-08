package com.example.handsight

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.SystemClock
import android.util.Size
import android.view.TextureView
import android.widget.Toast
import androidx.annotation.UiThread
import androidx.annotation.WorkerThread
import androidx.camera.core.*
import androidx.camera.core.Preview.OnPreviewOutputUpdateListener
import androidx.camera.core.Preview.PreviewOutput
import androidx.core.app.ActivityCompat

abstract class AbstractCameraXActivity<R> : BaseModuleActivity() {
    private var mLastAnalysisResultTime: Long = 0
    protected abstract val contentViewLayoutId: Int
    protected abstract val cameraPreviewTextureView: TextureView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //StatusBarUtils.setStatusBarOverlay(getWindow(), true);
        setContentView(contentViewLayoutId)
        startBackgroundThread()
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                PERMISSIONS,
                REQUEST_CODE_CAMERA_PERMISSION
            )
        } else {
            setupCameraX()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        if (requestCode == REQUEST_CODE_CAMERA_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                Toast.makeText(
                    this,
                    "You can't use image classification example without granting CAMERA permission",
                    Toast.LENGTH_LONG
                )
                    .show()
                finish()
            } else {
                setupCameraX()
            }
        }
    }

    private fun setupCameraX() {
        val textureView = cameraPreviewTextureView
        val previewConfig = PreviewConfig.Builder()
            .setLensFacing(CameraX.LensFacing.FRONT).build()
        val preview = Preview(previewConfig)
        preview.onPreviewOutputUpdateListener =
            OnPreviewOutputUpdateListener { output: PreviewOutput ->
                textureView.surfaceTexture = output.surfaceTexture
            }
        val imageAnalysisConfig = ImageAnalysisConfig.Builder()
            .setTargetResolution(Size(224, 224))
            .setCallbackHandler(mBackgroundHandler!!)
            .setImageReaderMode(ImageAnalysis.ImageReaderMode.ACQUIRE_LATEST_IMAGE)
            .setLensFacing(CameraX.LensFacing.FRONT)
            .build()
        val imageAnalysis = ImageAnalysis(imageAnalysisConfig)
        imageAnalysis.analyzer =
            ImageAnalysis.Analyzer { image: ImageProxy?, rotationDegrees: Int ->
                if (SystemClock.elapsedRealtime() - mLastAnalysisResultTime < 500) {
                    return@Analyzer
                }
                val result = analyzeImage(image, rotationDegrees)
                if (result != null) {
                    mLastAnalysisResultTime = SystemClock.elapsedRealtime()
                    runOnUiThread { applyToUiAnalyzeImageResult(result) }
                }
            }
        CameraX.bindToLifecycle(this, preview, imageAnalysis)
    }

    @WorkerThread
    protected abstract fun analyzeImage(image: ImageProxy?, rotationDegrees: Int): R?

    @UiThread
    protected abstract fun applyToUiAnalyzeImageResult(result: R)

    companion object {
        private const val REQUEST_CODE_CAMERA_PERMISSION = 200
        private val PERMISSIONS =
            arrayOf(Manifest.permission.CAMERA)
    }
}