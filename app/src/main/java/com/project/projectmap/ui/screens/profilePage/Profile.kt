package com.project.projectmap.ui.screens.profilePage

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
            .background(MaterialTheme.colorScheme.background)
            .padding(start = 16.dp, top = 54.dp, end = 16.dp, bottom = 0.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Close Button Row
        Row(
            modifier = Modifier
                .fillMaxWidth(),
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

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(Color.LightGray)
            ) {
                // If you want to add content inside the circle, put it here
                // The content will be centered by default
            }
            Text(
                text = "Hosea",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold
            )
        }

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

        // Target Section
        ProfileSection(
            title = "Target",
            content = {
                ProfileField("Set Your Target Calories", "2000")
            }
        )

        // Reminders Section
        ProfileSection(
            title = "Reminders",
            content = {
                ProfileField("Set Your Reminder Time", reminderTime, true) {
                    // Toggle reminder time options here if needed
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))
        // Log Out Button
        Button(
            onClick = onLogout,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Log Out", color = Color.White)
        }
        
        Spacer(modifier = Modifier.height(16.dp))
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
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
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
        Text(text = label, color = MaterialTheme.colorScheme.onBackground, fontSize = 18.sp, fontWeight = FontWeight.Medium)
        if (isDropdown) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { onDropdownClick() }
            ) {
                Text(text = value, color = MaterialTheme.colorScheme.onBackground, fontSize = 18.sp)
                Icon(
                    painter = painterResource(id = R.drawable.ic_dropdown),
                    contentDescription = "Dropdown",
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
        } else {
            Text(text = value, color = MaterialTheme.colorScheme.onBackground, fontSize = 18.sp)
        }
    }
}