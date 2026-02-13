# RootMAC Deployment Guide

This guide provides step-by-step instructions for building, testing, and deploying RootMAC.

## Prerequisites

- Android Studio Hedgehog or later
- Android SDK 34
- JDK 17 or later
- A rooted Android device (Magisk or SuperSU)
- Gradle 8.2.0 or later

## Building the Project

### Step 1: Open in Android Studio

1. Launch Android Studio
2. Click "Open" and navigate to the RootMAC project directory
3. Wait for Gradle sync to complete

### Step 2: Configure Build

The project is pre-configured with:
- Target SDK: 34 (Android 14)
- Min SDK: 28 (Android 9)
- Kotlin 1.9.22
- Jetpack Compose 1.6.4

### Step 3: Build APK

**Debug Build:**
```bash
./gradlew assembleDebug
```

Output: `app/build/outputs/apk/debug/app-debug.apk`

**Release Build:**
```bash
./gradlew assembleRelease
```

Output: `app/build/outputs/apk/release/app-release.apk`

### Step 4: Build AAB (for Play Store)

```bash
./gradlew bundleRelease
```

Output: `app/build/outputs/bundle/release/app-release.aab`

**Note:** RootMAC is not compatible with Google Play Store due to root requirements.

## Testing

### Unit Tests

```bash
./gradlew test
```

### Instrumented Tests (on device/emulator)

```bash
./gradlew connectedAndroidTest
```

### Manual Testing Checklist

- [ ] Root access detection works
- [ ] Network interfaces are detected
- [ ] MAC change succeeds with Method A
- [ ] MAC change succeeds with Method B
- [ ] MAC change succeeds with Method C
- [ ] Original MAC restore works
- [ ] Random MAC generation works
- [ ] Logs are recorded correctly
- [ ] Profiles can be created/edited/deleted
- [ ] Boot automation works
- [ ] Network change automation works
- [ ] Settings are persisted

## Installation on Device

### Via USB

1. Enable USB debugging on device
2. Connect device via USB
3. Run:
   ```bash
   adb install app/build/outputs/apk/debug/app-debug.apk
   ```

### Via ADB Push

1. Push APK to device:
   ```bash
   adb push app/build/outputs/apk/release/app-release.apk /sdcard/
   ```

2. Install via device file manager or:
   ```bash
   adb shell pm install /sdcard/app-release.apk
   ```

### Manual Installation

1. Build release APK
2. Transfer to device via USB or download link
3. Open file manager on device
4. Tap APK file to install
5. Grant permissions when prompted

## Signing Release APK

### Generate Keystore

```bash
keytool -genkey -v -keystore rootmac.jks \
  -keyalg RSA -keysize 2048 -validity 10000 \
  -alias rootmac
```

### Configure Signing in build.gradle.kts

```kotlin
signingConfigs {
    create("release") {
        storeFile = file("rootmac.jks")
        storePassword = "your_password"
        keyAlias = "rootmac"
        keyPassword = "your_password"
    }
}

buildTypes {
    release {
        signingConfig = signingConfigs.getByName("release")
    }
}
```

### Build Signed APK

```bash
./gradlew assembleRelease
```

## Distribution

### GitHub Releases

1. Create a GitHub repository
2. Build release APK
3. Create a release on GitHub
4. Upload APK as an asset
5. Add release notes

Example:
```
RootMAC v1.0.0

Free Root-Only Android MAC Address Changer

Features:
- Root validation and compatibility detection
- Multiple MAC modification methods
- Profile system with automation
- Advanced MAC generation
- Safety and recovery mechanisms

Installation:
1. Download app-release.apk
2. Enable "Unknown Sources" in Settings
3. Install the APK
4. Grant root permissions when prompted
```

### Direct APK Download

Host APK on a web server:
```
https://example.com/rootmac-v1.0.0.apk
```

### F-Droid

1. Create F-Droid repository
2. Add metadata file
3. Submit to F-Droid

## Versioning

Update version in `build.gradle.kts`:

```kotlin
defaultConfig {
    versionCode = 2  // Increment for each release
    versionName = "1.0.1"  // Semantic versioning
}
```

## Release Checklist

- [ ] All features implemented and tested
- [ ] No console errors or warnings
- [ ] ProGuard/R8 obfuscation enabled
- [ ] Version number updated
- [ ] Changelog prepared
- [ ] APK signed with release key
- [ ] APK tested on multiple devices
- [ ] README updated
- [ ] Release notes prepared
- [ ] GitHub release created

## Troubleshooting Build Issues

### Gradle Sync Fails

```bash
./gradlew clean
./gradlew sync
```

### Build Cache Issues

```bash
./gradlew clean build --no-build-cache
```

### Dependency Conflicts

```bash
./gradlew dependencies
```

### Memory Issues

Add to `gradle.properties`:
```
org.gradle.jvmargs=-Xmx4096m
```

## Performance Optimization

### ProGuard Rules

Already configured in `app/proguard-rules.pro`

### Build Time Optimization

```bash
# Parallel builds
org.gradle.parallel=true

# Caching
org.gradle.caching=true

# Daemon
org.gradle.daemon=true
```

## Continuous Integration

### GitHub Actions Example

```yaml
name: Build RootMAC

on: [push, pull_request]

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      - name: Set up JDK
        uses: actions/setup-java@v2
        with:
          java-version: '17'
      - name: Build
        run: ./gradlew build
      - name: Upload APK
        uses: actions/upload-artifact@v2
        with:
          name: app-debug.apk
          path: app/build/outputs/apk/debug/app-debug.apk
```

## Security Considerations

1. **Sign APKs**: Always sign release APKs with a private key
2. **Protect Keystore**: Keep keystore file secure
3. **Verify Downloads**: Provide SHA256 checksums
4. **HTTPS Only**: Distribute over HTTPS
5. **Code Review**: Review all changes before release

## Post-Release

1. Monitor crash reports
2. Collect user feedback
3. Track issues on GitHub
4. Plan next release
5. Update documentation

## Support

For issues or questions:
1. Check GitHub Issues
2. Review IMPLEMENTATION_GUIDE.md
3. Check logs: `adb logcat | grep RootMAC`

## License

RootMAC is provided as-is for educational and authorized use only.

---

**Built with Kotlin, Jetpack Compose, and libsu**
