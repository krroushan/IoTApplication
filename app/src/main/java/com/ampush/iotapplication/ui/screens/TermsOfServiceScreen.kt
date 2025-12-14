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
fun TermsOfServiceScreen(navController: androidx.navigation.NavController) {
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
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Terms of Service",
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
            
            // Acceptance
            item {
                TermsSection(
                    title = "1. Acceptance of Terms",
                    content = """
                        By downloading, installing, accessing, or using the IoT Motor Control Application ("App") developed and operated by AMPUSHWORKS ENTERPRISES PRIVATE LIMITED ("Company," "we," "us," or "our"), you agree to be bound by these Terms of Service ("Terms"). If you do not agree to these Terms, do not use the App.
                        
                        These Terms constitute a legally binding agreement between you and the Company. We may update these Terms from time to time, and your continued use of the App after such changes constitutes acceptance of the updated Terms.
                    """.trimIndent()
                )
            }
            
            // Description of Service
            item {
                TermsSection(
                    title = "2. Description of Service",
                    content = """
                        The IoT Motor Control Application is a mobile application that enables users to control and monitor IoT motor devices via SMS commands. The App provides:
                        
                        • Remote motor control (ON/OFF commands)
                        • Real-time status monitoring
                        • Historical data and reports
                        • Push notifications for motor status changes
                        • Device management and synchronization
                        
                        The App requires SMS permissions and internet connectivity to function properly.
                    """.trimIndent()
                )
            }
            
            // User Accounts
            item {
                TermsSection(
                    title = "3. User Accounts",
                    content = """
                        To use the App, you must:
                        • Create an account with accurate and complete information
                        • Maintain the security of your account credentials
                        • Notify us immediately of any unauthorized access
                        • Be responsible for all activities under your account
                        • Be at least 13 years of age (or age of majority in your jurisdiction)
                        
                        You are responsible for maintaining the confidentiality of your account password and for all activities that occur under your account.
                    """.trimIndent()
                )
            }
            
            // Acceptable Use
            item {
                TermsSection(
                    title = "4. Acceptable Use",
                    content = """
                        You agree NOT to:
                        • Use the App for any illegal or unauthorized purpose
                        • Violate any laws or regulations
                        • Interfere with or disrupt the App or servers
                        • Attempt to gain unauthorized access to any part of the App
                        • Use automated systems to access the App without permission
                        • Reverse engineer, decompile, or disassemble the App
                        • Remove or alter any proprietary notices or labels
                        • Use the App to send spam or unsolicited messages
                        • Impersonate any person or entity
                        
                        Violation of these terms may result in immediate termination of your account.
                    """.trimIndent()
                )
            }
            
            // SMS and Permissions
            item {
                TermsSection(
                    title = "5. SMS and Permissions",
                    content = """
                        The App requires SMS permissions to:
                        • Send motor control commands via SMS
                        • Receive motor status updates via SMS
                        • Identify your device phone number
                        
                        You acknowledge that:
                        • Standard SMS charges may apply as per your mobile carrier
                        • We only access SMS messages related to motor control operations
                        • You grant us permission to use SMS functionality for App purposes
                        • You are responsible for any SMS charges incurred
                        
                        We do not read, store, or transmit any SMS messages unrelated to motor control.
                    """.trimIndent()
                )
            }
            
            // Intellectual Property
            item {
                TermsSection(
                    title = "6. Intellectual Property",
                    content = """
                        The App, including its design, features, functionality, and content, is owned by AMPUSHWORKS ENTERPRISES PRIVATE LIMITED and protected by copyright, trademark, and other intellectual property laws.
                        
                        You are granted a limited, non-exclusive, non-transferable license to use the App for personal, non-commercial purposes. You may not:
                        • Copy, modify, or distribute the App
                        • Create derivative works based on the App
                        • Use our trademarks or logos without permission
                        • Remove any copyright or proprietary notices
                    """.trimIndent()
                )
            }
            
            // User Content and Data
            item {
                TermsSection(
                    title = "7. User Content and Data",
                    content = """
                        You retain ownership of any data you provide through the App. By using the App, you grant us a license to:
                        • Store and process your data to provide services
                        • Synchronize data across your devices
                        • Generate reports and analytics
                        • Improve our services
                        
                        We handle your data in accordance with our Privacy Policy. You are responsible for ensuring you have the right to provide any data you submit.
                    """.trimIndent()
                )
            }
            
            // Service Availability
            item {
                TermsSection(
                    title = "8. Service Availability",
                    content = """
                        We strive to provide reliable service but do not guarantee:
                        • Uninterrupted or error-free operation
                        • Availability at all times
                        • Compatibility with all devices or networks
                        • Immediate response to support requests
                        
                        The App may be temporarily unavailable due to:
                        • Maintenance or updates
                        • Technical issues
                        • Network connectivity problems
                        • Force majeure events
                        
                        We are not liable for any losses resulting from service unavailability.
                    """.trimIndent()
                )
            }
            
            // Limitation of Liability
            item {
                TermsSection(
                    title = "9. Limitation of Liability",
                    content = """
                        TO THE MAXIMUM EXTENT PERMITTED BY LAW:
                        
                        • The App is provided "AS IS" and "AS AVAILABLE" without warranties
                        • We disclaim all warranties, express or implied
                        • We are not liable for any indirect, incidental, or consequential damages
                        • Our total liability is limited to the amount you paid for the App (if any)
                        • We are not responsible for motor device malfunctions or damages
                        • You use the App at your own risk
                        
                        Some jurisdictions do not allow exclusion of implied warranties, so some of the above may not apply to you.
                    """.trimIndent()
                )
            }
            
            // Indemnification
            item {
                TermsSection(
                    title = "10. Indemnification",
                    content = """
                        You agree to indemnify and hold harmless AMPUSHWORKS ENTERPRISES PRIVATE LIMITED, its officers, directors, employees, and agents from any claims, damages, losses, liabilities, and expenses (including legal fees) arising from:
                        
                        • Your use of the App
                        • Your violation of these Terms
                        • Your violation of any rights of another party
                        • Any content or data you provide
                    """.trimIndent()
                )
            }
            
            // Termination
            item {
                TermsSection(
                    title = "11. Termination",
                    content = """
                        We may terminate or suspend your account and access to the App immediately, without prior notice, for:
                        • Violation of these Terms
                        • Fraudulent or illegal activity
                        • Extended periods of inactivity
                        • Request by law enforcement
                        
                        You may terminate your account at any time by:
                        • Using the account deletion feature in the App
                        • Contacting us at info@AmpushworksEnterprisesPvt.Ltd.in
                        
                        Upon termination, your right to use the App ceases immediately. We may delete your account and data in accordance with our Privacy Policy.
                    """.trimIndent()
                )
            }
            
            // Changes to Terms
            item {
                TermsSection(
                    title = "12. Changes to Terms",
                    content = """
                        We reserve the right to modify these Terms at any time. We will notify you of material changes by:
                        • Posting updated Terms in the App
                        • Updating the "Last Updated" date
                        • Sending notifications (if enabled)
                        
                        Your continued use of the App after changes constitutes acceptance. If you do not agree to changes, you must stop using the App and delete your account.
                    """.trimIndent()
                )
            }
            
            // Governing Law
            item {
                TermsSection(
                    title = "13. Governing Law",
                    content = """
                        These Terms are governed by and construed in accordance with the laws of India, without regard to conflict of law principles. Any disputes arising from these Terms or the App shall be subject to the exclusive jurisdiction of the courts in Patna, Bihar, India.
                    """.trimIndent()
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
                            text = "14. Contact Us",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "If you have questions about these Terms, please contact us:",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        
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
            
            // Agreement
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
                            text = "By using this App, you agree to these Terms of Service",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }
}

@Composable
private fun TermsSection(title: String, content: String) {
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
    Card(
        modifier = Modifier.fillMaxWidth(),
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

