package com.example.denemeapp2.helpers

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.collectIndexed
import kotlinx.coroutines.flow.map

class SettingsHelper(private val context: Context) {
    companion object {
            fun isInternetAvailable(context: Context): List<Boolean> {
                var wifi_result = false
                var mobilveri_result = false
                val connectivityManager =
                    context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                val networkCapabilities = connectivityManager.activeNetwork ?: return listOf(false,false)
                val actNw =
                    connectivityManager.getNetworkCapabilities(networkCapabilities) ?: return listOf(false,false)
                mobilveri_result = when {
                    actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                    else -> false
                }
                wifi_result = when {
                    actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                    actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                    else -> false
                }
                return listOf(wifi_result,mobilveri_result)
            }

        private val Context.dataStore : DataStore<Preferences> by preferencesDataStore("internet")
        val wifi_key = stringPreferencesKey("wifi_on")
        val mobil_key = stringPreferencesKey("mobil_on")

    }



    val getInternetStatus : Flow<List<String?>> = context.dataStore.data.map {
        preferences ->
        listOf(preferences[wifi_key] ?: "",
        preferences[mobil_key] ?: "")
    }



    suspend fun setdefaultStatus(){
        saveStatus(listOf(true.toString(),false.toString()))
    }

    suspend fun saveStatus(status : List<String>){
        context.dataStore.edit {
            preferences->
            preferences[wifi_key] = status[0]
            preferences[mobil_key] = status[1]
        }
    }

}

