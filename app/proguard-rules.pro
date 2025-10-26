# ==================== ROOM DATABASE ====================
# Keep only necessary Room classes
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class * {
    <fields>;
}
-keep @androidx.room.Dao class * {
    public <methods>;
}

# Keep data entity classes (only fields, not all methods)
-keep class com.travelagent.pos.data.Customer { <fields>; }
-keep class com.travelagent.pos.data.Trip { <fields>; }
-keep class com.travelagent.pos.data.Seat { <fields>; }
-keep class com.travelagent.pos.data.Ticket { <fields>; }
-keep class com.travelagent.pos.data.Driver { <fields>; }
-keep class com.travelagent.pos.data.Vehicle { <fields>; }
-keep class com.travelagent.pos.data.Payment { <fields>; }
-keep class com.travelagent.pos.data.CustomerStats { <fields>; }

# ==================== VIEWBINDING ====================
# Keep ViewBinding classes (generated code needs reflection)
-keep class com.travelagent.pos.databinding.** {
    public <init>(...);
    public static *** bind(android.view.View);
    public static *** inflate(android.view.LayoutInflater);
}

# ==================== KOTLIN ====================
# Keep Kotlin metadata
-keep class kotlin.Metadata { *; }
-keepclassmembers class ** {
    @kotlin.Metadata public <methods>;
}

# Keep Kotlin coroutines
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}

# ==================== PARCELABLE ====================
-keepclassmembers class * implements android.os.Parcelable {
    public static final ** CREATOR;
}

# ==================== SERIALIZABLE ====================
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# ==================== THERMAL PRINTER LIBRARY ====================
# Keep ESCPOS printer classes
-keep class com.dantsu.escposprinter.** { *; }
-dontwarn com.dantsu.escposprinter.**

# ==================== PDF LIBRARY ====================
# Keep iText7 classes
-keep class com.itextpdf.** { *; }
-dontwarn com.itextpdf.**

# ==================== REMOVE LOGGING IN RELEASE ====================
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}

# ==================== OPTIMIZATION ====================
-optimizationpasses 5
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-verbose

# Remove unnecessary code
-assumenosideeffects class kotlin.jvm.internal.Intrinsics {
    public static void checkNotNull(...);
    public static void checkParameterIsNotNull(...);
}