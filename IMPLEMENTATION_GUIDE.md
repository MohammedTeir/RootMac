# RootMAC Implementation Guide

This guide provides detailed information about the RootMAC architecture and how to extend it.

## Architecture Overview

RootMAC follows the **MVVM (Model-View-ViewModel)** pattern with clean architecture principles:

```
UI Layer (Jetpack Compose)
    ↓
ViewModel (State Management)
    ↓
Repository (Data Access)
    ↓
Data Layer (Room DB, SharedPreferences)
Core Layer (Root Access, MAC Operations)
```

## Core Components

### 1. RootAccess.kt
Handles all root-level operations using libsu library.

**Key Functions:**
- `isRooted()`: Check if device has root access
- `executeCommand()`: Execute shell commands with root privileges
- `hasBusyBox()`: Check for BusyBox availability
- `hasIproute2()`: Check for iproute2 availability
- `getSELinuxMode()`: Get current SELinux mode

**Usage:**
```kotlin
val isRooted = RootAccess.isRooted()
val result = RootAccess.executeCommand("id")
```

### 2. MacAddressManager.kt
Manages all MAC address operations.

**Key Functions:**
- `getNetworkInterfaces()`: Get list of all network interfaces
- `getCurrentMac()`: Get current MAC for an interface
- `changeMacMethodA/B/C()`: Change MAC using different methods
- `generateRandomMac()`: Generate valid random MAC
- `isValidMac()`: Validate MAC address format
- `normalizeMac()`: Normalize MAC format

**Usage:**
```kotlin
val interfaces = MacAddressManager.getNetworkInterfaces()
val result = MacAddressManager.changeMacMethodA("wlan0", "AA:BB:CC:DD:EE:FF")
```

### 3. Database Layer

#### Entities (Entities.kt)
- `MacProfileEntity`: Stores MAC profiles
- `MacLogEntity`: Stores operation logs
- `OriginalMacEntity`: Stores original MAC addresses
- `AppSettingEntity`: Stores app settings

#### DAOs (Daos.kt)
- `MacProfileDao`: Profile CRUD operations
- `MacLogDao`: Log operations
- `OriginalMacDao`: Original MAC operations
- `AppSettingDao`: Settings operations

#### Database (AppDatabase.kt)
Singleton Room database instance.

### 4. Repository Pattern (Repository.kt)
Abstracts data access and provides a clean API for ViewModels.

**Key Functions:**
- Profile management: `getAllProfiles()`, `insertProfile()`, `updateProfile()`, `deleteProfile()`
- Log management: `getRecentLogs()`, `insertLog()`, `clearAllLogs()`
- MAC management: `getOriginalMac()`, `saveOriginalMac()`
- Settings: `getSetting()`, `setSetting()`

### 5. ViewModel (RootMACViewModel.kt)
Manages UI state and handles business logic.

**State Properties:**
```kotlin
data class RootMACState(
    val isRooted: Boolean = false,
    val hasBusyBox: Boolean = false,
    val hasIproute2: Boolean = false,
    val seLinuxMode: String = "Unknown",
    val networkInterfaces: List<NetworkInterface> = emptyList(),
    val profiles: List<MacProfileEntity> = emptyList(),
    val recentLogs: List<MacLogEntity> = emptyList(),
    val selectedInterface: String? = null,
    val currentMac: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
```

**Key Functions:**
- `checkRootAccess()`: Validate root and system capabilities
- `refreshNetworkInterfaces()`: Load network interfaces
- `selectInterface()`: Select active interface
- `changeMAC()`: Execute MAC change with logging
- `restoreOriginalMAC()`: Restore original MAC
- `generateRandomMAC()`: Generate random MAC

### 6. UI Layer (MainScreen.kt)
Jetpack Compose UI with reactive state updates.

**Sections:**
- System Status Card: Shows root, BusyBox, iproute2, SELinux status
- Error Messages: Displays any errors
- Interface Selection: List of network interfaces
- Current MAC Display: Shows selected interface and current MAC
- MAC Change Controls: Input, method selection, action buttons
- Recent Logs: Shows last 5 operations

## Extending RootMAC

### Adding a New Profile Screen

1. Create a new Composable in `ui/screens/`:
```kotlin
@Composable
fun ProfilesScreen(viewModel: RootMACViewModel) {
    // UI implementation
}
```

2. Add navigation in MainScreen or create a navigation structure

### Adding Automation Features

1. Create a new Worker in `worker/`:
```kotlin
class ScheduledRotationWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        // Implementation
        return Result.success()
    }
}
```

2. Schedule with WorkManager:
```kotlin
val workRequest = PeriodicWorkRequestBuilder<ScheduledRotationWorker>(
    15, TimeUnit.MINUTES
).build()
WorkManager.getInstance(context).enqueueUniquePeriodicWork(
    "rotation_work",
    ExistingPeriodicWorkPolicy.KEEP,
    workRequest
)
```

### Adding New Receiver

1. Create a new BroadcastReceiver in `receiver/`:
```kotlin
class AirplaneModeReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        // Handle airplane mode change
    }
}
```

2. Register in AndroidManifest.xml:
```xml
<receiver android:name=".receiver.AirplaneModeReceiver"
    android:exported="true">
    <intent-filter>
        <action android:name="android.intent.action.AIRPLANE_MODE" />
    </intent-filter>
</receiver>
```

### Adding Settings

1. Add to database:
```kotlin
repository.setSetting("auto_restore_on_boot", "true")
```

2. Retrieve in ViewModel:
```kotlin
val autoRestore = repository.getSetting("auto_restore_on_boot")
```

## Testing

### Unit Tests
Create tests in `app/src/test/`:

```kotlin
class MacAddressManagerTest {
    @Test
    fun testValidMacGeneration() {
        val mac = MacAddressManager.generateRandomMac()
        assertTrue(MacAddressManager.isValidMac(mac))
    }
    
    @Test
    fun testMacValidation() {
        assertTrue(MacAddressManager.isValidMac("AA:BB:CC:DD:EE:FF"))
        assertFalse(MacAddressManager.isValidMac("ZZ:ZZ:ZZ:ZZ:ZZ:ZZ"))
    }
}
```

### Integration Tests
Create tests in `app/src/androidTest/`:

```kotlin
class RootAccessTest {
    @Test
    fun testRootCheck() = runBlocking {
        val isRooted = RootAccess.isRooted()
        assertTrue(isRooted)
    }
}
```

## Build Variants

### Debug Build
```bash
./gradlew assembleDebug
```

### Release Build
```bash
./gradlew assembleRelease
```

### ProGuard/R8 Obfuscation
Configured in `app/proguard-rules.pro`

## Logging

RootMAC uses Timber for logging:

```kotlin
Timber.d("Debug message")
Timber.e(exception, "Error message")
Timber.w("Warning message")
```

View logs with:
```bash
adb logcat | grep RootMAC
```

## Performance Considerations

1. **Root Commands**: Execute asynchronously in coroutines
2. **Database**: Use Flow for reactive updates
3. **UI**: Use StateFlow for state management
4. **Background Tasks**: Use WorkManager for reliability

## Security Best Practices

1. **Root Access**: Only execute necessary commands
2. **Data Storage**: Use EncryptedSharedPreferences for sensitive data
3. **Input Validation**: Always validate MAC addresses
4. **Error Handling**: Never expose sensitive information in errors

## Common Issues and Solutions

### Issue: Root command times out
**Solution**: Increase timeout in RootAccess initialization
```kotlin
Shell.setDefaultBuilder(
    Shell.Builder.create()
        .setTimeout(20)  // Increase timeout
)
```

### Issue: MAC change fails silently
**Solution**: Check logs and try different methods
```kotlin
Timber.e("MAC change failed: ${result.stderr}")
```

### Issue: Database migration errors
**Solution**: Increment database version in AppDatabase.kt and create migration

### Issue: Permissions denied
**Solution**: Ensure all required permissions are in AndroidManifest.xml

## Future Enhancements

1. **Phase 2**: Profile system with SSID targeting
2. **Phase 3**: Advanced randomization with OUI support
3. **Phase 4**: Stealth mode and UI polish
4. **Advanced**: Kernel module integration for better compatibility

## Resources

- [libsu Documentation](https://topjohnwu.github.io/libsu/)
- [Jetpack Compose](https://developer.android.com/jetpack/compose)
- [Room Database](https://developer.android.com/training/data-storage/room)
- [WorkManager](https://developer.android.com/topic/libraries/architecture/workmanager)
- [Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-overview.html)

---

For more information, see the main README.md file.
