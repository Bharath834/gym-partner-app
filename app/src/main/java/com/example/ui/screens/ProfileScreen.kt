package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.GymViewModel
import com.example.ui.components.AvatarConfig
import com.example.ui.components.AvatarRegistry
import com.example.ui.components.GymAvatar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: GymViewModel,
    modifier: Modifier = Modifier
) {
    val userProfileState by viewModel.userProfile.collectAsState()
    val scrollState = rememberScrollState()

    if (userProfileState == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val userProfile = userProfileState!!

    // Form Local States
    var name by remember { mutableStateOf(userProfile.name) }
    var ageString by remember { mutableStateOf(userProfile.age.toString()) }
    var city by remember { mutableStateOf(userProfile.city) }
    var gymName by remember { mutableStateOf(userProfile.gymName) }
    var workoutStyle by remember { mutableStateOf(userProfile.workoutStyle) }
    var timeSlot by remember { mutableStateOf(userProfile.timeSlot) }
    var avatarId by remember { mutableStateOf(userProfile.avatarId) }
    var hideLocationUntilMatch by remember { mutableStateOf(userProfile.hideLocationUntilMatch) }

    // Dropdown Expanded States
    var styleDropdownExpanded by remember { mutableStateOf(false) }
    var timeDropdownExpanded by remember { mutableStateOf(false) }

    // Verification Form States
    var selectedVerifyMethod by remember { mutableStateOf("Gym Card") }
    var verifyDetails by remember { mutableStateOf("") }
    var verifyError by remember { mutableStateOf("") }

    val stylesList = listOf("Bodybuilding", "Powerlifting", "Cardio / HIIT", "Yoga / Pilates", "General Fitness")
    val timesList = listOf("Morning (6AM - 10AM)", "Afternoon (12PM - 4PM)", "Evening (5PM - 8PM)", "Late Night (8PM - 12AM)")

    // Automatically update form fields if database updates (e.g. initial seeding)
    LaunchedEffect(userProfile) {
        name = userProfile.name
        ageString = userProfile.age.toString()
        city = userProfile.city
        gymName = userProfile.gymName
        workoutStyle = userProfile.workoutStyle
        timeSlot = userProfile.timeSlot
        avatarId = userProfile.avatarId
        hideLocationUntilMatch = userProfile.hideLocationUntilMatch
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Your Profile Dashboard",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp
            ),
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            text = "Keep your preferences updated to match with the ideal gym buddies who hit your exact workouts and times.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Large Avatar Display
        Box(contentAlignment = Alignment.BottomEnd, modifier = Modifier.padding(bottom = 16.dp)) {
            GymAvatar(avatarId = avatarId, size = 100.dp, showBorder = true, borderColor = MaterialTheme.colorScheme.primary)
            if (userProfile.isVerified) {
                Icon(
                    imageVector = Icons.Filled.Verified,
                    contentDescription = "Verified Badge",
                    tint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier
                        .size(28.dp)
                        .background(Color.White, CircleShape)
                        .border(1.5.dp, MaterialTheme.colorScheme.tertiary, CircleShape)
                )
            }
        }

        // Avatar Selection Row
        Text(
            text = "Choose Your Avatar:",
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        ) {
            items(AvatarRegistry.avatars) { avatar ->
                val selected = avatar.id == avatarId
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clickable { avatarId = avatar.id }
                        .padding(2.dp)
                ) {
                    GymAvatar(
                        avatarId = avatar.id,
                        size = 56.dp,
                        showBorder = selected,
                        borderColor = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = avatar.name,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Main Profile Fields
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Full Name") },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("profile_name_input"),
            shape = RoundedCornerShape(12.dp),
            leadingIcon = { Icon(Icons.Filled.Person, null) }
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = ageString,
            onValueChange = { ageString = it },
            label = { Text("Age (Standard accountability range is 18-35)") },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("profile_age_input"),
            shape = RoundedCornerShape(12.dp),
            leadingIcon = { Icon(Icons.Filled.Cake, null) }
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = city,
            onValueChange = { city = it },
            label = { Text("Your City / Metro Area") },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("profile_city_input"),
            shape = RoundedCornerShape(12.dp),
            leadingIcon = { Icon(Icons.Filled.LocationCity, null) }
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = gymName,
            onValueChange = { gymName = it },
            label = { Text("Primary Gym Facility") },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("profile_gym_input"),
            shape = RoundedCornerShape(12.dp),
            leadingIcon = { Icon(Icons.Filled.Place, null) }
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Workout Style Dropdown Selector
        ExposedDropdownMenuBox(
            expanded = styleDropdownExpanded,
            onExpandedChange = { styleDropdownExpanded = !styleDropdownExpanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                readOnly = true,
                value = workoutStyle,
                onValueChange = {},
                label = { Text("Workout Style / Discipline") },
                leadingIcon = { Icon(Icons.Filled.FitnessCenter, null) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = styleDropdownExpanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                shape = RoundedCornerShape(12.dp)
            )
            ExposedDropdownMenu(
                expanded = styleDropdownExpanded,
                onDismissRequest = { styleDropdownExpanded = false }
            ) {
                stylesList.forEach { selectionStyle ->
                    DropdownMenuItem(
                        text = { Text(selectionStyle) },
                        onClick = {
                            workoutStyle = selectionStyle
                            styleDropdownExpanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Preferred Time Slot Dropdown Selector
        ExposedDropdownMenuBox(
            expanded = timeDropdownExpanded,
            onExpandedChange = { timeDropdownExpanded = !timeDropdownExpanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                readOnly = true,
                value = timeSlot,
                onValueChange = {},
                label = { Text("Consistent Workout Time Slot") },
                leadingIcon = { Icon(Icons.Filled.Schedule, null) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = timeDropdownExpanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                shape = RoundedCornerShape(12.dp)
            )
            ExposedDropdownMenu(
                expanded = timeDropdownExpanded,
                onDismissRequest = { timeDropdownExpanded = false }
            ) {
                timesList.forEach { selectionTime ->
                    DropdownMenuItem(
                        text = { Text(selectionTime) },
                        onClick = {
                            timeSlot = selectionTime
                            timeDropdownExpanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Privacy location options
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Lock,
                    contentDescription = "Privacy Location Toggle",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Hide Location Data",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Hides city & gym details during discovery. Only shown to verified mutual matches.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
                Switch(
                    checked = hideLocationUntilMatch,
                    onCheckedChange = { hideLocationUntilMatch = it },
                    modifier = Modifier.testTag("location_privacy_switch")
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Submit Profile Save Button
        Button(
            onClick = {
                val age = ageString.toIntOrNull() ?: 25
                viewModel.updateProfile(
                    name = name,
                    age = age,
                    city = city,
                    gymName = gymName,
                    workoutStyle = workoutStyle,
                    timeSlot = timeSlot,
                    avatarId = avatarId,
                    hideLocationUntilMatch = hideLocationUntilMatch
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("save_profile_button"),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Filled.Save, null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Save Profile & Settings")
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Verification Badge Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.03f)
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Security,
                        contentDescription = "Verification Badge Center",
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Gym Verification Hub",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Request a green 'Verified' trust badge to build credibility in the fitness community. Enter your official gym membership card ID or upload a selfie confirmation.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (userProfile.isVerified) {
                    // Success state
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFDCFCE7), RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Verified,
                            contentDescription = "Verified Active",
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Verified Badge Active",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFF15803D)
                            )
                            Text(
                                text = "Method: ${userProfile.verificationType} (${userProfile.verificationDetails})",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF166534)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = { viewModel.resetVerification() },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Reset & Re-verify Profile")
                    }
                } else {
                    // Form state
                    Text(
                        text = "Select Verification Type:",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        listOf("Gym Card", "Selfie Verify").forEach { method ->
                            val selected = method == selectedVerifyMethod
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { selectedVerifyMethod = method },
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                                ),
                                border = if (selected) BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = method,
                                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                        color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = verifyDetails,
                        onValueChange = { verifyDetails = it },
                        placeholder = {
                            if (selectedVerifyMethod == "Gym Card") Text("Enter Gym Membership ID...")
                            else Text("Describe your selfie validation...")
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("verification_input"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    if (verifyError.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = verifyError,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            if (verifyDetails.isBlank()) {
                                verifyError = "Verification details cannot be empty"
                            } else {
                                verifyError = ""
                                viewModel.requestVerification(selectedVerifyMethod, verifyDetails)
                                verifyDetails = ""
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("submit_verification_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Filled.VerifiedUser, null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Apply Verification Shield")
                    }
                }
            }
        }
    }
}
