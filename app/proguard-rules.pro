# Add project specific ProGuard rules here.
# For Room Database
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# For Data Models
-keep class com.example.data.model.** { *; }

# For Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# Line numbers in stacktraces for crash diagnostics
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Moshi (reflection via KotlinJsonAdapterFactory) needs Kotlin metadata and the model classes.
-keep class kotlin.Metadata { *; }
-keepclassmembers class kotlin.Metadata { public <methods>; }
-keep class com.example.ui.viewmodel.CycleAnalysisResult { *; }
-keep class com.example.ui.viewmodel.CycleAnalysisResult$* { *; }
-keep class com.example.data.preferences.** { *; }
-dontwarn org.jetbrains.annotations.**

# Firebase AI Logic / Firestore ship their own consumer rules; silence optional deps.
-dontwarn com.google.firebase.**
-dontwarn io.ktor.**
-dontwarn org.slf4j.**

# Strip verbose/debug/info logging from release builds so no health data can reach logcat.
-assumenosideeffects class android.util.Log {
    public static int v(...);
    public static int d(...);
    public static int i(...);
}
