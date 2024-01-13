import androidx.compose.foundation.layout.Arrangement
import android.content.Context
import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.denemeapp2.R
import com.example.denemeapp2.helpers.BackendHelper
import com.example.denemeapp2.helpers.KalpDataModel
import com.example.denemeapp2.helpers.ModelActions
import com.example.denemeapp2.helpers.ModelHelper
import com.example.denemeapp2.helpers.Models
import com.example.denemeapp2.helpers.SettingsHelper
import com.example.denemeapp2.ui.theme.DenemeApp2Theme
import io.github.jan.supabase.SupabaseClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.nio.FloatBuffer


private var klabels : Array<String> = arrayOf("")

private val baglan: BackendHelper.DB = BackendHelper.DB()
private var init :SupabaseClient? = baglan.init()

private var kalp_gonderildi = false

private var wifi_ayar = mutableStateOf(false)
private var mobilveri_ayar =  mutableStateOf(false)

private var id = mutableIntStateOf(-1)

private var checkpoint :String? = ""

@Composable
fun KalpHastaligiSorgula() {
    val cx = LocalContext.current
    klabels = cx.resources.getStringArray(R.array.heartmodel_siniflar)
    kalp_gonderildi = false
    LaunchedEffect(Unit) {
        val dataStore = SettingsHelper(cx)
        dataStore.getInternetStatus.collect { preferences ->
            if(preferences[0] == null && preferences[1] == null){
                dataStore.setdefaultStatus()
            }
            wifi_ayar = mutableStateOf(preferences[0].toBoolean())
            mobilveri_ayar = mutableStateOf(preferences[1].toBoolean())
        }
    }

    LaunchedEffect(Unit) {
        val udataStore = BackendHelper.UserDataStore(cx)
        udataStore.getUserId.collect { kid ->
            if (kid != null) id = mutableIntStateOf(kid.toInt())
        }
    }

    LaunchedEffect(Unit){
        checkpoint = getLastCheckpoint(Models.KALP,cx)
        Log.i("interpreter-checkpoint","alindi "+checkpoint)
    }


    DenemeApp2Theme {
        FormPreview2()
    }

}


private suspend fun kalpFeedback(girdiler:List<Float>?, sonuc: Int,kid:Int) : Boolean{
    var ekle = false
    if(girdiler!=null) {
        var yeni_girdiler = ModelHelper.kalpgirdiAl(girdiler)
        var yeni_model = KalpDataModel(
            yeni_girdiler[0].toInt(),
            yeni_girdiler[1].toInt(),
            yeni_girdiler[2].toInt(),
            yeni_girdiler[3].toInt(),
            yeni_girdiler[4].toInt(),
            yeni_girdiler[5].toInt(),
            yeni_girdiler[6].toInt(),
            yeni_girdiler[7].toInt(),
            yeni_girdiler[8].toInt(),
            yeni_girdiler[9].toInt(),
            yeni_girdiler[10].toInt(),
            yeni_girdiler[11].toInt(),
            yeni_girdiler[12].toInt(),
            yeni_girdiler[13].toInt(),
            yeni_girdiler[14].toInt(),
            yeni_girdiler[15].toInt(),
            yeni_girdiler[16].toInt(),
            yeni_girdiler[17].toInt(),
            yeni_girdiler[18].toInt(),
            yeni_girdiler[19].toInt(),
            yeni_girdiler[20].toInt(),
            yeni_girdiler[21].toInt(),
            yeni_girdiler[22].toInt(),
            yeni_girdiler[23].toInt(), yeni_girdiler[24].toInt(),
            sonuc, kid
        )

        withContext(Dispatchers.IO) {
            ekle = baglan.kalp_feedback(init, yeni_model)
            Log.i("veritabani_ekle-kalp", "Eklendi : $ekle")
        }
    }
    return ekle
}

private fun kalpSorgula(cx:Context, girdiler: ArrayList<Float>) : Float{
    try {
        var d = ModelActions(cx,"heartmodel.tflite")
        var yenigirdiler = ModelHelper.kalpgirdiAl(girdiler)
        var outputArr = FloatArray(1)
        var sonuc :Map<String,Any> = mapOf()
        try {
            if (checkpoint != null) {
                if (checkpoint != "") {
                    Log.i("interpreter-sonuc","checkpoint var ve yüklenecek")
                    sonuc = d.restoreAndDoPredict(yenigirdiler,outputArr,cx,checkpoint)
                }
            }
        }catch(ex:Exception){
            Log.i("interpreter-predict-hata"," hata oluştu : "+ex.toString())
        }

        if(checkpoint.isNullOrEmpty()) {
            sonuc = d.doPredict(yenigirdiler,outputArr)
        }

        if(sonuc.isNotEmpty()) {
            var cb = sonuc["output"]
            if (cb is FloatBuffer) {
                Log.i("interpreter-sonuc-once: ", cb.get(0).toString())
                return cb.get(0)
            }
        }
        return -1.0f
    }catch(e:Exception){
        Log.e("interpreter-hata",e.message.toString())
        return -1.0f
    }
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormComp2() {
    var age by rememberSaveable { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    var expanded2 by remember { mutableStateOf(false) }
    var expanded3 by remember { mutableStateOf(false) }
    var cinsiyet by remember { mutableIntStateOf(0) }
    var selectedPainType by remember { mutableStateOf("") }
    var restingbp by rememberSaveable { mutableStateOf("") }
    var chol by rememberSaveable { mutableStateOf("") }
    var bloodsugar by rememberSaveable { mutableStateOf("") }
    var exerciseangina by rememberSaveable { mutableIntStateOf(0) }
    var sonucGoster by  remember {mutableStateOf(false)}
    var hatametni by rememberSaveable { mutableStateOf("") }

    var selectedStatus by remember { mutableIntStateOf(2) }
    var isPressed by remember  { mutableStateOf(false) }
    var geribildirimSonuc by remember  { mutableStateOf(false) }

    var wifi_gercek by remember {mutableStateOf(false)}
    var mobilveri_gercek by remember { mutableStateOf(false) }


    var internet_gecerlimi by remember { mutableStateOf(false) }

    val cx = LocalContext.current
    var durumlar :List<Boolean> = SettingsHelper.isInternetAvailable(cx)
    wifi_gercek = durumlar[0]
    mobilveri_gercek = durumlar[1]



    val scope = rememberCoroutineScope()




    Column(
        modifier = Modifier
            .padding(16.dp, 16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {

        Text(text = "Belirtilerinizi girin", fontWeight = FontWeight.Bold,style = MaterialTheme.typography.bodyLarge)
        Spacer(modifier = Modifier.padding(8.dp))

        Text(
            text = klabels[0],
            style = MaterialTheme.typography.bodyLarge
        )

        TextField(
            modifier = Modifier.fillMaxWidth(),
            value = age,
            onValueChange = { age = it },
            placeholder = { Text(text = "Örnek : 35")},
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number),
        )
        Spacer(modifier = Modifier.padding(8.dp))

        Text(
            text = klabels[1],
            style = MaterialTheme.typography.bodyLarge
        )
        ExposedDropdownMenuBox(
            modifier = Modifier.fillMaxWidth(),
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
        ) {
            TextField(
                modifier = Modifier.menuAnchor(),
                readOnly = true,
                value =  cinsiyet.toString().replace("0","Erkek").replace("1","Kadın"),
                onValueChange = {},
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                colors = ExposedDropdownMenuDefaults.textFieldColors(),
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
            ) {
                DropdownMenuItem(
                    text = { Text("Erkek") },
                    onClick = {
                        cinsiyet = 0
                        expanded = false
                    }
                )
                DropdownMenuItem(
                    text = { Text("Kadın") },
                    onClick = {
                        cinsiyet = 1
                        expanded = false
                    }
                )

            }
        }
        Spacer(modifier = Modifier.padding(8.dp))

        val chestpaintypes = arrayOf("TA:Typcial Angina",
            "ATA:Atypial Angina",
            "NAP:Non-Anginal Pain",
            "ASY:Asymptomatic")
        val chestpainvalues = arrayOf("0","1","2","3")
        Text(
            text =klabels[2],
            style = MaterialTheme.typography.bodyLarge
        )
        ExposedDropdownMenuBox(
            modifier = Modifier.fillMaxWidth(),
            expanded = expanded2,
            onExpandedChange = { expanded2 = !expanded2 },
        ) {
            TextField(
                modifier = Modifier.menuAnchor(),
                readOnly = true,
                value = selectedPainType,
                onValueChange = {},
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                colors = ExposedDropdownMenuDefaults.textFieldColors(),
            )
            ExposedDropdownMenu(
                expanded = expanded2,
                onDismissRequest = { expanded2 = false },
            ) {
                chestpaintypes.forEach {item->
                    DropdownMenuItem(
                        text = { Text(item) },
                        onClick = {
                            val idx = chestpaintypes.indexOf(item)
                            if(idx!=-1) {
                                selectedPainType = chestpainvalues[idx]
                            }
                            expanded2 = false
                        }
                    )
                }
            }
        }
        Spacer(modifier = Modifier.padding(8.dp))

        Text(
            text = klabels[3],
            style = MaterialTheme.typography.bodyLarge
        )

        TextField(
            modifier = Modifier.fillMaxWidth(),
            value = restingbp,
            onValueChange = { restingbp = it },
            placeholder = { Text(text = "Örnek : 150 mmHg") },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number)
        )
        Spacer(modifier = Modifier.padding(8.dp))

        Text(
            text = klabels[4],
            style = MaterialTheme.typography.bodyLarge
        )

        TextField(
            modifier = Modifier.fillMaxWidth(),
            value = chol,
            onValueChange = { chol = it },
            placeholder = { Text(text = "Örnek : 120 mg/dl") },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number)
        )
        Spacer(modifier = Modifier.padding(8.dp))

        Text(
            text = klabels[5],
            style = MaterialTheme.typography.bodyLarge
        )

        TextField(
            modifier = Modifier.fillMaxWidth(),
            value = bloodsugar,
            onValueChange = { bloodsugar = it },
            placeholder = { Text(text = "Örnek : 130 mg/dl") },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number)
        )
        Spacer(modifier = Modifier.padding(8.dp))

        Text(
            text = klabels[7],
            style = MaterialTheme.typography.bodyLarge
        )
        ExposedDropdownMenuBox(
            modifier = Modifier.fillMaxWidth(),
            expanded = expanded3,
            onExpandedChange = { expanded3 = !expanded3 },
        ) {
            TextField(
                modifier = Modifier.menuAnchor(),
                readOnly = true,
                value = exerciseangina.toString().replace("0","Hayır").replace("1","Evet"),
                onValueChange = {},
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                colors = ExposedDropdownMenuDefaults.textFieldColors(),
            )
            ExposedDropdownMenu(
                expanded = expanded3,
                onDismissRequest = { expanded3 = false },
            ) {
                DropdownMenuItem(
                    text = { Text("Evet") },
                    onClick = {
                        exerciseangina = 1
                        expanded3 = false
                    }
                )
                DropdownMenuItem(
                    text = { Text("Hayır") },
                    onClick = {
                        exerciseangina = 0
                        expanded3 = false
                    }
                )

            }
        }
        val cx = LocalContext.current
        var sonuc by remember { mutableFloatStateOf(-1.0f)}

        Button(onClick = {
            if(age == "" || selectedPainType == ""
                || chol== "" || bloodsugar == "" || restingbp=="" ){
                sonucGoster = false
                hatametni = "Lütfen tüm alanları doldurunuz."
            }else{
                val n_age = age.toFloat()
                val n_cinsiyet = cinsiyet.toFloat()
                val paintype = selectedPainType.toFloat()
                val rbp = restingbp.toFloat()
                val chl = chol.toFloat()
                val bldsgr =bloodsugar.toFloat()
                val mxhr = 220-age.toFloat()
                val exc = exerciseangina.toFloat()

                if(n_age>170 ||n_age<0 || rbp>700 || rbp < 0 || chl > 700 || chl <0 || bldsgr>700 || bldsgr<0){
                    sonucGoster = false
                    hatametni = "Lütfen doğru aralıklarda değer giriniz.Girdiğiniz değerler 0 dan büyük 700 den küçük olmalıdır."
                }else {
                    hatametni = ""
                    sonuc = kalpSorgula(cx, arrayListOf(n_age,n_cinsiyet,paintype,rbp,chl,bldsgr,mxhr,exc))
                    sonucGoster = true
                    kalp_gonderildi = false
                }

            }

        },modifier = Modifier.fillMaxWidth()){
            Text(text = "Tahmin et")
        }

        Button(onClick = {
            age = ""
            cinsiyet = 0
            selectedPainType = ""
            restingbp = ""
            chol = ""
            bloodsugar = ""
            exerciseangina = 0
        },modifier = Modifier.fillMaxWidth()){
            Text(text = "Temizle")
        }

        if(sonucGoster) {
            Text("Girdiğiniz değerler ",style = MaterialTheme.typography.headlineSmall)
            Text(
                text = " \n Yaş : $age" +
                        "\n Cinsiyet : $cinsiyet " +
                        "\n Göğüs ağrısı tipi : $selectedPainType" +
                        "\n Kan Basıncı : $restingbp" +
                        "\n Kolesterol : $chol" +
                        "\n Kan şekeri : $bloodsugar" +
                        "\n Egzersiz sırasında göğüs ağrısı varmı? : $exerciseangina",
                style = MaterialTheme.typography.bodyLarge
            )
            if(sonuc != -1.0f) {
                Text("Sonuçlar ",style = MaterialTheme.typography.headlineSmall)
                Text("(Sonuçların tahmini olduğunu unutmayın)",style = MaterialTheme.typography.bodyMedium)
                var sonucyazi = if (sonuc >= 0.5) {
                    "var"
                } else {
                    "yok"
                }
                Text(
                    text = "Sonuç yüzdesi : %${sonuc*100} ve  Kalp hastalığı $sonucyazi",
                    style = MaterialTheme.typography.bodyLarge
                )

            }else {
                Text("Hata oluştu",style = MaterialTheme.typography.bodyLarge)
            }
            internet_gecerlimi = ((mobilveri_gercek && mobilveri_ayar.value) || (wifi_gercek && wifi_ayar.value))
        if(internet_gecerlimi) {
            if (!kalp_gonderildi) {
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
                        internet_gecerlimi = ((mobilveri_gercek && mobilveri_ayar.value) || (wifi_gercek && wifi_ayar.value))
                        if (!kalp_gonderildi && internet_gecerlimi) {
                            val n_age = age.toFloat()
                            val n_cinsiyet = cinsiyet.toFloat()
                            val paintype = selectedPainType.toFloat()
                            val rbp = restingbp.toFloat()
                            val chl = chol.toFloat()
                            val bldsgr = bloodsugar.toFloat()
                            val mxhr = 220 - age.toFloat()
                            val exc = exerciseangina.toFloat()
                            val output = selectedStatus
                            scope.launch {
                                geribildirimSonuc = kalpFeedback(
                                    arrayListOf(
                                        n_age, n_cinsiyet, paintype, rbp, chl, bldsgr, mxhr, exc
                                    ), output,id.intValue
                                )
                            }
                            isPressed = true
                            Log.i("veritabani", "islem : $geribildirimSonuc")
                        }

                    }, modifier = Modifier.fillMaxWidth()) {
                        Text(text = "Geri bildir", color = Color.Black)
                    }
                }
            }
            if (kalp_gonderildi) {
                Text("Daha önce geri bildirim yaptınız.", color = Color.LightGray)
                isPressed = false
            }
            if (isPressed) {
                if (geribildirimSonuc) {
                    Text("Geri bildirim başarıyla eklendi", color = Color.Green)
                    kalp_gonderildi = true

                } else {
                    Text("Geri bildirim eklenirken sorun oluştu.", color = Color.Red)
                }
            }
        }

        }else if(hatametni !=""){
            Text(hatametni,style=MaterialTheme.typography.bodyLarge, color = Color.Red)
        }



    }
}

@Preview(showBackground = true)
@Composable
fun FormPreview2() {
    FormComp2()
}
