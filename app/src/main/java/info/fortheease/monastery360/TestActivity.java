package info.fortheease.monastery360;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

import org.mapsforge.core.graphics.Bitmap;
import org.mapsforge.core.model.LatLong;
import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import org.mapsforge.map.android.util.AndroidUtil;
import org.mapsforge.map.android.view.MapView;
import org.mapsforge.map.datastore.MapDataStore;
import org.mapsforge.map.layer.cache.TileCache;
import org.mapsforge.map.layer.overlay.Marker;
import org.mapsforge.map.layer.renderer.TileRendererLayer;
import org.mapsforge.map.reader.MapFile;
import org.mapsforge.map.rendertheme.internal.MapsforgeThemes;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class TestActivity extends AppCompatActivity {

    private MapView mapView;
    private TileCache tileCache;
    private TileRendererLayer tileRendererLayer;
    private static final String MAP_ASSET_NAME = "sikkim.map";
    private File mapFile;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private Marker currentUserLocationMarker;
    private Bitmap userMarkerBitmap; // To store the marker bitmap

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        // MUST be called before any Mapsforge view usage
        AndroidGraphicFactory.createInstance(getApplication());
        setContentView(R.layout.activity_test);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        mapView = findViewById(R.id.mapView);
        // ✅ Set initial center and zoom
        LatLong sikkimCenter = new LatLong(27.3389, 88.6065);
        mapView.getModel().mapViewPosition.setCenter(sikkimCenter);
        mapView.getModel().mapViewPosition.setZoomLevel((byte) 10);

        // ensure a place to store the map file in app private storage
        File mapsDir = new File(getExternalFilesDir(null), "maps");
        if (!mapsDir.exists()) mapsDir.mkdirs();
        mapFile = new File(mapsDir, MAP_ASSET_NAME);

        // copy from assets once if not present
        if (!mapFile.exists()) {
            copyAssetToFile(MAP_ASSET_NAME, mapFile);
        }

        // create tile cache
        tileCache = AndroidUtil.createTileCache(
                this,
                "mapcache",
                mapView.getModel().displayModel.getTileSize(),
                1f,
                mapView.getModel().frameBufferModel.getOverdrawFactor()
        );

        // open the map file as a MapDataStore
        MapDataStore mapDataStore = new MapFile(mapFile);

        // create and attach the renderer layer (uses internal render theme by default)
        tileRendererLayer = AndroidUtil.createTileRendererLayer(
                tileCache,
                mapView.getModel().mapViewPosition,
                mapDataStore,
                MapsforgeThemes.DEFAULT
        );
        mapView.getLayerManager().getLayers().add(tileRendererLayer);

        // set a reasonable start position (Gangtok area) + zoom
        mapView.getModel().mapViewPosition.setCenter(new LatLong(27.33, 88.6));
        mapView.getModel().mapViewPosition.setZoomLevel((byte)11);

        // create a bitmap from an app drawable (R.drawable.marker)
        Drawable drawable = ContextCompat.getDrawable(this, R.drawable.baseline_location_on_24);
        Bitmap markerBitmap = AndroidGraphicFactory.convertToBitmap(drawable);
        Marker monasteryMarker1 = new Marker(new LatLong(27.28886, 88.56146), markerBitmap, 0, -markerBitmap.getHeight() / 2);
        mapView.getLayerManager().getLayers().add(monasteryMarker1);
        Marker monasteryMarker2 = new Marker(new LatLong(27.68874, 88.74886), markerBitmap, 0, -markerBitmap.getHeight() / 2);
        mapView.getLayerManager().getLayers().add(monasteryMarker2);
        Marker monasteryMarker3 = new Marker(new LatLong(27.33610, 88.61924), markerBitmap, 0, -markerBitmap.getHeight() / 2);
        mapView.getLayerManager().getLayers().add(monasteryMarker3);
        Marker monasteryMarker4 = new Marker(new LatLong(27.41309, 88.58375), markerBitmap, 0, -markerBitmap.getHeight() / 2);
        mapView.getLayerManager().getLayers().add(monasteryMarker4);
        Marker monasteryMarker5 = new Marker(new LatLong(27.36671, 88.23007), markerBitmap, 0, -markerBitmap.getHeight() / 2);
        mapView.getLayerManager().getLayers().add(monasteryMarker5);

        startLocationUpdates();
    }

    private void copyAssetToFile(String assetName, File outFile) {
        try (InputStream is = getAssets().open(assetName);
             OutputStream os = new FileOutputStream(outFile)) {
            byte[] buffer = new byte[8192];
            int read;
            while ((read = is.read(buffer)) > 0) {
                os.write(buffer, 0, read);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // In TestActivity.java
    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Permissions were checked before calling this, but as a safeguard:
            Toast.makeText(this, "Location permission not granted. Cannot get location.", Toast.LENGTH_LONG).show();
            return;
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Prepare the bitmap for the user location marker (do this once)
        if (userMarkerBitmap == null) {
            Drawable userDrawable = ContextCompat.getDrawable(this, R.drawable.baseline_add_24); // Replace with your desired user location icon
            if (userDrawable != null) {
                userMarkerBitmap = AndroidGraphicFactory.convertToBitmap(userDrawable);
            }
        }


        LocationRequest locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000) // Update every 10 seconds
                .setMinUpdateIntervalMillis(5000) // Minimum interval 5 seconds
                .build();

        locationCallback = new LocationCallback() {

            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                for (Location location : locationResult.getLocations()) {
                    if (location != null) {
                        updateMapWithLocation(location);
                    }
                }
            }
        };

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, getMainLooper());
    }

    // In TestActivity.java
    private void updateMapWithLocation(Location location) {
        LatLong currentUserLatLong = new LatLong(location.getLatitude(), location.getLongitude());

        if (userMarkerBitmap == null) {
            // Should have been initialized in startLocationUpdates, but as a fallback:
            Log.e("TestActivity", "User marker bitmap is null in updateMapWithLocation");
            Toast.makeText(this, "Error: User location icon not loaded.", Toast.LENGTH_SHORT).show();
            return; // Can't add marker without a bitmap
        }

        if (currentUserLocationMarker == null) {
            // Create the marker for the first time
            currentUserLocationMarker = new Marker(
                    currentUserLatLong,
                    userMarkerBitmap,
                    0, // Horizontal offset
                    -userMarkerBitmap.getHeight() / 2 // Vertical offset to center the bottom
            );
            mapView.getLayerManager().getLayers().add(currentUserLocationMarker);
        } else {
            // Marker already exists, just update its position
            currentUserLocationMarker.setLatLong(currentUserLatLong);
        }

        // Optionally, move the map center to the new location
        // mapView.getModel().mapViewPosition.setCenter(currentUserLatLong);

        // Important: Request a redraw of the map layer containing the marker
        // If the marker is on the base tileRendererLayer, you might not need this explicitly
        // if other map interactions are happening. But for a standalone marker layer, it's good practice.
        // A simple way is to force a redraw of the specific layer or the whole map.
        // For Mapsforge, if the marker is in a layer, changing its LatLong
        // should trigger a redraw of that layer when the map view next renders.
        // If you find the marker isn't updating visually, you might need to call:
        // currentUserLocationMarker.requestRedraw(); or ensure the layer it's on is redrawn.
        // For simplicity with Mapsforge, often just updating the marker's LatLong is enough
        // if it's part of a layer that's actively being rendered.

        Log.d("TestActivity", "User location updated: " + currentUserLatLong.toString());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Stop location updates (already handled in onPause generally, but good for completeness)
        if (fusedLocationClient != null && locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }

        // Your existing Mapsforge cleanup
        if (tileRendererLayer != null && mapView != null) {
            mapView.getLayerManager().getLayers().remove(tileRendererLayer);
        }
        if (tileCache != null) {
            tileCache.destroy();
        }
        if (mapView != null) {
            mapView.destroyAll();
        }
        AndroidGraphicFactory.clearResourceMemoryCache();
    }


}