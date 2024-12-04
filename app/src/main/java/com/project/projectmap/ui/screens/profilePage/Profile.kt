package com.project.projectmap.ui.screens.profilePage

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.project.projectmap.R

@Composable
fun ProfileScreen(
    onClose: () -> Unit,
    onLogout: () -> Unit
) {
    var selectedGender by remember { mutableStateOf("Male") }
    var reminderTime by remember { mutableStateOf("Every 6 Hours") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Close Button Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.End
        ) {
            IconButton(onClick = onClose) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_close),
                    contentDescription = "Close"
                )
            }
        }

        // Profile Picture Placeholder
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(Color.LightGray)
        ) {
            // If you want to add content inside the circle, put it here
            // The content will be centered by default
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Hosea",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(32.dp))

        // My Profile Section
        ProfileSection(
            title = "My Profile",
            content = {
                ProfileField("Name", "Hosea")
                ProfileField("Birthday", "06/07/1970")
                ProfileField("Gender", selectedGender, true) {
                    selectedGender = if (selectedGender == "Male") "Female" else "Male"
                }
                ProfileField("Email", "Hosea@gmail.com")
                ProfileField("Location", "Indonesia")
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Target Section
        ProfileSection(
            title = "Target",
            content = {
                ProfileField("Set Your Target Calories", "2000")
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Reminders Section
        ProfileSection(
            title = "Reminders",
            content = {
                ProfileField("Set Your Reminder Time", reminderTime, true) {
                    // Toggle reminder time options here if needed
                }
            }
        )

        Spacer(modifier = Modifier.weight(1f))

        // Log Out Button
        Button(
            onClick = onLogout,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFEF5350)
            ),
            shape = RoundedCornerShape(24.dp)
        ) {
            Text("Log Out", color = Color.White)
        }
    }
}

@Composable
private fun ProfileSection(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF3E5F5)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            content()
        }
    }
}

@Composable
private fun ProfileField(
    label: String,
    value: String,
    isDropdown: Boolean = false,
    onDropdownClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, color = Color.Gray)
        if (isDropdown) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { onDropdownClick() }
            ) {
                Text(text = value)
                Icon(
                    painter = painterResource(id = R.drawable.ic_dropdown),
                    contentDescription = "Dropdown",
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
        } else {
            Text(text = value)
        }
    }
}