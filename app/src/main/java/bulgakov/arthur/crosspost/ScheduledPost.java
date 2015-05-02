package bulgakov.arthur.crosspost;

import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by A on 28.04.2015.
 */
public class ScheduledPost {
   private final static String POST_DATE = "date";
   private final static String POST_IMG_URI = "imgUri";
   private final static String POST_TEXT = "text";
   private final static String POST_SOCIAL_NETWORKS = "networkCBs";
   private final static String POST_WAS_NOTIFIED = "wasNotified";

   public Timer timer;
   public TimerTask task;
   public Date date;
   public Uri imgUri;
   public String text;
   public boolean[] networkCBs;
   public boolean wasNotified;

   public String toJsonString() {
      try {
         JSONObject jsonPost = new JSONObject();
         jsonPost.put(POST_DATE, Utils.formatDate(date));
         if (imgUri != null)
            jsonPost.put(POST_IMG_URI, imgUri.toString());
         jsonPost.put(POST_TEXT, text);
         jsonPost.put(POST_WAS_NOTIFIED, wasNotified);
         JSONArray jsonPSNs = new JSONArray();
         for (boolean networkCB : networkCBs) jsonPSNs.put(networkCB);
         jsonPost.put(POST_SOCIAL_NETWORKS, jsonPSNs);
         return jsonPost.toString();
      } catch (JSONException e) {
         Log.d(ActivityMain.APP_TAG, "couldn't toJson");
         return null;
      }
   }

   public ScheduledPost fromJsonString(String jsonString) {
      try {
         JSONObject jsonPost = new JSONObject(jsonString);
         date = Utils.parseDate(jsonPost.getString(POST_DATE));
         if (jsonPost.has(POST_IMG_URI))
            imgUri = Uri.parse(jsonPost.getString(POST_IMG_URI));
         text = jsonPost.getString(POST_TEXT);
         wasNotified = jsonPost.getBoolean(POST_WAS_NOTIFIED);
         JSONArray jsonPSNs = jsonPost.getJSONArray(POST_SOCIAL_NETWORKS);
         networkCBs = new boolean[jsonPSNs.toString().length()];
         for (int i = 0; i < jsonPSNs.length(); i++)
            networkCBs[i] = jsonPSNs.getBoolean(i);
         return this;
      } catch (Exception e) {
         Log.d(ActivityMain.APP_TAG, "couldn't fromJson (with errors) " + e.toString());
         return null;
      }
   }

   public void schedule() {
      timer = new Timer();
      task = new TimerTask() {
         @Override
         public void run() {
            String dateString = Utils.formatDate(date);
            ServicePosting.sendNotification(dateString);
            ServicePosting.allSPs.get(dateString).wasNotified = true;
            ServicePosting.saveSPs();
         }
      };
      timer.schedule(task, date);
   }
}
