## Quick context

This is an Android application (module `:app`) written in Kotlin/Java using modern AndroidX libraries.
Key high-level facts you should know immediately:
- Application package: `com.travelagent.pos` (see `app/src/main/AndroidManifest.xml`).
- Build system: Gradle (root `build.gradle`, module `app/build.gradle`).
- Language/platform: Kotlin with Java 17 bytecode (`compileOptions` + `kotlinOptions.jvmTarget = 17`).
- Primary libraries: Room (2.6.x), ViewBinding, Lifecycle (ViewModel/LiveData), Coroutines, WorkManager, Paging.

## Big-picture architecture

- Single primary Android app module: `app/` (declared in `settings.gradle` as `:app`).
- UI layer: Activities are declared in the manifest under `.ui` (examples: `ui.CustomerListActivity`, `ui.TripListActivity`, `ui.BookingActivity`). See `app/src/main/AndroidManifest.xml` for the full list.
- Data layer: Room is used for persistence. Room schema output is wired to `app/schemas` via `annotationProcessorOptions` in `app/build.gradle`.
- Background & batching: `WorkManager` is used for background tasks.
- Concurrency: Kotlin Coroutines are the recommended mechanism (see `kotlinx-coroutines-android` in `app/build.gradle`).

Why things are structured this way
- Room + schema directory: the project generates and persists Room schemas under `app/schemas` to help with migrations and testing.
- `viewBinding` is enabled (in `app/build.gradle`) so layout inflation is typically done via generated binding classes, not `findViewById`.

## Useful files to open first (examples)
- `app/build.gradle` — Gradle config, compileSdk, dependencies, Room schema location, viewBinding.
- `app/src/main/AndroidManifest.xml` — list of Activities, permissions (notable: BLUETOOTH, WRITE_EXTERNAL_STORAGE with maxSdk 32), and FileProvider configuration (`${applicationId}.fileprovider`).
- `app/schemas/` — generated Room schema folders (verify migrations and schema package names here).
- `gradle/libs.versions.toml` — repository shows a `gradle/` folder; prefer centralizing versions here if not already used.

## Developer workflows (copyable examples)
Use PowerShell on Windows (project root):

```powershell
# build debug APK
.\gradlew.bat assembleDebug

# run unit tests (JVM)
.\gradlew.bat test

# run Android instrumented tests (on connected device/emulator)
.\gradlew.bat connectedAndroidTest

# install debug APK to a single connected device
.\gradlew.bat installDebug
```

Notes:
- Android Studio remains the fastest path to run and debug on devices; ensure a JDK 17 toolchain is available.
- `local.properties` currently contains an SDK path — do not rely on it being identical across contributors.

## Project-specific conventions and patterns
- ViewBinding is used (enabled via `buildFeatures.viewBinding true`). Prefer binding classes for view access.
- Room schemas: annotation processor argument writes schemas to `$projectDir/schemas`. Keep schemas checked into VCS to help with schema diff/migration testing.
- FileProvider: authority uses `${applicationId}.fileprovider`. When constructing URIs in code, use `BuildConfig.APPLICATION_ID + ".fileprovider"` or `context.packageName` to be robust.
- Activities are organized under the `ui` package and often use parentActivityName attributes for navigation/back stack.

## Integration points & external dependencies to be aware of
- Bluetooth permissions and runtime handling (manifest contains multiple BLUETOOTH permissions). Android 12+ requires `BLUETOOTH_CONNECT` permission and runtime checks.
- External storage read/write are present with `maxSdkVersion=32` — code paths using legacy storage need conditional logic for Android 11+ scoped storage.
- Room (kapt) -> ensure contributors have `kapt` configured in IDE and JDK 17 installed locally to avoid compile issues.

## Immediate attention items (discoverable suggestions)
- Remove or document `myapplication/` module: it exists in the repo but is not included in `settings.gradle` (which only includes `:app`). Either remove stale code or include/update the module.
- Version skew: `app/build.gradle` references Room 2.6.1 while `myapplication/build.gradle` references 2.5.1; centralize versions (use `libs.versions.toml` or Gradle ext properties).
- `local.properties` is present in the repo — this file is normally local-only (SDK path) and should be removed from version control.
- Consider consolidating duplicate dependency entries and using a centralized versions catalog (`gradle/libs.versions.toml`) to simplify upgrades.
- Release build currently has `minifyEnabled false`. Consider enabling R8 and proguard rules for release APKs if distribution size or obfuscation are concerns.

## How an AI agent can be productive here (short checklist)
1. Open `app/build.gradle` and `app/src/main/AndroidManifest.xml` to learn the feature surface.
2. Inspect `app/schemas/` to understand the Room schema and table names/namespaces (important for migration logic).
3. Search for `AppDatabase` and `@Database` annotations to find DAOs and migration code.
4. Look for usages of `viewBinding` classes (files named `*Binding`) to see UI wiring patterns.
5. When changing dependencies or Gradle config, run `.\gradlew.bat assembleDebug` and one test task to validate.

## Where to add tests or small fixes (low-risk PR ideas)
- Add a short `README.md` at project root with a one-liner: required JDK, Android SDK, and how to build + run tests.
- Add a `CONTRIBUTING.md` or update `local.properties` handling guidance (exclude from git) so new contributors don't commit local SDK paths.
- Add a Gradle version catalog entry (if not used) and migrate Room/lifecycle/coroutines versions there for consistent upgrades.

---
If you want, I can: (a) create this file in the repository (done), (b) open a draft PR with the `local.properties` removal and a short README, or (c) scan for `AppDatabase` and list exact DAO files and locations. Which next step do you want?
