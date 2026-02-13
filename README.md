# RootMAC - Free Root-Only Android MAC Address Changer

A comprehensive native Android application for managing and changing MAC addresses on rooted devices. Built with Kotlin and Jetpack Compose.

## Features

### Phase 1: Core Engine & Manual Control ✓
- **Root & Environment Validation**
  - Detect root access (Magisk/SuperSU)
  - Verify su permissions
  - Detect BusyBox and iproute2
  - Display SELinux mode
  - Compatibility report

- **Interface Detection**
  - Scan all network interfaces (wlan0, eth0, rmnet*, etc.)
  - Display current MAC, interface state, IP address
  - Real-time interface monitoring

- **MAC Modification Engine**
  - Method A: `ip link set` commands
  - Method B: `ifconfig` commands
  - Method C: `/sys/class/net/` sysfs
  - Automatic fallback on failure

- **MAC Generator**
  - Fully random generation
  - Manual entry with validation
  - Prevent multicast MAC
  - Prevent broadcast MAC

- **Original MAC Restore**
  - Encrypted storage of original MAC
  - One-click restore
  - Verification after restore

- **Basic Logging**
  - Timestamp, old/new MAC
  - Success/failure status
  - Shell output capture

### Phase 2: Profiles & Automation (In Progress)
- Profile system with create/edit/delete
- Automation triggers:
  - Apply on boot (BOOT_COMPLETED)
  - Apply on network change
  - Apply when connected to specific SSID
  - Apply on WiFi toggle
  - Apply on airplane mode toggle
  - Apply on screen off

- Scheduled rotation (5min, 15min, 30min, 1hr, custom)
- Background execution via WorkManager
- Extended logging with shell output

### Phase 3: Advanced Randomization & Safety (Planned)
- Advanced MAC generator with OUI support
- Test mode with countdown timer
- Auto-revert on connection failure
- Crash-safe restore on boot
- Compatibility detection engine
- Persistent notification system

### Phase 4: Polish & Stealth (Planned)
- Optional stealth mode
- Hide launcher icon
- Advanced settings panel
- UI refinement
- Architecture stabilization

## Project Structure

```
RootMAC/
├── app/
│   ├── build.gradle.kts
│   ├── proguard-rules.pro
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml
│       │   ├── kotlin/com/rootmac/app/
│       │   │   ├── MainActivity.kt
│       │   │   ├── core/
│       │   │   │   ├── RootAccess.kt
│       │   │   │   └── MacAddressManager.kt
│       │   │   ├── data/
│       │   │   │   ├── Repository.kt
│       │   │   │   └── db/
│       │   │   │       ├── AppDatabase.kt
│       │   │   │       ├── Daos.kt
│       │   │   │       └── Entities.kt
│       │   │   ├── receiver/
│       │   │   │   ├── BootCompletedReceiver.kt
│       │   │   │   └── NetworkStateReceiver.kt
│       │   │   ├── worker/
│       │   │   │   ├── BootCompletedWorker.kt
│       │   │   │   └── NetworkChangeWorker.kt
│       │   │   ├── ui/
│       │   │   │   ├── screens/
│       │   │   │   │   └── MainScreen.kt
│       │   │   │   ├── theme/
│       │   │   │   │   ├── Theme.kt
│       │   │   │   │   ├── Color.kt
│       │   │   │   │   └── Type.kt
│       │   │   │   └── viewmodel/
│       │   │   │       └── RootMACViewModel.kt
│       │   │   └── BuildConfig.kt
│       │   └── res/
│       │       ├── values/
│       │       │   ├── strings.xml
│       │       │   ├── colors.xml
│       │       │   └── themes.xml
│       │       └── mipmap/
│       │           └── ic_launcher.png
│       └── test/
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
└── README.md
```

## Technology Stack

- **Language**: Kotlin 1.9.22
- **UI Framework**: Jetpack Compose 1.6.4
- **Database**: Room 2.6.1
- **Background Tasks**: WorkManager 2.9.0
- **Root Access**: libsu 6.2.1
- **Logging**: Timber 5.0.1
- **Build System**: Gradle 8.2.0
- **Target SDK**: 34 (Android 14)
- **Min SDK**: 28 (Android 9)

## Getting Started

### Prerequisites

- Android Studio Hedgehog or later
- Android SDK 34
- JDK 17 or later
- A rooted Android device (Magisk or SuperSU)

### Building

1. **Clone/Extract the project**
   ```bash
   cd RootMAC
   ```

2. **Build the APK**
   ```bash
   ./gradlew build
   ```

3. **Build and run on device**
   ```bash
   ./gradlew installDebug
   ```

4. **Build release APK**
   ```bash
   ./gradlew assembleRelease
   ```

### Installation on Rooted Device

1. Build the release APK:
   ```bash
   ./gradlew assembleRelease
   ```

2. Install on device:
   ```bash
   adb install app/build/outputs/apk/release/app-release.apk
   ```

3. Grant root permissions when prompted

## Usage

### Basic MAC Change

1. **Select Interface**: Choose the network interface (wlan0, eth0, etc.)
2. **Enter MAC Address**: Type the new MAC address in format `XX:XX:XX:XX:XX:XX`
3. **Choose Method**: Select execution method (A, B, or C)
4. **Apply**: Tap "Apply MAC Change"
5. **Verify**: Check the logs to confirm success

### Generate Random MAC

- Tap "Generate Random MAC" to create a valid random MAC address
- The app ensures:
  - Locally administered bit is set
  - Multicast bit is cleared
  - No broadcast MAC is generated

### Restore Original MAC

- Tap "Restore Original MAC" to revert to the saved original MAC
- Original MAC is saved on first change and stored securely

### View Logs

- Recent operations are displayed in the logs section
- Shows timestamp, interface, old/new MAC, and success status
- Expandable logs show shell output and errors

## MAC Modification Methods

### Method A: ip link (Recommended)
```bash
ip link set dev wlan0 down
ip link set dev wlan0 address XX:XX:XX:XX:XX:XX
ip link set dev wlan0 up
```

### Method B: ifconfig
```bash
ifconfig wlan0 down
ifconfig wlan0 hw ether XX:XX:XX:XX:XX:XX
ifconfig wlan0 up
```

### Method C: sysfs
```bash
echo XX:XX:XX:XX:XX:XX > /sys/class/net/wlan0/address
```

## Permissions

The app requires the following permissions:

- `CHANGE_NETWORK_STATE` - Modify network configuration
- `ACCESS_NETWORK_STATE` - Read network information
- `CHANGE_WIFI_STATE` - Control WiFi
- `ACCESS_WIFI_STATE` - Read WiFi information
- `RECEIVE_BOOT_COMPLETED` - Auto-apply on boot
- `POST_NOTIFICATIONS` - Show persistent notification
- `READ_EXTERNAL_STORAGE` - Export logs
- `WRITE_EXTERNAL_STORAGE` - Export logs
- `QUERY_ALL_PACKAGES` - Detect system capabilities

## Database Schema

### mac_profiles
- `id` (PK): Profile ID
- `name`: Profile name
- `interfaceName`: Target interface
- `macAddress`: MAC to apply
- `executionMethod`: A, B, or C
- `autoApply`: Auto-apply on boot
- `ssidTarget`: Optional SSID target
- `createdAt`, `updatedAt`: Timestamps

### mac_logs
- `id` (PK): Log ID
- `profileId` (FK): Associated profile
- `interfaceName`: Interface name
- `oldMac`, `newMac`: MAC addresses
- `isSuccess`: Operation result
- `executionTime`: Duration in ms
- `stdout`, `stderr`: Command output
- `timestamp`: Log timestamp

### original_macs
- `interfaceName` (PK): Interface name
- `originalMac`: Original MAC address
- `savedAt`: Save timestamp

### app_settings
- `key` (PK): Setting key
- `value`: Setting value

## Compatibility Notes

- **Android 10+**: WiFi MAC randomization is handled at framework level
- **Kernel Restrictions**: Some kernels prevent MAC modification
- **Driver Locking**: Some drivers lock MAC address
- **SELinux**: May require permissive mode for some operations
- **Reboot Reset**: Some devices reset MAC on reboot

## Known Limitations

- Requires rooted device
- May cause temporary network disconnection
- Some networks may block spoofed MAC
- Not guaranteed on all kernels/drivers
- May violate network policies

## Troubleshooting

### Root Access Denied
- Ensure device is properly rooted with Magisk or SuperSU
- Grant root permissions when prompted
- Check SELinux mode (may need permissive)

### MAC Change Fails
- Try different execution methods (A, B, C)
- Check if BusyBox/iproute2 are available
- Verify interface name is correct
- Check device logs: `adb logcat | grep RootMAC`

### Changes Revert After Reboot
- Some devices reset MAC on reboot
- Enable "Auto-apply on boot" in settings
- Create a profile and enable auto-apply

## Security Considerations

- Root access is required and dangerous
- This app can modify network configuration
- May violate network policies or ToS
- Use responsibly and legally
- No warranty or liability

## Distribution

This app is **not compatible with Google Play Store** due to root requirements and MAC spoofing capabilities.

**Recommended distribution:**
- GitHub Releases
- Direct APK download
- F-Droid (if compliant)

## License

This project is provided as-is for educational and authorized use only.

## Contributing

Contributions are welcome! Please follow the existing code style and architecture patterns.

## Support

For issues and feature requests, please open an issue on the project repository.

## Disclaimer

**IMPORTANT**: This application modifies network configuration and may:
- Violate network policies
- Cause temporary disconnection
- Be blocked by some networks
- Not work on all devices
- Require legal authorization to use

**Users assume full responsibility for any consequences of using this application.**

---

**Built with Kotlin, Jetpack Compose, and libsu**
