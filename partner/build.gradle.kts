plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.compose)
}

// "همراه هانیتا": a light companion app for the partner of a Hanita user.
// It works without any server or account: it is paired with Hanita by scanning a QR code and
// receives only an encrypted "cycle card" (see core/…/partner/PartnerLink.kt).
android {
  namespace = "com.hanita.partner"
  compileSdk { version = release(36) { minorApiLevel = 1 } }

  defaultConfig {
    applicationId = "com.hanita.partner"
    minSdk = 24
    targetSdk = 36
    versionCode = 1
    versionName = "1.0.0"
  }

  // Release signing is configured only from environment variables, never from files in the repo.
  val releaseStoreFile = System.getenv("KEYSTORE_PATH")
  val releaseStorePassword = System.getenv("STORE_PASSWORD")
  val releaseKeyPassword = System.getenv("KEY_PASSWORD")
  val hasReleaseSigning = !releaseStoreFile.isNullOrBlank() &&
    !releaseStorePassword.isNullOrBlank() &&
    !releaseKeyPassword.isNullOrBlank()

  signingConfigs {
    if (hasReleaseSigning) {
      create("release") {
        storeFile = file(releaseStoreFile!!)
        storePassword = releaseStorePassword
        keyAlias = System.getenv("KEY_ALIAS") ?: "upload"
        keyPassword = releaseKeyPassword
      }
    }
  }

  buildTypes {
    release {
      isMinifyEnabled = true
      isShrinkResources = true
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
      if (hasReleaseSigning) {
        signingConfig = signingConfigs.getByName("release")
      }
    }
  }
  compileOptions {
    isCoreLibraryDesugaringEnabled = true
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }
  buildFeatures {
    compose = true
  }
  lint {
    textReport = true
    textOutput = file("build/reports/lint-results.txt")
    checkDependencies = false
  }
}

dependencies {
  coreLibraryDesugaring(libs.desugar.jdk.libs)
  implementation(project(":core"))
  implementation(platform(libs.androidx.compose.bom))
  implementation(libs.androidx.activity.compose)
  implementation(libs.androidx.compose.material.icons.core)
  implementation(libs.androidx.compose.material.icons.extended)
  implementation(libs.androidx.compose.material3)
  implementation(libs.androidx.compose.ui)
  implementation(libs.androidx.compose.ui.graphics)
  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.lifecycle.runtime.ktx)
  // Camera QR scanner that works without Google Play services.
  implementation(libs.zxing.android.embedded)
  testImplementation(libs.junit)
}
