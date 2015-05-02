package bulgakov.arthur.crosspost;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.github.machinarius.preferencefragment.PreferenceFragment;

/**
 * A placeholder fragment containing a simple view.
 */
public class Fragment4 extends PreferenceFragment {
   private final static int FRAGMENT_NUM = 4;
   public static final String FRAGMENT_TAG = "4";

   public static Fragment newInstance() {
      return new Fragment4();
   }

   public Fragment4() {
   }

   @Override
   public void onCreate(Bundle paramBundle) {
      super.onCreate(paramBundle);
      addPreferencesFromResource(R.xml.fragment4_preferences);
   }

   /*@Override
   public View onCreateView(LayoutInflater inflater, ViewGroup container,
                            Bundle savedInstanceState) {
      setHasOptionsMenu(true);
      View rootView = inflater.inflate(R.layout.fragment4_layout, container, false);
      return rootView;
   }

   @Override
   public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
      FragmentNavigationDrawer mFNDrawer = (FragmentNavigationDrawer) getActivity()
              .getSupportFragmentManager().findFragmentById(R.id.fragment_navigation_drawer);
      if (mFNDrawer != null && mFNDrawer.isDrawerOpen())
         return;
      super.onCreateOptionsMenu(menu, inflater);
      inflater.inflate(R.menu.fragment4_menu, menu);
   }

   @Override
   public boolean onOptionsItemSelected(MenuItem item) {
      switch (item.getItemId()) {
         case R.id.fragment4_action:
            Toast.makeText(getActivity(), "action #4", Toast.LENGTH_SHORT).show();
            return true;
         default:
            return super.onOptionsItemSelected(item);
      }
   }

   @Override
   public void onAttach(Activity activity) {
      super.onAttach(activity);
//      ((ActivityMain) activity).onSectionAttached(FRAGMENT_NUM);
   }*/
}