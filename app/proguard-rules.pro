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

# ----------- Razorpay ProGuard Rules -----------
# These rules are essential for the Razorpay SDK to function correctly in release builds.

# Keeps the JavascriptInterface that Razorpay uses for its WebView.
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}
-keepattributes JavascriptInterface
-keepattributes *Annotation*

# Prevents warnings and ensures all Razorpay classes are kept.
-dontwarn com.razorpay.**
-keep class com.razorpay.** {*;}

# Required to prevent an issue with method inlining.
-optimizations !method/inlining/*

# Ensures the payment callback methods are not removed or renamed.
-keepclasseswithmembers class * {
    public void onPaymentSuccess(java.lang.String);
    public void onPaymentError(int, java.lang.String);
}
# ----------------- End of Razorpay Rules -----------------
