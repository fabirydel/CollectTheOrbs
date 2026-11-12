# Code shrinking (R8) for the release build: unused code from the Google SDKs is dropped, names are kept readable.
-dontobfuscate

# libGDX: classes reached from native code or by reflection
-dontwarn com.badlogic.gdx.backends.android.AndroidFragmentApplication
-dontwarn com.badlogic.gdx.utils.GdxBuild
-dontwarn com.badlogic.gdx.physics.box2d.utils.Box2DBuild
-dontwarn com.badlogic.gdx.jnigen.**
-keep class com.badlogic.gdx.backends.android.** { *; }
-keepclassmembers class com.badlogic.gdx.backends.android.AndroidInput* {
   <init>(com.badlogic.gdx.Application, android.content.Context, java.lang.Object, com.badlogic.gdx.backends.android.AndroidApplicationConfiguration);
}
-keepclassmembers class com.badlogic.gdx.physics.box2d.World {
   boolean contactFilter(long, long);
   void    beginContact(long);
   void    endContact(long);
   void    preSolve(long, long);
   void    postSolve(long, long);
   boolean reportFixture(long);
   float   reportRayFixture(long, float, float, float, float, float);
}
# scene2d skins and the asset loaders create classes by name
-keep class com.badlogic.gdx.scenes.scene2d.ui.** { *; }
-keep class com.badlogic.gdx.graphics.g2d.BitmapFont { *; }
-keep class com.badlogic.gdx.graphics.Color { *; }
-keepclassmembers class * implements com.badlogic.gdx.utils.Json$Serializable { *; }
-keepclasseswithmembernames class * { native <methods>; }

# the game itself, the tween engine and the SQLite wrapper (accessors and drivers are found by reflection)
-keep class com.spaceship.** { *; }
-keep class aurelienribon.tweenengine.** { *; }
-keep class com.badlogic.gdx.sql.** { *; }
-keep class com.badlogic.gdx.sqlite.** { *; }

# WorkManager (pulled in by the ads SDK) creates its Room database and its workers by reflection; R8 in full mode
# otherwise strips the constructors it cannot see being called
-keep class * extends androidx.room.RoomDatabase { <init>(); }
-keep class * extends androidx.work.ListenableWorker { <init>(android.content.Context, androidx.work.WorkerParameters); }
-keep class * extends androidx.work.InputMerger { <init>(); }
-keep class androidx.work.impl.** { <init>(...); }
