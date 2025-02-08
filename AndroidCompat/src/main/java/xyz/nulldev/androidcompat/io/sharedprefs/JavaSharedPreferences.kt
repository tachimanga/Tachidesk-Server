package xyz.nulldev.androidcompat.io.sharedprefs

/*
 * Copyright (C) Contributors to the Suwayomi project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */

import android.content.SharedPreferences
import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.PreferencesSettings
import com.russhwolf.settings.serialization.decodeValue
import com.russhwolf.settings.serialization.decodeValueOrNull
import com.russhwolf.settings.serialization.encodeValue
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.SetSerializer
import kotlinx.serialization.builtins.serializer
import java.util.prefs.PreferenceChangeListener
import java.util.prefs.Preferences

const val PREF_MAX_KEY_LENGTH = 8 * 1024

const val PREF_MAX_VALUE_LENGTH = 1024 * 1024

const val MAX_NAME_LENGTH = 80

fun shortKey(longKey: String) =
    if (longKey.length > PREF_MAX_KEY_LENGTH) longKey.substring(0, PREF_MAX_KEY_LENGTH) else longKey

fun shortNode(longKey: String) =
    if (longKey.length > MAX_NAME_LENGTH) longKey.substring(0, MAX_NAME_LENGTH) else longKey

@OptIn(ExperimentalSerializationApi::class, ExperimentalSettingsApi::class)
class JavaSharedPreferences(key: String) : SharedPreferences {
    private val javaPreferences = Preferences.userRoot().node("suwayomi/tachidesk/${shortNode(key)}")
    private val preferences = PreferencesSettings(javaPreferences)
    private val listeners = mutableMapOf<SharedPreferences.OnSharedPreferenceChangeListener, PreferenceChangeListener>()

    // TODO: 2021-05-29 Need to find a way to get this working with all pref types
    override fun getAll(): MutableMap<String, *> {
        return preferences.keys.associateWith { preferences.getStringOrNull(it) }.toMutableMap()
    }

    override fun getString(longKey: String, defValue: String?): String? {
        val key = shortKey(longKey)
        return if (defValue != null) {
            preferences.getString(key, defValue)
        } else {
            preferences.getStringOrNull(key)
        }
    }

    override fun getStringSet(longKey: String, defValues: Set<String>?): Set<String>? {
        val key = shortKey(longKey)
        try {
            return if (defValues != null) {
                preferences.decodeValue(SetSerializer(String.serializer()), key, defValues)
            } else {
                preferences.decodeValueOrNull(SetSerializer(String.serializer()), key)
            }
        } catch (e: SerializationException) {
            throw ClassCastException("$key was not a StringSet")
        }
    }

    override fun getInt(longKey: String, defValue: Int): Int {
        val key = shortKey(longKey)
        return preferences.getInt(key, defValue)
    }

    override fun getLong(longKey: String, defValue: Long): Long {
        val key = shortKey(longKey)
        return preferences.getLong(key, defValue)
    }

    override fun getFloat(longKey: String, defValue: Float): Float {
        val key = shortKey(longKey)
        return preferences.getFloat(key, defValue)
    }

    override fun getBoolean(longKey: String, defValue: Boolean): Boolean {
        val key = shortKey(longKey)
        return preferences.getBoolean(key, defValue)
    }

    override fun contains(longKey: String): Boolean {
        val key = shortKey(longKey)
        return key in preferences.keys
    }

    override fun edit(): SharedPreferences.Editor {
        return Editor(preferences)
    }

    class Editor(private val preferences: PreferencesSettings) : SharedPreferences.Editor {
        private val actions = mutableListOf<Action>()

        private sealed class Action {
            data class Add(val longKey: String, val value: Any) : Action() {
                val key = shortKey(longKey)
            }
            data class Remove(val longKey: String) : Action() {
                val key = shortKey(longKey)
            }
            object Clear : Action()
        }

        override fun putString(key: String, value: String?): SharedPreferences.Editor {
            if (value != null) {
                actions += Action.Add(key, value)
            } else {
                actions += Action.Remove(key)
            }
            return this
        }

        override fun putStringSet(
            key: String,
            values: MutableSet<String>?,
        ): SharedPreferences.Editor {
            if (values != null) {
                actions += Action.Add(key, values)
            } else {
                actions += Action.Remove(key)
            }
            return this
        }

        override fun putInt(key: String, value: Int): SharedPreferences.Editor {
            actions += Action.Add(key, value)
            return this
        }

        override fun putLong(key: String, value: Long): SharedPreferences.Editor {
            actions += Action.Add(key, value)
            return this
        }

        override fun putFloat(key: String, value: Float): SharedPreferences.Editor {
            actions += Action.Add(key, value)
            return this
        }

        override fun putBoolean(key: String, value: Boolean): SharedPreferences.Editor {
            actions += Action.Add(key, value)
            return this
        }

        override fun remove(key: String): SharedPreferences.Editor {
            actions += Action.Remove(key)
            return this
        }

        override fun clear(): SharedPreferences.Editor {
            actions.add(Action.Clear)
            return this
        }

        override fun commit(): Boolean {
            addToPreferences()
            return true
        }

        override fun apply() {
            addToPreferences()
        }

        private fun addToPreferences() {
            actions.forEach {
                @Suppress("UNCHECKED_CAST")
                when (it) {
                    is Action.Add -> when (val value = it.value) {
                        is Set<*> -> preferences.encodeValue(SetSerializer(String.serializer()), it.key, value as Set<String>)
                        is String -> preferences.putString(it.key, value)
                        is Int -> preferences.putInt(it.key, value)
                        is Long -> preferences.putLong(it.key, value)
                        is Float -> preferences.putFloat(it.key, value)
                        is Double -> preferences.putDouble(it.key, value)
                        is Boolean -> preferences.putBoolean(it.key, value)
                    }
                    is Action.Remove -> {
                        preferences.remove(it.key)
                        /**
                         * Set<String> are stored like
                         * key.0 = value1
                         * key.1 = value2
                         * key.size = 2
                         */
                        preferences.keys.forEach { key ->
                            if (key.startsWith(it.key + ".")) {
                                preferences.remove(key)
                            }
                        }
                    }
                    Action.Clear -> preferences.clear()
                }
            }
        }
    }

    override fun registerOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) {
        val javaListener = PreferenceChangeListener {
            listener.onSharedPreferenceChanged(this, it.key)
        }
        listeners[listener] = javaListener
        javaPreferences.addPreferenceChangeListener(javaListener)
    }

    override fun unregisterOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) {
        val registeredListener = listeners.remove(listener)
        if (registeredListener != null) {
            javaPreferences.removePreferenceChangeListener(registeredListener)
        }
    }

    fun deleteAll(): Boolean {
        javaPreferences.removeNode()
        return true
    }
}
