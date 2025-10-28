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
        private val KEY_BRANCH_ID = stringPreferencesKey("branch_id")
    }

    val displayId: Flow<String?> = context.dataStore.data.map { it[KEY_DISPLAY_ID] }
    val tenantId: Flow<String?> = context.dataStore.data.map { it[KEY_TENANT_ID] }
    val branchId: Flow<String?> = context.dataStore.data.map { it[KEY_BRANCH_ID] }

    suspend fun saveDisplayInfo(displayId: String, tenantId: String?, branchId: String?) {
        context.dataStore.edit {
            it[KEY_DISPLAY_ID] = displayId
            if (tenantId != null) it[KEY_TENANT_ID] = tenantId
            if (branchId != null) it[KEY_BRANCH_ID] = branchId
        }
    }

    suspend fun clear() {
        context.dataStore.edit { it.clear() }
    }
}