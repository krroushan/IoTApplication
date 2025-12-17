# Quick Fix Checklist for Google Play Rejection

## ✅ Completed (In-App Changes)

- [x] Updated in-app Delete Account screen to reference "Ampush Motor Controller" and "AMPUSHWORKS ENTERPRISES PRIVATE LIMITED"

## 🔧 Action Required (Web & Play Console)

### 1. Fix Account Deletion Web Page (URGENT)

**URL**: `https://laravel1.wizzyweb.com/customer/delete-account`

**Required Changes**:
- [ ] Add page title: "Delete Account - Ampush Motor Controller"
- [ ] Add header: "Ampush Motor Controller - Account Deletion"
- [ ] Add company name: "By AMPUSHWORKS ENTERPRISES PRIVATE LIMITED"
- [ ] Update body text to mention app name and company
- [ ] Add footer with company contact information
- [ ] Test the page is accessible and functional

**Example text to add**:
```
<h1>Ampush Motor Controller - Account Deletion</h1>
<p>By AMPUSHWORKS ENTERPRISES PRIVATE LIMITED</p>
<p>This will permanently delete your Ampush Motor Controller account and all associated data with AMPUSHWORKS ENTERPRISES PRIVATE LIMITED.</p>
```

### 2. Update Data Safety Form in Play Console (URGENT)

**Location**: Play Console → Your App → App Content → Data Safety

**📖 Detailed Guide**: See `DATA_SAFETY_CONTACTS_FIX.md` for complete step-by-step instructions

**Quick Steps**:
1. [ ] Go to "Data collection and security" → Select "Yes"
2. [ ] Find "Contacts" data type → **CHECK THE BOX**
3. [ ] Configure Contacts:
   - [ ] Does your app collect this data? → **Yes**
   - [ ] Is this data shared? → **Yes** (with backend servers)
   - [ ] Is this data collected for app functionality? → **Yes**
   - [ ] Is this data collected for security? → **Yes**
   - [ ] How is this data collected? → **From user** + **Automatically**
   - [ ] Is this data required? → **Yes**
   - [ ] Can users delete this data? → **Yes**
   - [ ] Is this data encrypted in transit? → **Yes**
   - [ ] Who is this data shared with? → **Your app's backend servers**
   - [ ] Why is this data shared? → **For app functionality** + **For security**

4. [ ] Review all other data types (Phone Number, Device IDs, etc.)
5. [ ] Save and submit form changes

### 3. Resubmit to Play Console

**Steps**:
1. [ ] Fix web deletion page (Step 1)
2. [ ] Update Data Safety form (Step 2)
3. [ ] Go to Publishing overview
4. [ ] Add notes to reviewers:
   ```
   Fixed account deletion page to include app and company name references.
   Updated Data Safety form to declare Contacts data collection and sharing.
   ```
5. [ ] Submit for review

---

## Why These Fixes Are Needed

### Issue 1: Account Deletion Link
Google requires the deletion page to clearly show it's related to your app. The page must reference:
- App name: "Ampush Motor Controller"
- Company: "AMPUSHWORKS ENTERPRISES PRIVATE LIMITED"

### Issue 2: Contacts Data
Your app sends phone numbers to backend servers. Google classifies phone numbers as "Contacts" data. You must declare this in the Data Safety form.

**Where phone numbers are sent**:
- User profile updates (`phone_number` field)
- Motor log synchronization (`phoneNumber` field)
- Device identification (SIM phone numbers)

---

## Timeline

**Recommended**: Complete both fixes within 24-48 hours to avoid delays in app publication.

---

## Need Help?

See detailed guide: `GOOGLE_PLAY_REJECTION_FIX.md`

