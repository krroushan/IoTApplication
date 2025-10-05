# IoT Motor Control Android Application

A comprehensive Android application for controlling and monitoring IoT motors via SMS commands with local storage and backend synchronization.

## Features

### Core Functionality
- **SMS Command Control**: Send MOTORON, MOTOROFF, and STATUS commands via SMS
- **SMS Response Parsing**: Automatically parse motor status responses (voltage, current, water level, mode, clock)
- **Local Database Storage**: Store all motor logs locally using Room database
- **Background Sync**: Automatically sync data to backend server using WorkManager
- **Real-time Notifications**: Show notifications for motor status changes and SMS updates

### Architecture
- **MVVM + Clean Architecture**: Separation of concerns with Repository pattern
- **Jetpack Compose**: Modern UI with Material Design 3
- **Room Database**: Local data persistence with automatic sync tracking
- **Retrofit**: REST API integration for backend synchronization
- **WorkManager**: Reliable background task execution
- **BroadcastReceiver**: Background SMS listening

## Technical Stack

### Dependencies
```kotlin
// Room Database
implementation "androidx.room:room-runtime:2.8.0"
implementation "androidx.room:room-ktx:2.8.0"
kapt "androidx.room:room-compiler:2.8.0"

// Kotlin Coroutines
implementation "org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2"

// Lifecycle ViewModel
implementation "androidx.lifecycle:lifecycle-viewmodel-ktx:2.10"
implementation "androidx.lifecycle:lifecycle-livedata-ktx:2.10"

// Retrofit for API
implementation "com.squareup.retrofit2:retrofit:3.0.0"
implementation "com.squareup.retrofit2:converter-gson:3.0.0"
implementation "com.squareup.okhttp3:logging-interceptor:5.1.0"

// WorkManager for background tasks
implementation "androidx.work:work-runtime-ktx:2.10.0"

// Navigation
implementation "androidx.navigation:navigation-compose:2.8.6"

// Permissions
implementation "com.google.accompanist:accompanist-permissions:0.35.1-alpha"
```

### Permissions
```xml
<uses-permission android:name="android.permission.SEND_SMS"/>
<uses-permission android:name="android.permission.RECEIVE_SMS"/>
<uses-permission android:name="android.permission.READ_SMS"/>
<uses-permission android:name="android.permission.INTERNET"/>
<uses-permission android:name="android.permission.FOREGROUND_SERVICE"/>
<uses-permission android:name="android.permission.WAKE_LOCK"/>
```

## Project Structure

```
app/src/main/java/com/ampush/iotapplication/
├── data/
│   ├── db/
│   │   ├── entities/LogEntity.kt
│   │   ├── dao/LogDao.kt
│   │   ├── AppDatabase.kt
│   │   └── DateConverter.kt
│   ├── model/MotorData.kt
│   ├── parser/SmsParser.kt
│   ├── repository/MotorRepository.kt
│   └── sms/SmsSender.kt
├── network/
│   ├── ApiService.kt
│   ├── RetrofitClient.kt
│   └── models/
│       ├── LogRequest.kt
│       └── LogResponse.kt
├── notifications/NotificationHelper.kt
├── receiver/SmsReceiver.kt
├── ui/
│   ├── components/
│   │   ├── Dashboard.kt
│   │   └── HistoryScreen.kt
│   ├── theme/
│   └── viewmodel/MotorViewModel.kt
├── worker/SyncLogsWorker.kt
├── MainActivity.kt
└── MotorControlApplication.kt
```

## Key Components

### 1. SMS Handling
- **SmsSender**: Sends MOTORON, MOTOROFF, STATUS commands to `+915754027372041`
- **SmsReceiver**: BroadcastReceiver that captures SMS responses
- **SmsParser**: Parses SMS content using regex patterns to extract motor data

### 2. Database Layer
- **LogEntity**: Room entity for storing motor logs
- **LogDao**: Data access object with queries for logs and sync status
- **AppDatabase**: Room database configuration with date converters

### 3. Repository Pattern
- **MotorRepository**: Central data access layer that coordinates between SMS, database, and API
- Handles automatic background sync scheduling
- Manages offline data storage and retry logic

### 4. ViewModel Layer
- **MotorViewModel**: Exposes LiveData/StateFlow for UI consumption
- Handles SMS command execution and error management
- Provides reactive data streams for motor status and history

### 5. UI Components
- **Dashboard**: Main control interface with motor buttons and status display
- **HistoryScreen**: Shows historical motor data with statistics
- **Material Design 3**: Modern UI with proper navigation and theming

### 6. Background Services
- **SyncLogsWorker**: WorkManager task for syncing unsynced logs to backend
- **NotificationHelper**: Manages local notifications for motor events
- **Automatic Sync**: Periodic background sync every 15 minutes

## Usage

### SMS Commands
The app sends SMS commands to `+915754027372041`:
- `MOTORON` - Turn motor on
- `MOTOROFF` - Turn motor off  
- `STATUS` - Request current motor status

### Expected SMS Response Format
The app expects SMS responses in this format:
```
Motor: ON
Voltage: 220.5
Current: 15.2
Water Level: 75.0
Mode: AUTO
Clock: 14:30:25
```

### Backend API Endpoints
The app syncs data to these endpoints:
- `POST /logs` - Sync individual log
- `POST /logs/batch` - Sync multiple logs
- `GET /logs` - Fetch historical logs
- `GET /reports/daily` - Daily usage reports
- `GET /reports/weekly` - Weekly usage reports
- `GET /reports/monthly` - Monthly usage reports

## Setup Instructions

1. **Clone the repository**
2. **Update backend URL** in `RetrofitClient.kt`
3. **Build and run** the application
4. **Grant SMS permissions** when prompted
5. **Start using** the motor control interface

## Architecture Benefits

- **Offline First**: Works without internet, syncs when available
- **Reliable**: WorkManager ensures background tasks complete
- **Scalable**: Clean architecture allows easy feature additions
- **Maintainable**: Separation of concerns and dependency injection
- **User-Friendly**: Modern UI with proper error handling and notifications

## Future Enhancements

- Real-time charts and graphs for motor data
- Advanced reporting with export functionality
- Multiple motor support
- Scheduled motor operations
- Integration with IoT platforms (MQTT, etc.)
- Advanced notification settings and customization
