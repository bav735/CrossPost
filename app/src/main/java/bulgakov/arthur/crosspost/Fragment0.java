package bulgakov.arthur.crosspost;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.github.gorbin.asne.core.SocialNetwork;
import com.github.gorbin.asne.core.SocialNetworkManager;
import com.github.gorbin.asne.core.listener.OnLoginCompleteListener;
import com.github.gorbin.asne.core.listener.OnPostingCompleteListener;
import com.github.gorbin.asne.facebook.FacebookSocialNetwork;
import com.github.gorbin.asne.linkedin.LinkedInSocialNetwork;
import com.github.gorbin.asne.twitter.TwitterSocialNetwork;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;

/**
 * A placeholder fragment containing a simple view.
 */
public class Fragment0 extends Fragment {
   public static final String SNM_TAG = "SocialIntegrationMain.SOCIAL_NETWORK_TAG";
   private final static int FRAGMENT_NUM = 0;
   public static final String FRAGMENT_TAG = "0";
   private final static int REQUEST_CODE_GALLERY = 1;
   private final static int REQUEST_CODE_CAMERA = 2;
   /**
    * SocialNetwork IDs in ASNE:
    * 1 - Twitter
    * 2 - LinkedIn
    * 3 - Google Plus
    * 4 - Facebook
    * 5 - Vkontakte
    * 6 - Odnoklassniki
    * 7 - Instagram
    */
   SharedPreferences prefs;
   private SocialNetworkManager snm;

   private RadioButton rbCamera;
   private RadioButton rbGallery;
   private RadioButton rbClear;
   private RadioButton rbRotate;
   private ImageView imageView;
   private EditText editText;
   private SeekBar seekBar;
   private TextView textView;
   private CheckBox[] networkCBs = new CheckBox[3];

   private int[] networkResIDs = {R.id.facebook, R.id.twitter, R.id.linkedin};
   private int[] networkID = {4, 1, 2};
   private String[] networkNames;
   private int[] networkNameResIDs;
   private Queue<Integer> networksToConnect;
   private Bitmap imgBitmap;
   private Uri imgUri;
   private Uri tempImgUri;
   private OnLoginCompleteListener loginCompleteListener = new OnLoginCompleteListener() {
      @Override
      public void onLoginSuccess(int i) {
         Log.d(ActivityMain.APP_TAG, "login succ");
         ActivityMain.hideProgress();
         networksToConnect.remove();
         checkIfAllNetworksConnected();
      }

      @Override
      public void onError(int networkId, String requestID, String errorMessage, Object data) {
         Log.d(ActivityMain.APP_TAG, "login err");
         ActivityMain.hideProgress();
         if (errorMessage.contains("Timestamp out of bounds"))
            getAlertDialogBuilder(getString(R.string.error),
                    getString(R.string.select_network_time), null)
                    .setPositiveButton("Got it", new DialogInterface.OnClickListener() {
                       @Override
                       public void onClick(DialogInterface dialog, int which) {
                          startActivity(new Intent(Settings.ACTION_DATE_SETTINGS));
                       }
                    }).show();
         else
            Toast.makeText(getActivity(), "ERROR: " + errorMessage, Toast.LENGTH_LONG).show();
      }
   };

   public static Fragment newInstance() {
      return new Fragment0();
   }

   public Fragment0() {
   }

   @Override
   public void onCreate(Bundle savedInstanceState) {
      Log.d(ActivityMain.APP_TAG, "Fragment0 onCreate");
      super.onCreate(savedInstanceState);
      //Get Keys for initiate SocialNetworks

      FragmentManager fm = getActivity().getSupportFragmentManager();
      //Use manager to manage SocialNetworks
      snm = (SocialNetworkManager) fm.findFragmentByTag(SNM_TAG);

      //Check if manager exist
      if (snm == null) {
         Log.d(ActivityMain.APP_TAG, "SNM new...");
         initSNM();
      } else if (!snm.getInitializedSocialNetworks().isEmpty()) {
         Log.d(ActivityMain.APP_TAG, "SNM inited networks...");
         List<SocialNetwork> socialNetworks = snm.getInitializedSocialNetworks();
         for (SocialNetwork socialNetwork : socialNetworks)
            socialNetwork.setOnLoginCompleteListener(loginCompleteListener);
      }
   }

   @Override
   public View onCreateView(LayoutInflater inflater, ViewGroup container,
                            Bundle savedInstanceState) {
      Log.d(ActivityMain.APP_TAG, "Fragment0 onCreatView");
      initNetworkNames();
      setHasOptionsMenu(true);
      prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
      View rootView = inflater.inflate(R.layout.fragment0_layout, container, false);
      // init buttons and set Listeners
      for (int i = 0; i < networkCBs.length; i++)
         networkCBs[i] = (CheckBox) rootView.findViewById(networkResIDs[i]);

      final View dialogView = inflater.inflate(R.layout.fragment0_dialog_add_img, null);
      final AlertDialog dialog = getAlertDialogBuilder(getString(R.string.select_image),
              null, dialogView).create();
      rbCamera = (RadioButton) dialogView.findViewById(R.id.fragment0_rb_camera);
      rbCamera.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View v) {
            new Thread(new Runnable() {
               @Override
               public void run() {
                  if (tempImgUri == null)
                     try {
                        File outputDir = new File(Utils.getTempDir(getActivity())); // context being the Activity pointer
                        outputDir.mkdirs();
                        File outputFile = File.createTempFile(Utils.formatDate(new Date()),
                                "jpg", outputDir);
                        tempImgUri = Utils.getUri(outputFile);
                     } catch (Exception e) {
                        Log.d(ActivityMain.APP_TAG, e.toString());
                     }
                  Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                  Bundle bundle = new Bundle();
                  bundle.putParcelable(MediaStore.EXTRA_OUTPUT, tempImgUri);
                  cameraIntent.putExtras(bundle);
                  startActivityForResult(cameraIntent, REQUEST_CODE_CAMERA);
                  dialog.dismiss();
               }
            }).start();
         }
      });
      rbGallery = (RadioButton) dialogView.findViewById(R.id.fragment0_rb_gallery);
      rbGallery.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View v) {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent,
                    getString(R.string.gallery_pick)), REQUEST_CODE_GALLERY);
            dialog.dismiss();
         }
      });
      rbClear = (RadioButton) dialogView.findViewById(R.id.fragment0_rb_clear);
      rbClear.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View v) {
            imgUri = null;
            imgBitmap = null;
            imageView.setImageDrawable(getResources().getDrawable(R.drawable.fragment0_add_image));
            dialog.dismiss();
         }
      });
      rbRotate = (RadioButton) dialogView.findViewById(R.id.fragment0_rb_rotate);
      rbRotate.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View v) {
            if (imgBitmap != null) {
               ActivityMain.showProgress("Rotating...");
               new Thread(new Runnable() {
                  @Override
                  public void run() {
                     imgBitmap = Utils.rotateBitmap(imgBitmap, 90);
                     getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                           if (imgBitmap != null)
                              imageView.setImageBitmap(imgBitmap);
                           ActivityMain.hideProgress();
                        }
                     });
                  }
               }).start();
            } else
               Log.d(ActivityMain.APP_TAG, "bitmap is null");
            dialog.dismiss();
         }
      });
      imageView = (ImageView) rootView.findViewById(R.id.fragment0_iv);
      if (imgBitmap == null)
         imageView.setImageDrawable(getResources().getDrawable(R.drawable.fragment0_add_image));
      imageView.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View v) {
            dialog.show();
         }
      });
      editText = (EditText) rootView.findViewById(R.id.fragment0_et);
      textView = (TextView) rootView.findViewById(R.id.fragment0_tv);
      seekBar = (SeekBar) rootView.findViewById(R.id.fragment0_sb);
      if (prefs.getBoolean(getString(R.string.remember_intervals), false))
         seekBar.setProgress(prefs.getInt(getString(R.string.remembered_interval), 0));
      else
         seekBar.setProgress(0);
      seekBar.setMax(77);
      seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
         @Override
         public void onStopTrackingTouch(SeekBar seekBar) {
         }

         @Override
         public void onStartTrackingTouch(SeekBar seekBar) {
         }

         @Override
         public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (progress == 0)
               textView.setText("Will be posted now");
            if (progress < 30 && progress > 0)
               textView.setText("Will be posted in " + String.valueOf(progress * 2) + " minutes");
            if (progress >= 30 && progress < 53)
               textView.setText("Will be posted in " + String.valueOf(progress - 29) + " hours");
            if (progress >= 53 && progress < 78)
               textView.setText("Will be posted in "
                       + String.valueOf((progress - 53) / 4 + 1) + " days");
            if (prefs.getBoolean(getString(R.string.remember_intervals), false))
               prefs.edit().putInt(getString(R.string.remembered_interval), progress).commit();
         }
      });

      return rootView;
   }

   private void initNetworkNames() {
      networkNameResIDs = new int[]{R.string.facebook,
              R.string.twitter,
              R.string.linkedIn,};
      networkNames = new String[networkNameResIDs.length];
      for (int i = 0; i < networkNames.length; i++)
         networkNames[i] = getString(networkNameResIDs[i]);
   }

   private void initSNM() {
      snm = new SocialNetworkManager();
      Context context = getActivity();
      String TWITTER_CONSUMER_KEY = context.getString(R.string.twitter_consumer_key);
      String TWITTER_CONSUMER_SECRET = context.getString(R.string.twitter_consumer_secret);
      String TWITTER_CALLBACK_URL = context.getString(R.string.callback);
      String LINKEDIN_CONSUMER_KEY = context.getString(R.string.linkedin_consumer_key);
      String LINKEDIN_CONSUMER_SECRET = context.getString(R.string.linkedin_consumer_secret);
      String LINKEDIN_CALLBACK_URL = context.getString(R.string.callback);

      //Chose permissions
      ArrayList<String> fbScope = new ArrayList<String>();
      fbScope.addAll(Arrays.asList("public_profile, email, user_friends"));
      String linkedInScope = "r_basicprofile+r_fullprofile+rw_nus+r_network+w_messages+r_emailaddress+r_contactinfo";

      //Init and add to manager FacebookSocialNetwork
      FacebookSocialNetwork fbNetwork = new FacebookSocialNetwork(this, fbScope);
      snm.addSocialNetwork(fbNetwork);

      //Init and add to manager TwitterSocialNetwork
      TwitterSocialNetwork twNetwork = new TwitterSocialNetwork(this, TWITTER_CONSUMER_KEY, TWITTER_CONSUMER_SECRET, TWITTER_CALLBACK_URL);
      snm.addSocialNetwork(twNetwork);

      //Init and add to manager LinkedInSocialNetwork
      LinkedInSocialNetwork liNetwork = new LinkedInSocialNetwork(this, LINKEDIN_CONSUMER_KEY, LINKEDIN_CONSUMER_SECRET, LINKEDIN_CALLBACK_URL, linkedInScope);
      snm.addSocialNetwork(liNetwork);

      getActivity().getSupportFragmentManager().beginTransaction().add(snm, SNM_TAG).commit();
      snm.setOnInitializationCompleteListener(new SocialNetworkManager.OnInitializationCompleteListener() {
         @Override
         public void onSocialNetworkManagerInitialized() {
            for (SocialNetwork socialNetwork : snm.getInitializedSocialNetworks()) {
               socialNetwork.setOnLoginCompleteListener(loginCompleteListener);
            }
         }
      });
   }

   @Override
   public void onActivityCreated(Bundle savedInstanceState) {
      Log.d(ActivityMain.APP_TAG, "Fragment0 onActivityCreated");
      super.onActivityCreated(savedInstanceState);
   }

   @Override
   public void onActivityResult(int requestCode, int resultCode, Intent data) {
      Log.d(ActivityMain.APP_TAG, "Fragment0 onActivityResult");
      super.onActivityResult(requestCode, resultCode, data);
      if (resultCode == Activity.RESULT_OK) {
         switch (requestCode) {
            case REQUEST_CODE_GALLERY:
               if (data != null) {
                  setNewUri(data.getData());
               }
               break;
            case REQUEST_CODE_CAMERA:
               Log.d(ActivityMain.APP_TAG, "camera code");
               if (tempImgUri != null) {
                  setNewUri(tempImgUri);
               }
               break;
            default:
               Fragment fragment = getActivity().getSupportFragmentManager()
                       .findFragmentByTag(SNM_TAG);
               if (fragment != null)
                  fragment.onActivityResult(requestCode, resultCode, data);
         }
      }
   }

   @Override
   public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
      FragmentNavigationDrawer mFNDrawer = (FragmentNavigationDrawer) getActivity()
              .getSupportFragmentManager().findFragmentById(R.id.fragment_navigation_drawer);
      if (mFNDrawer != null && mFNDrawer.isDrawerOpen())
         return;
      super.onCreateOptionsMenu(menu, inflater);
      inflater.inflate(R.menu.fragment0_menu, menu);
   }

   @Override
   public boolean onOptionsItemSelected(MenuItem item) {
      switch (item.getItemId()) {
         case R.id.fragment0_action_post:
            boolean noSelected = true;
            for (CheckBox networkCB : networkCBs)
               if (networkCB.isChecked())
                  noSelected = false;
            if (noSelected) {
               Toast.makeText(getActivity(), "Choose social networks to post to", Toast.LENGTH_LONG).show();
               return true;
            }
            if (imgUri == null && editText.getText().toString().isEmpty()) {
               Toast.makeText(getActivity(), "Add picture or text to post", Toast.LENGTH_LONG).show();
               return true;
            }
            if (imgBitmap != null)
               try {
                  imgUri = Utils.getUri(Utils.saveBitmap(imgBitmap, Utils.getTempDir(getActivity()),
                          Utils.formatDate(new Date())));
               } catch (IOException e) {
                  Log.d(ActivityMain.APP_TAG, "couldn't save bitmap - " + e.toString());
               }
            networksToConnect = new PriorityQueue<>();
            for (int i = 0; i < networkCBs.length; i++) {
               if (networkCBs[i].isChecked()) {
                  SocialNetwork socialNetwork = snm.getSocialNetwork(networkID[i]);
                  if (!socialNetwork.isConnected()) {
                     networksToConnect.add(i);
                  } else {
                     Log.d(ActivityMain.APP_TAG, networkNames[i] + " is already connected");
                  }
               }
            }
            checkIfAllNetworksConnected();
            return true;
         default:
            return super.onOptionsItemSelected(item);
      }

   }

   private void setNewUri(Uri newUri) {
      imgUri = newUri;
      new Thread(new Runnable() {
         @Override
         public void run() {
            String imgPath = Utils.getPath(getActivity(), imgUri);
            Log.d("!", "new path = " + imgPath);
            try {
               imgBitmap = Utils.getBitmapByPath(imgPath);
               getActivity().runOnUiThread(new Runnable() {
                  @Override
                  public void run() {
                     imageView.setImageBitmap(imgBitmap);
                  }
               });
            } catch (Exception e) {
               Log.d("!", e.toString());
            }
         }
      }).start();
   }

   private void checkIfAllNetworksConnected() {
      Log.d(ActivityMain.APP_TAG, "checking...");
      if (networksToConnect.isEmpty())
         addNewSP();
      else {
         int networkNum = networksToConnect.peek();
         SocialNetwork socialNetwork = snm.getSocialNetwork(networkID[networkNum]);
         socialNetwork.requestLogin(loginCompleteListener);
         ActivityMain.showProgress(getString(R.string.authorize) + " "
                 + networkNames[networkNum] + "...");
         Log.d(ActivityMain.APP_TAG, "requested login...");
      }
   }

   private void makePost() {
      for (int i = 0; i < networkCBs.length; i++) {
         if (networkCBs[i].isChecked()) {
            File imgFile = Utils.getFile(getActivity(), imgUri);
            final int networkNum = i;
            SocialNetwork socialNetwork = snm.getSocialNetwork(networkID[networkNum]);
            final String postText = editText.getText().toString();
            OnPostingCompleteListener listener = new OnPostingCompleteListener() {
               @Override
               public void onPostSuccessfully(int id) {
                  getActivity().runOnUiThread(new Runnable() {
                     @Override
                     public void run() {
                        Toast.makeText(getActivity(), "Post to "
                                        + networkNames[networkNum] + " done!",
                                Toast.LENGTH_LONG).show();
                        Log.d(ActivityMain.APP_TAG, "Post to " + networkNames[networkNum] + " done!");
                     }
                  });
               }

               @Override
               public void onError(int id, String s, final String error, Object o) {
                  getActivity().runOnUiThread(new Runnable() {
                     @Override
                     public void run() {
                        Toast.makeText(getActivity(), "Post to "
                                + networkNames[networkNum] + " failed! - "
                                + error, Toast.LENGTH_LONG).show();
                        Log.d(ActivityMain.APP_TAG, "Post to " + networkNames[networkNum] + " failed!" + error);
                     }
                  });
               }
            };
            if (imgFile != null) {
               if (socialNetwork.getID() != LinkedInSocialNetwork.ID)
                  socialNetwork.requestPostPhoto(imgFile, postText + " " + getString(R.string.post_by_app),
                          listener);
               else
                  Toast.makeText(getActivity(), "Post photo to LinkedIn " +
                          "is not allowed, try to share link instead", Toast.LENGTH_LONG).show();
            } else {
               socialNetwork.requestPostMessage(postText + " " + getString(R.string.post_by_app),
                       listener);
            }
         }
      }
   }

   private void addNewSP() {
      Log.d(ActivityMain.APP_TAG, "scheduling post...");
      if (seekBar.getProgress() == 0)
         makePost();
      else {
         ScheduledPost sp = new ScheduledPost();
         sp.wasNotified = false;
         sp.date = getScheduledDate(seekBar.getProgress());
         Log.d(ActivityMain.APP_TAG, "scheduled date = " + sp.date);
         sp.imgUri = imgUri;
         sp.text = editText.getText().toString();
         sp.networkCBs = new boolean[networkCBs.length];
         for (int i = 0; i < networkCBs.length; i++)
            sp.networkCBs[i] = networkCBs[i].isChecked();
         ServicePosting.addNewSP(sp);
//         ServicePosting.saveSPs();
         Toast.makeText(getActivity(), "Post was scheduled", Toast.LENGTH_SHORT).show();
      }
   }

   @Override
   public void onAttach(Activity activity) {
      super.onAttach(activity);
//      ((ActivityMain) activity).onSectionAttached(FRAGMENT_NUM);
   }

   private Date getScheduledDate(int progress) {
      Date currentDate = new Date();
      long SECOND = 1000;
      long MINUTE = 60 * SECOND;
      long HOUR = 60 * MINUTE;
      long DAY = 24 * HOUR;
      //TODO remove this debug feature
      if (progress == 77)
         return new Date(currentDate.getTime() + SECOND * 7);
      //--
      if (progress < 30 && progress > 0)
         return new Date(currentDate.getTime() + MINUTE * progress * 2);
      if (progress >= 30 && progress < 53)
         return new Date(currentDate.getTime() + HOUR * (progress - 29));
      if (progress >= 53 && progress < 78)
         return new Date(currentDate.getTime() + DAY * ((progress - 53) / 4 + 1));
      return null;
   }

   private AlertDialog.Builder getAlertDialogBuilder(String title, String message, View view) {
      return new AlertDialog.Builder(getActivity())
              .setTitle(title).setMessage(message).setView(view);
   }

   ///DO WE NEED THIS 2 OVERRIDES OR NOT?
   @Override
   public void onResume() {
      Log.d(ActivityMain.APP_TAG, "onResume Fragment0");
      super.onResume();
      boolean isNothingChecked = true;
      for (CheckBox cb : networkCBs)
         if (cb.isChecked())
            isNothingChecked = false;
      if (isNothingChecked)
         for (int i = 0; i < networkCBs.length; i++)
            networkCBs[i].setChecked(prefs.getBoolean(networkNames[i], false));
      //WHY THIS NEEDED?
      if (null == snm || snm.getInitializedSocialNetworks().isEmpty())
         snm = (SocialNetworkManager) getFragmentManager().findFragmentByTag(SNM_TAG);

      if (ActivityMain.notifiedDateString != null) {
         final ScheduledPost sp = ServicePosting.allSPs.get(ActivityMain.notifiedDateString);
         if (sp != null) {
            if (sp.imgUri != null)
               imageView.setImageURI(sp.imgUri);
            seekBar.setProgress(0);
            for (int i = 0; i < networkCBs.length; i++)
               networkCBs[i].setChecked(sp.networkCBs[i]);
            editText.setText(sp.text);
         }
      }

   }

   @Override
   public void onPause() {
      super.onPause();
      if (isRemoving() || getActivity().isFinishing()/* or check for change rotation event*/) {
         if (null != snm && snm.isAdded())
            getFragmentManager().beginTransaction().remove(snm).commit();
         snm = null;
      }
   }
}