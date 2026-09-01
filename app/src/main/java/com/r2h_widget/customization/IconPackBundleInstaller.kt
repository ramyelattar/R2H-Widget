package com.r2h_widget.customization

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageInstaller
import android.content.pm.PackageManager
import android.os.Build
import android.util.Xml
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.zip.ZipFile

enum class IconPackBundlePhase {
    NOT_INSTALLED,
    UPDATE_AVAILABLE,
    INSTALLED,
    INSTALLING,
    AWAITING_USER_CONFIRMATION,
    INSTALLED_SUCCESSFULLY,
    FAILED,
    CANCELED,
    PERMISSION_REQUIRED,
    BLOCKED,
    INVALID,
    CONFLICT,
    STORAGE_FAILURE,
    INCOMPATIBLE,
}

enum class IconPackPackagePhase {
    NOT_INSTALLED,
    INSTALLED,
    UPDATE_AVAILABLE,
    VERSION_AHEAD,
    INVALID_BUNDLE,
}

data class IconPackPackageStatus(
    val packId: String,
    val packageName: String,
    val label: String,
    val phase: IconPackPackagePhase,
    val installedVersionCode: Long? = null,
    val bundledVersionCode: Long? = null,
)

data class IconPackBundleStatus(
    val phase: IconPackBundlePhase,
    val installedCount: Int,
    val updateCount: Int,
    val missingCount: Int,
    val packages: List<IconPackPackageStatus>,
    val message: String? = null,
) {
    val totalCount: Int get() = packages.size
    val isReady: Boolean get() = phase == IconPackBundlePhase.INSTALLED || phase == IconPackBundlePhase.INSTALLED_SUCCESSFULLY

    companion object {
        fun empty(): IconPackBundleStatus = IconPackBundleStatus(
            phase = IconPackBundlePhase.NOT_INSTALLED,
            installedCount = 0,
            updateCount = 0,
            missingCount = LauncherIconPackCatalog.builtIn.size,
            packages = emptyList(),
        )
    }
}

object IconPackBundleStatusResolver {
    fun resolve(
        artifacts: List<LauncherIconPackArtifact>,
        installedVersions: Map<String, Long>,
        bundledVersions: Map<String, Long>,
    ): IconPackBundleStatus {
        val packages = artifacts.map { artifact ->
            val installed = installedVersions[artifact.packageName]
            val bundled = bundledVersions[artifact.packageName]
            val phase = when {
                bundled == null -> IconPackPackagePhase.INVALID_BUNDLE
                installed == null -> IconPackPackagePhase.NOT_INSTALLED
                installed < bundled -> IconPackPackagePhase.UPDATE_AVAILABLE
                installed > bundled -> IconPackPackagePhase.VERSION_AHEAD
                else -> IconPackPackagePhase.INSTALLED
            }
            IconPackPackageStatus(
                packId = artifact.packId,
                packageName = artifact.packageName,
                label = artifact.label,
                phase = phase,
                installedVersionCode = installed,
                bundledVersionCode = bundled,
            )
        }
        val invalidCount = packages.count { it.phase == IconPackPackagePhase.INVALID_BUNDLE }
        val aheadCount = packages.count { it.phase == IconPackPackagePhase.VERSION_AHEAD }
        val missingCount = packages.count { it.phase == IconPackPackagePhase.NOT_INSTALLED }
        val updateCount = packages.count { it.phase == IconPackPackagePhase.UPDATE_AVAILABLE }
        val phase = when {
            invalidCount > 0 -> IconPackBundlePhase.FAILED
            aheadCount > 0 -> IconPackBundlePhase.FAILED
            missingCount > 0 -> IconPackBundlePhase.NOT_INSTALLED
            updateCount > 0 -> IconPackBundlePhase.UPDATE_AVAILABLE
            packages.isNotEmpty() -> IconPackBundlePhase.INSTALLED
            else -> IconPackBundlePhase.FAILED
        }
        val message = when {
            invalidCount > 0 -> "One or more bundled icon packs has invalid package metadata."
            aheadCount > 0 -> "An installed icon pack is newer than the bundled version; downgrade was not attempted."
            else -> null
        }
        return IconPackBundleStatus(
            phase = phase,
            installedCount = packages.count { it.installedVersionCode != null },
            updateCount = updateCount,
            missingCount = missingCount,
            packages = packages,
            message = message,
        )
    }
}

data class IconPackBundleApkMetadata(
    val packageName: String,
    val versionCode: Long,
    val versionName: String?,
    val sizeBytes: Long,
    val hasAppFilter: Boolean,
    val hasDrawableMap: Boolean,
    val drawableCount: Int,
)

sealed interface IconPackInstallStartResult {
    data object Started : IconPackInstallStartResult
    data object PermissionRequired : IconPackInstallStartResult
    data object AlreadyInstalled : IconPackInstallStartResult
    data class Failed(val message: String) : IconPackInstallStartResult
}

object IconPackInstallResultMapper {
    fun phaseFor(packageInstallerStatus: Int): IconPackBundlePhase = when (packageInstallerStatus) {
        PackageInstaller.STATUS_SUCCESS -> IconPackBundlePhase.INSTALLED_SUCCESSFULLY
        PackageInstaller.STATUS_PENDING_USER_ACTION -> IconPackBundlePhase.AWAITING_USER_CONFIRMATION
        PackageInstaller.STATUS_FAILURE_ABORTED -> IconPackBundlePhase.CANCELED
        PackageInstaller.STATUS_FAILURE_BLOCKED -> IconPackBundlePhase.BLOCKED
        PackageInstaller.STATUS_FAILURE_INVALID -> IconPackBundlePhase.INVALID
        PackageInstaller.STATUS_FAILURE_CONFLICT -> IconPackBundlePhase.CONFLICT
        PackageInstaller.STATUS_FAILURE_STORAGE -> IconPackBundlePhase.STORAGE_FAILURE
        PackageInstaller.STATUS_FAILURE_INCOMPATIBLE -> IconPackBundlePhase.INCOMPATIBLE
        else -> IconPackBundlePhase.FAILED
    }

    fun messageFor(packageInstallerStatus: Int, statusMessage: String? = null): String {
        val exactMessage = statusMessage?.takeIf { it.isNotBlank() }
        return when (packageInstallerStatus) {
            PackageInstaller.STATUS_SUCCESS -> "Icon packs installed successfully."
            PackageInstaller.STATUS_PENDING_USER_ACTION -> "Confirm the Android installation to finish installing the icon packs."
            PackageInstaller.STATUS_FAILURE_ABORTED -> exactMessage ?: "Icon-pack installation was canceled."
            PackageInstaller.STATUS_FAILURE_BLOCKED -> exactMessage ?: "Android blocked the icon-pack installation."
            PackageInstaller.STATUS_FAILURE_INVALID -> exactMessage ?: "Android rejected an invalid icon-pack package."
            PackageInstaller.STATUS_FAILURE_CONFLICT -> exactMessage ?: "An icon-pack package conflicts with an installed package."
            PackageInstaller.STATUS_FAILURE_STORAGE -> exactMessage ?: "There is not enough storage to install the icon packs."
            PackageInstaller.STATUS_FAILURE_INCOMPATIBLE -> exactMessage ?: "The icon packs are incompatible with this Android device."
            else -> exactMessage ?: "Android could not install the icon packs."
        }
    }
}

class IconPackBundleInstaller(context: Context) : AutoCloseable {
    private val appContext = context.applicationContext
    private val packageManager = appContext.packageManager
    private val packageInstaller = packageManager.packageInstaller
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val _state = MutableStateFlow(IconPackBundleStatus.empty())
    val state: StateFlow<IconPackBundleStatus> = _state.asStateFlow()

    private val resultReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            handleInstallResult(intent)
        }
    }

    init {
        appContext.registerReceiver(
            resultReceiver,
            IntentFilter(IconPackInstallResultReceiver.ACTION_RESULT),
            Context.RECEIVER_NOT_EXPORTED,
        )
    }

    suspend fun refresh(): IconPackBundleStatus = withContext(Dispatchers.IO) {
        val status = runCatching {
            val bundled = LauncherIconPackCatalog.builtIn.associateWith { readBundledApkMetadata(it) }
            val installed = LauncherIconPackCatalog.builtIn.mapNotNull { artifact ->
                runCatching {
                    @Suppress("DEPRECATION")
                    packageManager.getPackageInfo(artifact.packageName, 0).longVersionCode
                }.getOrNull()?.let { artifact.packageName to it }
            }.toMap()
            IconPackBundleStatusResolver.resolve(
                artifacts = LauncherIconPackCatalog.builtIn,
                installedVersions = installed,
                bundledVersions = bundled
                    .mapKeys { it.key.packageName }
                    .mapValues { it.value.versionCode },
            )
        }.getOrElse { error ->
            IconPackBundleStatus(
                phase = IconPackBundlePhase.FAILED,
                installedCount = 0,
                updateCount = 0,
                missingCount = LauncherIconPackCatalog.builtIn.size,
                packages = LauncherIconPackCatalog.builtIn.map { artifact ->
                    IconPackPackageStatus(
                        packId = artifact.packId,
                        packageName = artifact.packageName,
                        label = artifact.label,
                        phase = IconPackPackagePhase.INVALID_BUNDLE,
                    )
                },
                message = error.message ?: "The bundled icon packs could not be inspected.",
            )
        }
        _state.value = status
        status
    }

    fun canInstallFromThisSource(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.O || packageManager.canRequestPackageInstalls()

    fun unknownSourcesIntent(): Intent = Intent(
        android.provider.Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
        android.net.Uri.parse("package:${appContext.packageName}"),
    )

    fun launcherPickerIntent(): Intent? {
        val intent = Intent("com.nothing.launcher.icon_pack_picker")
            .setPackage("com.nothing.launcher")
        return intent.takeIf {
            packageManager.resolveActivity(it, PackageManager.MATCH_DEFAULT_ONLY) != null
        }
    }

    suspend fun installOrUpdate(): IconPackInstallStartResult = withContext(Dispatchers.IO) {
        if (!canInstallFromThisSource()) {
            _state.update {
                it.copy(
                    phase = IconPackBundlePhase.PERMISSION_REQUIRED,
                    message = "Allow installs from R2H in Android settings, then try again.",
                )
            }
            return@withContext IconPackInstallStartResult.PermissionRequired
        }

        val current = refresh()
        when (current.phase) {
            IconPackBundlePhase.INSTALLED,
            IconPackBundlePhase.INSTALLED_SUCCESSFULLY,
            -> return@withContext IconPackInstallStartResult.AlreadyInstalled

            IconPackBundlePhase.FAILED,
            IconPackBundlePhase.INSTALLING,
            IconPackBundlePhase.AWAITING_USER_CONFIRMATION,
            IconPackBundlePhase.PERMISSION_REQUIRED,
            IconPackBundlePhase.CANCELED,
            IconPackBundlePhase.BLOCKED,
            IconPackBundlePhase.INVALID,
            IconPackBundlePhase.CONFLICT,
            IconPackBundlePhase.STORAGE_FAILURE,
            IconPackBundlePhase.INCOMPATIBLE,
            -> {
                if (current.phase == IconPackBundlePhase.FAILED) {
                    return@withContext IconPackInstallStartResult.Failed(
                        current.message ?: "The icon-pack bundle is not installable.",
                    )
                }
            }

            IconPackBundlePhase.NOT_INSTALLED,
            IconPackBundlePhase.UPDATE_AVAILABLE,
            -> Unit
        }

        _state.value = current.copy(
            phase = IconPackBundlePhase.INSTALLING,
            message = "Installing ${LauncherIconPackCatalog.builtIn.size} icon packs…",
        )

        val metadata = runCatching {
            LauncherIconPackCatalog.builtIn.map { artifact ->
                artifact to readBundledApkMetadata(artifact)
            }
        }.getOrElse { error ->
            val message = error.message ?: "A bundled icon-pack APK is missing or invalid."
            _state.update { it.copy(phase = IconPackBundlePhase.FAILED, message = message) }
            return@withContext IconPackInstallStartResult.Failed(message)
        }

        var parentSessionId = -1
        var parentSession: PackageInstaller.Session? = null
        try {
            val parentParams = PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL)
            parentParams.setMultiPackage()
            parentSessionId = packageInstaller.createSession(parentParams)
            parentSession = packageInstaller.openSession(parentSessionId)

            metadata.forEach { (artifact, apk) ->
                val childParams = PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL)
                childParams.setAppPackageName(apk.packageName)
                childParams.setSize(apk.sizeBytes)
                val childSessionId = packageInstaller.createSession(childParams)
                val childSession = packageInstaller.openSession(childSessionId)
                try {
                    childSession.openWrite("base.apk", 0L, apk.sizeBytes).use { output ->
                        appContext.assets.open("iconpacks/${artifact.assetFileName}").use { input ->
                            input.copyTo(output)
                        }
                        childSession.fsync(output)
                    }
                } finally {
                    childSession.close()
                }
                parentSession.addChildSessionId(childSessionId)
            }

            val resultIntent = Intent(appContext, IconPackInstallResultReceiver::class.java).apply {
                action = IconPackInstallResultReceiver.ACTION_PACKAGE_INSTALL_RESULT
                putExtra(IconPackInstallResultReceiver.EXTRA_PARENT_SESSION_ID, parentSessionId)
            }
            val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
            val callback = PendingIntent.getBroadcast(
                appContext,
                parentSessionId,
                resultIntent,
                flags,
            )
            parentSession.commit(callback.intentSender)
            IconPackInstallStartResult.Started
        } catch (error: Throwable) {
            if (parentSessionId >= 0) {
                runCatching { packageInstaller.abandonSession(parentSessionId) }
            }
            val message = error.message ?: "Android could not start the icon-pack installation."
            _state.update { it.copy(phase = IconPackBundlePhase.FAILED, message = message) }
            IconPackInstallStartResult.Failed(message)
        } finally {
            parentSession?.close()
        }
    }

    private fun handleInstallResult(intent: Intent) {
        val status = intent.getIntExtra(IconPackInstallResultReceiver.EXTRA_STATUS, PackageInstaller.STATUS_FAILURE)
        when (status) {
            PackageInstaller.STATUS_SUCCESS -> scope.launch {
                val refreshed = refresh()
                _state.value = refreshed.copy(
                    phase = if (refreshed.isReady) IconPackBundlePhase.INSTALLED_SUCCESSFULLY else refreshed.phase,
                    message = if (refreshed.isReady) {
                        "${refreshed.totalCount} icon packs installed."
                    } else {
                        refreshed.message
                    },
                )
            }

            else -> _state.update {
                it.copy(
                    phase = IconPackInstallResultMapper.phaseFor(status),
                    message = IconPackInstallResultMapper.messageFor(
                        status,
                        intent.getStringExtra(IconPackInstallResultReceiver.EXTRA_STATUS_MESSAGE),
                    ),
                )
            }
        }
    }

    private fun readBundledApkMetadata(artifact: LauncherIconPackArtifact): IconPackBundleApkMetadata {
        val apkFile = copyAssetToCache(artifact)
        val packageInfo = packageManager.getPackageArchiveInfo(apkFile.absolutePath, 0)
            ?: error("${artifact.label} is not a readable Android package.")
        check(packageInfo.packageName == artifact.packageName) {
            "${artifact.label} has package ${packageInfo.packageName}, expected ${artifact.packageName}."
        }
        val entries = ZipFile(apkFile).use { zip -> zip.entries().asSequence().map { it.name }.toList() }
        val drawableCount = ZipFile(apkFile).use(::countMappedDrawableResources)
        check(entries.any { it == "assets/appfilter.xml" }) {
            "${artifact.label} is missing assets/appfilter.xml."
        }
        check(entries.any { it == "assets/drawable.xml" }) {
            "${artifact.label} is missing assets/drawable.xml."
        }
        check(drawableCount > 0) {
            "${artifact.label} contains no mapped icon drawables."
        }
        return IconPackBundleApkMetadata(
            packageName = packageInfo.packageName,
            versionCode = packageInfo.longVersionCode,
            versionName = packageInfo.versionName,
            sizeBytes = apkFile.length(),
            hasAppFilter = true,
            hasDrawableMap = true,
            drawableCount = drawableCount,
        )
    }

    /**
     * AGP may shorten resource file paths in a release APK while preserving
     * the Android resource names in resources.arsc. The icon-pack contract
     * references those resource names from drawable.xml, so ZIP path names
     * are not a reliable coverage signal.
     */
    private fun countMappedDrawableResources(zip: ZipFile): Int {
        val drawableMap = zip.getEntry("assets/drawable.xml") ?: return 0
        val parser = Xml.newPullParser()
        val mappedNames = linkedSetOf<String>()
        zip.getInputStream(drawableMap).use { input ->
            parser.setInput(input, Charsets.UTF_8.name())
            var eventType = parser.eventType
            while (eventType != org.xmlpull.v1.XmlPullParser.END_DOCUMENT) {
                if (eventType == org.xmlpull.v1.XmlPullParser.START_TAG && parser.name == "item") {
                    parser.getAttributeValue(null, "drawable")
                        ?.takeIf { it.startsWith("r2h_") }
                        ?.let(mappedNames::add)
                }
                eventType = parser.next()
            }
        }
        return mappedNames.size
    }

    private fun copyAssetToCache(artifact: LauncherIconPackArtifact): File {
        val directory = File(appContext.cacheDir, "iconpacks")
        check(directory.mkdirs() || directory.isDirectory) { "Unable to prepare icon-pack cache." }
        val destination = File(directory, artifact.assetFileName)
        appContext.assets.open("iconpacks/${artifact.assetFileName}").use { input ->
            destination.outputStream().use { output -> input.copyTo(output) }
        }
        check(destination.length() > 0L) { "${artifact.label} is empty." }
        return destination
    }

    override fun close() {
        runCatching { appContext.unregisterReceiver(resultReceiver) }
        scope.coroutineContext.cancel()
    }
}

class IconPackInstallResultReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // PackageInstaller sends its result under its own namespaced extra. The
        // app-internal EXTRA_STATUS is used only on the rebroadcast below.
        val status = intent.getIntExtra(PackageInstaller.EXTRA_STATUS, PackageInstaller.STATUS_FAILURE)
        if (status == PackageInstaller.STATUS_PENDING_USER_ACTION) {
            @Suppress("DEPRECATION")
            val confirmation = intent.getParcelableExtra(Intent.EXTRA_INTENT) as? Intent
            confirmation?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)?.let {
                runCatching { context.startActivity(it) }
            }
        }
        context.sendBroadcast(
            Intent(ACTION_RESULT)
                .setPackage(context.packageName)
                .putExtra(EXTRA_STATUS, status)
                .putExtra(EXTRA_STATUS_MESSAGE, intent.getStringExtra(PackageInstaller.EXTRA_STATUS_MESSAGE)),
        )
    }

    companion object {
        const val ACTION_PACKAGE_INSTALL_RESULT = "com.r2h_widget.action.ICON_PACK_INSTALL_RESULT"
        const val ACTION_RESULT = "com.r2h_widget.action.ICON_PACK_INSTALL_EVENT"
        const val EXTRA_PARENT_SESSION_ID = "extra_parent_session_id"
        const val EXTRA_STATUS = "extra_status"
        const val EXTRA_STATUS_MESSAGE = "extra_status_message"
    }
}

