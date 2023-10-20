package com.example.carrismobile;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity {

    Fragment realTimeFragment = new RealTimeFragment();
    Fragment routeFragment = new RouteDetailsFragment();
    int currentFragment = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.main_layout, routeFragment);
        transaction.add(R.id.main_layout, realTimeFragment);
        transaction.hide(realTimeFragment);
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
        }
        return super.onOptionsItemSelected(item);
    }

    public void openRealTimeAFragment(){
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.hide(routeFragment);
        transaction.show(realTimeFragment);
        transaction.commit();
        currentFragment = 0; //keeps track of current fragment
    }

    public void openRouteDetailFragment(){
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.hide(realTimeFragment);
        transaction.show(routeFragment);
        transaction.commit();
        currentFragment = 1; //keeps track of current fragment
    }

}
