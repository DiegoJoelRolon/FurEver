package com.example.furever.ui

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.furever.auth.AuthState
import com.example.furever.auth.AuthViewModel
import com.example.furever.location.LocationHelper
import com.example.furever.R

@Composable
fun RegisterScreen(authViewModel: AuthViewModel, onNavigateToLogin: () -> Unit) {
    val context = LocalContext.current
    val authState by authViewModel.authState.collectAsStateWithLifecycle()

    var name by remember { mutableStateOf("") }
    var lastname by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var latitude by remember { mutableStateOf(0.0) }
    var longitude by remember { mutableStateOf(0.0) }
    var locationStatus by remember { mutableStateOf("") }

    // Lanzador de permiso de ubicación
    val locationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            LocationHelper.getCurrentLocation(
                context = context,
                onSuccess = { result ->
                    city = result.city
                    latitude = result.latitude
                    longitude = result.longitude
                    locationStatus = "Ubicación obtenida: ${result.city}"
                },
                onError = { locationStatus = "Error: $it" }
            )
        } else {
            locationStatus = "Permiso denegado. Ingresá la ciudad manualmente."
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F0EB))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                stringResource(R.string.create_account), fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color(0xFF3E2723))
            Text(stringResource(R.string.complete_your_details), fontSize = 14.sp, color = Color(0xFF795548))
            Spacer(modifier = Modifier.height(24.dp))

            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    val fieldColors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF5C4033),
                        focusedLabelColor = Color(0xFF5C4033)
                    )
                    val fieldShape = RoundedCornerShape(12.dp)

                    OutlinedTextField(value = name, onValueChange = { name = it },
                        label = { Text(stringResource(R.string.first_name)) }, modifier = Modifier.fillMaxWidth(),
                        shape = fieldShape, colors = fieldColors)

                    OutlinedTextField(value = lastname, onValueChange = { lastname = it },
                        label = { Text(stringResource(R.string.last_name)) }, modifier = Modifier.fillMaxWidth(),
                        shape = fieldShape, colors = fieldColors)

                    OutlinedTextField(value = email, onValueChange = { email = it },
                        label = { Text(stringResource(R.string.email)) }, modifier = Modifier.fillMaxWidth(),
                        shape = fieldShape, colors = fieldColors)

                    OutlinedTextField(value = password, onValueChange = { password = it },
                        label = { Text(stringResource(R.string.password)) }, modifier = Modifier.fillMaxWidth(),
                        shape = fieldShape, colors = fieldColors,
                        visualTransformation = PasswordVisualTransformation())

                    OutlinedTextField(value = phone, onValueChange = { phone = it },
                        label = { Text(stringResource(R.string.phone)) }, modifier = Modifier.fillMaxWidth(),
                        shape = fieldShape, colors = fieldColors)

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = city,
                            onValueChange = { city = it },
                            label = { Text(stringResource(R.string.city)) },
                            modifier = Modifier.weight(1f),
                            shape = fieldShape,
                            colors = fieldColors
                        )
                        OutlinedButton(
                            onClick = {
                                locationLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF5C4033)),
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF5C4033))
                        ) {
                            Text("GPS", fontWeight = FontWeight.SemiBold)
                        }
                    }

                    if (locationStatus.isNotEmpty()) {
                        Text(locationStatus, fontSize = 12.sp,
                            color = if (locationStatus.startsWith("Error") || locationStatus.startsWith("Permiso"))
                                Color(0xFFC62828) else Color(0xFF388E3C))
                    }

                    Button(
                        onClick = {
                            authViewModel.signup(
                                email = email,
                                password = password,
                                name = name,
                                lastname = lastname,
                                phone = phone,
                                city = city,
                                latitude = latitude,
                                longitude = longitude
                            )
                        },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5C4033)),
                        enabled = name.isNotBlank() && email.isNotBlank() && password.isNotBlank()
                    ) {
                        Text(stringResource(R.string.signup), fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                    }

                    TextButton(
                        onClick = onNavigateToLogin,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.already_have_account),
                            color = Color(0xFF795548), fontSize = 14.sp)
                    }

                    when (authState) {
                        is AuthState.Loading -> CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.CenterHorizontally),
                            color = Color(0xFF5C4033)
                        )
                        is AuthState.Error -> Text(
                            (authState as AuthState.Error).message,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                        else -> Unit
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}