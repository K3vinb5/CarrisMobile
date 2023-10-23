package com.example.carrismobile;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.google.android.material.bottomappbar.BottomAppBar;

public class MainActivity extends AppCompatActivity {

    Fragment realTimeFragment = new RealTimeFragment();
    Fragment routeDetailsFragment = new RouteDetailsFragment();
    Fragment routesFragment = new RoutesFragment();
    Fragment currentFragment = null;
    int currentIndexFragment = 0;
    BottomAppBar bottomAppBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        bottomAppBar = findViewById(R.id.bottomAppBar);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.main_layout, routeDetailsFragment);
        transaction.add(R.id.main_layout, realTimeFragment);
        transaction.add(R.id.main_layout, routesFragment);
        transaction.hide(realTimeFragment);
        transaction.hide(routeDetailsFragment);
        currentFragment = routesFragment;

        transaction.commit();
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
        if (item.getItemId() == R.id.bottomitem1) {
            openRouteDetailFragment();
            return true;
        }else if(item.getItemId() == R.id.bottomitem2){
            openRealTimeAFragment();
            return true;
        }else if(item.getItemId() == R.id.bottomitem3){
            openRouteFragment();
            return true;
        }else if (item.getItemId() == R.id.realTimeonDetails){
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
        return super.onOptionsItemSelected(item);
    }

    public void openRealTimeAFragment(){
        int oldIndexFragment = currentIndexFragment;
        currentIndexFragment = 0; //keeps track of current fragment
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        decideAnimation(transaction, oldIndexFragment, currentIndexFragment);
        transaction.hide(currentFragment);
        transaction.show(realTimeFragment);
        transaction.commit();
        currentFragment = realTimeFragment;
    }

    public void openRouteDetailFragment(){
        int oldIndexFragment = currentIndexFragment;
        currentIndexFragment = 2; //keeps track of current fragment
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        decideAnimation(transaction, oldIndexFragment, currentIndexFragment);
        transaction.hide(currentFragment);
        transaction.show(routeDetailsFragment);
        transaction.commit();
        currentFragment = routeDetailsFragment;
    }

    public void openRouteFragment(){
        int oldIndexFragment = currentIndexFragment;
        currentIndexFragment = 1; //keeps track of current fragment
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        decideAnimation(transaction, oldIndexFragment, currentIndexFragment);
        transaction.hide(currentFragment);
        transaction.show(routesFragment);
        transaction.commit();
        currentFragment = routesFragment;
    }

    private void slideRight(FragmentTransaction transaction){
        transaction.setCustomAnimations(R.anim.slide_in, R.anim.fade_out, R.anim.fade_in, R.anim.slide_out); //slideRight
    }

    private void slideLeft(FragmentTransaction transaction){
        transaction.setCustomAnimations(R.anim.fade_in, R.anim.slide_out, R.anim.slide_in, R.anim.fade_out);
    }

    private void decideAnimation(FragmentTransaction transaction, int oldIndex, int newIndex){
        if (newIndex > oldIndex){
            slideRight(transaction);
        }else if (newIndex < oldIndex){
            slideLeft(transaction);
        }else{
            //nothing
        }
    }

}
