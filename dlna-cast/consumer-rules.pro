# Consumer rules for dlna-cast library
# This file is used by consumers of this library, not by the library itself

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