# IoT Motor Control App - Backend API Documentation

## Overview
This document outlines the required API endpoints for syncing data from the IoT Motor Control Android application to a backend database. The app manages motor control operations via SMS and stores logs locally, requiring periodic synchronization with a backend server.

## Base URL
```
https://your-backend-server.com/api/
```

## Authentication
API endpoints may require authentication depending on your backend implementation. If authentication is required, include the following header:
```
Authorization: Bearer <access_token>
```

## Data Models

### 1. Motor Log Entity
```json
{
  "id": "long (auto-generated)",
  "timestamp": "long (Unix timestamp)",
  "motorStatus": "string (ON|OFF|STATUS)",
  "voltage": "float (nullable)",
  "current": "float (nullable)", 
  "waterLevel": "float (nullable)",
  "mode": "string (nullable)",
  "clock": "string (nullable)",
  "command": "string (MOTORON|MOTOROFF|STATUS)",
  "phoneNumber": "string",
  "isSynced": "boolean",
  "createdAt": "datetime",
  "updatedAt": "datetime"
}
```

### 2. API Request/Response Models

#### LogRequest (for sync)
```json
{
  "timestamp": "long",
  "motorStatus": "string",
  "voltage": "float (nullable)",
  "current": "float (nullable)",
  "waterLevel": "float (nullable)",
  "mode": "string (nullable)",
  "clock": "string (nullable)",
  "command": "string",
  "phoneNumber": "string"
}
```

#### LogResponse
```json
{
  "id": "long",
  "success": "boolean",
  "message": "string",
  "syncedAt": "datetime"
}
```

#### LogsResponse
```json
{
  "logs": "array of LogEntity",
  "totalCount": "long",
  "page": "int",
  "size": "int",
  "hasNext": "boolean"
}
```

#### ReportResponse
```json
{
  "period": "string",
  "startDate": "datetime",
  "endDate": "datetime",
  "totalOperations": "int",
  "motorOnCount": "int",
  "motorOffCount": "int",
  "statusRequests": "int",
  "averageVoltage": "float",
  "averageCurrent": "float",
  "averageWaterLevel": "float",
  "uptime": "string",
  "downtime": "string"
}
```

## API Endpoints

### 1. Sync Single Log
**POST** `/logs`

Sync a single motor log entry to the backend.

**Request Body:**
```json
{
  "timestamp": 1704067200000,
  "motorStatus": "ON",
  "voltage": 220.5,
  "current": 2.3,
  "waterLevel": 85.0,
  "mode": "AUTO",
  "clock": "2024-01-01 10:30:00",
  "command": "MOTORON",
  "phoneNumber": "+919876543210"
}
```

**Response:**
```json
{
  "id": 12345,
  "success": true,
  "message": "Log synced successfully",
  "syncedAt": "2024-01-01T10:30:00Z"
}
```

**Status Codes:**
- `200` - Success
- `400` - Bad Request (validation error)
- `401` - Unauthorized
- `500` - Internal Server Error

### 2. Batch Sync Logs
**POST** `/logs/batch`

Sync multiple motor log entries in a single request (recommended for efficiency).

**Request Body:**
```json
[
  {
    "timestamp": 1704067200000,
    "motorStatus": "ON",
    "voltage": 220.5,
    "current": 2.3,
    "waterLevel": 85.0,
    "mode": "AUTO",
    "clock": "2024-01-01 10:30:00",
    "command": "MOTORON",
    "phoneNumber": "+919876543210"
  },
  {
    "timestamp": 1704067260000,
    "motorStatus": "OFF",
    "voltage": 0.0,
    "current": 0.0,
    "waterLevel": 85.0,
    "mode": "AUTO",
    "clock": "2024-01-01 10:31:00",
    "command": "MOTOROFF",
    "phoneNumber": "+919876543210"
  }
]
```

**Response:**
```json
[
  {
    "id": 12345,
    "success": true,
    "message": "Log synced successfully",
    "syncedAt": "2024-01-01T10:30:00Z"
  },
  {
    "id": 12346,
    "success": true,
    "message": "Log synced successfully",
    "syncedAt": "2024-01-01T10:31:00Z"
  }
]
```

**Status Codes:**
- `200` - Success (all logs synced)
- `207` - Multi-Status (some logs failed)
- `400` - Bad Request
- `401` - Unauthorized
- `500` - Internal Server Error

### 3. Get Logs
**GET** `/logs`

Retrieve motor logs with optional filtering and pagination.

**Query Parameters:**
- `startDate` (optional): Unix timestamp for start date filter
- `endDate` (optional): Unix timestamp for end date filter
- `page` (optional): Page number (default: 0)
- `size` (optional): Page size (default: 100, max: 1000)

**Example:**
```
GET /logs?startDate=1704067200000&endDate=1704153600000&page=0&size=50
```

**Response:**
```json
{
  "logs": [
    {
      "id": 12345,
      "timestamp": 1704067200000,
      "motorStatus": "ON",
      "voltage": 220.5,
      "current": 2.3,
      "waterLevel": 85.0,
      "mode": "AUTO",
      "clock": "2024-01-01 10:30:00",
      "command": "MOTORON",
      "phoneNumber": "+919876543210",
      "isSynced": true,
      "createdAt": "2024-01-01T10:30:00Z",
      "updatedAt": "2024-01-01T10:30:00Z"
    }
  ],
  "totalCount": 150,
  "page": 0,
  "size": 50,
  "hasNext": true
}
```

### 4. Daily Report
**GET** `/reports/daily`

Get daily motor operation report.

**Query Parameters:**
- `date` (required): Unix timestamp for the date

**Example:**
```
GET /reports/daily?date=1704067200000
```

**Response:**
```json
{
  "period": "daily",
  "startDate": "2024-01-01T00:00:00Z",
  "endDate": "2024-01-01T23:59:59Z",
  "totalOperations": 25,
  "motorOnCount": 12,
  "motorOffCount": 12,
  "statusRequests": 1,
  "averageVoltage": 218.5,
  "averageCurrent": 2.1,
  "averageWaterLevel": 82.3,
  "uptime": "8h 30m",
  "downtime": "15h 30m"
}
```

### 5. Weekly Report
**GET** `/reports/weekly`

Get weekly motor operation report.

**Query Parameters:**
- `weekStart` (required): Unix timestamp for the start of the week

**Example:**
```
GET /reports/weekly?weekStart=1704067200000
```

### 6. Monthly Report
**GET** `/reports/monthly`

Get monthly motor operation report.

**Query Parameters:**
- `monthStart` (required): Unix timestamp for the start of the month

**Example:**
```
GET /reports/monthly?monthStart=1704067200000
```

## Webhook Integration

### 7. Motor Command Webhook
The app sends real-time motor command events to a webhook for monitoring.

**Webhook URL:** `https://webhook.site/d130e307-28fb-4c90-9c3c-ac65d37a5b69`

**Payload:**
```json
{
  "event": "motor_command",
  "command": "MOTOR_ON",
  "status": "SMS_SENT",
  "phone_number": "+919876543210",
  "timestamp": 1704067200000,
  "app": "IoT_Motor_Control",
  "version": "1.0.0"
}
```

### 8. SMS Status Webhook
The app sends SMS status updates to the webhook.

**Payload:**
```json
{
  "event": "sms_status",
  "sms_type": "MOTOR_ON",
  "phone_number": "+919876543210",
  "message": "MOTORON",
  "success": true,
  "timestamp": 1704067200000,
  "app": "IoT_Motor_Control"
}
```

## Error Handling

### Standard Error Response
```json
{
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "Invalid request data",
    "details": [
      {
        "field": "phoneNumber",
        "message": "Phone number is required"
      }
    ],
    "timestamp": "2024-01-01T10:30:00Z"
  }
}
```

### Common Error Codes
- `VALIDATION_ERROR` - Request validation failed
- `AUTHENTICATION_ERROR` - Invalid or missing authentication
- `AUTHORIZATION_ERROR` - Insufficient permissions
- `NOT_FOUND` - Resource not found
- `DUPLICATE_ENTRY` - Resource already exists
- `RATE_LIMIT_EXCEEDED` - Too many requests
- `SERVER_ERROR` - Internal server error

## Rate Limiting
- **Sync endpoints**: 100 requests per minute
- **Report endpoints**: 20 requests per minute

## Data Retention
- **Motor logs**: Retained for 2 years
- **Reports**: Generated on-demand, not stored

## Security Considerations
1. **HTTPS**: All endpoints must use HTTPS
2. **Input Validation**: Validate all input data
3. **SQL Injection**: Use parameterized queries
4. **Rate Limiting**: Implement rate limiting
5. **Logging**: Log all API requests and responses
6. **Data Encryption**: Encrypt sensitive data at rest

## Database Schema

### Motor Logs Table
```sql
CREATE TABLE motor_logs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    timestamp BIGINT NOT NULL,
    motor_status VARCHAR(10) NOT NULL,
    voltage FLOAT NULL,
    current FLOAT NULL,
    water_level FLOAT NULL,
    mode VARCHAR(20) NULL,
    clock VARCHAR(50) NULL,
    command VARCHAR(20) NOT NULL,
    phone_number VARCHAR(20) NOT NULL,
    is_synced BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_timestamp (timestamp),
    INDEX idx_phone_number (phone_number),
    INDEX idx_is_synced (is_synced)
);
```


## Implementation Notes

### Sync Strategy
1. **Immediate Sync**: For real-time motor commands (no delay)
2. **Periodic Sync**: Every 15 minutes for batch operations
3. **Retry Logic**: Exponential backoff for failed syncs
4. **Conflict Resolution**: Last-write-wins strategy

### Data Flow
1. App receives SMS with motor data
2. Data is stored locally in Room database
3. Background worker syncs unsynced data to backend
4. Backend processes and stores data
5. App marks data as synced locally

### Monitoring
- Webhook integration for real-time monitoring
- Comprehensive logging for debugging
- Health check endpoints for system monitoring
- Performance metrics and analytics

## Testing

### Test Data
```json
{
  "timestamp": 1704067200000,
  "motorStatus": "ON",
  "voltage": 220.5,
  "current": 2.3,
  "waterLevel": 85.0,
  "mode": "AUTO",
  "clock": "2024-01-01 10:30:00",
  "command": "MOTORON",
  "phoneNumber": "+919876543210"
}
```

### Test Scenarios
1. Single log sync
2. Batch log sync
3. Network failure handling
4. Authentication failure
5. Rate limiting
6. Data validation
7. Large dataset handling

---

**Note**: This API documentation should be implemented on the backend server to support the IoT Motor Control Android application's data synchronization requirements.
