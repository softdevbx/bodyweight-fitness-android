package com.bodyweight.fitness.utils

import android.content.SharedPreferences
import android.preference.PreferenceManager

import com.bodyweight.fitness.App
import com.bodyweight.fitness.Constants

import com.bodyweight.fitness.R
import com.bodyweight.fitness.model.WeightMeasurementUnit

object Preferences {
    init {
        PreferenceManager.setDefaultValues(App.context, R.xml.settings, false)
    }

    var defaultRoutine: String
        get() {
            return getSharedPreferences().getString(Constants.preferencesDefaultRoutineKey, "routine0")
        }

        set(defaultRoutine) {
            getSharedPreferences()
                    .edit()
                    .putString(Constants.preferencesDefaultRoutineKey, defaultRoutine)
                    .commit()
        }

    val weightMeasurementUnit: WeightMeasurementUnit
        get() {
            val value = getSharedPreferences().getString(Constants.preferencesWeightMeasurementUnitsKey, "kg")

            return WeightMeasurementUnit.valueOf(value)
        }

    fun playSoundWhenTimerStops(): Boolean {
        return getSharedPreferences().getBoolean(Constants.preferencesPlaySoundWhenTimerStopsKey, true)
    }

    fun automaticallyLogWorkoutTime(): Boolean {
        return getSharedPreferences().getBoolean(Constants.preferencesAutomaticallyLogWorkoutTimeKey, true)
    }

    fun keepScreenOnWhenAppIsRunning(): Boolean {
        return getSharedPreferences().getBoolean(Constants.preferencesKeepScreenOnKey, true)
    }

    fun setTimerValue(exerciseId: String, value: Long) {
        getSharedPreferences().edit().putLong(String.format("%s%s", Constants.preferencesTimerKey, exerciseId), value).commit()
    }

    fun setNumberOfReps(exerciseId: String, value: Int) {
        getSharedPreferences().edit().putInt(String.format("%s%s", Constants.preferencesNumberOfRepsKey, exerciseId), value).commit()
    }

    fun getTimerValueForExercise(exerciseId: String, defaultValue: Long): Long {
        return getSharedPreferences().getLong(String.format("%s%s", Constants.preferencesTimerKey, exerciseId), defaultValue)
    }

    fun getNumberOfRepsForExercise(exerciseId: String, defaultValue: Int): Int {
        return getSharedPreferences().getInt(String.format("%s%s", Constants.preferencesNumberOfRepsKey, exerciseId), defaultValue)
    }

    private fun getSharedPreferences(): SharedPreferences {
        return PreferenceManager.getDefaultSharedPreferences(App.context)
    }
}
