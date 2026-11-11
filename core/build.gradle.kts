plugins {
  alias(libs.plugins.android.library)
}

// Android-free logic shared by Hanita and the partner companion app ("همراه هانیتا"):
// cycle prediction, Jalali dates, Persian digits and the encrypted partner link.
android {
  namespace = "com.hanita.core"
  compileSdk { version = release(36) { minorApiLevel = 1 } }

  defaultConfig {
    minSdk = 24
  }
  compileOptions {
    isCoreLibraryDesugaringEnabled = true
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }
  lint {
    textReport = true
    textOutput = file("build/reports/lint-results.txt")
  }
}

dependencies {
  coreLibraryDesugaring(libs.desugar.jdk.libs)
  // QR codes are drawn from pure Java (no Google Play services, works on every phone).
  api(libs.zxing.core)
  testImplementation(libs.junit)
}
