# Professional Task Manager Android App

This is the Android WebView app for the PHP Professional Task Manager.

## Build APK using GitHub Actions

1. Extract this zip.
2. Upload all extracted files/folders to your GitHub repository root.
3. Open the **Actions** tab.
4. Enable workflows if GitHub asks.
5. Open **Build Android APK**.
6. Click **Run workflow**.
7. After build completes, download the artifact named **ProfessionalTaskManager-APK**.
8. Extract the artifact zip and install the APK on Android.

## Important

Do not upload this zip file directly to GitHub if you want Actions to build it.
Extract it first, then upload the contents.

Your repo root should contain:

- app/
- .github/workflows/build-apk.yml
- build.gradle
- settings.gradle
- gradle.properties

The app asks for your live PHP task manager URL on first launch.
