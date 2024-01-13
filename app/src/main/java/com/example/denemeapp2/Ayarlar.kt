package com.example.denemeapp2

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.navigation.NavController
import com.example.denemeapp2.ui.theme.DenemeApp2Theme
import com.example.denemeapp2.R
import com.example.denemeapp2.helpers.BackendHelper
import com.example.denemeapp2.helpers.SettingsHelper
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

@Composable
fun Ayarlar() {
    val cx = LocalContext.current
    DenemeApp2Theme {
        AyarlarPreview()
    }

}
var wifi = mutableStateOf(true)
var mobilveri = mutableStateOf(false)

@Preview(showBackground = true)
@Composable
fun AyarlarPreview(){
        val cx  = LocalContext.current
        val dataStore = SettingsHelper(cx)
        val getStatus = dataStore.getInternetStatus.collectAsState(initial = listOf())
        var status = getStatus.value


    LaunchedEffect(Unit) {
        // DataStore'dan verileri al ve MutableState içine depola
        dataStore.getInternetStatus.collect { preferences ->
            wifi = mutableStateOf(preferences[0].toBoolean())
            mobilveri = mutableStateOf(preferences[1].toBoolean())
        }
    }


        var isc = remember { mutableStateOf(false) }
        var scope = rememberCoroutineScope()

        Column(modifier = Modifier
            .padding(16.dp, 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)){
            Text("Lütfen model güncellemelerimizi hangi seçeneklerde yapabileceğimizi seçin : ")
            Spacer(modifier = Modifier.padding(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly){
                Text("Wifi ile yapılsın mı ? : ")
                Spacer(modifier = Modifier.size(32.dp))
                Switch(checked = wifi.value,onCheckedChange = {wifi.value = it})
            }
            Spacer(modifier = Modifier.padding(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly){
                Text("Mobil veri ile yapılsın mı ? : ")
                Spacer(modifier = Modifier.size(32.dp))
                Switch(checked = mobilveri.value,onCheckedChange = {mobilveri.value = it})
            }
            Spacer(modifier = Modifier.padding(8.dp))
            Button(onClick = {
                scope.launch {
                    dataStore.saveStatus(listOf(wifi.value.toString(),mobilveri.value.toString()))
                }
                isc.value = true
            }){
                Text("Değişikleri kaydet")
            }

            Spacer(modifier = Modifier.padding(8.dp))

            if(isc.value){
                Text("Ayarlar başarıyla keydedildi.",color= Color.Green)

                var wifiStatus : String? = status[0]!!
                var mobilStatus : String? = status[1]!!
                var wstr = if(wifiStatus == "true") "yapilacak" else "yapilmayacak"
                var mstr = if(mobilStatus == "true") "yapilacak" else "yapilmayacak"
                Spacer(modifier = Modifier.padding(8.dp))
                Text("Wifi ile güncelleme  $wstr ,mobil veri ile güncelleme $mstr",
                    style = MaterialTheme.typography.bodyMedium )
            }
        }


}


