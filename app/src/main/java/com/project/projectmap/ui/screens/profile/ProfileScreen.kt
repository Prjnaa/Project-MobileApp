package com.project.projectmap.ui.screens.profile

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.width
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
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.project.projectmap.R
import com.project.projectmap.components.msc.PasswordInput
import com.project.projectmap.firebase.model.Gender
import com.project.projectmap.firebase.model.Profile
import com.project.projectmap.firebase.model.User
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

@Composable
fun ProfileScreen(onClose: () -> Unit, onLogout: () -> Unit) {
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser

    val db = FirebaseFirestore.getInstance()
    val userRef = currentUser?.uid?.let { db.collection("users").document(it) }

    var userName by remember { mutableStateOf("") }
    var userEmail by remember { mutableStateOf("") }
    var userAge by remember { mutableStateOf(0) }
    var userGender by remember { mutableStateOf(Gender.Male) }
    var reminderInterval by remember { mutableStateOf(5 * 60 * 60 * 1000) }
    var userCoin by remember { mutableStateOf(0) }
    var userPhotoUrl by remember { mutableStateOf("") }

    var isEditingProfile by remember { mutableStateOf(false) }
    var showPasswordDialog by remember { mutableStateOf(false) }


    val context = LocalContext.current
    // Photo Picker Launcher
    val photoPickerLauncher =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) {
            uri: Uri? ->
            uri?.let {
                val localPath = saveUriToInternalStorage(context, it, "profile_picture.jpg")
                if (localPath != null) {
                    userPhotoUrl = localPath
                    userRef?.update("profile.photoUrl", localPath)
                } else {
                    Log.e("ProfileScreen", "Failed to save photo locally.")
                }
            }
        }

    LaunchedEffect(currentUser) {
        userRef?.get()?.addOnSuccessListener { document ->
            document.toObject(User::class.java)?.let { user ->
                userName = user.profile.name
                userEmail = user.profile.email
                userAge = user.profile.age
                userGender = user.profile.gender
                reminderInterval = user.profile.reminderInterval
                userCoin = user.profile.coin
                userPhotoUrl = user.profile.photoUrl
            }

            Log.d(
                "ProfileScreen",
                "User data loaded : $userName, $userEmail, $userAge, $userGender, $reminderInterval, $userCoin, $userPhotoUrl")
        }
    }

    Column(
        modifier =
            Modifier.fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                IconButton(onClick = onClose) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_close),
                        contentDescription = "Close")
                }
            }

            // Profile Photo with Edit Button
            Box(contentAlignment = Alignment.BottomEnd, modifier = Modifier.size(120.dp)) {
                AsyncImage(
                    model =
                        ImageRequest.Builder(context)
                            .data(if (userPhotoUrl.isNotEmpty()) File(userPhotoUrl) else null)
                            .build(),
                    contentDescription = "Profile Picture",
                    modifier = Modifier.size(120.dp).clip(CircleShape).background(Color.Gray),
                    contentScale = ContentScale.Crop)
                IconButton(
                    onClick = { photoPickerLauncher.launch("image/*") },
                    modifier =
                        Modifier.size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)) {
                        Icon(
                            painter = painterResource(id = R.drawable.editable_24),
                            contentDescription = "Edit Photo",
                            tint = MaterialTheme.colorScheme.onPrimary)
                    }
            }

            Text(text = userName, fontSize = 32.sp, fontWeight = FontWeight.Bold)

            ProfileSection(
                title = "My Profile",
                isEditing = isEditingProfile,
                onEditClicked = { isEditingProfile = !isEditingProfile }) {
                    EditableProfileField("Name", userName, isEditingProfile) { userName = it }
                    EditableProfileField("Age", userAge.toString(), isEditingProfile) {
                        userAge = it.toIntOrNull() ?: userAge
                    }
                    GenderDropdownField(userGender, isEditingProfile) { userGender = it }
                    EditableProfileField("Email", userEmail, isEditingProfile) { userEmail = it }
                }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { showPasswordDialog = true },
                enabled = isEditingProfile,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary,
                        contentColor = MaterialTheme.colorScheme.onSecondary),
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
                                photoUrl = userPhotoUrl,
                                reminderInterval = reminderInterval)
                        userRef?.update("profile", updatedProfile)?.addOnCompleteListener {
                            if (it.isSuccessful) {
                                isEditingProfile = false
                                showPasswordDialog = false
                            }
                        }
                    },
                    onDismiss = { showPasswordDialog = false })
            }

            Spacer(modifier = Modifier.height(16.dp))

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

@Composable
private fun EditableProfileField(
    label: String,
    value: String,
    isEditing: Boolean,
    onValueChange: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically) {
            Text(text = label, fontSize = 18.sp, fontWeight = FontWeight.Medium)
            if (isEditing) {
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    modifier =
                        Modifier.border(
                                1.dp,
                                MaterialTheme.colorScheme.onBackground.copy(0.5f),
                                RoundedCornerShape(8.dp))
                            .padding(8.dp)
                            .width(225.dp),
                    singleLine = true,
                    textStyle =
                        TextStyle(
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Normal,
                            color = MaterialTheme.colorScheme.onBackground))
            } else {
                Text(text = value, fontSize = 18.sp, fontWeight = FontWeight.Normal)
            }
        }
}

@Composable
private fun GenderDropdownField(
    selectedGender: Gender,
    isEditing: Boolean,
    onGenderSelected: (Gender) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

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

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = { expanded = false },
        modifier = Modifier.shadow(4.dp).clip(RoundedCornerShape(8.dp))) {
            Gender.values().forEach { gender ->
                DropdownMenuItem(
                    text = { Text(gender.name) },
                    onClick = {
                        onGenderSelected(gender)
                        expanded = false
                    })
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
                modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp, horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically) {
                            Text(text = title, fontWeight = FontWeight.Bold, fontSize = 24.sp)
                            TextButton(onClick = onEditClicked) {
                                Text(
                                    text = if (isEditing) "Cancel" else "Edit",
                                    textDecoration = TextDecoration.Underline)
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
        title = { Text(text = "Enter Password") },
        text = {
            Column {
                Text("Please enter your password to save changes.")
                PasswordInput(
                    password = password,
                    onPasswordChange = { password = it },
                    modifier = Modifier.padding(top = 16.dp))
            }
        },
        confirmButton = {
            TextButton(onClick = { onPasswordEntered(password) }) { Text("Confirm") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } })
}

fun saveUriToInternalStorage(context: Context, uri: Uri, fileName: String): String? {
    return try {
        val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
        val file = File(context.filesDir, fileName)
        val outputStream = FileOutputStream(file)
        inputStream?.copyTo(outputStream)
        inputStream?.close()
        outputStream.close()
        file.absolutePath
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
