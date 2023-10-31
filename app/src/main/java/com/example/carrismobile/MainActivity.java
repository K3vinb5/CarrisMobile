package com.example.carrismobile;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.bottomnavigation.BottomNavigationItemView;
import com.google.android.material.bottomnavigation.BottomNavigationMenuView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity {

    public Fragment realTimeFragment = new RealTimeFragment();
    public Fragment routeDetailsFragment = new RouteDetailsFragment();
    public Fragment routesFragment = new RoutesFragment();
    public Fragment stopsMapFragment = new StopsMapFragment();
    public Fragment stopDetailsFragment = new StopDetailsFragment();
    public Fragment stopFavoritesFragment = new StopFavoritesFragment();
    public Fragment routeFavoritesFragment = new RouteFavoritesFragment();
    public Fragment currentFragment = null;
    public boolean routeDetailsFavorite = true;
    public boolean stopDetailsFavorite = true;
    int currentIndexFragment = 0;
    BottomNavigationView bottomAppBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bottomAppBar = findViewById(R.id.bottomAppBar);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.main_layout, routeDetailsFragment);
        transaction.add(R.id.main_layout, realTimeFragment);
        transaction.add(R.id.main_layout, routesFragment);
        transaction.add(R.id.main_layout, stopsMapFragment);
        transaction.add(R.id.main_layout, stopDetailsFragment);
        transaction.add(R.id.main_layout, stopFavoritesFragment);
        transaction.add(R.id.main_layout, routeFavoritesFragment);
        transaction.hide(realTimeFragment);
        transaction.hide(routeDetailsFragment);
        transaction.hide(stopsMapFragment);
        transaction.hide(stopDetailsFragment);
        transaction.hide(stopFavoritesFragment);
        transaction.hide(routeFavoritesFragment);
        currentIndexFragment = 2;
        currentFragment = routesFragment;
        //transaction.hide(routesFragment);
        transaction.commit();
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
        //Application bottomBar
        if (item.getItemId() == R.id.bottomitem1) {
            if(routeDetailsFavorite){
                openRouteFavoritesFragment(true);
            }else{
                openRouteDetailsFragment(true);
            }
            return super.onOptionsItemSelected(item);
        }else if(item.getItemId() == R.id.bottomitem2){
            openRealTimeAFragment();
            return super.onOptionsItemSelected(item);
        }else if(item.getItemId() == R.id.bottomitem3){
            openRouteFragment();
            return super.onOptionsItemSelected(item);
        }else if(item.getItemId() == R.id.bottomitem4){
            openstopsMapFragment();
        }else if(item.getItemId() == R.id.bottomitem5){
            if (stopDetailsFavorite){
                openstopFavoritesFragment(true);
            }else{
                openstopDetailsFragment(true);
            }
        }
        //routeDetails topBar
        if (item.getItemId() == R.id.realTimeonDetails){
            openRealTimeAFragment();
            RouteDetailsFragment fragment = (RouteDetailsFragment) routeDetailsFragment;
            RealTimeFragment realTimeFragment1 = (RealTimeFragment) realTimeFragment;
            if (fragment.getCurrentCarreiraId() != null){
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        realTimeFragment1.editText.setText(fragment.getCurrentCarreiraId().toString());
                        realTimeFragment1.button.performClick();
                    }
                });
            }
            return true;
        }

        //stopdetailsbar
        if(item.getItemId() == R.id.stopDetailstopItem2){
            openstopFavoritesFragment(false);
        }

        //stopfavoritesbar
        if(item.getItemId() == R.id.stopFavoritesItem2){
            openstopDetailsFragment(false);
        }
        //routedetailsbar
        if(item.getItemId() == R.id.routeDetailstopItem2){
            openRouteFavoritesFragment(false);
        }
        //routefavoritesbar
        if(item.getItemId() == R.id.routeFavoritesitem1){
            openRouteDetailsFragment(false);
        }
        return super.onOptionsItemSelected(item);
    }

    public void openRealTimeAFragment(){
        int oldIndexFragment = currentIndexFragment;
        currentIndexFragment = 4; //keeps track of current fragment
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        decideAnimation(transaction, oldIndexFragment, currentIndexFragment);
        transaction.hide(currentFragment);
        transaction.show(realTimeFragment);
        transaction.commit();
        currentFragment = realTimeFragment;
        checkRightMenu(oldIndexFragment, currentIndexFragment);
    }

    public void openRouteDetailsFragment(boolean animate){
        routeDetailsFavorite = false;
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        if (animate){
            int oldIndexFragment = currentIndexFragment;
            currentIndexFragment = 0; //keeps track of current fragment
            decideAnimation(transaction, oldIndexFragment, currentIndexFragment);
            checkRightMenu(oldIndexFragment, currentIndexFragment);
        }
        transaction.hide(currentFragment);
        transaction.show(routeDetailsFragment);
        transaction.commit();
        currentFragment = routeDetailsFragment;
    }

    public void openRouteFavoritesFragment(boolean animate){
        routeDetailsFavorite = true;
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        if (animate){
            int oldIndexFragment = currentIndexFragment;
            currentIndexFragment = 0; //keeps track of current fragment
            decideAnimation(transaction, oldIndexFragment, currentIndexFragment);
            checkRightMenu(oldIndexFragment, currentIndexFragment);
        }
        transaction.hide(currentFragment);
        transaction.show(routeFavoritesFragment);
        transaction.commit();
        currentFragment = routeFavoritesFragment;
    }

    public void openRouteFragment(){
        int oldIndexFragment = currentIndexFragment;
        currentIndexFragment = 2; //keeps track of current fragment
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        decideAnimation(transaction, oldIndexFragment, currentIndexFragment);
        transaction.hide(currentFragment);
        transaction.show(routesFragment);
        transaction.commit();
        currentFragment = routesFragment;
        checkRightMenu(oldIndexFragment, currentIndexFragment);
    }

    public void openstopsMapFragment(){
        int oldIndexFragment = currentIndexFragment;
        currentIndexFragment = 3; //keeps track of current fragment
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        decideAnimation(transaction, oldIndexFragment, currentIndexFragment);
        transaction.hide(currentFragment);
        transaction.show(stopsMapFragment);
        transaction.commit();
        currentFragment = stopsMapFragment;
        checkRightMenu(oldIndexFragment, currentIndexFragment);
    }

    public void openstopDetailsFragment(boolean animate){
        stopDetailsFavorite = false;

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        if (animate){
            int oldIndexFragment = currentIndexFragment;
            currentIndexFragment = 1; //keeps track of current fragment
            decideAnimation(transaction, oldIndexFragment, currentIndexFragment);
            checkRightMenu(oldIndexFragment, currentIndexFragment);
        }
        transaction.hide(currentFragment);
        transaction.show(stopDetailsFragment);
        transaction.commit();
        currentFragment = stopDetailsFragment;
    }
    public void openstopFavoritesFragment(boolean animate){
        stopDetailsFavorite = true;
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        if (animate){
            int oldIndexFragment = currentIndexFragment;
            currentIndexFragment = 1; //keeps track of current fragment
            decideAnimation(transaction, oldIndexFragment, currentIndexFragment);
            checkRightMenu(oldIndexFragment, currentIndexFragment);
        }
        transaction.hide(currentFragment);
        transaction.show(stopFavoritesFragment);
        transaction.commit();
        currentFragment = stopFavoritesFragment;
    }

    private void slideRight(FragmentTransaction transaction){
        transaction.setCustomAnimations(R.anim.slide_in, R.anim.fade_out, R.anim.fade_in, R.anim.slide_out); //slideRight
    }

    private void slideLeft(FragmentTransaction transaction){
        transaction.setCustomAnimations(R.anim.fade_in, R.anim.slide_out, R.anim.slide_in, R.anim.fade_out);
    }

    private void decideAnimation(FragmentTransaction transaction, int oldIndex, int newIndex){
        if (newIndex > oldIndex){
            slideLeft(transaction);
        }else if (newIndex < oldIndex){
            slideRight(transaction);
        }else{
            //nothing
        }
    }

    public void checkRightMenu(int oldIndexFragment, int currentIndexFragment){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                bottomAppBar.getMenu().getItem(oldIndexFragment).setChecked(false);
                bottomAppBar.getMenu().getItem(currentIndexFragment).setChecked(true);
            }
        });
    }
}
