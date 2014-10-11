package com.baochu.androidassignment.notification;

import com.baochu.androidassignment.Utils;
import com.baochu.androidassignment.login.MainActivity;
import com.baochu.assignment.R;
import com.igexin.sdk.PushConsts;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

public class IGetuiBroadcastReceiver extends BroadcastReceiver {
    
    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();
        Log.d("IGetuiBroadcastReceiver", "########### onReceive() action=" + bundle.getInt("action") + "#########");
        
        switch (bundle.getInt(PushConsts.CMD_ACTION)) {
        case PushConsts.GET_CLIENTID:
            String cid = bundle.getString("clientid");
            if (cid != null && cid.length() > 0) {
                IGetuiActivity.setClientId(cid);
                Log.d("IGetuiBroadcastReceiver", "########### clientid =" + cid + "###########");
            }
            break;
        case PushConsts.GET_MSG_DATA:
            byte[] payload = bundle.getByteArray("payload");
            if (payload != null) {
                String data = new String(payload);
                sendNotification(context, data);
            }
            break;
        default:
            break;
        }
    }
    
    /** Issues a notification to inform the user */
    private void sendNotification(Context context, String msg) {
        int messageId = Utils.getMessageId();
        NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        Intent intent = new Intent(context, MainActivity.class);
        // set intent so it does not start a new activity
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra(GcmActivity.EXTRA_MESSAGE, msg);
        PendingIntent contentIntent = PendingIntent.getActivity(context, messageId, intent, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
        .setSmallIcon(R.drawable.ic_stat_gcm)
        .setContentTitle(context.getString(R.string.app_name))
        .setStyle(new NotificationCompat.BigTextStyle()
        .bigText(msg))
        .setContentText(msg)
        .setDefaults(Notification.DEFAULT_SOUND)
        .setAutoCancel(true);

        builder.setContentIntent(contentIntent);
        notificationManager.notify(messageId, builder.build());
    }
}
