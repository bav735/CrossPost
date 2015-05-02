package bulgakov.arthur.crosspost;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

/**
 * A placeholder fragment containing a simple view.
 */
public class Fragment2 extends Fragment {
   private final static int FRAGMENT_NUM = 2;
   public static final String FRAGMENT_TAG = "2";

   public static Fragment newInstance() {
      return new Fragment2();
   }

   public Fragment2() {
   }

   @Override
   public View onCreateView(LayoutInflater inflater, ViewGroup container,
                            Bundle savedInstanceState) {
      setHasOptionsMenu(true);
      View rootView = inflater.inflate(R.layout.fragment2_layout, container, false);
      return rootView;
   }

   @Override
   public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
      FragmentNavigationDrawer mFNDrawer = (FragmentNavigationDrawer) getActivity()
              .getSupportFragmentManager().findFragmentById(R.id.fragment_navigation_drawer);
      if (mFNDrawer != null && mFNDrawer.isDrawerOpen())
         return;
      super.onCreateOptionsMenu(menu, inflater);
      inflater.inflate(R.menu.fragment2_menu, menu);
   }

   @Override
   public boolean onOptionsItemSelected(MenuItem item) {
      switch (item.getItemId()) {
         case R.id.fragment2_action:
            Toast.makeText(getActivity(), "action #2", Toast.LENGTH_SHORT).show();
            return true;
         default:
            return super.onOptionsItemSelected(item);
      }
   }

   @Override
   public void onAttach(Activity activity) {
      super.onAttach(activity);
//      ((ActivityMain) activity).onSectionAttached(FRAGMENT_NUM);
   }
}