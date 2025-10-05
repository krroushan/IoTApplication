# IoT Motor Control App - Logging Guide

## Overview
This application now includes comprehensive logging to help monitor and debug issues. All logs are visible in Android Logcat.

## How to View Logs

### Android Studio Logcat
1. Open Android Studio
2. Run the app on device/emulator
3. Open **Logcat** tab (usually at bottom)
4. Filter by tags to see specific logs:

### Log Tags to Filter By:
- **`IoTMotorControl`** - General app logs
- **`NETWORK`** - API calls and responses
- **`DATABASE`** - Database operations
- **`SMS`** - SMS sending/receiving
- **`MOTOR`** - Motor commands and status
- **`UI`** - User interface events
- **`WORKER`** - Background tasks
- **`REPOSITORY`** - Data repository operations

### Example Logcat Filters:
```
tag:MOTOR           # Show only motor-related logs
tag:SMS             # Show only SMS logs
tag:NETWORK         # Show only network logs
tag:IoTMotorControl # Show all app logs
```

## What Gets Logged

### 🔧 Motor Commands
- ✅ Motor ON/OFF command initiation
- ✅ Command success/failure status
- ✅ Error details with stack traces

### 📱 SMS Operations
- ✅ SMS received from target number
- ✅ SMS parsing results
- ✅ SMS sending attempts
- ✅ SMS errors and failures

### 🌐 Network Operations
- ✅ API request URLs and methods
- ✅ Response codes and data
- ✅ Network errors and timeouts

### 💾 Database Operations
- ✅ Data insertion/updates
- ✅ Query executions
- ✅ Database errors

### 🎨 UI Events
- ✅ App startup and screen transitions
- ✅ User interactions
- ✅ UI errors and crashes

### ⚙️ Background Workers
- ✅ Sync operations
- ✅ Worker start/completion
- ✅ Background task errors

## Log Levels

### 🟢 Debug (D)
- Detailed flow information
- Function entry/exit
- State changes

### 🔵 Info (I)
- Important events
- Successful operations
- Status updates

### 🟡 Warning (W)
- Non-critical issues
- Retry attempts
- Deprecated usage

### 🔴 Error (E)
- Exceptions and errors
- Failed operations
- Critical issues

## Sample Log Output

```
D/MOTOR: 🔧 Motor Command: MOTOR_ON -> Status: INITIATED
D/REPOSITORY: Sending Motor ON command via SMS
D/SMS: 📱 SMS Received from +915754027372041: MOTOR_STATUS:ON,TIMESTAMP:2024-01-15T10:30:00
I/MOTOR: 🔧 Motor Command: MOTOR_ON -> Status: SUCCESS
D/DATABASE: 💾 Database: INSERT on logs Motor status updated
```

## Troubleshooting Common Issues

### Motor Commands Not Working
1. Check logs with filter: `tag:MOTOR`
2. Look for SMS sending errors: `tag:SMS`
3. Verify network connectivity: `tag:NETWORK`

### SMS Not Received
1. Filter by: `tag:SMS`
2. Check if SMS receiver is triggered
3. Verify target phone number matches

### App Crashes
1. Look for error logs: `level:error`
2. Check stack traces in logs
3. Filter by specific component tags

### Network Issues
1. Filter by: `tag:NETWORK`
2. Check API response codes
3. Look for timeout errors

## Debug Mode
- Logging is automatically enabled in DEBUG builds
- Production builds have minimal logging for performance
- All logs include timestamps and thread information

## Log File Location
Logs are displayed in Android Logcat and can be exported:
- **Android Studio**: Logcat → Right-click → Save
- **Command Line**: `adb logcat > app_logs.txt`

---

**Note**: Always check logs when troubleshooting issues. The comprehensive logging will help identify exactly where problems occur in the application flow.
