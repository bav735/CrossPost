package bulgakov.arthur.crosspost;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


/**
 * Created by A on 28.04.2015.
 */
public class ServicePosting extends Service {
   //   public static final int MESSAGE_POST = 0;
//   public static final String MESSENGER_NAME = "messenger";
   public static final String SCHEDULED_POSTS = "scheduledPosts";
   public static final String NOTIFIED_DATE_STRING = "dateString";

//   public static Map<String, ScheduledPost> scheduledPosts;
   public static Map<String, ScheduledPost> allSPs;

//   class ComparatorSP implements Comparator<ScheduledPost> {
//      @Override
//      public int compare(ScheduledPost sp1, ScheduledPost sp2) {
//         if (sp2.date.after(sp1.date))
//            return -1;
//         if (sp1.date.after(sp2.date))
//            return -1;
//         return 0;
//      }
//   }

   private static NotificationManager notificationManager;
   private static Context context;
   private static SharedPreferences prefs;

   public static void sendNotification(String dateString) {
      NotificationCompat.Builder builder = getBuilder(dateString);
      builder.setAutoCancel(true);
      Notification notification = builder.build();
      notification.defaults |= Notification.DEFAULT_SOUND;
      notificationManager.notify(0, notification);
   }

   private static NotificationCompat.Builder getBuilder(String dateString) {
      int icon = R.drawable.ic_launcher;
      long when = System.currentTimeMillis();
      Intent intent = new Intent(context, ActivityMain.class);
      intent.putExtra(NOTIFIED_DATE_STRING, dateString);
      intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
              Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);

      PendingIntent pending = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

      return new NotificationCompat.Builder(context)
              .setContentText("Would you like to make a post?")
              .setContentTitle(Utils.stringFromRes(context, R.string.app_name))
              .setSmallIcon(icon)
              .setContentIntent(pending)
              .setWhen(when)
              .setSound(null);
   }

   public static void addNewSP(ScheduledPost sp) {
      allSPs.put(Utils.formatDate(sp.date), sp);
      sp.schedule();
      saveSPs();
      Log.d(ActivityMain.APP_TAG, "added sp, now set size = " + allSPs.size());
   }

   public static void saveSPs() {
      Set<String> setSP = new HashSet<>();
      for (Map.Entry<String, ScheduledPost> entry : ServicePosting.allSPs.entrySet())
         setSP.add(entry.getValue().toJsonString());
      prefs.edit().putStringSet(ServicePosting.SCHEDULED_POSTS, setSP).commit();
   }

   @Override
   public IBinder onBind(Intent intent) {
      return (null);
   }

   @Override
   public void onCreate() {
      Log.d(ActivityMain.APP_TAG, "onCreate Service");
      super.onCreate();
      context = getApplicationContext();
      notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
      prefs = PreferenceManager.getDefaultSharedPreferences(context);
      Set<String> setSP = prefs.getStringSet(SCHEDULED_POSTS, null);
      allSPs = new HashMap<>();
      if (setSP != null) {
         Log.d(ActivityMain.APP_TAG, "getting set from prefs, size = " + String.valueOf(setSP.size()));
         for (String stringSP : setSP) {
            ScheduledPost sp = new ScheduledPost().fromJsonString(stringSP);
            allSPs.put(Utils.formatDate(sp.date), sp);
            if (!sp.wasNotified)
               sp.schedule();
         }
//         saveSPs();
      }
   }

   @Override
   public int onStartCommand(Intent intent, int flags, int startId) {
      Log.d(ActivityMain.APP_TAG, "onStartCommand Service");
      super.onStartCommand(intent, flags, startId);
//      if (intent != null) {
//         Bundle extras = intent.getExtras();
//      }
      return START_STICKY;
   }
   //   private static Messenger messenger;

//   public class MessageHandler extends Handler {
//      @Override
//      public void handleMessage(Message message) {
//         int state = message.arg1;
//         switch (state) {
//            case MESSAGE_POST:
//               startActivity(new Intent(ServicePost.this, ActivityMain.class));
//         }
//      }
//   }
}