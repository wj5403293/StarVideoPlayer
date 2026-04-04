# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in ${sdk.dir}/tools/proguard/proguard-android.txt

# Keep DLNA model classes
-keep class com.star.dlna.cast.model.** { *; }

# Keep CastDevice Parcelable
-keepclassmembers class com.star.dlna.cast.model.CastDevice {
    <fields>;
    <init>(...);
}

# Keep MediaInfo Parcelable
-keepclassmembers class com.star.dlna.cast.model.MediaInfo {
    <fields>;
    <init>(...);
}

# Keep CastState enum
-keepclassmembers enum com.star.dlna.cast.model.CastState {
    <fields>;
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Keep DLNACastManager interface
-keep interface com.star.dlna.cast.api.DLNACastManager { *; }

# Keep callbacks
-keep interface com.star.dlna.cast.api.DLNACastManager$CastCallback { *; }
-keep interface com.star.dlna.cast.control.DLNAController$Callback { *; }
-keep interface com.star.dlna.cast.discovery.DeviceDiscovery$Callback { *; }
-keep interface com.star.dlna.cast.ui.DeviceListDialog$OnDeviceSelectedListener { *; }