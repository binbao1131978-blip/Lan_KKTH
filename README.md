# Lân_KKTH Android project

This ZIP is an Android Studio / GitHub Actions project. The project root contains `app/` directly (do not move it into another nested folder).

## Build with GitHub Actions
1. Extract this ZIP.
2. Upload the **contents of this folder** to the root of a GitHub repository. The repository root must show `app/`, `.github/`, `build.gradle`, and `settings.gradle`.
3. Open the repository's **Actions** tab.
4. Select **Build Lân_KKTH APK** and choose **Run workflow** (or push to `main`/`master`).
5. When the workflow completes, open the run and download artifact **Lan_KKTH-debug-apk**.
6. Extract the artifact ZIP; install `app-debug.apk` on the Android phone.

## Project details
- App name: Lân_KKTH
- Android application ID: `com.nguyenlan.lankkth`
- Minimum Android: 6.0 (API 23)
- HTML entry: `app/src/main/assets/index.html`
- WebView DOM storage is enabled so the app's browser storage can persist on the device.
- The Android bridge supports saving exported files through the Android system file picker.

The HTML app uses an online SheetJS script for Excel functions, so an internet connection is needed for those functions unless the library is later bundled locally.
