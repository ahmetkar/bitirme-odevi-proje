
import android.content.Context
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.denemeapp2.R
import com.example.denemeapp2.ui.theme.DenemeApp2Theme
import com.kanyidev.searchable_dropdown.SearchableExpandedDropDownMenu
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import com.example.denemeapp2.helpers.BackendHelper
import com.example.denemeapp2.helpers.GenelDataModel
import com.example.denemeapp2.helpers.ModelActions
import com.example.denemeapp2.helpers.ModelHelper
import com.example.denemeapp2.helpers.Models
import com.example.denemeapp2.helpers.Retraining
import com.example.denemeapp2.helpers.SettingsHelper
import com.example.denemeapp2.helpers.UserGet
import com.example.denemeapp2.helpers.UserAdd
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.Json.Default.encodeToString
import java.nio.FloatBuffer
import java.sql.Timestamp


private var belirtiler : Array<String> = arrayOf("")

private var genel_gonderildi = false

private val baglan : BackendHelper.DB = BackendHelper.DB()
private var init = baglan.init()

private var wifi_ayar = mutableStateOf(true)
private var mobilveri_ayar =  mutableStateOf(false)

private var id = mutableIntStateOf(0)

private var checkpoint :String? = ""


suspend fun getLastCheckpoint(model : Models,cx: Context) : String?{
    var sonuc = ""
    try {
        var ds = BackendHelper.UpdateDataStore(cx)
        ds.getUpdateInfo(model)?.collect {item->
            sonuc = item.toString()
        }
        return sonuc
    }catch(ex:Exception){
        Log.e("interpreter-checkpoint","checkpoint alınırken hata : "+ex.toString()+"ve"+ex.stackTrace.contentToString())
        return null
    }

}





@Composable
fun GenelHastalikSorgula() {

    val cx = LocalContext.current
    belirtiler =  cx.resources.getStringArray(R.array.genelmodel_siniflar)
    var wifi_gercek by remember {mutableStateOf(false)}
    var mobilveri_gercek by remember { mutableStateOf(false) }

    var durumlar :List<Boolean> = SettingsHelper.isInternetAvailable(cx)
    wifi_gercek = durumlar[0]
    mobilveri_gercek = durumlar[1]

    LaunchedEffect(Unit) {
        val dataStore = SettingsHelper(cx)
        dataStore.getInternetStatus.collect { preferences ->
            if (preferences[0] == null && preferences[1] == null) {
                dataStore.setdefaultStatus()
            }
            wifi_ayar = mutableStateOf(preferences[0].toBoolean())
            mobilveri_ayar = mutableStateOf(preferences[1].toBoolean())
        }
    }

            LaunchedEffect(Unit){
                try {
                    val udataStore = BackendHelper.UserDataStore(cx)
                    udataStore.getUserId.collect { kid ->
                        when (kid) {
                            null -> Log.i("kullanici","null")
                            "" -> {
                                Log.i("kullanici","kullanici id yok")
                                val currentTimestamp = Timestamp(System.currentTimeMillis())
                                var bugunun_tarihi = currentTimestamp.toString()
                                Log.i("kullanici","tarih->$bugunun_tarihi")
                                val ekle = baglan.user_add(init, UserAdd(bugunun_tarihi))
                                if(ekle != null){
                                    udataStore.saveID(ekle)
                                }
                            }
                            else -> {
                                    id = mutableIntStateOf(kid.toInt())
                            }
                        }
                    }
                }catch(e : Exception){
                    Log.e("kullanici_hata",e.toString())
                }
        }

          LaunchedEffect(Unit){
              withContext(Dispatchers.IO){
                  val rt = Retraining(init,baglan,cx,id.intValue)
                  rt.checkUpdate()
              }
          }


          LaunchedEffect(Unit){
              withContext(Dispatchers.IO) {
                  checkpoint = getLastCheckpoint(Models.GENEL, cx)
                  Log.i("interpreter-checkpoint", "alindi " + checkpoint)
              }
          }











    DenemeApp2Theme {
        FormPreview()

    }



}


private fun genelModelSorgula(cx:Context, girdiler: SnapshotStateList<String>) : FloatArray{
    try {
        var d = ModelActions(cx,"genelmodel.tflite")
        var yenigirdiler = ModelHelper.genelgirdiAl(belirtiler,girdiler)
        var outputArr = FloatArray(42)
        var sonuc :Map<String,Any> = mapOf()
        try {
            if(checkpoint != null){
                if(checkpoint!=""){
                    Log.i("interpreter-sonuc","checkpoint var ve yüklenecek")
                    sonuc = d.restoreAndDoPredict(yenigirdiler,outputArr,cx,checkpoint)
                }
            }
        }catch(ex:Exception){
            Log.i("interpreter-predict-hata"," hata oluştu : "+ex.toString())
        }
       if(checkpoint.isNullOrEmpty()){
            Log.i("interpreter-sonuc","checkpoint bulunamadı normal tahmin")
            sonuc = d.doPredict(yenigirdiler,outputArr)
        }
        if(sonuc.isNotEmpty()) {
            var _sonuc = sonuc["output"]
            if (_sonuc is FloatBuffer) {
                var sonuclar = FloatArray(_sonuc.remaining())
                _sonuc.get(sonuclar)
                Log.i("interpreter-sonuc-once: ", sonuclar.contentToString())
                _sonuc.rewind()
                return sonuclar
            }
        }
        return floatArrayOf(-1.0f)
    }catch(e:Exception){
        Log.e("interpreter-hata",e.message.toString())
        return floatArrayOf(-1.0f)
    }
}

@Serializable
data class DataContainer(val keys: List<String>, val values: List<Float>)

private suspend fun genelGeribildir(girdiler: SnapshotStateList<String>?, sonuc:String?, yuzde:String?, geribildirim:Int,kid:Int) : Boolean{

    if(girdiler!=null && sonuc!=null && yuzde!=null && geribildirim!=2){
        var n_girdiler = mutableMapOf<String, Float>()
        girdiler.forEach { item ->
            if(item!="") {
                n_girdiler[item] = 1.0f
            }
        }
        val dataContainer = DataContainer(n_girdiler.keys.toList(),n_girdiler.values.toList())
        val girdi_json = Json.encodeToString(dataContainer)

        Log.e("json-deneme",girdi_json)
        Log.e("json-deneme",sonuc+" ve "+yuzde+" ve "+geribildirim)

        var gModel = GenelDataModel(girdi_json,sonuc,yuzde,geribildirim,kid)
        var ekle = false
        withContext(Dispatchers.IO) {
            ekle = baglan.genel_feedback(init, gModel)
            Log.i("veritabani_ekle-genel", "Eklendi : $ekle")
        }
        return ekle

    }
    return false
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormComp() {
    val secenekler = belirtiler.toList()
    val enableState by remember { mutableStateOf(false) }
    var selectedItems = remember { mutableStateListOf("") }
    var isPressed by remember { mutableStateOf(false) }

    var isPressed2 by remember { mutableStateOf(false) }
    var hatametni by remember { mutableStateOf("") }
    var internet_gecerlimi by remember { mutableStateOf(false) }
    var wifi_gercek by remember { mutableStateOf(false) }
    var mobilveri_gercek by remember { mutableStateOf(false) }
    var selectedStatus by remember { mutableIntStateOf(2) }
    var geribildirimSonuc by remember { mutableStateOf(false) }
    var sonuclar by remember { mutableStateOf(floatArrayOf()) }
    val scope = rememberCoroutineScope()

    val cx = LocalContext.current
    var durumlar: List<Boolean> = SettingsHelper.isInternetAvailable(cx)
    wifi_gercek = durumlar[0]
    mobilveri_gercek = durumlar[1]


    Column(
        modifier = Modifier
            .padding(16.dp, 16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {


        Text(
            text = "Lütfen sizde olduğunu düşündüğünüz belirtileri seçin ",
            modifier = Modifier.padding(20.dp)
        )

        SearchableExpandedDropDownMenu(
            listOfItems = secenekler, // provide the list of items of any type you want to populated in the dropdown,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            onDropDownItemSelected = { item -> // Returns the item selected in the dropdown
                if (!selectedItems.contains(item)) {
                    selectedItems.add(item)
                }
            },
            enable = enableState,// controls the enabled state of the OutlinedTextField
            placeholder = "Bir belirti seçiniz.", // Add your preferred placeholder name,
            openedIcon = Icons.Outlined.KeyboardArrowUp,// Add your preffered icon when the dropdown is opened,
            closedIcon = Icons.Outlined.KeyboardArrowDown, // Add your preffered icon when the dropdown is closed,
            parentTextFieldCornerRadius = 12.dp, // By default the corner radius is 12.dp but you can customize it,
            colors = TextFieldDefaults.outlinedTextFieldColors(), // Customize the colors of the input text, background and content used in a text field in different states
            dropdownItem = { name -> // Provide a Composable that will be used to populate the dropdown and that takes a type i.e String,Int or even a custom type
                Text(name)
            }
        )

        Text(text = "Seçtiğiniz Belirtiler ", fontSize = 18.sp, modifier = Modifier.padding(20.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 1.dp, start = 20.dp)
        ) {
            selectedItems.forEach { item ->
                Text(
                    text = item,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
                )
            }
        }

        Button(onClick = {
            if (selectedItems.size >= 3) {
                hatametni = ""
                sonuclar = genelModelSorgula(cx, selectedItems)
                isPressed = true
                genel_gonderildi = false
            } else {
                isPressed = false
                hatametni = "Lütfen üç veya daha fazla belirti seçin."
            }
        }, modifier = Modifier.fillMaxWidth()) {
            Text(text = "Tahmin et")
        }
        Spacer(modifier = Modifier.padding(8.dp))


        Button(onClick = {
            selectedItems.clear()
        }, modifier = Modifier.fillMaxWidth()) {
            Text(text = "Temizle")
        }
        Spacer(modifier = Modifier.padding(8.dp))

        if (isPressed) {
            if (sonuclar[0] != -1.0f) {
                Text(text = "Tahmin sonuçları  ", fontSize = 25.sp)
                Spacer(modifier = Modifier.padding(8.dp))
                Text(text = "(Bu sonucun tahmini olduğunu unutmayın)  ", fontSize = 15.sp)

                //Text("sonuclar : ${sonuclar.contentToString()}")

                var maxFloat = sonuclar.maxOrNull()
                var maxIndex = sonuclar.indexOfFirst { it == maxFloat }
                val etiketler = cx.resources.getStringArray(R.array.genelmodel_etiketler)
                var etiket = etiketler[maxIndex]
                var yuzde = sonuclar[maxIndex] * 1000

                Spacer(modifier = Modifier.padding(8.dp))
                //$maxFloat -> $maxIndex
                Text(
                    text = "%${yuzde} ihtimalle hastalığınız,  $etiket olabilir.",
                    fontSize = 15.sp, style = MaterialTheme.typography.bodyLarge
                )

                Log.i("kullanici-fc","kullanici id : $id")
                internet_gecerlimi =
                    ((mobilveri_gercek && mobilveri_ayar.value) || (wifi_gercek && wifi_ayar.value))
                if (internet_gecerlimi) {
                    if (!genel_gonderildi) {
                        Spacer(modifier = Modifier.padding(16.dp))
                        Column(
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Sonuçlar hakkında geri bildirimde bulunun..",
                                color = Color.Gray, style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(modifier = Modifier.padding(5.dp))
                            Row {
                                RadioButton(
                                    selected = selectedStatus == 1, onClick = {
                                        selectedStatus = 1
                                    }, colors = RadioButtonDefaults.colors(Color.Green),
                                    modifier = Modifier.scale(0.8f)
                                )
                                Spacer(modifier = Modifier.size(0.dp))
                                Text(
                                    text = "Sonuç doğru",
                                    color = Color.Green,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(0.dp, 13.dp)
                                )
                                Spacer(modifier = Modifier.size(16.dp))
                                RadioButton(
                                    selected = selectedStatus == 0,
                                    onClick = {
                                        selectedStatus = 0
                                    },
                                    colors = RadioButtonDefaults.colors(Color.Red),
                                    modifier = Modifier.scale(0.8f)
                                )
                                Spacer(modifier = Modifier.size(0.dp))
                                Text(
                                    text = "Sonuç yanlış.",
                                    color = Color.Red,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(0.dp, 13.dp)
                                )
                            }
                            Spacer(modifier = Modifier.padding(5.dp))

                            Button(onClick = {
                                internet_gecerlimi =
                                    ((mobilveri_gercek && mobilveri_ayar.value) || (wifi_gercek && wifi_ayar.value))
                                if (!genel_gonderildi && internet_gecerlimi) {

                                    scope.launch {
                                            geribildirimSonuc =
                                                genelGeribildir(selectedItems,etiket,yuzde.toString(),selectedStatus,id.intValue)
                                    }
                                    isPressed2 = true
                                    Log.i("veritabani", "islem : $geribildirimSonuc")

                                }

                            }, modifier = Modifier.fillMaxWidth()) {
                                Text(text = "Geri bildir", color = Color.Black)
                            }
                        }
                    }

                    if (genel_gonderildi) {
                        Text("Daha önce geri bildirim yaptınız.", color = Color.LightGray)
                        isPressed2 = false
                    }

                    if (isPressed2) {
                        if (geribildirimSonuc) {
                            Text("Geri bildirim başarıyla eklendi", color = Color.Green)
                            genel_gonderildi = true
                        } else {
                            Text("Geri bildirim eklenirken sorun oluştu.", color = Color.Red)
                        }
                    }


                } else {
                    Text("Hata oluştu", style = MaterialTheme.typography.bodyLarge)
                }

            } else if (hatametni != "") {
                Text(hatametni, style = MaterialTheme.typography.bodyLarge, color = Color.Red)
            }

        }
    }
}

@Preview(showBackground = true)
@Composable
fun FormPreview(){
    FormComp()
}

