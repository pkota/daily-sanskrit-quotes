# macOS Development Environment Setup

## Prerequisites

- macOS 13 (Ventura) or later
- At least 15 GB free disk space (Android Studio + SDK + emulator images)
- Admin access for Homebrew installations

## 1. Install Homebrew

Skip if already installed (`brew --version` to check).

```bash
/bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"
```

## 2. Install JDK 17

The project requires JDK 17 (configured in `build.gradle.kts` via `JavaVersion.VERSION_17`).

```bash
brew install --cask corretto17
```

Set `JAVA_HOME` — add to your `~/.zshrc`:

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
```

Reload:

```bash
source ~/.zshrc
```

Verify:

```bash
java -version
```

## 3. Install Android Studio

```bash
brew install --cask android-studio
```

## 4. Configure Android SDK

1. Open Android Studio
2. Go to **Settings → Languages & Frameworks → Android SDK**
3. Under **SDK Platforms**, install:
   - Android 14 (API 34) — this is the `compileSdk` and `targetSdk`
   - Android 8.0 (API 26) — this is the `minSdk`
4. Under **SDK Tools**, install:
   - Android SDK Build-Tools
   - Android SDK Command-line Tools
   - Android SDK Platform-Tools
   - Android Emulator

Set `ANDROID_HOME` — add to your `~/.zshrc`:

```bash
export ANDROID_HOME=$HOME/Library/Android/sdk
export PATH=$PATH:$ANDROID_HOME/platform-tools:$ANDROID_HOME/cmdline-tools/latest/bin
```

Reload:

```bash
source ~/.zshrc
```

## 5. SDK Versions

| Setting      | Value |
|-------------|-------|
| `minSdk`     | 26    |
| `targetSdk`  | 34    |
| `compileSdk` | 34    |

These are defined in `app/build.gradle.kts`.

## 6. Project Setup

```bash
# Clone the repository
git clone <repository-url>
cd DailySanskritQuotes

# Open in Android Studio
open -a "Android Studio" .
```

Android Studio will automatically run Gradle sync on first open. Wait for it to complete — this downloads all dependencies.

## 7. Build Commands

All commands use the Gradle wrapper (`./gradlew`) included in the project.

```bash
# Build debug APK
./gradlew :app:assembleDebug

# Run unit tests
./gradlew :app:testDebugUnitTest

# Build release APK
./gradlew :app:assembleRelease

# Clean build
./gradlew clean

# Run all checks (lint + tests)
./gradlew :app:check
```

The debug APK is output to `app/build/outputs/apk/debug/app-debug.apk`.

## 8. Troubleshooting

**Gradle sync fails with "SDK location not found"**
Create a `local.properties` file in the project root:
```properties
sdk.dir=/Users/<your-username>/Library/Android/sdk
```

**`JAVA_HOME` not set or wrong version**
```bash
/usr/libexec/java_home -V          # list installed JDKs
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
```

**"License not accepted" errors**
```bash
yes | sdkmanager --licenses
```

**Emulator won't start on Apple Silicon**
Ensure you installed the ARM64 system image:
```bash
sdkmanager "system-images;android-34;google_apis;arm64-v8a"
```

**Build fails with "Could not resolve dependencies"**
```bash
./gradlew --refresh-dependencies :app:assembleDebug
```

**Out of memory during build**
The project sets `-Xmx2048m` in `gradle.properties`. If builds still OOM, increase it:
```properties
org.gradle.jvmargs=-Xmx4096m -Dfile.encoding=UTF-8
```
