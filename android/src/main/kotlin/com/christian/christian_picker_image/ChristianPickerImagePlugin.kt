package com.christian.christian_picker_image

import androidx.annotation.NonNull

import android.app.Application;
import android.app.Activity
import android.content.Context
import android.content.Intent
import java.util.ArrayList
import android.os.Environment

import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.FlutterPlugin.FlutterPluginBinding
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding

import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.PluginRegistry
import io.flutter.plugin.common.PluginRegistry.Registrar
import io.flutter.view.FlutterView

import com.imagepicker.features.ImagePicker
import com.imagepicker.model.Image

/**
 * ChristianPickerImagePlugin
 */
class ChristianPickerImagePlugin : MethodCallHandler, FlutterPlugin, ActivityAware, PluginRegistry.ActivityResultListener, PluginRegistry.RequestPermissionsResultListener {

    private var pluginBinding: FlutterPluginBinding? = null
    private lateinit var activityBinding: ActivityPluginBinding
    private val CHANNEL = "christian_picker_image"

    private var application: Application? = null

    private val view: FlutterView? = null
    private var pendingResult: Result? = null
    private val methodCall: MethodCall? = null

    private var context: Context? = null
    private var activity: Activity? = null
    private lateinit var channel: MethodChannel
    private var messenger: BinaryMessenger? = null

    private val REQUEST_CODE_CHOOSE = 1001
    private val REQUEST_CODE_GRANT_PERMISSIONS = 2001

    private val NumberOfImagesToSelect = 5

    override fun onAttachedToEngine(flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        pluginBinding = flutterPluginBinding;
    }

    override fun onDetachedFromEngine(flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        pluginBinding = null;
    }

    override fun onAttachedToActivity(binding: ActivityPluginBinding) {
        activityBinding = binding
        setup(
            pluginBinding!!.getBinaryMessenger(),
            pluginBinding!!.getApplicationContext() as Application,
            activityBinding.getActivity(),
            null,
            activityBinding
        )
    }

    private fun setup(
        messenger: BinaryMessenger,
        _application: Application,
        activity: Activity,
        registrar: PluginRegistry.Registrar?,
        activityBinding: ActivityPluginBinding
    ) {
        this.activity = activity
        this.application = application
        channel = MethodChannel(messenger, CHANNEL)
        channel.setMethodCallHandler(this)
        if (registrar != null) {
            registrar.addActivityResultListener(this)
            registrar.addRequestPermissionsResultListener(this)
        } else {
            // V2 embedding setup for activity listeners.
            activityBinding.addActivityResultListener(this)
            activityBinding.addRequestPermissionsResultListener(this)
        }
    }

    override fun onDetachedFromActivity() {
        tearDown()
    }

    override fun onDetachedFromActivityForConfigChanges() {
        onDetachedFromActivity()
    }

    override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
        onAttachedToActivity(binding)
    }

    private fun tearDown() {
        activityBinding.removeActivityResultListener(this)
        activityBinding.removeRequestPermissionsResultListener(this)
        channel.setMethodCallHandler(null)
        application = null
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray): Boolean {
        return false
    }

    private fun presentPicker(maxImages: Int) {
        ImagePicker.create(this.activity)
                .limit(maxImages)
                .showCamera(true)// Activity or Fragment
                .start();
    }

    override fun onMethodCall(call: MethodCall, result: Result) {

        this.pendingResult = result;

        when (call.method) {
            PICK_IMAGES -> {
                val maxImages = call.argument<Int>(MAX_IMAGES)!!

                if (maxImages <= 0) {
                    return
                }

                presentPicker(maxImages)
            }
            "getPlatformVersion" -> result.success("Android " + android.os.Build.VERSION.RELEASE)
            else -> result.notImplemented()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean {

        if (ImagePicker.shouldHandle(requestCode, resultCode, data)) {
            // Get a list of picked images
            val images: List<Image> = ImagePicker.getImages (data)
            val list: ArrayList<Map<String,String>> = ArrayList<Map<String,String>>(0)

            for (image in images) {
                val containerMap = HashMap<String, String>()
                containerMap.put("path", image.path)
                list.add(containerMap)
            }

            this.pendingResult?.success(list)
        }

        return false
    }

    companion object {

        private val PICK_IMAGES = "pickImages"
        private val REFRESH_IMAGE = "refreshImage"
        private val MAX_IMAGES = "maxImages"
        private val ANDROID_OPTIONS = "androidOptions"

        private val SELECTED_ASSETS = "selectedAssets"
        private val ENABLE_CAMERA = "enableCamera"
        private val REQUEST_CODE_CHOOSE = 1001
        private val REQUEST_CODE_GRANT_PERMISSIONS = 2001
    }

}
