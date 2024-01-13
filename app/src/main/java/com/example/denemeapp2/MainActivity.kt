package com.example.denemeapp2

import DiyabetSorgula
import GenelHastalikSorgula
import KalpHastaligiSorgula
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels

import androidx.compose.foundation.layout.Column

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Healing
import androidx.compose.material.icons.filled.HeartBroken
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sick
import androidx.compose.material.icons.outlined.Healing
import androidx.compose.material.icons.outlined.HeartBroken
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Sick
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults

import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.LaunchedEffect

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.denemeapp2.helpers.BackendHelper
import com.example.denemeapp2.helpers.Retraining
import com.example.denemeapp2.helpers.UserAdd
import com.example.denemeapp2.ui.theme.DenemeApp2Theme
import kotlinx.coroutines.Dispatchers

import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.sql.Timestamp

data class NavigationItem(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val route:String
)

@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        setContent {
            DenemeApp2Theme {

                val navController = rememberNavController()
                var sayfa_baslik by remember {
                    mutableStateOf("")
                }
                // A surface container using the 'background' color from the theme
                var items = listOf(
                     NavigationItem (
                         title="Genel Hastalık Sorgula",
                         selectedIcon = Icons.Filled.Sick,
                         unselectedIcon = Icons.Outlined.Sick,
                         route = "genel_hastalik"
                     ),
                    NavigationItem (
                        title="Kalp hastalığı Sorgula",
                        selectedIcon = Icons.Filled.HeartBroken,
                        unselectedIcon = Icons.Outlined.HeartBroken,
                        route = "kalp_hastaligi"
                    ),
                    NavigationItem (
                        title="Diyabet Sorgula",
                        selectedIcon = Icons.Filled.Healing,
                        unselectedIcon = Icons.Outlined.Healing,
                        route = "diyabet"
                    ),
                    NavigationItem (
                        title="Ayarlar",
                        selectedIcon = Icons.Filled.Settings,
                        unselectedIcon = Icons.Outlined.Settings,
                        route = "ayarlar"
                    )

                )

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ){
                    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
                    val scope = rememberCoroutineScope()
                    var selectedItemIndex by rememberSaveable {
                        mutableStateOf(0)
                    }
                    ModalNavigationDrawer(
                        drawerContent = {
                            ModalDrawerSheet {
                                Spacer(modifier = Modifier.height(16.dp))
                                items.forEachIndexed { index, item ->
                                    NavigationDrawerItem(
                                        label = {
                                            Text(text = item.title)
                                        },
                                        selected = index == selectedItemIndex,
                                        onClick = {
                                            selectedItemIndex = index
                                            scope.launch {
                                                drawerState.close()
                                                navController.navigate(item.route)
                                            }
                                        },
                                        icon = {
                                            Icon(
                                                imageVector = if (index == selectedItemIndex) {
                                                    item.selectedIcon
                                                } else item.unselectedIcon,
                                                contentDescription = item.title
                                            )
                                        },
                                        modifier = Modifier
                                            .padding(NavigationDrawerItemDefaults.ItemPadding)
                                    )
                                }
                            }
                        },
                        drawerState = drawerState
                    ) {

                        Scaffold(
                            topBar = {

                                TopAppBar(
                                    title = {
                                        Text(text =sayfa_baslik)
                                    },
                                    navigationIcon = {
                                        IconButton(onClick = {
                                            scope.launch {
                                                drawerState.open()
                                            }
                                        }) {
                                            Icon(
                                                imageVector = Icons.Default.Menu,
                                                contentDescription = "Menu"
                                            )
                                        }
                                    }
                                )
                            }
                        ) { padding ->
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(padding)
                            ) {



                                NavHost(
                                    navController = navController,
                                    startDestination = "genel_hastalik"
                                ) {

                                    composable(route = "genel_hastalik") {
                                        sayfa_baslik = "Genel Hastalık Sorgula"
                                        GenelHastalikSorgula()
                                    }
                                    composable(route = "kalp_hastaligi") {
                                        sayfa_baslik = "Kalp Hastalığı Sorgula"
                                        KalpHastaligiSorgula()
                                    }
                                    composable(route = "diyabet") {
                                        sayfa_baslik = "Diyabet Sorgula"
                                        DiyabetSorgula()
                                    }
                                    composable(route = "ayarlar"){
                                        sayfa_baslik = "Ayarlar"
                                        Ayarlar()
                                    }
                                }
                            }
                        }

                    }

                }

            }

        }

    }


}




