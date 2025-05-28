# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# Keep the generic signatures for your data/model classes used with Gson
-keepattributes Signature
-keep class com.example.test.model.local.FaceInfo { *; } # Replace with your actual FaceInfo class
# If FaceInfo has nested classes or other related classes, keep them too:
# -keep class com.example.test.model.local.FaceInfo$* { *; }

# Keep the generic signatures for classes used with TypeToken in your Converters
-keepattributes Signature
-keep class com.example.test.model.local.Converters { *; } # Or be more specific if possible

# General Gson rules (often helpful, but the above are more targeted for this specific error)
-keepattributes EnclosingMethod
-keep class com.google.gson.reflect.TypeToken { *; }
-keep class * extends com.google.gson.reflect.TypeToken