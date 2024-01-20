package com.example.greeting_screen;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.appcompat.app.AppCompatActivity;

import com.example.greeting_screen.databinding.ActivityMenuBinding;

public class MenuActivity extends AppCompatActivity {

    private ActivityMenuBinding binding;

    private DrawerLayout drawerLayout;
    private GestureDetector gestureDetector;
    private LinearLayout profileDrawer;
    private LinearLayout guestListDrawer;

    private Fragment currentFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMenuBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String userEmail = getIntent().getStringExtra("userEmail");

        Bundle bundle = new Bundle();
        bundle.putString("userEmail", userEmail);

        drawerLayout = findViewById(R.id.drawer_layout);
        profileDrawer = findViewById(R.id.navigation_drawer_profile);
        guestListDrawer = findViewById(R.id.navigation_drawer_guestList);

        gestureDetector = new GestureDetector(this, new SwipeGestureListener());

        findViewById(R.id.frameLayout).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return gestureDetector.onTouchEvent(event);
            }
        });

        currentFragment = new HomeFragment();  // Set the initial fragment

        binding.bottomNavigationView.setSelectedItemId(R.id.Home);

        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.Connections && !(currentFragment instanceof ConnectionsFragment)) {
                replaceFragment(new ConnectionsFragment(), bundle);
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
            } else if (itemId == R.id.Pinboard && !(currentFragment instanceof PinboardFragment)) {
                replaceFragment(new PinboardFragment(), bundle);
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
            } else if (itemId == R.id.Home && !(currentFragment instanceof HomeFragment)) {
                replaceFragment(new HomeFragment(), bundle);
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
            } else if (itemId == R.id.Account && !(currentFragment instanceof AccountFragment)) {
                replaceFragment(new AccountFragment(), bundle);
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
            }

            return true;
        });

        replaceFragment(currentFragment, bundle);
    }

    private void replaceFragment(Fragment fragment, Bundle bundle){
        currentFragment = fragment;  // Update the current fragment
        fragment.setArguments(bundle);
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_frameLayout, fragment);
        fragmentTransaction.commit();
    }

    @Override
    public void onBackPressed() {
        // Handle the back button press in your fragment
        // Set shouldAutoRedirect to false
        MainActivity.shouldAutoRedirect = false;

        // Call the super method to handle the back button press
        super.onBackPressed();
    }

    private class SwipeGestureListener extends GestureDetector.SimpleOnGestureListener {
        private static final int SWIPE_THRESHOLD = 50;
        private static final int SWIPE_VELOCITY_THRESHOLD = 50;

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            float deltaX = e2.getX() - e1.getX();
            float deltaY = e2.getY() - e1.getY();

            if (Math.abs(deltaX) > Math.abs(deltaY)) {
                if (Math.abs(deltaX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                    if (deltaX > 0) {
                        drawerLayout.openDrawer(profileDrawer);
                    } else {
                        drawerLayout.openDrawer(guestListDrawer);
                    }
                    return true;
                }
            }

            return super.onFling(e1, e2, velocityX, velocityY);
        }
    }
}
