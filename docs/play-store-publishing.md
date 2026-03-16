# Google Play Store Publishing Guide

Guide for publishing Daily Sanskrit Quotes to the Google Play Store.

## 1. Generate a Signing Key

Google Play requires all APKs/App Bundles to be signed with a release key.

```bash
keytool -genkey -v \
  -keystore release-key.jks \
  -keyalg RSA \
  -keysize 2048 \
  -validity 10000 \
  -alias release
```

You'll be prompted for a keystore password, key password, and identity details (name, org, etc.).

**Important:**
- Store `release-key.jks` in a secure location outside the project directory.
- NEVER commit the keystore file to version control.
- Back up the keystore and passwords — losing them means you can't update your app.

## 2. Configure Signing in Gradle

Create a `keystore.properties` file in the project root (also excluded from version control):

```properties
storeFile=/path/to/release-key.jks
storePassword=your_store_password
keyAlias=release
keyPassword=your_key_password
```

Add to your root `.gitignore`:

```
keystore.properties
*.jks
```

Update `app/build.gradle.kts` to load the signing config:

```kotlin
import java.util.Properties

val keystoreProperties = Properties().apply {
    val file = rootProject.file("keystore.properties")
    if (file.exists()) load(file.inputStream())
}

android {
    // ... existing config ...

    signingConfigs {
        create("release") {
            storeFile = file(keystoreProperties["storeFile"] as String)
            storePassword = keystoreProperties["storePassword"] as String
            keyAlias = keystoreProperties["keyAlias"] as String
            keyPassword = keystoreProperties["keyPassword"] as String
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
    }
}
```

## 3. Build a Signed App Bundle

Google Play requires Android App Bundles (AAB) for new apps.

```bash
./gradlew :app:bundleRelease
```

The signed AAB will be at:

```
app/build/outputs/bundle/release/app-release.aab
```

To verify the bundle was signed correctly:

```bash
jarsigner -verify -verbose -certs app/build/outputs/bundle/release/app-release.aab
```

If you need a signed APK instead (e.g., for sideloading):

```bash
./gradlew :app:assembleRelease
```

Output: `app/build/outputs/apk/release/app-release.apk`

## 4. Create a Google Play Developer Account

1. Go to [Google Play Console](https://play.google.com/console).
2. Sign in with a Google account.
3. Pay the one-time **$25 registration fee**.
4. Complete identity verification (personal or organization).
   - Personal accounts: government-issued ID.
   - Organization accounts: D-U-N-S number and additional business docs.
5. Wait for verification approval (can take a few days for organizations).

## 5. Create the App Listing

### 5.1 Create a New App

1. In Play Console, click **Create app**.
2. Fill in:
   - **App name:** Daily Sanskrit Quotes
   - **Default language:** English (United States)
   - **App or game:** App
   - **Free or paid:** Free

### 5.2 Store Listing Details

Navigate to **Main store listing** and fill in:

**Short description** (max 80 characters):
> A daily Sanskrit quote with English translation to inspire your day.

**Full description** (max 4000 characters):
> Start each day with timeless wisdom. Daily Sanskrit Quotes delivers one carefully selected Sanskrit quote every morning, complete with its English translation and source attribution.
>
> Features:
> • One new quote every day — never the same until you've seen them all
> • Favorite quotes to revisit later
> • Share quotes with friends via any app
> • Search the full collection by keyword or topic
> • Daily notification reminders
> • Works offline with 1000+ bundled quotes
> • Automatic updates with new quotes over time
> • Dark mode support

### 5.3 Graphics Assets

Prepare the following:

| Asset | Dimensions | Format |
|---|---|---|
| App icon | 512 × 512 px | PNG (32-bit, no alpha) |
| Feature graphic | 1024 × 500 px | PNG or JPEG |
| Phone screenshots | Min 2, 16:9 or 9:16 | PNG or JPEG, min 320px, max 3840px per side |
| 7-inch tablet screenshots | Optional but recommended | Same format as phone |
| 10-inch tablet screenshots | Optional but recommended | Same format as phone |

Tips:
- Take screenshots on a Pixel device or emulator for clean results.
- Use `adb shell screencap -p /sdcard/screenshot.png && adb pull /sdcard/screenshot.png` to capture.
- Show the daily quote screen, favorites, search, and settings.

### 5.4 Privacy Policy

A privacy policy is **required** for all apps on Google Play.

If the app does not collect personal data (Daily Sanskrit Quotes only makes network requests to fetch quote updates from a static file host), create a simple privacy policy stating:

- The app does not collect, store, or share personal information.
- The app makes network requests solely to download quote data updates.
- No analytics, tracking, or advertising SDKs are included.

Host the privacy policy as a public URL (e.g., a GitHub Pages site or a simple HTML page in your repo) and link it in the Play Console under **App content > Privacy policy**.

## 6. Upload and Submit for Review

### 6.1 Set Up App Content

Before uploading, complete all required sections under **App content** in Play Console:

1. **Privacy policy** — paste the URL from step 5.4.
2. **Ads** — select "No, my app does not contain ads."
3. **Content rating** — complete the IARC questionnaire. For a quotes app with no violence, sexual content, or user-generated content, you'll likely receive an "Everyone" rating.
4. **Target audience** — select appropriate age groups (likely 13+ or all ages).
5. **Data safety** — declare what data the app collects (likely "no data collected").

### 6.2 Create a Release

1. Go to **Release > Production** (or start with **Testing > Internal testing** for a limited rollout first).
2. Click **Create new release**.
3. If prompted, opt in to **Google Play App Signing** (recommended — Google manages your upload key).
4. Upload the `app-release.aab` file from step 3.
5. Add release notes, e.g.:
   > Initial release — 1000+ Sanskrit quotes with daily selection, favorites, search, sharing, and notifications.
6. Click **Review release**, then **Start rollout to Production**.

### 6.3 Select Countries and Pricing

1. Go to **Release > Countries/regions**.
2. Select the countries where you want the app available (or select all).
3. Pricing is already set to **Free** from step 5.1.

### 6.4 Submit for Review

After completing all sections, click **Submit for review**. Google typically reviews new apps within a few hours to a few days.

## 7. Post-Launch

### Monitor

- **Android Vitals** in Play Console — watch for ANRs and crash rates.
- **Ratings & reviews** — respond to user feedback promptly.
- **Statistics** — track installs, uninstalls, and active users.

### Update the App

1. Increment `versionCode` and `versionName` in `app/build.gradle.kts`:
   ```kotlin
   defaultConfig {
       versionCode = 2
       versionName = "1.1"
   }
   ```
2. Build a new signed AAB: `./gradlew :app:bundleRelease`
3. Create a new release in Play Console and upload the updated AAB.
4. Add release notes describing what changed.
5. Submit for review.

### Staged Rollouts

For updates, consider using staged rollouts (e.g., 10% → 50% → 100%) to catch issues before they affect all users. This is configurable in the Production release page.
