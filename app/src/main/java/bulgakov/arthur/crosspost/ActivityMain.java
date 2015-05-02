package bulgakov.arthur.crosspost;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;


public class ActivityMain extends ActionBarActivity
        implements FragmentNavigationDrawer.NavigationDrawerCallbacks {
   public final static String APP_TAG = "cross_post_tag";

   public static boolean fragmentsInited = false;
   public static String[] fragmentTitles;
   private Fragment[] fragments;
   private String[] fragmentTags;
   private FragmentNavigationDrawer fragmentNavigationDrawer;

   private CharSequence actionBarTitle;
   public static String notifiedDateString;

   @Override
   protected void onNewIntent(Intent intent) {
      Log.d(ActivityMain.APP_TAG, "new intent to ActivityMain");
      notifiedDateString = intent.getStringExtra(ServicePosting.NOTIFIED_DATE_STRING);
   }

   @Override
   protected void onResume() {
      Log.d(ActivityMain.APP_TAG, "onResume ActivityMain");
      super.onResume();
   }

   @Override
   protected void onCreate(Bundle savedInstanceState) {
      Log.d(ActivityMain.APP_TAG, "ActivityMain onCreate");
      super.onCreate(savedInstanceState);

      context = this;
      initFragmentArrays();
      setContentView(R.layout.activity_main);

      FragmentManager fm = getSupportFragmentManager();
      fm.addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
         @Override
         public void onBackStackChanged() {
            FragmentManager fm = getSupportFragmentManager();
            if (fm != null) {
               Fragment fragment = fm.findFragmentById(R.id.fragment_container);
               if (fragment != null)
                  updateTitle(fragment);
               else
                  finish();
            }
         }
      });
      for (int pos = fragments.length - 1; pos >= 0; pos--) {
         //DEAL WITH ROTATIONS!
         if (fm.findFragmentByTag(fragmentTags[pos]) == null) {
            FragmentTransaction ft = fm.beginTransaction();
            ft.replace(R.id.fragment_container, fragments[pos], fragmentTags[pos]).commit();
         }
      }
      fragmentsInited = true;

      fragmentNavigationDrawer = (FragmentNavigationDrawer)
              getSupportFragmentManager().findFragmentById(R.id.fragment_navigation_drawer);
      DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
      fragmentNavigationDrawer.setUp(R.id.fragment_navigation_drawer, drawerLayout);
      actionBarTitle = fragmentTitles[0];

      Intent serviceIntent = new Intent(this, ServicePosting.class);
      //      serviceIntent.putExtra("MESSENGER", new Messenger(new ServicePost.MessageHandler()));
      startService(serviceIntent);
      notifiedDateString = getIntent().getStringExtra(ServicePosting.NOTIFIED_DATE_STRING);
   }

   private void updateTitle(Fragment fragment) {
      String fragmentClassName = fragment.getClass().getName();
      for (int i = 0; i < fragments.length; i++)
         if (fragmentClassName.equals(fragments[i].getClass().getName()))
            actionBarTitle = fragmentTitles[i];
   }

   private void initFragmentArrays() {
      fragments = new Fragment[]{Fragment0.newInstance(),
              Fragment1.newInstance(),
              Fragment2.newInstance(),
              Fragment3.newInstance(),
              Fragment4.newInstance(),};
      fragmentTags = new String[]{Fragment0.FRAGMENT_TAG,
              Fragment1.FRAGMENT_TAG,
              Fragment2.FRAGMENT_TAG,
              Fragment3.FRAGMENT_TAG,
              Fragment4.FRAGMENT_TAG,};
      int[] fragmentNamesResId = new int[]{R.string.title_section0,
              R.string.title_section1,
              R.string.title_section2,
              R.string.title_section3,
              R.string.title_section4,};
      fragmentTitles = new String[fragments.length];
      for (int i = 0; i < fragments.length; i++) {
         fragmentTitles[i] = getString(fragmentNamesResId[i]);
      }
   }

   @Override
   public void onNavigationDrawerItemSelected(int pos) {
      Log.d(ActivityMain.APP_TAG, "onNavigationDrawerItemSelected, #" + String.valueOf(pos));
      if (!fragmentsInited)
         return;
      FragmentManager fm = getSupportFragmentManager();
      Fragment fragment = fm.findFragmentByTag(fragmentTags[pos]);
      if (fragment != null)
         fm.beginTransaction().replace(R.id.fragment_container, fragment).addToBackStack(null).commit();
      else
         fm.beginTransaction().replace(R.id.fragment_container, fragments[pos], fragmentTags[pos])
                 .addToBackStack(null).commit();
      actionBarTitle = fragmentTitles[pos];
   }

//   public void onSectionAttached(int position) {
//      actionBarTitle = fragmentTitles[position];
//   }

   public void restoreActionBar() {
      ActionBar actionBar = getSupportActionBar();
      actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
      actionBar.setDisplayShowTitleEnabled(true);
      actionBar.setTitle(actionBarTitle);
   }

   @Override
   protected void onActivityResult(int requestCode, int resultCode, Intent data) {
      super.onActivityResult(requestCode, resultCode, data);
      Log.d(ActivityMain.APP_TAG, "ActivityMain onActivityResult");
      String tag = findFragmentTagByTitle();
      if (tag != null) {
//         Log.d("!", tag);
         Fragment fragment = getSupportFragmentManager()
                 .findFragmentByTag(tag);
         if (fragment != null)
            fragment.onActivityResult(requestCode, resultCode, data);
         else
            Toast.makeText(this, "fragment is null activity on result",
                    Toast.LENGTH_LONG).show();
      } else
         Toast.makeText(this, "tag is null activity on result",
                 Toast.LENGTH_LONG).show();
   }

   @Override
   public boolean onCreateOptionsMenu(Menu menu) {
      // if the drawer is not showing
      if (!fragmentNavigationDrawer.isDrawerOpen())
         restoreActionBar();
      return true;
//      return super.onCreateOptionsMenu(menu);
   }

   @Override
   public boolean onOptionsItemSelected(MenuItem item) {
      // Handle action bar item clicks here. The action bar will
      // automatically handle clicks on the Home/Up button, so long
      // as you specify a parent activity in AndroidManifest.xml.
      return super.onOptionsItemSelected(item);
   }

   @Override
   protected void onDestroy() {
      Log.d(ActivityMain.APP_TAG, "ActivityMain onDestroy");
      super.onDestroy();
   }

   private static ProgressDialog pd;
   private static Context context;

   protected static void showProgress(String message) {
      pd = new ProgressDialog(context);
      pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
      pd.setMessage(message);
      pd.setCancelable(false);
      pd.setCanceledOnTouchOutside(false);
      pd.show();
   }

   protected static void hideProgress() {
      pd.dismiss();
   }

   private String findFragmentTagByTitle() {
      for (int i = 0; i < fragments.length; i++)
         if (fragmentTitles[i].equals(actionBarTitle))
            return fragmentTags[i];
      return null;
   }
}
