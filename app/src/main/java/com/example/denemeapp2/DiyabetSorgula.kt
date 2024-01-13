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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
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
import com.example.denemeapp2.helpers.DiyabetDataModel
import com.example.denemeapp2.helpers.SettingsHelper
import com.example.denemeapp2.helpers.ModelActions
import com.example.denemeapp2.helpers.ModelHelper
import com.example.denemeapp2.helpers.Models
import com.example.denemeapp2.ui.theme.DenemeApp2Theme
import io.github.jan.supabase.SupabaseClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.nio.FloatBuffer


private val baglan: BackendHelper.DB = BackendHelper.DB()
private var init : SupabaseClient? = baglan.init()
private var diyabet_gonderildi = false

private var dlabels : Array<String> = arrayOf("")


private var wifi_ayar = mutableStateOf(false)
private var mobilveri_ayar =  mutableStateOf(false)

private var id = mutableIntStateOf(-1)

private var checkpoint :String? = ""

@Composable
fun DiyabetSorgula() {
    val cx = LocalContext.current
    diyabet_gonderildi = false
    dlabels = cx.resources.getStringArray(R.array.diyabetmodel_siniflar)

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
        checkpoint = getLastCheckpoint(Models.DIYABET,cx)
        Log.i("interpreter-checkpoint","alindi "+checkpoint)
    }


    DenemeApp2Theme {
        FormPreview3()

    }

}

private fun diyabetSorgula(cx:Context, girdiler: ArrayList<Float>) : Float{
    try {
        var d = ModelActions(cx,"diyabetmodel.tflite")
        var yenigirdiler = ModelHelper.diabetgirdiAl(girdiler)
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
            sonuc = d.doPredict(yenigirdiler, outputArr)
        }

        if(sonuc.isNotEmpty()) {
            var cb = sonuc["output"]
            if (cb is FloatBuffer) {
                Log.i("interpreter-sonuc-once: ", cb.get(0).toString())
                return cb.get(0);
            }
        }
        return -1.0f
    }catch(e:Exception){
        Log.e("interpreter-hata",e.message.toString())
        return -1.0f
    }
}

private suspend fun diyabetFeedback(girdiler:List<Float>?, sonuc: Int,kid:Int) : Boolean{
    var ekle = false
    if(girdiler!=null) {
        var yeni_girdiler: List<Float> = ModelHelper.diabetgirdiAl(girdiler).toList()
        var yeni_model = DiyabetDataModel(
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
            yeni_girdiler[23].toInt(),
            sonuc, kid
        )

        withContext(Dispatchers.IO) {
            ekle = baglan.diyabet_feedback(init, yeni_model)
            Log.i("veritabani_ekle-diabet", "Eklendi : $ekle")
        }
    }
    return ekle
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormComp3(){
    val cx = LocalContext.current
    var pregnancies by rememberSaveable { mutableStateOf("") }
    var age by rememberSaveable { mutableStateOf("") }
    var glicose by rememberSaveable { mutableStateOf("") }
    var diastolicbp by rememberSaveable { mutableStateOf("") }
    var thickness by rememberSaveable { mutableStateOf("") }
    var insulin by rememberSaveable { mutableStateOf("") }
    var kilo by rememberSaveable { mutableStateOf("") }
    var boy by rememberSaveable { mutableStateOf("") }
    var sonucGoster by remember {mutableStateOf(false)}
    var hatametni by rememberSaveable { mutableStateOf("") }

    var selectedStatus by remember { mutableIntStateOf(2) }
    var isPressed by remember  { mutableStateOf(false) }
    var geribildirimSonuc by remember  { mutableStateOf(false) }

    var wifi_gercek by remember {mutableStateOf(false)}
    var mobilveri_gercek by remember { mutableStateOf(false) }


    var internet_gecerlimi by remember { mutableStateOf(false) }


    var durumlar :List<Boolean> = SettingsHelper.isInternetAvailable(cx)
    wifi_gercek = durumlar[0]
    mobilveri_gercek = durumlar[1]


    val scope = rememberCoroutineScope()

    Column( modifier = Modifier
        .padding(16.dp, 16.dp)
        .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = "Belirtilerinizi girin", fontWeight = FontWeight.Bold,style = MaterialTheme.typography.bodyLarge)
        Spacer(modifier = Modifier.padding(8.dp))

        Text(
            text = dlabels[0],
            style = MaterialTheme.typography.bodyLarge
        )

        TextField(
            modifier = Modifier.fillMaxWidth(),
            value = pregnancies,
            onValueChange = { pregnancies = it },
            placeholder = { Text(text = "Örnek : 7 ay (Erkekseniz girmeyin)")},
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number),
        )
        Spacer(modifier = Modifier.padding(8.dp))

        Text(
            text = dlabels[1],
            style = MaterialTheme.typography.bodyLarge
        )

        TextField(
            modifier = Modifier.fillMaxWidth(),
            value = glicose,
            onValueChange = { glicose = it },
            placeholder = { Text(text = "Örnek : 140 mg/dl")},
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number),
        )
        Spacer(modifier = Modifier.padding(8.dp))

        Text(
            text = dlabels[2],
            style = MaterialTheme.typography.bodyLarge
        )

        TextField(
            modifier = Modifier.fillMaxWidth(),
            value = diastolicbp,
            onValueChange = { diastolicbp = it },
            placeholder = { Text(text = "Örnek : 80 mmHg")},
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number),
        )
        Spacer(modifier = Modifier.padding(8.dp))

        Text(
            text = dlabels[3],
            style = MaterialTheme.typography.bodyLarge
        )

        TextField(
            modifier = Modifier.fillMaxWidth(),
            value = thickness,
            onValueChange = { thickness = it },
            placeholder = { Text(text = "Örnek : 20 mm")},
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number),
        )
        Spacer(modifier = Modifier.padding(8.dp))

        Text(
            text = dlabels[4],
            style = MaterialTheme.typography.bodyLarge
        )

        TextField(
            modifier = Modifier.fillMaxWidth(),
            value = insulin,
            onValueChange = { insulin = it },
            placeholder = { Text(text = "Örnek : 180 mU/ml")},
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number),
        )
        Spacer(modifier = Modifier.padding(8.dp))

        Text(
            text = "Kilo :",
            style = MaterialTheme.typography.bodyLarge
        )

        TextField(
            modifier = Modifier.fillMaxWidth(),
            value = kilo,
            onValueChange = { kilo = it },
            placeholder = { Text(text = "Örnek : 50 kg")},
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number),
        )
        Spacer(modifier = Modifier.padding(8.dp))

        Text(
            text = "Boy :",
            style = MaterialTheme.typography.bodyLarge
        )

        TextField(
            modifier = Modifier.fillMaxWidth(),
            value = boy,
            onValueChange = { boy = it },
            placeholder = { Text(text = "Örnek : 1.75")},
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number),
        )
        Spacer(modifier = Modifier.padding(8.dp))

        Text(
            text = dlabels[6],
            style = MaterialTheme.typography.bodyLarge
        )

        TextField(
            modifier = Modifier.fillMaxWidth(),
            value = age,
            onValueChange = { age = it },
            placeholder = { Text(text = "Örnek : 18")},
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number),
        )
        Spacer(modifier = Modifier.padding(8.dp))
        val cx = LocalContext.current
        var sonuc by remember { mutableFloatStateOf(-1.0f)}

Button(onClick = {
if(age == "" || kilo=="" ||  glicose == ""
    || boy== "" || diastolicbp == "" || insulin=="" ) {
    sonucGoster = false
    hatametni = "Lütfen tüm alanları doldurunuz."
}else {
    var w = kilo.toFloat()
    var h = boy.toFloat()
    if(h>4 && h<400){
        h /= 100
        boy = h.toString()
    }
    var bmi = w / (h * h)
    var preg = 0.0f
    if(pregnancies != ""){
        preg = pregnancies.toFloat()
    }
    var glic = glicose.toFloat()
    var dcbp = diastolicbp.toFloat()
    var thck = thickness.toFloat()
    var ins = insulin.toFloat()
    var nage = age.toFloat()

    if(w > 500 || w < 0 || h>400 || h<0 || preg >12 || preg<0 || glic > 700 || glic < 0 || thck > 100 || thck < 0
        || ins > 700 || ins <0|| nage>200  || nage<0){
        sonucGoster = false
        hatametni = "Lütfen doğru aralıklarda değer giriniz.Girdiğiniz değerler çok büyük veya sıfırdan küçük"
    }else {
        sonuc = diyabetSorgula(cx,arrayListOf(preg,glic,dcbp,thck,ins,bmi,nage))
        hatametni = ""
        sonucGoster = true
        diyabet_gonderildi = false
    }
}
},modifier = Modifier.fillMaxWidth()){
Text(text = "Tahmin et")
}
Button(onClick = {
kilo = ""
boy = ""
pregnancies = ""
glicose = ""
diastolicbp = ""
thickness = ""
insulin = ""
age = ""
},modifier = Modifier.fillMaxWidth()){
Text(text = "Temizle")
}

if(sonucGoster) {
Text("Girdiğiniz değerler ",style = MaterialTheme.typography.headlineSmall)
Text(
    text = "Yaş : $age" +
            "\n Boy : $boy"+
            "\n Kilo : $kilo"+
            "\n Hamile kalınan ay : $pregnancies " +
            "\n Glikoz testi : $glicose" +
            "\n Diastolic Kan Basıncı : $diastolicbp" +
            "\n Kas kalınlığı : $thickness" +
            "\n Insulin : $insulin",
    style = MaterialTheme.typography.bodyLarge
)

if(sonuc != -1.0f) {
    Text("Sonuçlar ",style = MaterialTheme.typography.headlineSmall)
    Text("(Sonuçların tahmini olduğunu unutmayın)",style = MaterialTheme.typography.bodyMedium)
    val sonucyazi = if (sonuc >= 0.5) "var" else "yok"
    Text(
        text = "Sonuç yüzdesi %${sonuc*100} ve Diyabet ihtimali $sonucyazi",
        style = MaterialTheme.typography.bodyLarge
    )
    internet_gecerlimi = ((mobilveri_gercek && mobilveri_ayar.value) || (wifi_gercek && wifi_ayar.value))
    if(internet_gecerlimi) {
        if (!diyabet_gonderildi) {
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
                    if (!diyabet_gonderildi && internet_gecerlimi) {
                        val w = kilo.toFloat()
                        var h = boy.toFloat()
                        if (h > 4 && h < 400) {
                            h /= 100
                            boy = h.toString()
                        }
                        val bmi = w / (h * h)
                        var preg = 0.0f
                        if (pregnancies != "") {
                            preg = pregnancies.toFloat()
                        }
                        val glic = glicose.toFloat()
                        val dcbp = diastolicbp.toFloat()
                        val thck = thickness.toFloat()
                        val ins = insulin.toFloat()
                        val nage = age.toFloat()
                        val output = selectedStatus
                        scope.launch {
                            geribildirimSonuc = diyabetFeedback(
                                arrayListOf(
                                    preg, glic, dcbp, thck, ins, bmi, nage
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

        if (diyabet_gonderildi) {
            Text("Daha önce geri bildirim yaptınız.", color = Color.LightGray)
            isPressed = false
        }

        if (isPressed) {
            if (geribildirimSonuc) {
                Text("Geri bildirim başarıyla eklendi", color = Color.Green)
                diyabet_gonderildi = true
            } else {
                Text("Geri bildirim eklenirken sorun oluştu.", color = Color.Red)
            }
        }
    }
}else {
    Text("Tahmin yapılırken bir hata oluştu",style = MaterialTheme.typography.bodyLarge)
}


}else if(hatametni !=""){
Text(hatametni,style=MaterialTheme.typography.bodyLarge, color = Color.Red)
}


}


}



@Preview(showBackground = true)
@Composable
fun FormPreview3() {
FormComp3()
}

