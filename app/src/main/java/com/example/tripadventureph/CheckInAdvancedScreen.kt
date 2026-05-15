package com.example.tripadventureph

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Looper
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import coil.compose.rememberAsyncImagePainter
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.JointType
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PatternItem
import com.google.android.gms.maps.model.RoundCap
import com.google.maps.android.compose.*
import kotlinx.coroutines.launch
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

data class RoutePoint(
    val latitude: Double,
    val longitude: Double
)

@SuppressLint("MissingPermission")
@Composable
fun CheckInAdvancedScreen(
    modifier: Modifier = Modifier,
    repository: AuthRepository,
    sessionManager: SessionManager
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val fusedLocationClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }

    val accessToken = sessionManager.getAccessToken().orEmpty()
    val userId = sessionManager.getUserId().orEmpty()

    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasLocationPermission = granted
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
    }

    var capturedImageUri by remember { mutableStateOf<Uri?>(null) }
    var uploadedImageUrl by remember { mutableStateOf<String?>(null) }
    var uploadingPhoto by remember { mutableStateOf(false) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            val savedUri = saveBitmapToCache(context, bitmap)
            capturedImageUri = savedUri
        }
    }

    var destinationName by remember { mutableStateOf("Osmeña Peak") }

    var startLat by remember { mutableStateOf(9.8150) }
    var startLng by remember { mutableStateOf(123.5960) }

    var targetLat by remember { mutableStateOf(9.8175) }
    var targetLng by remember { mutableStateOf(123.5985) }

    var currentLat by remember { mutableStateOf(startLat) }
    var currentLng by remember { mutableStateOf(startLng) }

    var startVerified by remember { mutableStateOf(false) }
    var destinationReached by remember { mutableStateOf(false) }
    var tracking by remember { mutableStateOf(false) }
    var photoAttached by remember { mutableStateOf(false) }
    var proofMetadata by remember { mutableStateOf("") }
    var statusMessage by remember { mutableStateOf("") }
    var submitting by remember { mutableStateOf(false) }

    val routePoints = remember { mutableStateListOf<RoutePoint>() }

    val startRadiusMeters = 40.0
    val targetRadiusMeters = 40.0

    val startLatLng = LatLng(startLat, startLng)
    val targetLatLng = LatLng(targetLat, targetLng)
    val currentLatLng = LatLng(currentLat, currentLng)

    val cameraPositionState = rememberCameraPositionState()

    val mapUiSettings = remember {
        MapUiSettings(
            zoomControlsEnabled = true,
            myLocationButtonEnabled = true,
            compassEnabled = true
        )
    }

    val mapProperties = remember(hasLocationPermission) {
        MapProperties(
            isMyLocationEnabled = hasLocationPermission
        )
    }

    val distanceFromStart = haversineMeters(
        currentLat,
        currentLng,
        startLat,
        startLng
    )

    val distanceToTarget = haversineMeters(
        currentLat,
        currentLng,
        targetLat,
        targetLng
    )

    val totalPlannedDistance = haversineMeters(
        startLat,
        startLng,
        targetLat,
        targetLng
    ).coerceAtLeast(1.0)

    val progressFraction =
        ((totalPlannedDistance - distanceToTarget) / totalPlannedDistance)
            .toFloat()
            .coerceIn(0f, 1f)

    val polylinePoints = routePoints.map { LatLng(it.latitude, it.longitude) }

    val locationRequest = remember {
        LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            3000L
        ).apply {
            setMinUpdateIntervalMillis(2000L)
            setWaitForAccurateLocation(true)
        }.build()
    }

    val locationCallback = remember {
        object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                val location = locationResult.lastLocation ?: return
                currentLat = location.latitude
                currentLng = location.longitude

                if (tracking) {
                    val newPoint = RoutePoint(currentLat, currentLng)
                    val lastPoint = routePoints.lastOrNull()

                    if (lastPoint == null ||
                        haversineMeters(
                            lastPoint.latitude,
                            lastPoint.longitude,
                            newPoint.latitude,
                            newPoint.longitude
                        ) >= 3.0
                    ) {
                        routePoints.add(newPoint)
                    }
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        routePoints.clear()
        routePoints.add(RoutePoint(startLat, startLng))
        cameraPositionState.move(
            CameraUpdateFactory.newLatLngZoom(startLatLng, 15f)
        )
    }

    LaunchedEffect(currentLat, currentLng) {
        startVerified = distanceFromStart <= startRadiusMeters
        destinationReached = distanceToTarget <= targetRadiusMeters
    }

    LaunchedEffect(capturedImageUri) {
        if (capturedImageUri != null) {
            photoAttached = true
            proofMetadata =
                "destination=$destinationName;lat=$currentLat;lng=$currentLng;points=${routePoints.size};imageUri=$capturedImageUri;time=${System.currentTimeMillis()}"
            statusMessage = "Proof photo captured."
        }
    }

    DisposableEffect(hasLocationPermission, tracking) {
        if (hasLocationPermission && tracking) {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        }

        onDispose {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Advanced Check-In Tracking",
            style = MaterialTheme.typography.headlineSmall
        )

        OutlinedTextField(
            value = destinationName,
            onValueChange = { destinationName = it },
            label = { Text("Destination") },
            modifier = Modifier.fillMaxWidth()
        )

        if (!hasLocationPermission) {
            Button(
                onClick = {
                    locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Grant Location Permission")
            }
        }

        if (!hasCameraPermission) {
            Button(
                onClick = {
                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Grant Camera Permission")
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Start Point: %.5f, %.5f".format(startLat, startLng))
                Text("Target Point: %.5f, %.5f".format(targetLat, targetLng))
                Text("Current Point: %.5f, %.5f".format(currentLat, currentLng))
                Text("Distance from Start Geofence: ${distanceFromStart.toInt()} m")
                Text("Distance to Target Geofence: ${distanceToTarget.toInt()} m")
                Text("Start Verified: ${if (startVerified) "Yes" else "No"}")
                Text("Destination Reached: ${if (destinationReached) "Yes" else "No"}")
                Text("Photo Attached: ${if (photoAttached) "Yes" else "No"}")
                Text("Photo Uploaded: ${if (uploadedImageUrl != null) "Yes" else "No"}")
                Text("Tracking: ${if (tracking) "Active" else "Stopped"}")
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Route Progress",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { progressFraction },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("Progress: ${(progressFraction * 100).toInt()}%")
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Live Route Map",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))

                GoogleMap(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp),
                    cameraPositionState = cameraPositionState,
                    properties = mapProperties,
                    uiSettings = mapUiSettings
                ) {
                    Marker(
                        state = MarkerState(position = startLatLng),
                        title = "Start",
                        snippet = "Initial location",
                        icon = BitmapDescriptorFactory.defaultMarker(
                            BitmapDescriptorFactory.HUE_GREEN
                        )
                    )

                    Marker(
                        state = MarkerState(position = targetLatLng),
                        title = "Target",
                        snippet = destinationName,
                        icon = BitmapDescriptorFactory.defaultMarker(
                            BitmapDescriptorFactory.HUE_RED
                        )
                    )

                    Marker(
                        state = MarkerState(position = currentLatLng),
                        title = "Current Position",
                        snippet = "Live tracked point",
                        icon = BitmapDescriptorFactory.defaultMarker(
                            BitmapDescriptorFactory.HUE_AZURE
                        )
                    )

                    if (polylinePoints.size >= 2) {
                        Polyline(
                            points = polylinePoints,
                            geodesic = true,
                            jointType = JointType.ROUND,
                            startCap = RoundCap(),
                            endCap = RoundCap(),
                            pattern = emptyList<PatternItem>(),
                            width = 12f
                        )
                    }
                }
            }
        }

        Row(modifier = Modifier.fillMaxWidth()) {
            Button(
                onClick = {
                    if (!hasLocationPermission) {
                        statusMessage = "Location permission is required."
                        return@Button
                    }

                    tracking = true
                    routePoints.clear()
                    routePoints.add(RoutePoint(currentLat, currentLng))
                    statusMessage = "Tracking started."

                    scope.launch {
                        cameraPositionState.animate(
                            CameraUpdateFactory.newLatLngZoom(
                                LatLng(currentLat, currentLng),
                                17f
                            )
                        )
                    }
                },
                modifier = Modifier.weight(1f),
                enabled = !tracking
            ) {
                Text("Start Tracking")
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = {
                    tracking = false
                    statusMessage = "Tracking stopped."
                },
                modifier = Modifier.weight(1f),
                enabled = tracking
            ) {
                Text("Stop Tracking")
            }
        }

        Button(
            onClick = {
                if (!hasCameraPermission) {
                    statusMessage = "Camera permission is required."
                    return@Button
                }

                cameraLauncher.launch(null)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Capture Proof Photo")
        }

        if (capturedImageUri != null) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Captured Proof Preview",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Image(
                        painter = rememberAsyncImagePainter(capturedImageUri),
                        contentDescription = "Captured proof photo",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                    )
                }
            }

            Button(
                onClick = {
                    val uri = capturedImageUri ?: return@Button
                    uploadingPhoto = true
                    statusMessage = "Uploading proof photo..."

                    kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                        val uploadedUrl = repository.uploadProofImage(
                            accessToken = accessToken,
                            userId = userId,
                            imageUri = uri
                        )

                        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                            uploadedImageUrl = uploadedUrl
                            uploadingPhoto = false
                            statusMessage = if (uploadedUrl != null) {
                                proofMetadata =
                                    "destination=$destinationName;lat=$currentLat;lng=$currentLng;points=${routePoints.size};imageUrl=$uploadedUrl;time=${System.currentTimeMillis()}"
                                "Proof photo uploaded."
                            } else {
                                "Failed to upload proof photo."
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uploadingPhoto && uploadedImageUrl == null
            ) {
                Text(if (uploadingPhoto) "Uploading..." else "Upload Proof Photo")
            }
        }

        if (uploadedImageUrl != null) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Uploaded URL: $uploadedImageUrl",
                    modifier = Modifier.padding(16.dp)
                )
            }
        }

        OutlinedTextField(
            value = proofMetadata,
            onValueChange = { proofMetadata = it },
            label = { Text("Proof Metadata") },
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                statusMessage = when {
                    destinationName.isBlank() -> "Destination is required."
                    !startVerified -> "User must begin inside the start geofence."
                    capturedImageUri == null -> "Photo proof is required."
                    uploadedImageUrl == null -> "Upload the proof photo first."
                    !destinationReached -> "Target geofence has not been reached yet."
                    routePoints.isEmpty() -> "No tracked route found."
                    else -> {
                        submitting = true
                        "Submitting advanced check-in..."
                    }
                }

                if (submitting) {
                    kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                        val result = repository.submitAdvancedCheckIn(
                            accessToken = accessToken,
                            userId = userId,
                            destinationName = destinationName,
                            currentLat = currentLat,
                            currentLng = currentLng,
                            routePointCount = routePoints.size,
                            proofImageUrl = uploadedImageUrl,
                            proofMetadata = proofMetadata,
                            startVerified = startVerified,
                            destinationReached = destinationReached
                        )

                        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                            statusMessage = result.message
                            submitting = false
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !submitting
        ) {
            Text(if (submitting) "Submitting..." else "Submit Advanced Check-In")
        }

        if (statusMessage.isNotBlank()) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = statusMessage,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Tracked Route Points",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp),
            contentPadding = PaddingValues(vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(routePoints) { point ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Lat: %.5f  Lng: %.5f".format(point.latitude, point.longitude),
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        }
    }
}

fun haversineMeters(
    lat1: Double,
    lon1: Double,
    lat2: Double,
    lon2: Double
): Double {
    val earthRadius = 6371000.0

    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)

    val a = sin(dLat / 2).pow(2.0) +
            cos(Math.toRadians(lat1)) *
            cos(Math.toRadians(lat2)) *
            sin(dLon / 2).pow(2.0)

    val c = 2 * atan2(sqrt(a), sqrt(1 - a))
    return earthRadius * c
}