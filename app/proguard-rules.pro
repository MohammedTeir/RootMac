# Jetpack Compose
-keep class androidx.compose.** { *; }
-keepclasseswithmembers class androidx.compose.** {
    public <methods>;
}

# Room Database
-keep class androidx.room.** { *; }
-keepclasseswithmembers class * {
    @androidx.room.* <fields>;
}
-keepclasseswithmembers class * {
    @androidx.room.* <methods>;
}

# WorkManager
-keep class androidx.work.** { *; }

# libsu (Root access)
-keep class com.topjohnwu.superuser.** { *; }
-keepclasseswithmembers class com.topjohnwu.superuser.** {
    public <methods>;
}

# Timber
-keep class timber.log.** { *; }

# Kotlin
-keep class kotlin.** { *; }
-keepclasseswithmembers class kotlin.** {
    public <methods>;
}

# Keep data classes
-keepclasseswithmembers class com.rootmac.app.** {
    public <init>(...);
    public <methods>;
    public <fields>;
}

# Keep enum values
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
