package com.bodyweight.fitness.ui

import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle

import android.view.Menu
import android.view.MenuItem
import android.view.WindowManager

import com.bodyweight.fitness.Constants
import com.bodyweight.fitness.repository.Repository
import com.bodyweight.fitness.R
import com.bodyweight.fitness.dialog.LogWorkoutDialog
import com.bodyweight.fitness.dialog.ProgressDialog
import com.bodyweight.fitness.extension.debug
import com.bodyweight.fitness.stream.DialogType
import com.bodyweight.fitness.stream.RoutineStream
import com.bodyweight.fitness.stream.Stream
import com.bodyweight.fitness.stream.UiEvent
import com.bodyweight.fitness.utils.Preferences

import com.kobakei.ratethisapp.RateThisApp
import com.trello.rxlifecycle.ActivityEvent

import com.trello.rxlifecycle.components.support.RxAppCompatActivity
import com.trello.rxlifecycle.kotlin.bindUntilEvent

import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.view_toolbar.*

class MainActivity : RxAppCompatActivity(), SharedPreferences.OnSharedPreferenceChangeListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        setToolbar()
        keepScreenOnWhenAppIsRunning()

        val event = ActivityEvent.DESTROY

        UiEvent.dialogObservable
                .bindUntilEvent(this, event)
                .doOnSubscribe { debug(this.javaClass.simpleName + " = doOnSubscribe") }
                .doOnUnsubscribe { debug(this.javaClass.simpleName + " = doOnUnsubscribe") }
                .subscribe {
                    if (it.dialogType === DialogType.LogWorkout) {
                        val bundle = Bundle()
                        bundle.putString(Constants.exerciseId, it.exerciseId)

                        val logWorkoutDialog = LogWorkoutDialog()
                        logWorkoutDialog.arguments = bundle
                        logWorkoutDialog.show(supportFragmentManager, "logWorkoutDialog")
                    } else if (it.dialogType === DialogType.Progress) {
                        val bundle = Bundle()
                        bundle.putString(Constants.exerciseId, it.exerciseId)

                        val progressDialog = ProgressDialog()
                        progressDialog.arguments = bundle
                        progressDialog.show(supportFragmentManager, "progressDialog")
                    }
                }

        Stream.menuObservable
                .bindUntilEvent(this, event)
                .doOnSubscribe { debug(this.javaClass.simpleName + " = doOnSubscribe") }
                .doOnUnsubscribe { debug(this.javaClass.simpleName + " = doOnUnsubscribe") }
                .filter { it == R.id.action_dashboard }
                .subscribe {
                    startActivity(Intent(this, DashboardActivity::class.java))
                }

        Stream.drawerObservable()
                .bindUntilEvent(this, event)
                .doOnSubscribe { debug(this.javaClass.simpleName + " = doOnSubscribe") }
                .doOnUnsubscribe { debug(this.javaClass.simpleName + " = doOnUnsubscribe") }
                .subscribe {
                    when (it) {
                        R.id.action_menu_support_developer -> {
                            startActivity(Intent(Intent.ACTION_VIEW).apply {
                                data = Uri.parse(Constants.googlePlayUrl)
                            })
                        }

                        R.id.action_menu_settings -> {
                            startActivity(Intent(applicationContext, SettingsActivity::class.java))
                        }
                    }
                }
    }

    override fun onResume() {
        super.onResume()

        keepScreenOnWhenAppIsRunning()
    }

    override fun onStart() {
        super.onStart()

        RateThisApp.onStart(this)
        RateThisApp.showRateDialogIfNeeded(this)
    }

    override fun onStop() {
        super.onStop()

        clearFlagKeepScreenOn()

        Repository.realm.close()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        Stream.setMenu(item.itemId)

        return super.onOptionsItemSelected(item)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        keepScreenOnWhenAppIsRunning()
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        if (Stream.currentDrawerId == R.id.action_menu_home) {
            if (Stream.currentHomePage != 0 && Stream.currentHomePage != 2) {
                menuInflater.inflate(R.menu.home, menu)
            }
        } else if (Stream.currentDrawerId  == R.id.action_menu_workout_log) {
            menuInflater.inflate(R.menu.calendar, menu)
        }

        return super.onCreateOptionsMenu(menu)
    }

    private fun setToolbar() {
        setSupportActionBar(toolbar)

        supportActionBar?.let {
            it.elevation = 0f
            it.setHomeButtonEnabled(true)
            it.setDisplayHomeAsUpEnabled(true)
        }

        val actionBarDrawerToggle = ActionBarDrawerToggle(
                this,
                drawer_layout,
                toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close)

        drawer_layout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START)
        drawer_layout.addDrawerListener(actionBarDrawerToggle)

        actionBarDrawerToggle.syncState()

        navigation_view.setNavigationItemSelectedListener { item ->
            drawer_layout.closeDrawers()

            Stream.setDrawer(item.itemId)

            if (item.itemId == R.id.action_menu_support_developer || item.itemId == R.id.action_menu_settings) {
                return@setNavigationItemSelectedListener false
            } else {
                return@setNavigationItemSelectedListener true
            }
        }
    }

    private fun keepScreenOnWhenAppIsRunning() {
        if (Preferences.keepScreenOnWhenAppIsRunning()) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            clearFlagKeepScreenOn()
        }
    }

    private fun clearFlagKeepScreenOn() {
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }
}
