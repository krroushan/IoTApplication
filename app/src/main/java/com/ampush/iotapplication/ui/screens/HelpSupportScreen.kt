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
fun HelpSupportScreen(navController: androidx.navigation.NavController) {
    val context = LocalContext.current
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
            // Header Card
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
                            text = "We're Here to Help!",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Get assistance with your IoT Motor Control App",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                    }
                }
            }
            
            // Contact Information
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Contact Us",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        ContactItem(
                            icon = Icons.Default.Phone,
                            title = "Phone",
                            value = "+91 9470213937",
                            onClick = {
                                val intent = Intent(Intent.ACTION_DIAL).apply {
                                    data = Uri.parse("tel:+919470213937")
                                }
                                context.startActivity(intent)
                            }
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        ContactItem(
                            icon = Icons.Default.Email,
                            title = "Email",
                            value = "info@AmpushworksEnterprisesPvt.Ltd.in",
                            onClick = {
                                val intent = Intent(Intent.ACTION_SENDTO).apply {
                                    data = Uri.parse("mailto:info@AmpushworksEnterprisesPvt.Ltd.in")
                                }
                                context.startActivity(Intent.createChooser(intent, "Send Email"))
                            }
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        ContactItem(
                            icon = Icons.Default.Info,
                            title = "Website",
                            value = "https://ampushworks.com",
                            onClick = {
                                val intent = Intent(Intent.ACTION_VIEW).apply {
                                    data = Uri.parse("https://ampushworks.com")
                                }
                                context.startActivity(intent)
                            }
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        ContactItem(
                            icon = Icons.Default.LocationOn,
                            title = "Address",
                            value = "Priyadarshi Nagar, Bhagwat Nagar, Patna- 800026, Bihar, India",
                            onClick = {
                                val intent = Intent(Intent.ACTION_VIEW).apply {
                                    data = Uri.parse("geo:0,0?q=Priyadarshi+Nagar+Bhagwat+Nagar+Patna+800026")
                                }
                                context.startActivity(intent)
                            }
                        )
                    }
                }
            }
            
            // FAQ Section
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Frequently Asked Questions",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        FAQItem(
                            question = "How do I control my motor?",
                            answer = "Use the Dashboard screen to send commands: MOTORON to turn on, MOTOROFF to turn off, and STATUS to check current status."
                        )
                        
                        FAQItem(
                            question = "Why do I need SMS permissions?",
                            answer = "The app uses SMS to send control commands to your IoT motor device and receive status updates. We only access SMS messages related to motor control."
                        )
                        
                        FAQItem(
                            question = "How do I add a device?",
                            answer = "Devices are assigned by your administrator. Contact support if you need devices added to your account."
                        )
                        
                        FAQItem(
                            question = "What if I don't receive notifications?",
                            answer = "Ensure notification permissions are granted in your device settings. Check the FCM status in the Profile screen."
                        )
                        
                        FAQItem(
                            question = "How do I view motor history?",
                            answer = "Navigate to the History tab to view all past motor operations, including timestamps and status information."
                        )
                        
                        FAQItem(
                            question = "Can I use multiple devices?",
                            answer = "Yes! You can manage multiple IoT motor devices. Select your default device from settings or choose a device when sending commands."
                        )
                    }
                }
            }
            
            // Quick Links
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Quick Links",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        QuickLinkButton(
                            text = "Visit Our Website",
                            icon = Icons.Default.Info,
                            onClick = {
                                val intent = Intent(Intent.ACTION_VIEW).apply {
                                    data = Uri.parse("https://ampushworks.com")
                                }
                                context.startActivity(intent)
                            }
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        QuickLinkButton(
                            text = "About Us",
                            icon = Icons.Default.Info,
                            onClick = {
                                val intent = Intent(Intent.ACTION_VIEW).apply {
                                    data = Uri.parse("https://ampushworks.com/Website/page/19")
                                }
                                context.startActivity(intent)
                            }
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        QuickLinkButton(
                            text = "Contact Page",
                            icon = Icons.Default.Info,
                            onClick = {
                                val intent = Intent(Intent.ACTION_VIEW).apply {
                                    data = Uri.parse("https://ampushworks.com/Website/page/28")
                                }
                                context.startActivity(intent)
                            }
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Delete Account Button
                        OutlinedButton(
                            onClick = {
                                navController.navigate("delete_account")
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Delete My Account")
                        }
                    }
                }
            }
            
            // Support Hours
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            modifier = Modifier.size(32.dp),
                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Support Hours",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Monday - Saturday: 9:00 AM - 6:00 PM IST",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "Sunday: Closed",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
}

@Composable
private fun ContactItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    value: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }
            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = "Open",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun FAQItem(question: String, answer: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = question,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = answer,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun QuickLinkButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text)
    }
}

