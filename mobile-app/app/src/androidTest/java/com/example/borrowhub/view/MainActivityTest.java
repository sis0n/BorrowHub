package com.example.borrowhub.view;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.borrowhub.R;
import com.example.borrowhub.data.local.SessionManager;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class MainActivityTest {

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule =
            new ActivityScenarioRule<>(MainActivity.class);

    @Test
    public void testBottomNavigationAndToolbarVisibility() {
        // Check if BottomNavigationView is visible
        onView(withId(R.id.bottomNavigationView)).check(matches(isDisplayed()));

        // Check if TopAppBar is visible
        onView(withId(R.id.topAppBar)).check(matches(isDisplayed()));
    }

    @Test
    public void testNavigationToInventory() {
        // Click on Inventory menu item in Bottom Navigation
        onView(withId(R.id.inventoryFragment)).perform(click());

        // Verify that the Inventory fragment is displayed (checking the TextView inside it)
        onView(withId(R.id.tv_inventory)).check(matches(isDisplayed()));
        onView(withText("Inventory")).check(matches(isDisplayed()));
    }

    @Test
    public void testNavigationToTransaction() {
        // Click on Transaction menu item
        onView(withId(R.id.transactionFragment)).perform(click());

        // Verify Transaction fragment
        onView(withId(R.id.tv_transaction)).check(matches(isDisplayed()));
        onView(withText("Transaction")).check(matches(isDisplayed()));
    }

    @Test
    public void testNavigationToLogs() {
        // Click on Logs menu item
        onView(withId(R.id.logsFragment)).perform(click());

        // Verify Logs fragment
        onView(withId(R.id.tv_logs)).check(matches(isDisplayed()));
        onView(withText("Logs")).check(matches(isDisplayed()));
    }

    @Test
    public void testActivityDoesNotCrashAfterDarkThemeRecreation() {
        // Persist dark mode into SessionManager
        activityRule.getScenario().onActivity(activity -> {
            SessionManager sessionManager = new SessionManager(activity);
            sessionManager.setThemeMode(AppCompatDelegate.MODE_NIGHT_YES);
        });

        // Recreate simulates the activity lifecycle triggered by a theme change
        activityRule.getScenario().recreate();

        // Verify that the persisted theme mode is read back correctly
        activityRule.getScenario().onActivity(activity -> {
            SessionManager sessionManager = new SessionManager(activity);
            assert sessionManager.getThemeMode() == AppCompatDelegate.MODE_NIGHT_YES;
        });

        // Verify that the core UI is still visible and the activity did not crash
        onView(withId(R.id.bottomNavigationView)).check(matches(isDisplayed()));
        onView(withId(R.id.topAppBar)).check(matches(isDisplayed()));
    }

    @Test
    public void testActivityDoesNotCrashAfterLightThemeRecreation() {
        // Persist light mode into SessionManager
        activityRule.getScenario().onActivity(activity -> {
            SessionManager sessionManager = new SessionManager(activity);
            sessionManager.setThemeMode(AppCompatDelegate.MODE_NIGHT_NO);
        });

        // Recreate simulates the activity lifecycle triggered by a theme change
        activityRule.getScenario().recreate();

        // Verify that the persisted theme mode is read back correctly
        activityRule.getScenario().onActivity(activity -> {
            SessionManager sessionManager = new SessionManager(activity);
            assert sessionManager.getThemeMode() == AppCompatDelegate.MODE_NIGHT_NO;
        });

        // Verify that the core UI is still visible and the activity did not crash
        onView(withId(R.id.bottomNavigationView)).check(matches(isDisplayed()));
        onView(withId(R.id.topAppBar)).check(matches(isDisplayed()));
    }
}