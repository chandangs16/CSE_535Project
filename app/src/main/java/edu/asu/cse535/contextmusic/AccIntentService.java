package gnayak2.msse.asu.edu.assignment1;

import android.app.IntentService;
import android.app.Service;
import android.content.Intent;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class AccIntentService  extends Service implements SensorEventListener {
    public final IBinder localBinder = new LocalBinder();
    public Handler handler;
    private Sensor sensor;
    private SensorManager sensorManager;
    long lastSaved;
    private long sensorReferenceTime;
    static int ACCE_FILTER_DATA_MIN_TIME = 1000;

    public AccIntentService(){

    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        if ((System.currentTimeMillis() - lastSaved) > ACCE_FILTER_DATA_MIN_TIME && handler !=null ) {
            lastSaved = System.currentTimeMillis();
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            Message msg = handler.obtainMessage();
            Bundle b = new Bundle();

            b.putString("timestamp", String.valueOf(lastSaved));
            b.putFloat("xvalue", x);
            b.putFloat("yvalue", y);
            b.putFloat("zvalue",z);
            msg.setData(b);
            handler.sendMessage(msg);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return localBinder;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public class LocalBinder extends Binder
    {
        public AccIntentService getInstance()
        {
            return AccIntentService.this;
        }
    }
    public void onCreate() {
        Log.w("Tag: ", "on create called");
        lastSaved = System.currentTimeMillis();
        sensorManager = (SensorManager)  getSystemService(Context.SENSOR_SERVICE);
        sensor  = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener((SensorEventListener) this, sensor, 1000000);
        sensorReferenceTime = System.currentTimeMillis();
    }

    public void setHandler(Handler handler)
    {
        this.handler = handler;
    }
}
