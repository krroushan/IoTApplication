package com.ampush.iotapplication.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun PrivacyPolicyScreen(navController: androidx.navigation.NavController) {
    val context = LocalContext.current
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
            // Header
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Privacy Policy",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Last Updated: January 2025",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                    }
                }
            }
            
            // Introduction
            item {
                PolicySection(
                    title = "1. Introduction",
                    content = """
                        Welcome to the IoT Motor Control Application ("we," "our," or "us"), developed and operated by AMPUSHWORKS ENTERPRISES PRIVATE LIMITED. We are committed to protecting your privacy and ensuring the security of your personal information. This Privacy Policy explains how we collect, use, disclose, and safeguard your information when you use our mobile application (the "App").
                        
                        By using our App, you agree to the collection and use of information in accordance with this policy. If you do not agree with our policies and practices, please do not use our App.
                    """.trimIndent()
                )
            }
            
            // Information We Collect
            item {
                PolicySection(
                    title = "2. Information We Collect",
                    content = """
                        We collect various types of information to provide and improve our services:
                        
                        • Personal Information: Name, email, phone number, address, profile photo
                        • Device and SIM Information: Phone number, carrier name, country code
                        • SMS Data: Motor control commands and status responses
                        • Motor Operation Data: Status, voltage, current, water level, mode
                        • Device Information: Device names, SMS numbers, activity timestamps
                        • Technical Information: Device model, OS version, FCM tokens, crash reports
                        • Analytics Data: App usage patterns and performance metrics
                        
                        Note: We only access SMS messages directly related to motor control operations. We do not read, store, or transmit any other SMS messages on your device.
                    """.trimIndent()
                )
            }
            
            // How We Use Information
            item {
                PolicySection(
                    title = "3. How We Use Your Information",
                    content = """
                        We use collected information for:
                        
                        • Service Provision: Provide and improve motor control services
                        • Account Management: Create and manage user accounts
                        • SMS Communication: Send commands and receive status updates
                        • Data Synchronization: Sync data between device and servers
                        • Notifications: Send push notifications about motor status
                        • Analytics: Analyze usage patterns and improve user experience
                        • Support: Provide customer support
                        • Security: Detect and prevent security threats
                        • Compliance: Comply with legal obligations
                    """.trimIndent()
                )
            }
            
            // Data Storage and Security
            item {
                PolicySection(
                    title = "4. Data Storage and Security",
                    content = """
                        Local Storage: Data stored on your device using Room database.
                        Cloud Storage: Motor operation data synchronized to secure backend servers.
                        
                        Security Measures:
                        • Encryption of data in transit using HTTPS/TLS
                        • Secure authentication using tokens
                        • Regular security audits and updates
                        • Access controls and authentication requirements
                        • Secure password storage practices
                        
                        While we implement industry-standard security measures, no method of transmission over the internet is 100% secure.
                    """.trimIndent()
                )
            }
            
            // Data Sharing
            item {
                PolicySection(
                    title = "5. Data Sharing and Disclosure",
                    content = """
                        Third-Party Services:
                        • Google Firebase: For push notifications and analytics
                        • Backend API Services: For data management and reporting
                        • Webhook Services: For monitoring and integration
                        
                        We may disclose information if required by law or in response to valid requests by public authorities. We do not sell, rent, or trade your personal information to third parties for marketing purposes.
                    """.trimIndent()
                )
            }
            
            // Permissions
            item {
                PolicySection(
                    title = "6. Permissions Required",
                    content = """
                        Our App requires the following permissions:
                        
                        • SMS Permissions: SEND_SMS, RECEIVE_SMS, READ_SMS - For motor control commands
                        • Phone Permissions: READ_PHONE_STATE, READ_PHONE_NUMBERS - For device identification
                        • Internet Permission: INTERNET - For data synchronization
                        • Notification Permission: POST_NOTIFICATIONS - For push notifications
                        • Background Service Permissions: FOREGROUND_SERVICE, WAKE_LOCK - For background sync
                        
                        You can revoke these permissions at any time through device settings, but this may limit app functionality.
                    """.trimIndent()
                )
            }
            
            // Data Retention
            item {
                PolicySection(
                    title = "7. Data Retention",
                    content = """
                        • Account Information: Retained while account is active and for reasonable period after deletion
                        • Motor Operation Logs: Retained for up to 2 years
                        • Analytics Data: Retained in aggregated and anonymized form
                        
                        You can request deletion of your data at any time by contacting us.
                    """.trimIndent()
                )
            }
            
            // User Rights
            item {
                PolicySection(
                    title = "8. Your Rights and Choices",
                    content = """
                        You have the right to:
                        • Access your personal information
                        • Correct inaccurate information
                        • Delete your personal information
                        • Request data portability
                        • Opt-out of certain data collection
                        • Delete your account and associated data
                        
                        To exercise these rights, contact us using the information in the "Contact Us" section.
                    """.trimIndent()
                )
            }
            
            // Children's Privacy
            item {
                PolicySection(
                    title = "9. Children's Privacy",
                    content = "Our App is not intended for children under 13. We do not knowingly collect personal information from children under 13. If you believe your child has provided us with personal information, please contact us immediately."
                )
            }
            
            // Contact Information
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "13. Contact Us",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        ContactInfoRow(
                            label = "Company",
                            value = "AMPUSHWORKS ENTERPRISES PRIVATE LIMITED"
                        )
                        ContactInfoRow(
                            label = "Email",
                            value = "info@AmpushworksEnterprisesPvt.Ltd.in",
                            onClick = {
                                val intent = Intent(Intent.ACTION_SENDTO).apply {
                                    data = Uri.parse("mailto:info@AmpushworksEnterprisesPvt.Ltd.in")
                                }
                                context.startActivity(Intent.createChooser(intent, "Send Email"))
                            }
                        )
                        ContactInfoRow(
                            label = "Phone",
                            value = "+91 9470213937",
                            onClick = {
                                val intent = Intent(Intent.ACTION_DIAL).apply {
                                    data = Uri.parse("tel:+919470213937")
                                }
                                context.startActivity(intent)
                            }
                        )
                        ContactInfoRow(
                            label = "Address",
                            value = "Priyadarshi Nagar, Bhagwat Nagar, Patna- 800026, Bihar, India"
                        )
                        ContactInfoRow(
                            label = "Website",
                            value = "https://ampushworks.com",
                            onClick = {
                                val intent = Intent(Intent.ACTION_VIEW).apply {
                                    data = Uri.parse("https://ampushworks.com")
                                }
                                context.startActivity(intent)
                            }
                        )
                    }
                }
            }
            
            // View Full Policy Online
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "View Full Policy Online",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                val intent = Intent(Intent.ACTION_VIEW).apply {
                                    data = Uri.parse("https://ampushworks.com/privacy-policy")
                                }
                                context.startActivity(intent)
                            }
                        ) {
                            Icon(Icons.Default.Info, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Open in Browser")
                        }
                    }
                }
            }
        }
}

@Composable
private fun PolicySection(title: String, content: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = content,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun ContactInfoRow(
    label: String,
    value: String,
    onClick: (() -> Unit)? = null
) {
    val cardModifier = if (onClick != null) {
        Modifier.fillMaxWidth()
    } else {
        Modifier.fillMaxWidth()
    }
    
    Card(
        modifier = cardModifier,
        onClick = onClick ?: {},
        enabled = onClick != null,
        colors = CardDefaults.cardColors(
            containerColor = if (onClick != null) {
                MaterialTheme.colorScheme.surfaceVariant
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }
            if (onClick != null) {
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = "Open",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
    Spacer(modifier = Modifier.height(8.dp))
}

