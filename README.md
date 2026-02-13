# RootMAC - Advanced Root-Only Android MAC Address Changer

<p align="center">
  <img src="https://img.shields.io/badge/Platform-Android-green?style=for-the-badge&logo=android" alt="Platform">
  <img src="https://img.shields.io/badge/Language-Kotlin-blue?style=for-the-badge&logo=kotlin" alt="Language">
  <img src="https://img.shields.io/badge/Status-Beta-yellow?style=for-the-badge" alt="Status">
  <img src="https://img.shields.io/badge/License-MIT-orange?style=for-the-badge" alt="License">
</p>

<p align="center">
  <em>A comprehensive native Android application for managing and changing MAC addresses on rooted devices. Built with Kotlin and Jetpack Compose.</em>
</p>

## 🚀 Overview

RootMAC is a powerful tool designed for advanced Android users who need to modify their device's MAC address for privacy, security, or network compatibility purposes. This application provides a user-friendly interface to change MAC addresses on rooted Android devices with multiple execution methods and robust error handling.

## ✨ Key Features

### 🔧 Core Functionality
- **Root Environment Validation** - Comprehensive detection of root access, busybox, and system utilities
- **Multi-Method MAC Modification** - Three different approaches for MAC address changes with automatic fallback
- **Advanced MAC Generation** - Random MAC generation with proper OUI compliance and validation
- **Secure Original MAC Storage** - Encrypted preservation of original MAC addresses for easy restoration

### 📊 Interface Management
- **Real-time Interface Detection** - Automatic scanning of all network interfaces (wlan0, eth0, rmnet*, etc.)
- **Detailed Interface Information** - Current MAC, interface state, and IP address display
- **Live Monitoring** - Continuous tracking of network interface changes

### 🎯 Profile System
- **Customizable Profiles** - Create, edit, and manage MAC address profiles
- **Automation Triggers** - Apply MAC changes on boot, network changes, specific SSIDs, and more
- **Scheduled Rotation** - Automatic MAC rotation at configurable intervals

### 🛡️ Safety Features
- **Test Mode** - Countdown timer with automatic revert for safe testing
- **Connection Failure Recovery** - Auto-revert on network connection failure
- **Crash-Safe Restore** - Automatic MAC restoration on device boot after crashes

## 📋 Prerequisites

- Rooted Android device (Magisk or SuperSU recommended)
- Android 9.0 (API 28) or higher
- BusyBox or iproute2 installed (recommended)

## 🛠️ Installation

### From Source

1. **Clone the repository**
   ```bash
   git clone https://github.com/yourusername/RootMAC.git
   cd RootMAC
   ```

2. **Build the application**
   ```bash
   ./gradlew build
   ```

3. **Install on device**
   ```bash
   ./gradlew installDebug
   # or for release build
   ./gradlew assembleRelease
   adb install app/build/outputs/apk/release/app-release.apk
   ```

### Direct APK Installation

1. Download the latest release APK from the [Releases](https://github.com/yourusername/RootMAC/releases) page
2. Install on your rooted device
3. Grant root permissions when prompted

## 🎮 Usage Guide

### Basic MAC Change

1. **Launch the Application** - Open RootMAC on your rooted device
2. **Select Interface** - Choose the network interface you want to modify (typically wlan0 for WiFi)
3. **Enter New MAC** - Input the desired MAC address in `XX:XX:XX:XX:XX:XX` format
4. **Choose Method** - Select from three execution methods (A, B, or C) based on your device compatibility
5. **Apply Changes** - Tap "Apply MAC Change" and confirm root access
6. **Verify Success** - Check the logs to confirm the operation completed successfully

### Generate Random MAC

- Use the "Generate Random MAC" button to create a valid, randomly-generated MAC address
- The application ensures proper MAC address formatting and avoids multicast/broadcast addresses

### Profile Management

- Create profiles for different network environments
- Set up automation rules to apply MAC changes based on triggers
- Schedule automatic MAC rotation for enhanced privacy

### Restore Original MAC

- Use "Restore Original MAC" to revert to the MAC address that was present when the app was first run
- Original MAC addresses are securely stored and can be restored at any time

## ⚙️ Technical Details

### MAC Modification Methods

#### Method A: `ip link` (Recommended)
```bash
ip link set dev wlan0 down
ip link set dev wlan0 address XX:XX:XX:XX:XX:XX
ip link set dev wlan0 up
```

#### Method B: `ifconfig`
```bash
ifconfig wlan0 down
ifconfig wlan0 hw ether XX:XX:XX:XX:XX:XX
ifconfig wlan0 up
```

#### Method C: `sysfs`
```bash
echo XX:XX:XX:XX:XX:XX > /sys/class/net/wlan0/address
```

### Supported Interfaces

- **WiFi**: `wlan0`, `wlan1`, `wlp*`
- **Ethernet**: `eth0`, `eth1`, `enp*`
- **Cellular**: `rmnet*`, `ccmni*`, `v4-rmnet*`
- **Tethering**: `rndis0`, `usb0`, `bnep*`

## 📁 Project Structure

```
RootMAC/
├── app/
│   ├── build.gradle.kts          # Module build configuration
│   ├── proguard-rules.pro        # Code obfuscation rules
│   └── src/
│       └── main/
│           ├── AndroidManifest.xml
│           ├── kotlin/
│           │   └── com/rootmac/app/
│           │       ├── MainActivity.kt
│           │       ├── core/
│           │       │   ├── RootAccess.kt
│           │       │   └── MacAddressManager.kt
│           │       ├── data/
│           │       │   ├── Repository.kt
│           │       │   └── db/
│           │       │       ├── AppDatabase.kt
│           │       │       ├── Daos.kt
│           │       │       └── Entities.kt
│           │       ├── receiver/
│           │       │   ├── BootCompletedReceiver.kt
│           │       │   └── NetworkStateReceiver.kt
│           │       ├── worker/
│           │       │   ├── BootCompletedWorker.kt
│           │       │   └── NetworkChangeWorker.kt
│           │       ├── ui/
│           │       │   ├── screens/
│           │       │   │   └── MainScreen.kt
│           │       │   ├── theme/
│           │       │   │   ├── Theme.kt
│           │       │   │   ├── Color.kt
│           │       │   │   └── Type.kt
│           │       │   └── viewmodel/
│           │       │       └── RootMACViewModel.kt
│           └── res/
│               └── values/
├── build.gradle.kts              # Project build configuration
├── settings.gradle.kts           # Project settings
├── gradle.properties             # Gradle properties
└── README.md                     # This file
```

## 🏗️ Technology Stack

- **Language**: Kotlin 1.9.22
- **UI Framework**: Jetpack Compose 1.6.4
- **Database**: Room 2.6.1
- **Background Tasks**: WorkManager 2.9.0
- **Root Access**: libsu 6.2.1
- **Logging**: Timber 5.0.1
- **Build System**: Gradle 8.2.0
- **Target SDK**: 34 (Android 14)
- **Min SDK**: 28 (Android 9)

## 🧪 Testing

Run unit tests:
```bash
./gradlew test
```

Run instrumented tests:
```bash
./gradlew connectedAndroidTest
```

## 🤝 Contributing

Contributions are welcome! Please follow these steps:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

Please ensure your code follows the existing style and includes appropriate tests.

## ⚠️ Important Disclaimers

- **Root Requirement**: This application requires a rooted device and root access. Improper use may damage your device.
- **Network Policies**: Changing MAC addresses may violate network terms of service or local regulations.
- **Security**: Modifying network configuration carries inherent risks. Use responsibly and at your own risk.
- **Compatibility**: Not all devices support MAC address modification due to kernel or driver restrictions.

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🆘 Support

For support, bug reports, or feature requests, please open an issue on the GitHub repository.

## 🙏 Acknowledgments

- Thanks to the Magisk and SuperSU teams for root solutions
- Special thanks to the Android developer community for resources and documentation
- Inspired by the need for privacy-focused networking tools on Android

---

<p align="center">
  <em>Made with ❤️ for the Android community</em>
</p>