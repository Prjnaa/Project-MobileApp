package com.project.projectmap.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.project.projectmap.R
import com.project.projectmap.components.msc.ConstantsStyle
import com.project.projectmap.firebase.model.Gender
import com.project.projectmap.firebase.model.Profile
import com.project.projectmap.firebase.model.User

@Composable
fun ProfileScreen(onClose: () -> Unit, onLogout: () -> Unit) {
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser

    val db = FirebaseFirestore.getInstance()
    val userRef = db.collection("users").document(currentUser?.uid ?: "")

    var userName by remember { mutableStateOf("") }
    var userEmail by remember { mutableStateOf("") }
    var userAge by remember { mutableStateOf(0) }
    var userGender by remember { mutableStateOf(Gender.Male) }
    var reminderInterval by remember { mutableStateOf(5 * 60 * 60 * 1000) }
    var userCoin by remember { mutableStateOf(0) }

    var isEditingProfile by remember { mutableStateOf(false) }
    var isEditingReminders by remember { mutableStateOf(false) }
    var showPasswordDialog by remember { mutableStateOf(false) }

    userRef.get().addOnSuccessListener { document ->
        val user = document.toObject(User::class.java)
        if (user != null) {
            val profile = user.profile
            userName = profile.name
            userEmail = profile.email
            userAge = profile.age
            userGender = profile.gender
            reminderInterval = profile.reminderInterval
            userCoin = profile.coin
        }
    }

    Column(
        modifier =
            Modifier.fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(ConstantsStyle.APP_PADDING_VAL)
                .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)) {
            // Close Button
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                IconButton(onClick = onClose) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_close),
                        contentDescription = "Close")
                }
            }

            // Profile Picture Placeholder
            Box(modifier = Modifier.size(100.dp).clip(CircleShape).background(Color.LightGray))

            Text(text = userName, fontSize = 32.sp, fontWeight = FontWeight.Bold)

            // Profile Section
            ProfileSection(
                title = "My Profile",
                isEditing = isEditingProfile,
                onEditClicked = { isEditingProfile = !isEditingProfile },
                content = {
                    EditableProfileField("Name", userName, isEditingProfile) { userName = it }
                    EditableProfileField("Age", userAge.toString(), isEditingProfile) {
                        userAge = it.toIntOrNull() ?: userAge
                    }
                    GenderDropdownField(userGender, isEditingProfile) { userGender = it }
                    EditableProfileField("Email", userEmail, isEditingProfile) { userEmail = it }
                })

            // Reminder Section
            //            ProfileSection(
            //                title = "Reminders",
            //                isEditing = isEditingReminders,
            //                onEditClicked = { isEditingReminders = !isEditingReminders },
            //                content = {
            //                    EditableProfileField(
            //                        "Reminder Interval (ms)", reminderInterval.toString(),
            // isEditingReminders) {
            //                            reminderInterval = it.toIntOrNull() ?: reminderInterval
            //                        }
            //                })

            Spacer(modifier = Modifier.height(16.dp))

            // Save Button
            Button(
                onClick = { showPasswordDialog = true },
                enabled = isEditingProfile || isEditingReminders,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(16.dp)) {
                    Text("Save Changes")
                }

            if (showPasswordDialog) {
                PasswordDialog(
                    onPasswordEntered = { password ->
                        val updatedProfile =
                            Profile(
                                name = userName,
                                email = userEmail,
                                age = userAge,
                                gender = userGender,
                                coin = userCoin,
                                reminderInterval = reminderInterval)
                        userRef.update("profile", updatedProfile)
                        isEditingProfile = false
                        isEditingReminders = false
                        showPasswordDialog = false
                    },
                    onDismiss = { showPasswordDialog = false })
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Log Out Button
            Button(
                onClick = onLogout,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                colors =
                    ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                shape = RoundedCornerShape(16.dp)) {
                    Text("Log Out", color = MaterialTheme.colorScheme.onError)
                }
        }
}

// Editable Field
@Composable
private fun EditableProfileField(
    label: String,
    value: String,
    isEditing: Boolean,
    onValueChange: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically) {
            Text(text = label, fontSize = 18.sp, fontWeight = FontWeight.Medium)
            if (isEditing) {
                Box(
                    modifier =
                        Modifier.fillMaxWidth()
                            .padding(start = 12.dp)
                            .border(
                                1.dp,
                                MaterialTheme.colorScheme.onBackground.copy(0.5f),
                                shape = RoundedCornerShape(ConstantsStyle.ROUNDED_CORNER_SM_VAL))) {
                        BasicTextField(
                            value = value,
                            onValueChange = { newValue ->
                                if (newValue != value) {
                                    onValueChange(newValue) // Update nilai state hanya jika berubah
                                }
                            },
                            modifier =
                                Modifier.fillMaxWidth()
                                    .padding(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 8.dp),
                            singleLine = true,
                            textStyle =
                                TextStyle(
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Normal,
                                    color = MaterialTheme.colorScheme.onBackground,
                                    textAlign = TextAlign.End,
                                    letterSpacing = 0.5.sp))
                    }
            } else {
                Box(modifier = Modifier.weight(1f).align(Alignment.CenterVertically)) {
                    Divider(
                        modifier =
                            Modifier.align(Alignment.CenterStart)
                                .padding(start = 4.dp, end = 4.dp, top = 8.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.25f),
                        thickness = 1.5.dp)
                }
                Text(text = value, fontSize = 18.sp, fontWeight = FontWeight.Normal)
            }
        }
}

// Gender Dropdown
@Composable
private fun GenderDropdownField(
    selectedGender: Gender,
    isEditing: Boolean,
    onGenderSelected: (Gender) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().clickable(enabled = isEditing) { expanded = true },
            horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = "Gender", fontSize = 18.sp)
                Row {
                    Text(text = selectedGender.name, fontSize = 18.sp)
                    if (isEditing) {
                        Icon(
                            painter = painterResource(id = R.drawable.arrow_down_24),
                            contentDescription = "Dropdown Arrow",
                            tint = MaterialTheme.colorScheme.onBackground)
                    }
                }
            }

        Box(
            modifier =
                Modifier.shadow(4.dp, shape = RoundedCornerShape(8.dp))
                    .clip(RoundedCornerShape(8.dp)),
        ) {
            DropdownMenu(
                expanded = expanded,
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                onDismissRequest = { expanded = false }) {
                    Gender.values().forEach { gender ->
                        DropdownMenuItem(
                            text = {
                                Text(gender.name, color = MaterialTheme.colorScheme.secondary)
                            },
                            onClick = {
                                onGenderSelected(gender)
                                expanded = false
                            })
                    }
                }
        }
    }
}

@Composable
private fun ProfileSection(
    title: String,
    isEditing: Boolean,
    onEditClicked: () -> Unit,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors =
            CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        shape = RoundedCornerShape(16.dp)) {
            Column(
                modifier =
                    Modifier.fillMaxWidth()
                        .padding(start = 20.dp, top = 12.dp, end = 20.dp, bottom = 12.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically) {
                        Text(text = title, fontWeight = FontWeight.Bold, fontSize = 24.sp)
                        if (!isEditing) {
                            TextButton(onClick = onEditClicked) { Text("Edit") }
                        } else {
                            TextButton(onClick = onEditClicked) { Text("Cancel") }
                        }
                    }
                content()
            }
        }
}

@Composable
private fun PasswordDialog(onPasswordEntered: (String) -> Unit, onDismiss: () -> Unit) {
    var password by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { Button(onClick = { onPasswordEntered(password) }) { Text("Confirm") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
        title = { Text("Enter Password") },
        text = {
            TextField(
                value = password,
                onValueChange = { password = it },
                placeholder = { Text("Password") },
                singleLine = true,
                colors =
                    TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent))
        })
}
