# FCM Token Update Troubleshooting Guide

## ❌ Problem: FCM Token Not Updating to Server

Based on the logs, the FCM token is **not updating to the server** due to authentication issues.

---

## 🔍 Root Causes

### 1. **Wrong Auth Token (Primary Issue)**
The app is using an **old admin token** instead of a **customer token**:
```
Authorization: Bearer 1|tt4QRw8uqUUThozaqzlehJTTUtYoUx9sIkLfW32B731dc5bb
```

This token is:
- ❌ An **admin** role token
- ❌ Trying to access **customer** endpoints
- ❌ Getting redirected to login page (HTML response)

**Expected Token**: Customer token from `/api/customer/login` endpoint

### 2. **Firebase Configuration** (Already Fixed ✅)
Previously had placeholder values in `google-services.json`, now fixed with real Firebase project values.

---

## ✅ Solution Steps

### Step 1: Clear Old Session Data

**Option A: Use Logout Button (Recommended)**
1. Open the app
2. Go to **Profile** screen
3. Scroll down to **About** section
4. Click **Logout** button
5. This will clear:
   - Session token
   - Devices
   - FCM token
   - All cached data

**Option B: Clear App Data (Alternative)**
1. Go to Android **Settings** → **Apps**
2. Find **IoT Motor Control**
3. Tap **Storage**
4. Tap **Clear Data** or **Clear Storage**
5. Confirm

### Step 2: Login with Customer Credentials

Use the **correct customer credentials**:
```
Phone Number: +1234567890
Password: password
```

These are from the test customer user you created:
```sql
email: customer@test.com
phone_number: +1234567890  
role: customer
```

### Step 3: Verify FCM Token Update

After login, check the logs for:

```
=== FCM Token Initialization Started ===
✅ FCM token obtained: xxx...
Auth token available: 4|LWrkR0aoRGJJhjt...
Attempting to update FCM token on server
✅ FCM token updated on server successfully
```

### Step 4: Check Profile Screen Status

Go to **Profile** → **My Devices** section:
- Should show: **"Notifications Active"** (green checkmark)
- If shows: **"Notifications Pending"** → Check logs for errors

---

## 📱 How FCM Token Update Works

### Flow Diagram
```
App Launch (MainActivity.onCreate)
    ↓
[Line 57] fcmTokenManager.initializeFcmToken()
    ↓
Check notification permission
    ↓
Check if user logged in (sessionManager.getAccessToken())
    ↓
┌──────────────┬──────────────┐
│ NOT LOGGED IN│  LOGGED IN   │
├──────────────┼──────────────┤
│ ⚠️ Warning   │ ✅ Continue  │
│ Skip update  │ Get FCM token│
└──────────────┴──────────────┘
                    ↓
    Get FCM token from Firebase
                    ↓
    Compare with saved token
                    ↓
           ┌────────┴────────┐
           │ Token changed?  │
           ├─────────────────┤
           │ YES     │  NO   │
           ↓         ↓       
  Update server  Skip update
           ↓
POST /api/customer/fcm-token
{
  "fcm_token": "xxx..."
}
Headers:
  Authorization: Bearer {customer_token}
```

### Key Files

1. **FcmTokenManager.kt** (Lines 32-77)
   - `initializeFcmToken()` - Called on app start
   - `updateFcmTokenOnServer()` - Sends token to API
   - Enhanced logging added for debugging

2. **MainActivity.kt** (Line 57)
   - Initializes FCM token on every app launch
   - Before login screen shows

3. **ProfileScreen.kt** (Lines 443-447)
   - Logout button now clears FCM token
   - Forces fresh token on next login

4. **CustomerRepository.kt** (Lines 282-307)
   - API call to `/api/customer/fcm-token`
   - Requires Bearer token (customer role)

---

## 🔧 Debugging Commands

### Check Current Auth Token (from Android logs)
```
grep "Auth token available" logcat.txt
```

### Test FCM Token API Manually
```bash
# Get customer login token first
curl -X POST https://laravel1.wizzyweb.com/api/customer/login \
  -H "Content-Type: application/json" \
  -H "Accept: application/json" \
  -d '{
    "phone_number": "+1234567890",
    "password": "password"
  }' | jq .

# Use the token to update FCM token
curl -X POST https://laravel1.wizzyweb.com/api/customer/fcm-token \
  -H "Authorization: Bearer YOUR_CUSTOMER_TOKEN" \
  -H "Content-Type: application/json" \
  -H "Accept: application/json" \
  -d '{
    "fcm_token": "test-fcm-token-12345"
  }' | jq .
```

### Expected Success Response
```json
{
  "success": true,
  "message": "FCM token updated successfully",
  "data": {
    "user_id": 6,
    "fcm_token": "test-fcm-token-12345"
  }
}
```

---

## 📋 Checklist

Before reporting FCM token not updating, verify:

- [ ] Logged out completely (cleared old session)
- [ ] Logged in with customer credentials (+1234567890 / password)
- [ ] Notification permission granted (Android 13+)
- [ ] Firebase initialized (no "IllegalArgumentException: Please set your Application ID")
- [ ] Auth token is customer token (not admin token)
- [ ] Backend API returns JSON (not HTML login page)
- [ ] Checked FCM_MANAGER logs in Logcat
- [ ] Profile screen shows "Notifications Active" status

---

## 🎯 Expected Log Sequence (Success)

```
FCM_MANAGER: === FCM Token Initialization Started ===
FCM_MANAGER: Notification permission granted
FCM_MANAGER: Auth token available: 4|LWrkR0aoRGJJhjt...
FCM_MANAGER: Requesting FCM token from Firebase...
FCM_MANAGER: ✅ FCM token obtained: fX9kL2_abc123xyz...
FCM_MANAGER: Saved token: null...
FCM_MANAGER: FCM token changed, updating server
FCM_MANAGER: Attempting to update FCM token on server
FCM_MANAGER: Auth token available: 4|LWrkR0aoRGJJhjt...
FCM_MANAGER: FCM token: fX9kL2_abc123xyz...
CUSTOMER_API: Updating FCM token
CUSTOMER_API: FCM token updated successfully
FCM_MANAGER: ✅ FCM token updated on server successfully
FCM_MANAGER: Server response: FCM token updated successfully
```

---

## ❓ Common Issues

### Issue: "No auth token available"
**Cause**: User not logged in yet  
**Solution**: Login with customer credentials first

### Issue: "API endpoint returning HTML"
**Cause**: Wrong token (admin trying to access customer endpoint)  
**Solution**: Logout and login with customer credentials

### Issue: "Notification permission not granted"
**Cause**: Android 13+ requires POST_NOTIFICATIONS permission  
**Solution**: Grant notification permission when prompted

### Issue: "FCM token unchanged, skipping update"
**Cause**: Token already sent to server  
**Solution**: Normal behavior - no action needed

---

## 📞 Need Help?

If FCM token still not updating after following this guide:

1. Clear app data completely
2. Uninstall and reinstall app
3. Login with customer credentials
4. Capture full Logcat output with filters:
   - `FCM_MANAGER`
   - `CUSTOMER_API`
   - `AUTH`
5. Check Profile screen → My Devices section for status

---

**Last Updated**: 2025-10-12  
**App Version**: 1.0  
**Backend API**: https://laravel1.wizzyweb.com/api/

