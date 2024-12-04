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

// Bagian Fungsi Untuk View
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
        // Bagian Header
        ProfileHeader(onClose = onClose)

        // Foto Profil
        ProfilePicture()

        Spacer(modifier = Modifier.height(16.dp))

        // Bagian Profil
        ProfileDetails(
            selectedGender = selectedGender,
            reminderTime = reminderTime,
            onGenderChange = {
                selectedGender = if (selectedGender == "Male") "Female" else "Male"
            }
        )

        Spacer(modifier = Modifier.weight(1f))

        // Tombol Logout
        LogoutButton(onLogout = onLogout)
    }
}
// Bagian Fungsi Untuk View End

// Component Untuk View
@Composable
private fun ProfileHeader(onClose: () -> Unit) {
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
}

@Composable
private fun ProfilePicture() {
    Box(
        modifier = Modifier
            .size(100.dp)
            .clip(CircleShape)
            .background(Color.LightGray)
    )
}

@Composable
private fun ProfileDetails(
    selectedGender: String,
    reminderTime: String,
    onGenderChange: () -> Unit
) {
    // My Profile Section
    ProfileSection(title = "My Profile") {
        ProfileField(label = "Name", value = "Hosea")
        ProfileField(label = "Birthday", value = "06/07/1970")
        ProfileField(label = "Gender", value = selectedGender, isDropdown = true, onDropdownClick = onGenderChange)
        ProfileField(label = "Email", value = "Hosea@gmail.com")
        ProfileField(label = "Location", value = "Indonesia")
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Target Section
    ProfileSection(title = "Target") {
        ProfileField(label = "Set Your Target Calories", value = "2000")
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Reminders Section
    ProfileSection(title = "Reminders") {
        ProfileField(label = "Set Your Reminder Time", value = reminderTime, isDropdown = true)
    }
}

@Composable
private fun LogoutButton(onLogout: () -> Unit) {
    Button(
        onClick = onLogout,
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF5350)),
        shape = RoundedCornerShape(24.dp)
    ) {
        Text("Log Out", color = Color.White)
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
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF3E5F5)),
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
                fontSize = 18.sp,
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
        Text(text = label, color = Color.Black)
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
// Component Untuk View End
