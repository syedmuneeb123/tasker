Professional Task Manager - Android Studio Project

Important:
The earlier 5-7 KB APK files were not proper compiled Android APKs, so Android rejected them with:
"There was a problem while parsing the package."

This folder is a real Android Studio project. Build it using Android Studio:

1. Install/open Android Studio.
2. File > Open > select this folder: ProfessionalTaskManagerAndroidStudio
3. Let Gradle sync finish.
4. Build > Build Bundle(s) / APK(s) > Build APK(s)
5. Android Studio will create the APK here:
   app/build/outputs/apk/debug/app-debug.apk

For client/final install:
Build > Generate Signed Bundle / APK > APK
Then create a release key and build a signed APK.

How app connects:
1. Install APK.
2. Open app.
3. Enter your live PHP task manager URL, example:
   https://yourdomain.com/task-manager/
4. It will open the same web system and use the same database/login.

Notes:
- Upload the PHP task manager to hosting first.
- HTTPS is recommended.
- If the URL is wrong, the app shows a Change URL option on connection error.
- Package name: com.syed.professionaltaskmanager
- minSdk: 23
- targetSdk: 35
