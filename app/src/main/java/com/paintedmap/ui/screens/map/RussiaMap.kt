package com.paintedmap.data

import android.content.Context
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import org.json.JSONArray
import org.json.JSONObject
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

private const val GLOBE_RADIUS_FACTOR = 0.52f
private const val GLOBE_OFFSET_X_FACTOR = 0.0f
private const val GLOBE_OFFSET_Y_FACTOR = 0.0f
private const val MAP_SCALE = 1.55f
private const val MAP_VERTICAL_STRETCH = 1.1f
private const val ROTATION_SENSITIVITY = 0.15f
private const val MIN_ROTATION_DEGREES = -25f
private const val MAX_ROTATION_DEGREES = 35f
private const val GLOBE_LON = 95.0
private const val GLOBE_LAT = 65.0

private data class GeoPoint(
    val longitude: Double,
    val latitude: Double
)

private data class ProjectedPoint(
    val x: Float,
    val y: Float,
    val visible: Boolean
)

private data class Region(
    val id: String,
    val fullName: String,
    val polygons: List<List<List<GeoPoint>>>,
    val centerLongitude: Double
)

@Composable
fun RussiaMap(
    modifier: Modifier = Modifier,
    visitedRegions: Set<RegionEntry> = emptySet(),
    color: Color = Color.Blue,
    circuitedRegion: RegionEntry?
) {
    val context = LocalContext.current
    val regions = remember { loadRegions(context) }
    var rotationDegrees by rememberSaveable { mutableFloatStateOf(0f) }

    val visitedNames = remember(visitedRegions.size) {
        visitedRegions.map { it.name.lowercase().trim() }.toSet()
    }

    RussiaGlobeMap(
        regions = regions,
        modifier = modifier.pointerInput(Unit) {
            detectHorizontalDragGestures { _, dragAmount ->
                rotationDegrees = (rotationDegrees - dragAmount * ROTATION_SENSITIVITY)
                    .coerceIn(MIN_ROTATION_DEGREES, MAX_ROTATION_DEGREES)
            }
        },
        visitedIds = visitedNames,
        visitedColor = color,
        globeLongitude = GLOBE_LON,
        globeLatitude = GLOBE_LAT,
        rotationDegrees = rotationDegrees,
        circuitedRegion = circuitedRegion
    )
}


@Composable
private fun RussiaGlobeMap(
    regions: List<Region>,
    modifier: Modifier,
    visitedIds: Set<String>,
    visitedColor: Color,
    globeLongitude: Double,
    globeLatitude: Double,
    rotationDegrees: Float,
    circuitedRegion: RegionEntry?
) {
    val globeColor = MaterialTheme.colorScheme.tertiary

    Canvas(modifier = modifier.padding(8.dp)) {
        if (regions.isEmpty()) return@Canvas

        val globeRadius = min(size.width, size.height) * GLOBE_RADIUS_FACTOR
        val globeCenterX = size.width / 2f + size.width * GLOBE_OFFSET_X_FACTOR
        val globeCenterY = size.height / 2f + size.height * GLOBE_OFFSET_Y_FACTOR
        val currentLongitude = globeLongitude + rotationDegrees
        val spherePath = Path().apply {
            addOval(
                Rect(
                    left = globeCenterX - globeRadius,
                    top = globeCenterY - globeRadius,
                    right = globeCenterX + globeRadius,
                    bottom = globeCenterY + globeRadius
                )
            )
        }

        drawPath(path = spherePath, color = globeColor)

        clipPath(spherePath) {
            for (region in regions) {
                val path = Path().apply { fillType = PathFillType.EvenOdd }
                var hasVisiblePoint = false

                for (polygon in region.polygons) {
                    for (ring in polygon) {
                        if (ring.isEmpty()) continue
                        var previousVisible = false

                        for (point in ring) {
                            val projected = projectToSphere(
                                point = point,
                                globeLongitude = currentLongitude,
                                globeLatitude = globeLatitude
                            )

                            if (projected.visible) {
                                val screenX = globeCenterX + projected.x * globeRadius * MAP_SCALE
                                val screenY = globeCenterY + projected.y * globeRadius * MAP_SCALE * MAP_VERTICAL_STRETCH

                                if (!previousVisible) path.moveTo(screenX, screenY)
                                else path.lineTo(screenX, screenY)
                                hasVisiblePoint = true
                                previousVisible = true
                            } else {
                                previousVisible = false
                            }
                        }

                        if (hasVisiblePoint) path.close()
                    }
                }

                if (!hasVisiblePoint) continue

                val currentRegionName = region.fullName.lowercase().trim()

                val isVisited = visitedIds.contains(currentRegionName)
                val isCircuited = currentRegionName == (circuitedRegion?.name?.lowercase()?.trim() ?: "")

                val fillColor = if (isVisited) visitedColor else Color.LightGray
                val circuitColor = if (isCircuited) Color.Red else Color.DarkGray
                val strokeWeight = if (isCircuited) 5f else 1f

                drawPath(path = path, color = fillColor)
                drawPath(path = path, color = circuitColor, style = Stroke(width = strokeWeight))

            }
        }
    }
}

private fun projectToSphere(
    point: GeoPoint,
    globeLongitude: Double,
    globeLatitude: Double
): ProjectedPoint {
    val latitude = Math.toRadians(point.latitude)
    val longitude = Math.toRadians(point.longitude)
    val centerLatitude = Math.toRadians(globeLatitude)
    val centerLongitude = Math.toRadians(globeLongitude)
    val deltaLongitude = longitude - centerLongitude
    val sphereX = cos(latitude) * sin(deltaLongitude)
    val sphereY = cos(centerLatitude) * sin(latitude) - sin(centerLatitude) * cos(latitude) * cos(deltaLongitude)
    val sphereZ = sin(centerLatitude) * sin(latitude) + cos(centerLatitude) * cos(latitude) * cos(deltaLongitude)
    return ProjectedPoint(x = sphereX.toFloat(), y = (-sphereY).toFloat(), visible = sphereZ >= 0.0)
}

private fun loadRegions(context: Context): List<Region> {
    val jsonText = context.assets.open("regions_simple.geojson").bufferedReader().use { it.readText() }
    val root = JSONObject(jsonText)
    val features = root.getJSONArray("features")
    val result = mutableListOf<Region>()

    for (i in 0 until features.length()) {
        val feature = features.getJSONObject(i)
        val properties = feature.optJSONObject("properties")

        val regionId = feature.optString("id").takeIf { it.isNotBlank() }
            ?: properties?.optString("id")?.takeIf { it.isNotBlank() }
            ?: properties?.optString("name")?.takeIf { it.isNotBlank() }
            ?: "region_$i"

        val fullName = properties?.optString("name_full")
            ?: properties?.optString("name")
            ?: ""

        val geometry = feature.getJSONObject("geometry")
        val geometryType = geometry.getString("type")
        val coordinates = geometry.getJSONArray("coordinates")
        val polygons = when (geometryType) {
            "Polygon" -> listOf(parsePolygon(coordinates))
            "MultiPolygon" -> parseMultiPolygon(coordinates)
            else -> emptyList()
        }

        if (polygons.isEmpty()) continue
        result.add(
            Region(
                id = regionId,
                fullName = fullName,
                polygons = polygons,
                centerLongitude = calculateCenterLongitude(polygons)
            )
        )
    }

    return result
}


private fun calculateCenterLongitude(polygons: List<List<List<GeoPoint>>>): Double {
    var minLongitude = Double.POSITIVE_INFINITY
    var maxLongitude = Double.NEGATIVE_INFINITY

    for (polygon in polygons) {
        for (ring in polygon) {
            for (point in ring) {
                minLongitude = min(minLongitude, point.longitude)
                maxLongitude = max(maxLongitude, point.longitude)
            }
        }
    }

    return (minLongitude + maxLongitude) / 2.0
}

private fun parsePolygon(coordinates: JSONArray): List<List<GeoPoint>> {
    val rings = mutableListOf<List<GeoPoint>>()

    for (i in 0 until coordinates.length()) {
        val ringArray = coordinates.getJSONArray(i)
        val ring = mutableListOf<GeoPoint>()

        for (j in 0 until ringArray.length()) {
            val point = ringArray.getJSONArray(j)
            ring.add(GeoPoint(longitude = point.getDouble(0), latitude = point.getDouble(1)))
        }

        rings.add(ring)
    }

    return rings
}

private fun parseMultiPolygon(coordinates: JSONArray): List<List<List<GeoPoint>>> {
    val polygons = mutableListOf<List<List<GeoPoint>>>()

    for (i in 0 until coordinates.length()) {
        polygons.add(parsePolygon(coordinates.getJSONArray(i)))
    }

    return polygons
}
