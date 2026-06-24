package info.fortheease.monastery360;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import com.panoramagl.PLManager;
import com.panoramagl.PLSphericalPanorama;

public class GyroPanoramaController implements SensorEventListener {
    private final SensorManager sensorManager;
    private final Sensor rotationVectorSensor;
    private final PLManager plManager;

    private final float[] rotationMatrix = new float[9];
    float[] remappedMatrix = new float[9];
    private final float[] orientationAngles = new float[3];

    public GyroPanoramaController(Context context, PLManager plManager) {
        this.plManager = plManager;
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        rotationVectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
    }

    public void start() {
        sensorManager.registerListener(this, rotationVectorSensor, SensorManager.SENSOR_DELAY_GAME);
    }

    public void stop() {
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
            // Convert rotation vector to rotation matrix
            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values);

            SensorManager.remapCoordinateSystem(
                    rotationMatrix,
                    SensorManager.AXIS_X,
                    SensorManager.AXIS_Z,
                    remappedMatrix
            );
            // Convert rotation matrix to orientation (radians)
            SensorManager.getOrientation(remappedMatrix, orientationAngles);

            float azimuth = (float) Math.toDegrees(orientationAngles[0]); // yaw (left-right)
            float pitch   = (float) Math.toDegrees(orientationAngles[1]); // up-down
            // float roll = (float) Math.toDegrees(orientationAngles[2]);  // tilt

            // Apply to Panorama camera
            if (plManager.getPanorama() != null) {
                PLSphericalPanorama panorama = (PLSphericalPanorama) plManager.getPanorama();
                panorama.getCamera().setYaw(azimuth);   // invert if needed
                panorama.getCamera().setPitch(-pitch);
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}
}
