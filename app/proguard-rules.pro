# Enterprise Proguard & R8 Shrinking Rules for Reqstrata
# Optimizes container and APK/AAB bundle size while preserving reflective serialization

# 1. Room Database
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**
-keep class androidx.room.** { *; }
-keep class com.theoriongd.reqstrata.data.local.entity.** { *; }
-keep class com.theoriongd.reqstrata.data.local.dao.** { *; }

# 2. Retrofit & OkHttp
-dontwarn retrofit2.**
-dontwarn okhttp3.**
-dontwarn org.bouncycastle.jsse.**
-dontwarn org.conscrypt.**
-dontwarn org.openjsse.**
-keep class retrofit2.** { *; }
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keepclassmembers,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}

# 3. Moshi & JSON Serialization
-keepclasseswithmembers class * {
    @com.squareup.moshi.* <methods>;
}
-keepclasseswithmembers class * {
    @com.squareup.moshi.* <fields>;
}
-keep class com.theoriongd.reqstrata.data.remote.gemini.** { *; }
-keep class com.theoriongd.reqstrata.domain.model.** { *; }

# 4. Kotlin Coroutines
-dontwarn kotlinx.coroutines.**
-keep class kotlinx.coroutines.** { *; }

# 5. Jetpack Compose
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**

# 6. Android Platform Optimization
-keepattributes *Annotation*
-repackageclasses ''
-allowaccessmodification
