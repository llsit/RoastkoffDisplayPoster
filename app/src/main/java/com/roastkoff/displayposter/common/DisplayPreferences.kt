package com.roastkoff.displayposter.common

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
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
    }

    val displayId: Flow<String?> = context.dataStore.data.map { it[KEY_DISPLAY_ID] }
    val tenantId: Flow<String?> = context.dataStore.data.map { it[KEY_TENANT_ID] }
    val groupId: Flow<String?> = context.dataStore.data.map { it[KEY_GROUP_ID] }

    suspend fun saveDisplayInfo(displayId: String, tenantId: String?, groupId: String?) {
        context.dataStore.edit {
            it[KEY_DISPLAY_ID] = displayId
            if (tenantId != null) it[KEY_TENANT_ID] = tenantId
            if (groupId != null) it[KEY_GROUP_ID] = groupId
        }
    }

    suspend fun clear() {
        context.dataStore.edit { it.clear() }
    }
}