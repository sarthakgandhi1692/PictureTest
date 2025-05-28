package com.example.test.utils

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.test.R

/**
 * Utility class for handling media permissions.
 */
class PermissionUtil {

    /**
     * Requests media permission from the user.
     * @param activity The component activity requesting the permission.
     */
    fun requestMediaPermission(
        activity: ComponentActivity,
        onGranted: () -> Unit,
        onLimitedAccess: () -> Unit,
        onSettingsOpened: () -> Unit
    ) {
        val permission = getMediaPermission()

        // Check current permission state
        val permissionState = checkPermissionState(activity)
        when (permissionState) {
            PermissionState.FULL_ACCESS -> {
                onGranted()
                return
            }

            PermissionState.LIMITED_ACCESS -> {
                onLimitedAccess()
                return
            }

            PermissionState.NO_ACCESS -> {
                // Need to request permission
                val permissionLauncher = activity.activityResultRegistry.register(
                    getPermissionRequestKey(),
                    ActivityResultContracts.RequestPermission()
                ) { isGranted ->
                    if (isGranted) {
                        when (checkPermissionState(activity)) {
                            PermissionState.FULL_ACCESS -> onGranted()
                            PermissionState.LIMITED_ACCESS -> {
                                onLimitedAccess()
                            }

                            PermissionState.NO_ACCESS -> {
                                handlePermissionDenied(
                                    activity = activity,
                                    permission = permission,
                                    onSettingsOpened = onSettingsOpened,
                                    onRetry = {
                                        requestMediaPermission(
                                            activity = activity,
                                            onGranted = onGranted,
                                            onLimitedAccess = onLimitedAccess,
                                            onSettingsOpened = onSettingsOpened
                                        )
                                    }
                                )
                            }
                        }
                    } else {
                        handlePermissionDenied(
                            activity = activity,
                            permission = permission,
                            onSettingsOpened = onSettingsOpened,
                            onRetry = {
                                requestMediaPermission(
                                    activity = activity,
                                    onGranted = onGranted,
                                    onLimitedAccess = onLimitedAccess,
                                    onSettingsOpened = onSettingsOpened
                                )
                            }
                        )
                    }
                }
                permissionLauncher.launch(permission)
            }
        }
    }

    private fun checkPermissionState(activity: ComponentActivity): PermissionState {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val hasImageAccess = ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.READ_MEDIA_IMAGES
            ) == PackageManager.PERMISSION_GRANTED
            val hasVideoAccess = ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.READ_MEDIA_VIDEO
            ) == PackageManager.PERMISSION_GRANTED
            val hasAudioAccess = ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.READ_MEDIA_AUDIO
            ) == PackageManager.PERMISSION_GRANTED

            return when {
                hasImageAccess && hasVideoAccess && hasAudioAccess -> PermissionState.FULL_ACCESS
                hasImageAccess || hasVideoAccess || hasAudioAccess -> PermissionState.LIMITED_ACCESS
                else -> PermissionState.NO_ACCESS
            }
        } else {
            // Pre-Android 11: only full or no access possible
            return if (isMediaPermissionGranted(activity)) {
                PermissionState.FULL_ACCESS
            } else {
                PermissionState.NO_ACCESS
            }
        }
    }

    fun isMediaPermissionGranted(activity: ComponentActivity): Boolean {
        val permission = getMediaPermission()
        return ContextCompat.checkSelfPermission(
            activity,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Retrieves the appropriate media permission based on the Android SDK version.
     * @return The media permission string.
     */
    private fun getMediaPermission(): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
    }

    /**
     * Handles the scenario where media permission is denied.
     * @param activity The activity where the permission was denied.
     * @param permission The denied permission string.
     */
    private fun handlePermissionDenied(
        activity: Activity,
        permission: String,
        onSettingsOpened: () -> Unit,
        onRetry: () -> Unit
    ) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
            showRationaleDialog(activity, onRetry)
        } else {
            onSettingsOpened()
            showSettingsDialog(activity)
        }
    }

    /**
     * Displays a rationale dialog explaining why the permission is needed.
     * @param context The context for displaying the dialog.
     */
    private fun showRationaleDialog(
        context: Context,
        onRetry: () -> Unit
    ) {
        AlertDialog.Builder(context)
            .setTitle(context.getString(R.string.permission_required))
            .setMessage(context.getString(R.string.we_need_access_to_your_storage_to_load_and_process_images))
            .setPositiveButton(context.getString(R.string.try_again)) { _, _ -> onRetry() }
            .setNegativeButton(context.getString(R.string.cancel)) { dialog, _ -> dialog.dismiss() }
            .show()
    }

    /**
     * Displays a dialog prompting the user to go to settings to grant the permission.
     * @param context The context for displaying the dialog.
     */
    private fun showSettingsDialog(context: Context) {
        AlertDialog.Builder(context)
            .setTitle(context.getString(R.string.permission_denied_permanently))
            .setMessage(context.getString(R.string.please_go_to_settings_to_allow_storage_access))
            .setPositiveButton(context.getString(R.string.go_to_settings)) { _, _ ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    setData(Uri.fromParts(PACKAGE, context.packageName, null))
                }
                context.startActivity(intent)
            }
            .setNegativeButton(context.getString(R.string.cancel)) { dialog, _ -> dialog.dismiss() }
            .show()
    }

    /**
     * Generates a unique key for the permission request.
     * @return The permission request key.
     */
    private fun getPermissionRequestKey(): String {
        return "media_permission_request_${System.currentTimeMillis()}"
    }

    private enum class PermissionState {
        FULL_ACCESS,
        LIMITED_ACCESS,
        NO_ACCESS
    }

    /**
     * Companion object holding constants.
     */
    companion object {
        private const val PACKAGE = "package"
    }
}
