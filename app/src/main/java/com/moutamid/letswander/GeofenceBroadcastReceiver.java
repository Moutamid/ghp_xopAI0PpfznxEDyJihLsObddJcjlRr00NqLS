package com.moutamid.letswander;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.Location;
import android.os.IBinder;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.fxn.stash.Stash;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
import com.moutamid.letswander.activities.MapsActivity;
import com.moutamid.letswander.helper.NotificationHelper;
import com.moutamid.letswander.service.TtsService;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class GeofenceBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = "GeofenceBroadcastReceiv";
    TextToSpeech textToSpeech;
    Context context;
    String desc;

    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;
        NotificationHelper notificationHelper = new NotificationHelper(context);

        Log.d(TAG, "onReceive");

        if (intent != null) {
            GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
            if (geofencingEvent != null) {
                int transitionType = geofencingEvent.getGeofenceTransition();
                switch (transitionType) {
                    case Geofence.GEOFENCE_TRANSITION_ENTER:
                        Log.d(TAG, "GEOFENCE_TRANSITION_ENTER");
                        String snippet = getLocationName(geofencingEvent.getTriggeringGeofences());
                        desc = snippet;
                        Intent ttsIntent = new Intent(context, TtsService.class);
                        ttsIntent.putExtra("description", desc);
                        context.startService(ttsIntent);
                       // ContextCompat.startForegroundService(context, ttsIntent);
                        context.getApplicationContext().bindService(ttsIntent, ttsServiceConnection, Context.BIND_AUTO_CREATE);
                        notificationHelper.sendHighPriorityNotification("GEOFENCE_TRANSITION_ENTER", desc, MapsActivity.class);
                        break;
                    case Geofence.GEOFENCE_TRANSITION_DWELL:
                        Toast.makeText(context, "GEOFENCE_TRANSITION_DWELL", Toast.LENGTH_SHORT).show();
                        // notificationHelper.sendHighPriorityNotification("GEOFENCE_TRANSITION_DWELL", "", MapsActivity.class);
                        break;
                    case Geofence.GEOFENCE_TRANSITION_EXIT:
                        Toast.makeText(context, "GEOFENCE_TRANSITION_EXIT", Toast.LENGTH_SHORT).show();
                        // notificationHelper.sendHighPriorityNotification("GEOFENCE_TRANSITION_EXIT", "", MapsActivity.class);
                        break;
                }
            } else {
                Log.e(TAG, "GeofencingEvent is null");
            }
        } else {
            Log.e(TAG, "Received null intent");
        }
    }

    private ServiceConnection ttsServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            // This method is called when the service is connected.
            // You can interact with the service here, e.g., communicate with it using the binder.
            Log.e(TAG, "onServiceConnected");
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            // This method is called when the service is unexpectedly disconnected.
            // You should handle any necessary cleanup or reconnection logic here.
            Log.e(TAG, "onServiceDisconnected");
        }
    };


    private String getLocationName(List<Geofence> geofences) {
        String locationName = "";
        Map<String, String> map = (Map<String, String>) Stash.getObject(Constants.GEOFENCE, HashMap.class);

        if (!geofences.isEmpty()) {
            for (Geofence geofence: geofences) {
                locationName = map.get(geofence.getRequestId());
            }
        }
        return locationName;
    }
}
