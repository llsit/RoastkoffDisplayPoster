package com.roastkoff.displayposter.common

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

private val Context.dataStore by preferencesDataStore(name = "display_prefs")

class DisplayPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private val KEY_DISPLAY_ID = stringPreferencesKey("display_id")
        private val KEY_TENANT_ID = stringPreferencesKey("tenant_id")
        private val KEY_GROUP_ID = stringPreferencesKey("group_id")
        private val KEY_PAIRING_CODE = stringPreferencesKey("pairing_code")
        private val KEY_USER_ID = stringPreferencesKey("user_id")
    }

    val displayId: Flow<String?> = context.dataStore.data.map { it[KEY_DISPLAY_ID] }
    val tenantId: Flow<String?> = context.dataStore.data.map { it[KEY_TENANT_ID] }
    val groupId: Flow<String?> = context.dataStore.data.map { it[KEY_GROUP_ID] }
    val pairingCode: Flow<String?> = context.dataStore.data.map { it[KEY_PAIRING_CODE] }
    val userId: Flow<String?> = context.dataStore.data.map { it[KEY_USER_ID] }

    suspend fun getPairingCode(): String? {
        return pairingCode.first()
    }

    suspend fun savePairingCode(code: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_PAIRING_CODE] = code
        }
    }

    suspend fun clearPairingCode() {
        context.dataStore.edit { prefs ->
            prefs.remove(KEY_PAIRING_CODE)
        }
    }

    suspend fun saveUserId(id: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_USER_ID] = id
        }
    }

    suspend fun clearUserId(id: String) {
        context.dataStore.edit { prefs ->
            prefs.remove(KEY_USER_ID)
        }
    }

    suspend fun isLoggedIn(): Boolean {
        val id = userId.first()
        return id != null
    }

    suspend fun isPaired(): Boolean {
        val id = displayId.first()
        return id != null
    }

    suspend fun saveDisplayInfo(displayId: String, tenantId: String?, groupId: String?) {
        context.dataStore.edit {
            it[KEY_DISPLAY_ID] = displayId
            if (tenantId != null) it[KEY_TENANT_ID] = tenantId
            if (groupId != null) it[KEY_GROUP_ID] = groupId
        }
    }

    suspend fun clearAll() {
        context.dataStore.edit { it.clear() }
    }
}