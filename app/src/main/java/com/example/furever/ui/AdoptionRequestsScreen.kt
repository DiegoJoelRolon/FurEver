package com.example.furever.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Pets
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.furever.models.AdoptionRequest
import com.example.furever.viewmodels.PetViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdoptionRequestsScreen(petViewModel: PetViewModel) {

    val requests by petViewModel.pendingRequests.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) { petViewModel.fetchPendingRequests() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("Solicitudes recibidas", fontWeight = FontWeight.SemiBold, fontSize = 20.sp)
                        if (requests.isNotEmpty()) {
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = Color(0xFFC62828)
                            ) {
                                Text(
                                    "${requests.size}",
                                    modifier   = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                    fontSize   = 11.sp,
                                    color      = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor    = Color(0xFF5C4033),
                    titleContentColor = Color.White
                )
            )
        },
        containerColor = Color(0xFFF5F0EB)
    ) { padding ->

        if (requests.isEmpty()) {
            Box(
                modifier         = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Rounded.Pets,
                        contentDescription = null,
                        tint     = Color(0xFFD7CCC8),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "No tenés solicitudes pendientes",
                        fontSize   = 16.sp,
                        color      = Color(0xFF9E9E9E),
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Cuando alguien quiera adoptar\nuna de tus mascotas, aparecerá acá",
                        fontSize  = 13.sp,
                        color     = Color(0xFFBCAAA4),
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )
                }
            }
        } else {
            LazyColumn(
                modifier       = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        "Estas personas quieren adoptar tus mascotas.",
                        fontSize = 13.sp,
                        color    = Color(0xFF9E9E9E)
                    )
                }
                items(requests) { request ->
                    RequestCard(
                        request   = request,
                        onAccept  = { petViewModel.acceptRequest(request) },
                        onReject  = { petViewModel.rejectRequest(request) }
                    )
                }
            }
        }
    }
}

@Composable
private fun RequestCard(
    request: AdoptionRequest,
    onAccept: () -> Unit,
    onReject: () -> Unit
) {
    val date = remember(request.timestamp) {
        SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            .format(Date(request.timestamp))
    }

    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // Mascota + solicitante
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AsyncImage(
                    model              = request.petImageUrl,
                    contentDescription = request.petName,
                    modifier           = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale       = ContentScale.Crop
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        request.petName,
                        fontWeight = FontWeight.Bold,
                        fontSize   = 15.sp,
                        color      = Color(0xFF3E2723)
                    )
                    Text(
                        "Solicitado por",
                        fontSize = 11.sp,
                        color    = Color(0xFF9E9E9E)
                    )
                    Text(
                        request.requesterName.ifEmpty { request.requesterId },
                        fontSize   = 13.sp,
                        color      = Color(0xFF5C4033),
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        request.requesterId,
                        fontSize = 11.sp,
                        color    = Color(0xFFBCAAA4)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(date, fontSize = 11.sp, color = Color(0xFFBCAAA4))
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = Color(0xFFD7CCC8))
            Spacer(modifier = Modifier.height(12.dp))

            // Botones
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick  = onReject,
                    modifier = Modifier.weight(1f).height(44.dp),
                    shape    = RoundedCornerShape(12.dp),
                    colors   = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFFC62828)
                    ),
                    border   = androidx.compose.foundation.BorderStroke(
                        1.dp, Color(0xFFC62828)
                    )
                ) {
                    Icon(
                        Icons.Rounded.Close,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Rechazar", fontWeight = FontWeight.Medium, fontSize = 13.sp)
                }

                Button(
                    onClick  = onAccept,
                    modifier = Modifier.weight(1f).height(44.dp),
                    shape    = RoundedCornerShape(12.dp),
                    colors   = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF388E3C)
                    )
                ) {
                    Icon(
                        Icons.Rounded.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Aceptar", fontWeight = FontWeight.Medium, fontSize = 13.sp)
                }
            }
        }
    }
}