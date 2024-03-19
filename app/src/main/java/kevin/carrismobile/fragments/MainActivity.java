package kevin.carrismobile.fragments;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.example.carrismobile.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import kevin.carrismobile.api.CarrisApi;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    public Fragment realTimeFragment;
    public Fragment routeDetailsFragment;
    public Fragment routesFragment;
    public Fragment stopsMapFragment;
    public Fragment stopDetailsFragment;
    public Fragment stopFavoritesFragment;
    public Fragment routeFavoritesFragment;
    public Fragment trainsFragment;
    public Fragment settingsFragment;
    public Fragment cpStationFrament;
    public Fragment metroStationFragment;
    public Fragment carrisStopDetailsFragment;
    public Fragment currentFragment = null;
    public List<Fragment> oldFragmentsList = new ArrayList<>();
    private HashMap<Fragment, Integer> mapper = new HashMap<>();
    public boolean stopOrRouteFavorite = false;
    int currentIndexFragment = 0;
    BottomNavigationView bottomAppBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bottomAppBar = findViewById(R.id.bottomAppBar);

        realTimeFragment = new RealTimeFragment();
        routeDetailsFragment = new RouteDetailsFragment();
        routesFragment = new RoutesFragment();
        stopsMapFragment = new StopsMapFragment();
        stopDetailsFragment = new StopDetailsFragment();
        stopFavoritesFragment = new StopFavoritesFragment();
        routeFavoritesFragment = new RouteFavoritesFragment();
        trainsFragment = new TrainsFragment();
        settingsFragment = new SettingsFragment();
        cpStationFrament = new CPStationFragment();
        metroStationFragment = new MetroStationFragment();
        carrisStopDetailsFragment = new CarrisStopDetailsFragment();

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.main_layout, routesFragment).commit();
        mapper.put(routesFragment, 2);
        initFragment(R.id.main_layout, settingsFragment, 2);
        initFragment(R.id.main_layout, cpStationFrament, 0);
        initFragment(R.id.main_layout, metroStationFragment, 0);
        initFragment(R.id.main_layout, stopFavoritesFragment, 1);
        initFragment(R.id.main_layout, routeFavoritesFragment, 1);
        initFragment(R.id.main_layout, stopDetailsFragment, 2);
        initFragment(R.id.main_layout, carrisStopDetailsFragment, 2);
        initFragment(R.id.main_layout, stopsMapFragment, 3);
        initFragment(R.id.main_layout, realTimeFragment, 4);
        initFragment(R.id.main_layout, routeDetailsFragment, 2);
        initFragment(R.id.main_layout, trainsFragment, 0);
        CarrisApi.init(MainActivity.this);
        currentIndexFragment = 2;
        currentFragment = routesFragment;
        oldFragmentsList.add(routesFragment);
        getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (oldFragmentsList.size() > 1) {
                    FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                    transaction.hide(currentFragment);
                    transaction.show(oldFragmentsList.get(oldFragmentsList.size() - 1));
                    currentFragment = oldFragmentsList.get(oldFragmentsList.size() - 1);
                    checkRightMenu(mapper.get(oldFragmentsList.get(oldFragmentsList.size() - 1)).intValue(), mapper.get(currentFragment).intValue());
                    oldFragmentsList.remove(oldFragmentsList.size() - 1);
                    transaction.commit();
                } else {
                    this.setEnabled(false);
                    onBackPressed();
                    this.setEnabled(true);
                }
            }
        });
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                bottomAppBar.getMenu().getItem(0).setChecked(false);
                bottomAppBar.getMenu().getItem(currentIndexFragment).setChecked(true);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.top_menu, menu);
        inflater.inflate(R.menu.bottom_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        bottomItemMenu(item);
        routeDetailsTopBar(item);
        stopDeitalsTopBar(item);
        topItemMenu(item);
        return super.onOptionsItemSelected(item);
    }

    public void openFragment(Fragment newFragment, int newIndexFragment, boolean animate) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        if (animate) {
            int oldIndexFragment = currentIndexFragment;
            this.currentIndexFragment = newIndexFragment;
            decideAnimation(transaction, oldIndexFragment, currentIndexFragment);
            checkRightMenu(oldIndexFragment, currentIndexFragment);
        }
        transaction.hide(currentFragment);
        transaction.show(newFragment);
        transaction.commit();
        if (oldFragmentsList.size() < 11) {
            oldFragmentsList.add(currentFragment);
        }
        currentFragment = newFragment;
    }

    private void initFragment(int layout, Fragment fragment, int menuIndex) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(layout, fragment);
        transaction.hide(fragment);
        transaction.commit();
        mapper.put(fragment, menuIndex);
    }

    private void slideRight(FragmentTransaction transaction) {
        transaction.setCustomAnimations(R.anim.slide_in, R.anim.fade_out); //slideRight
    }

    private void slideLeft(FragmentTransaction transaction) {
        transaction.setCustomAnimations(R.anim.fade_in, R.anim.slide_out); //slideLeft
    }

    private void decideAnimation(FragmentTransaction transaction, int oldIndex, int newIndex) {
        if (newIndex > oldIndex) {
            slideLeft(transaction);
        } else if (newIndex < oldIndex) {
            slideRight(transaction);
        } else {
            //nothing yet
        }
    }

    public void checkRightMenu(int oldIndexFragment, int currentIndexFragment) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                bottomAppBar.getMenu().getItem(oldIndexFragment).setChecked(false);
                bottomAppBar.getMenu().getItem(currentIndexFragment).setChecked(true);
            }
        });
    }

    private void bottomItemMenu(MenuItem item) {
        //Application bottomBar
        if (item.getItemId() == R.id.bottomitem1) {
            openFragment(trainsFragment, 0, true);
        } else if (item.getItemId() == R.id.bottomitem2) {
            openFragment(realTimeFragment, 4, true);
        } else if (item.getItemId() == R.id.bottomitem3) {
            openFragment(routesFragment, 2, true);
        } else if (item.getItemId() == R.id.bottomitem4) {
            openFragment(stopsMapFragment, 3, true);
        } else if (item.getItemId() == R.id.bottomitem5) {
            if (stopOrRouteFavorite) {
                stopOrRouteFavorite = false;
                openFragment(stopFavoritesFragment, 1, true);
            } else {
                stopOrRouteFavorite = true;
                openFragment(routeFavoritesFragment, 1, true);
            }
        }
    }

    private void standartTopMenu(MenuItem item) {
        if (item.getItemId() == R.id.topItem1) {
            openFragment(settingsFragment, 2, true);
        }
    }

    private void routeDetailsTopBar(MenuItem item) {
        //routeDetails topBar
        if (item.getItemId() == R.id.realTimeonDetails) {
            openFragment(realTimeFragment, 4, true);
            RouteDetailsFragment fragment = (RouteDetailsFragment) routeDetailsFragment;
            RealTimeFragment realTimeFragment1 = (RealTimeFragment) realTimeFragment;
            if (fragment.getCurrentCarreiraId() != null) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        realTimeFragment1.editText.setText(fragment.getCurrentCarreiraId().toString());
                        realTimeFragment1.button.performClick();
                    }
                });
            }
        } else if (item.getItemId() == R.id.routeDetailstopItem2) {
            RouteDetailsFragment fragment = (RouteDetailsFragment) this.routeDetailsFragment;
            fragment.addCurrentRouteToFavorites();
        }
    }

    private void stopDeitalsTopBar(MenuItem item) {
        //stopDetails topBar
        if (item.getItemId() == R.id.stopDetailstopItem2) {
            StopDetailsFragment stopDetails = (StopDetailsFragment) this.stopDetailsFragment;
            CarrisStopDetailsFragment carrisStopDetails = (CarrisStopDetailsFragment) this.carrisStopDetailsFragment;
            if (!stopDetails.isHidden()) {
                stopDetails.addCurrentStopToFavorites();
            } else if (!carrisStopDetails.isHidden()) {
                carrisStopDetails.addCurrentStopToFavorites();
            }
        }
    }

    private void topItemMenu(MenuItem item) {
        if (item.getItemId() == R.id.topItem1) {
            openFragment(settingsFragment, 2, false);
        } else if (item.getItemId() == R.id.topItem2) {
            if (oldFragmentsList.size() > 1) {
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.hide(currentFragment);
                transaction.show(oldFragmentsList.get(oldFragmentsList.size() - 1));
                currentFragment = oldFragmentsList.get(oldFragmentsList.size() - 1);
                checkRightMenu(mapper.get(oldFragmentsList.get(oldFragmentsList.size() - 1)).intValue(), mapper.get(currentFragment).intValue());
                oldFragmentsList.remove(oldFragmentsList.size() - 1);
                transaction.commit();
            }
        }
    }
}
