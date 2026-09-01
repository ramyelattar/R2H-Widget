package com.r2h_widget.customization

import java.io.File
import android.content.pm.PackageInstaller
import java.util.zip.ZipFile
import android.util.Xml
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import kotlinx.coroutines.runBlocking

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class IconPackBundleInstallerTest {

    @Test
    fun mainAppDistributionContainsOnePayloadForEachLauncherPack() {
        val relativePayloads = listOf(
            "fluffy.apk",
            "liquid-dark-glass.apk",
            "liquid-siri-glass.apk",
            "luxury-vip.apk",
        )
        val roots = listOf(
            File("app/build/generated/iconpack-assets/debug/iconpacks"),
            File("build/generated/iconpack-assets/debug/iconpacks"),
            File("app/src/main/assets/iconpacks"),
            File("src/main/assets/iconpacks"),
            File("app/build/generated/iconpacks"),
            File("build/generated/iconpacks"),
        )

        relativePayloads.forEach { payload ->
            assertTrue(
                "Missing bundled icon-pack payload: $payload",
                roots.any { File(it, payload).isFile },
            )
        }
    }

    @Test
    fun bundleCatalogUsesTheFourStablePackageIdsAndPayloadNames() {
        assertEquals(
            listOf(
                "com.r2h_widget.iconpack.fluffy",
                "com.r2h_widget.iconpack.darkglass",
                "com.r2h_widget.iconpack.siriglass",
                "com.r2h_widget.iconpack.luxuryvip",
            ),
            LauncherIconPackCatalog.builtIn.map { it.packageName },
        )
        assertEquals(
            listOf("fluffy.apk", "liquid-dark-glass.apk", "liquid-siri-glass.apk", "luxury-vip.apk"),
            LauncherIconPackCatalog.builtIn.map { it.assetFileName },
        )
        assertEquals(
            LauncherIconPackCatalog.builtIn.size,
            LauncherIconPackCatalog.builtIn.map { it.packageName }.toSet().size,
        )
    }

    @Test
    fun statusResolver_distinguishesMissingInstalledAndUpdateStates() {
        val artifacts = LauncherIconPackCatalog.builtIn
        val bundled = artifacts.associate { it.packageName to 2L }

        val missing = IconPackBundleStatusResolver.resolve(artifacts, emptyMap(), bundled)
        assertEquals(IconPackBundlePhase.NOT_INSTALLED, missing.phase)
        assertEquals(0, missing.installedCount)
        assertEquals(4, missing.missingCount)

        val installed = IconPackBundleStatusResolver.resolve(
            artifacts,
            artifacts.associate { it.packageName to 2L },
            bundled,
        )
        assertEquals(IconPackBundlePhase.INSTALLED, installed.phase)
        assertEquals(4, installed.installedCount)
        assertTrue(installed.packages.all { it.phase == IconPackPackagePhase.INSTALLED })

        val update = IconPackBundleStatusResolver.resolve(
            artifacts,
            artifacts.associate { it.packageName to 1L },
            bundled,
        )
        assertEquals(IconPackBundlePhase.UPDATE_AVAILABLE, update.phase)
        assertEquals(4, update.updateCount)
    }

    @Test
    fun statusResolver_rejectsInstalledVersionAheadOfBundle() {
        val artifacts = LauncherIconPackCatalog.builtIn
        val status = IconPackBundleStatusResolver.resolve(
            artifacts,
            artifacts.associate { it.packageName to 3L },
            artifacts.associate { it.packageName to 2L },
        )

        assertEquals(IconPackBundlePhase.FAILED, status.phase)
        assertTrue(status.message.orEmpty().contains("newer"))
        assertTrue(status.packages.all { it.phase == IconPackPackagePhase.VERSION_AHEAD })
    }

    @Test
    fun resultMapper_exposesSuccessPendingCancelAndFailureTransitions() {
        assertEquals(
            IconPackBundlePhase.INSTALLED_SUCCESSFULLY,
            IconPackInstallResultMapper.phaseFor(PackageInstaller.STATUS_SUCCESS),
        )
        assertEquals(
            IconPackBundlePhase.AWAITING_USER_CONFIRMATION,
            IconPackInstallResultMapper.phaseFor(PackageInstaller.STATUS_PENDING_USER_ACTION),
        )
        assertEquals(
            IconPackBundlePhase.CANCELED,
            IconPackInstallResultMapper.phaseFor(PackageInstaller.STATUS_FAILURE_ABORTED),
        )
        assertEquals(
            IconPackBundlePhase.FAILED,
            IconPackInstallResultMapper.phaseFor(PackageInstaller.STATUS_FAILURE),
        )
        assertNotNull(IconPackInstallResultMapper.messageFor(PackageInstaller.STATUS_FAILURE, "signature mismatch"))
        assertTrue(
            IconPackInstallResultMapper.messageFor(PackageInstaller.STATUS_FAILURE, "signature mismatch")
                .contains("signature mismatch"),
        )
    }

    @Test
    fun resultMapper_classifiesAndroidFailureReasons_andPreservesExactStatusMessage() {
        assertEquals(
            IconPackBundlePhase.BLOCKED,
            IconPackInstallResultMapper.phaseFor(PackageInstaller.STATUS_FAILURE_BLOCKED),
        )
        assertEquals(
            IconPackBundlePhase.INVALID,
            IconPackInstallResultMapper.phaseFor(PackageInstaller.STATUS_FAILURE_INVALID),
        )
        assertEquals(
            IconPackBundlePhase.CONFLICT,
            IconPackInstallResultMapper.phaseFor(PackageInstaller.STATUS_FAILURE_CONFLICT),
        )
        assertEquals(
            IconPackBundlePhase.STORAGE_FAILURE,
            IconPackInstallResultMapper.phaseFor(PackageInstaller.STATUS_FAILURE_STORAGE),
        )
        assertEquals(
            IconPackBundlePhase.INCOMPATIBLE,
            IconPackInstallResultMapper.phaseFor(PackageInstaller.STATUS_FAILURE_INCOMPATIBLE),
        )
        assertEquals(
            "Play Protect blocked this package",
            IconPackInstallResultMapper.messageFor(
                PackageInstaller.STATUS_FAILURE_BLOCKED,
                "Play Protect blocked this package",
            ),
        )
    }

    @Test
    fun resultReceiver_forwardsPackageInstallerStatusToTheAppResultEvent() {
        val application = RuntimeEnvironment.getApplication()
        val packageInstallerResult = android.content.Intent(
            IconPackInstallResultReceiver.ACTION_PACKAGE_INSTALL_RESULT,
        ).putExtra(
            PackageInstaller.EXTRA_STATUS,
            PackageInstaller.STATUS_PENDING_USER_ACTION,
        ).putExtra(
            PackageInstaller.EXTRA_STATUS_MESSAGE,
            "confirmation required",
        )

        IconPackInstallResultReceiver().onReceive(application, packageInstallerResult)

        val resultEvent = Shadows.shadowOf(application).broadcastIntents
            .last { it.action == IconPackInstallResultReceiver.ACTION_RESULT }
        assertEquals(
            PackageInstaller.STATUS_PENDING_USER_ACTION,
            resultEvent.getIntExtra(IconPackInstallResultReceiver.EXTRA_STATUS, -1),
        )
        assertEquals(
            "confirmation required",
            resultEvent.getStringExtra(IconPackInstallResultReceiver.EXTRA_STATUS_MESSAGE),
        )
    }

    @Test
    fun packagedPayloads_haveExpectedPackageMetadataAndStaticIconContracts() {
        val root = listOf(
            File("app/build/generated/iconpack-assets/debug/iconpacks"),
            File("build/generated/iconpack-assets/debug/iconpacks"),
            File("app/src/main/assets/iconpacks"),
            File("src/main/assets/iconpacks"),
        ).first { it.isDirectory }
        LauncherIconPackCatalog.builtIn.forEach { artifact ->
            val apk = File(root, artifact.assetFileName)
            assertTrue("Missing payload ${artifact.assetFileName}", apk.isFile)
            val packageInfo = RuntimeEnvironment.getApplication().packageManager
                .getPackageArchiveInfo(apk.absolutePath, 0)
            assertNotNull("Unreadable payload ${artifact.assetFileName}", packageInfo)
            assertEquals(artifact.packageName, packageInfo?.packageName)
            assertTrue((packageInfo?.longVersionCode ?: 0L) > 0L)

            ZipFile(apk).use { zip ->
                val names = zip.entries().asSequence().map { it.name }.toList()
                assertTrue(names.contains("assets/appfilter.xml"))
                assertTrue(names.contains("assets/drawable.xml"))
                assertTrue(names.contains("res/raw/appfilter.xml"))
                val drawableMap = zip.getEntry("assets/drawable.xml")
                assertNotNull(drawableMap)
                val parser = Xml.newPullParser()
                val mappedNames = mutableSetOf<String>()
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
                assertEquals(100, mappedNames.size)
            }
        }
    }

    @Test
    fun packagedPayloads_targetCurrentAndroidPrivacyContract() {
        val root = listOf(
            File("app/build/generated/iconpack-assets/debug/iconpacks"),
            File("build/generated/iconpack-assets/debug/iconpacks"),
            File("app/src/main/assets/iconpacks"),
            File("src/main/assets/iconpacks"),
        ).first { it.isDirectory }

        LauncherIconPackCatalog.builtIn.forEach { artifact ->
            val apk = File(root, artifact.assetFileName)
            val packageInfo = RuntimeEnvironment.getApplication().packageManager
                .getPackageArchiveInfo(apk.absolutePath, 0)
            assertEquals(
                "${artifact.label} must target the current Android app contract",
                37,
                packageInfo?.applicationInfo?.targetSdkVersion,
            )
        }
    }

    @Test
    fun installerRefresh_detectsAllFourPayloadsAndMissingInstalledPackages() = runBlocking {
        IconPackBundleInstaller(RuntimeEnvironment.getApplication()).use { installer ->
            val status = installer.refresh()

            assertEquals(status.message, IconPackBundlePhase.NOT_INSTALLED, status.phase)
            assertEquals(4, status.totalCount)
            assertEquals(4, status.missingCount)
            assertEquals(
                LauncherIconPackCatalog.builtIn.map { it.packageName },
                status.packages.map { it.packageName },
            )
        }
    }
}



