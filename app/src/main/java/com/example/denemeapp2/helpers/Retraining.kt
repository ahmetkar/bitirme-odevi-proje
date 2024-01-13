package com.example.denemeapp2.helpers

import android.content.Context
import android.util.Log
import androidx.compose.runtime.mutableIntStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.denemeapp2.R
import io.github.jan.supabase.SupabaseClient
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json


    class Retraining(var supabase : SupabaseClient?, var baglan:BackendHelper.DB?, var cx:Context,var kid:Int = 0) {
        private var genel_siniflar : Array<String> = arrayOf("")
        private var genel_etiketler : Array<String> = arrayOf("")



        suspend fun get_tablo_idx(hangitablo:Models) : Int?{
            return baglan?.get_last_idx_from_table(supabase,hangitablo)
        }

        suspend fun get_last_update_idx(hangitablo:Models) : Int?{
            val lastupdate = baglan?.get_last_update_idx_from_table(supabase,hangitablo,this.kid)
            return lastupdate?.alinan_son_tablo_idx
        }


        suspend fun checkUpdate(){
            try {

                Log.i("update-info", "id->${this.kid}")
                if (this.kid != 0) {

                    var genelupdateInfo : Int? = baglan?.get_count_update(supabase,this.kid,"genelmodel")
                    var diyabetupdateInfo : Int? = baglan?.get_count_update(supabase,this.kid,"diyabetmodel")
                    var kalpupdateInfo : Int? = baglan?.get_count_update(supabase,this.kid,"kalpmodel")

                    if (genelupdateInfo != null) {
                        if(genelupdateInfo > 0) {
                            //Update bilgilerini kontrol et ve yeni güncelleme gerekiyorsa yap
                            var genelBilgi = get_tablo_idx(Models.GENEL)
                            if(genelBilgi!=null) {
                                var last_update_idx = get_last_update_idx(Models.GENEL)
                                Log.i("update-info", "genel update bilgisi var -> $genelBilgi and $last_update_idx")
                                if(genelBilgi == last_update_idx){
                                    Log.i("update-info","Güncelleme yok")
                                }else if(genelBilgi>last_update_idx!!) {
                                    val guncellenecekSayi = genelBilgi?.minus(last_update_idx!!)
                                    Log.i("update-info","Genel modelde $guncellenecekSayi kadar satır güncellenmeli.")
                                    doUpdate(Models.GENEL,last_update_idx!!)
                                }
                            }
                        }else {
                            //Update bilgisi yok
                            Log.i("update-info", "genel update bilgisi yok.")
                            genelsetFirstTime()
                        }
                    }else {
                        Log.i("update-info","genel update sayisi null")
                    }

                    if (kalpupdateInfo != null) {
                        if(kalpupdateInfo > 0) {
                            //Update bilgilerini kontrol et ve yeni güncelleme gerekiyorsa yap
                            var kalpBilgi = get_tablo_idx(Models.KALP)
                            if(kalpBilgi!=null) {
                                var last_update_idx = get_last_update_idx(Models.KALP)
                                Log.i("update-info", "kalp update bilgisi var -> $kalpBilgi and $last_update_idx")
                                if(kalpBilgi <= last_update_idx!!){
                                    Log.i("update-info","Güncelleme yok")
                                }else {
                                    val guncellenecekSayi = kalpBilgi?.minus(last_update_idx)
                                    Log.i("update-info"," Kalp modelde $guncellenecekSayi kadar satır güncellenmeli.")
                                    doUpdate(Models.KALP,last_update_idx)
                                }
                            }
                        }else {
                            //Update bilgisi yok
                            Log.i("update-info", "kalp update bilgisi yok.")
                            kalpsetFirstTime()
                        }
                    }else {
                        Log.i("update-info","kalp update sayisi null")
                    }

                    if (diyabetupdateInfo != null) {
                        if(diyabetupdateInfo > 0) {
                            //Update bilgilerini kontrol et ve yeni güncelleme gerekiyorsa yap
                            var diyabetBilgi = get_tablo_idx(Models.DIYABET)
                            if(diyabetBilgi!=null) {
                                var last_update_idx = get_last_update_idx(Models.DIYABET)
                                Log.i("update-info", "diyabet update bilgisi var -> $diyabetBilgi and $last_update_idx")
                                if(diyabetBilgi <= last_update_idx!!){
                                    Log.i("update-info","Güncelleme yok")
                                }else {
                                    val guncellenecekSayi = diyabetBilgi?.minus(last_update_idx)
                                    Log.i("update-info"," Diyabet modelde $guncellenecekSayi kadar satır güncellenmeli.")
                                    doUpdate(Models.DIYABET,last_update_idx)
                                }
                            }
                        }else {
                            //Update bilgisi yok
                            Log.i("update-info", "diyabet update bilgisi yok.")
                            diyabetsetFirstTime()
                        }
                    }else {
                        Log.i("update-info","diyabet update sayisi null")
                    }

                } else {
                    Log.i("update-info", "kullanici id bulunamadi")
                }
            }catch(ex:Exception){
                Log.i("update-info","Hata : $ex and ${ex.stackTrace.contentToString()}")
            }
        }


        suspend fun genelsetFirstTime(){
            val genelcheckname = "genelcheckpoint-0.ckpt"
            try {
                Log.i("update-info", "genel first time girdi")
                val genel_son_idx = get_tablo_idx(Models.GENEL)
                val currentTimestamp = java.sql.Timestamp(System.currentTimeMillis())
                val datastore = BackendHelper.UpdateDataStore(cx)

                if(genel_son_idx!=null) {
                    if(genel_son_idx > 0) {
                        val genelupdate = doUpdateFirstTime(genelcheckname, Models.GENEL)

                        if (genelupdate) {
                            val newuserUpdategenel = UserUpdateInfoModel(
                                currentTimestamp.toString(),
                                "genelmodel",
                                genel_son_idx,
                                kid,
                                genelcheckname
                            )
                            val genelekle = baglan?.add_user_update_info(supabase, newuserUpdategenel)
                            if (genelekle == true) {
                                Log.i("update-info","veritabanına eklendi.")
                                datastore.saveInfo(
                                    Models.GENEL,
                                    genelcheckname
                                )
                                Log.i("update-info","genel first update datastore kaydedildi : $genelcheckname")

                            }
                        }
                    }else {
                        Log.i("update-info","Genel model geribildirim tablosu boş.")
                    }
                }else {
                    Log.i("update-info","Genel model geribildirim tablosu alınamadı.")
                }

            }catch(ex:Exception){
                Log.i("update-info","Hata 2 : $ex and ${ex.stackTrace.contentToString()}")

            }
        }

        suspend fun kalpsetFirstTime(){
            val kalpcheckname = "kalpcheckpoint-0.ckpt"
            try {

                Log.i("update-info", "kalp first time girdi")
                val currentTimestamp = java.sql.Timestamp(System.currentTimeMillis())
                val datastore = BackendHelper.UpdateDataStore(cx)
                val kalp_son_idx = get_tablo_idx(Models.KALP)


                if(kalp_son_idx!=null) {
                    if(kalp_son_idx>0) {
                        val kalpupdate = doUpdateFirstTime(kalpcheckname, Models.KALP)
                        if (kalpupdate) {
                            val newuserUpdatekalp = UserUpdateInfoModel(
                                currentTimestamp.toString(),
                                "kalpmodel",
                                kalp_son_idx,
                                kid,
                                kalpcheckname
                            )
                            val kalpekle = baglan?.add_user_update_info(supabase, newuserUpdatekalp)
                            if (kalpekle == true) {
                                datastore.saveInfo(
                                    Models.KALP,
                                    kalpcheckname
                                )
                                Log.i("update-info","diyabet first update datastore kaydedildi : $kalpcheckname")
                            }
                        }
                    }else {
                        Log.i("update-info","Kalp model geribildirim tablosu boş.")
                    }
                }else {
                    Log.i("update-info","Kalp model geribildirim tablosu alınamadı.")
                }


            }catch(ex:Exception){
                Log.i("update-info","Hata 2 : $ex and ${ex.stackTrace.contentToString()}")

            }
        }

        suspend fun diyabetsetFirstTime(){
            val diyabetcheckname = "diyabetcheckpoint-0.ckpt"
            try {
                val diyabet_son_idx = get_tablo_idx(Models.DIYABET)
                Log.i("update-info", "diyabet first time girdi")
                val currentTimestamp = java.sql.Timestamp(System.currentTimeMillis())
                val datastore = BackendHelper.UpdateDataStore(cx)
                if(diyabet_son_idx!= null) {
                    if(diyabet_son_idx>0) {
                        val diyabetupdate = doUpdateFirstTime(diyabetcheckname, Models.DIYABET)
                        if (diyabetupdate) {
                            val newuserUpdatediyabet = UserUpdateInfoModel(
                                currentTimestamp.toString(),
                                "diyabetmodel",
                                diyabet_son_idx,
                                kid,
                                diyabetcheckname
                            )
                            val diyabetekle =
                                baglan?.add_user_update_info(supabase, newuserUpdatediyabet)
                            if (diyabetekle == true) {
                                datastore.saveInfo(
                                    Models.DIYABET,
                                   diyabetcheckname
                                )
                                Log.i("update-info","diyabet first update datastore kaydedildi : $diyabetcheckname")
                            }
                        }
                    }else {
                        Log.i("update-info","Diyabet model geribildirim tablosu boş.")
                    }
                }else {
                    Log.i("update-info","Diyabet model geribildirim tablosu alınamadı.")
                }
            }catch(ex:Exception){
                Log.i("update-info","Hata 2 : $ex and ${ex.stackTrace.contentToString()}")

            }
        }


        @Serializable
        data class GenelOzellik(val keys:List<String>,val values:List<Float>)

        suspend fun genelVeriHazirla(fromId : Int) : Pair<ArrayList<FloatArray>,ArrayList<FloatArray>>?{
            try {
                var genel_veriler = baglan?.get_new_genel_datas(supabase, fromId)
                if (genel_veriler != null) {
                    Log.i("update-info","genel veriler geldi. ${genel_veriler.size}")
                    genel_siniflar =  cx.resources.getStringArray(R.array.genelmodel_siniflar)
                    genel_etiketler =  cx.resources.getStringArray(R.array.genelmodel_etiketler)
                    var inputs : ArrayList<FloatArray>  = arrayListOf()
                    var outputs:ArrayList<FloatArray> = arrayListOf()
                    for (i in genel_veriler) {
                        var girdiler:MutableList<Float> = MutableList(132){0.0f}
                        var etiket:MutableList<Float> = MutableList(1){0f}
                        var ozellikler = Json.decodeFromString<GenelOzellik>(i.ozellikler)
                        Log.i("update-info","${ozellikler.keys}")
                        for(key in ozellikler.keys){
                                val idx = genel_siniflar.indexOf(key)
                                if(idx!=-1) girdiler[idx] = 1.0f
                        }
                        val etiketidx = genel_etiketler.indexOf(i.prognosis)
                        if(etiketidx!=-1) etiket[0] = etiketidx.toFloat()
                        inputs.add(girdiler.toFloatArray())
                        outputs.add(etiket.toFloatArray())
                    }
                    for(i in  0 until inputs.size) {
                        Log.i(
                            "update-info",
                            " girdiler ${inputs[i].joinToString(",")} ve etiket : ${
                                outputs[i].joinToString(",")
                            } "
                        )
                    }
                    return Pair(inputs,outputs)
                }else {
                    Log.i("update-info","genel veriler alınamadı : $genel_veriler")
                    return null
                }

            }catch(ex:Exception){
                Log.i("update-info","hata oluştu : $ex")
                return null
            }

        }

        suspend fun kalpVeriHazirla(fromId : Int) : Pair<ArrayList<FloatArray>,ArrayList<FloatArray>>?{
            var kalp_veriler = baglan?.get_new_kalp_datas(supabase, fromId)
            if(kalp_veriler!=null){
                var inputs : ArrayList<FloatArray>  = arrayListOf()
                var outputs:ArrayList<FloatArray> = arrayListOf()
                for(i in kalp_veriler){
                    var girdiler : MutableList<Float> = mutableListOf(
                        i.Age_1.toFloat(),i.Age_2.toFloat(),i.Age_3.toFloat(),i.Age_4.toFloat(),
                        i.Sex_0.toFloat(),i.Sex_1.toFloat(),i.ChestPainType_0.toFloat(),
                        i.ChestPainType_1.toFloat(),i.ChestPainType_2.toFloat(),i.ChestPainType_3.toFloat(),
                        i.RestingBP_0.toFloat(),i.RestingBP_1.toFloat(),i.RestingBP_2.toFloat(),
                        i.Cholesterol_0.toFloat(),i.Cholesterol_1.toFloat(),i.Cholesterol_2.toFloat(),
                        i.FastingBS_0.toFloat(),i.FastingBS_1.toFloat(),
                        i.MaxHR_0.toFloat(),i.MaxHR_1.toFloat(),i.MaxHR_2.toFloat(),i.MaxHR_3.toFloat(),
                        i.MaxHR_4.toFloat(),i.ExerciseAngina_0.toFloat(),i.ExerciseAngina_1.toFloat(),
                        )
                    var cikti = FloatArray(1) {i.HeartDisease.toFloat()}

                    Log.i("update-info","class : $i")
                    Log.i("update-info","$girdiler ve $cikti ")

                    inputs.add(girdiler.toFloatArray())
                    outputs.add(cikti)
                }

                return Pair(inputs,outputs)

            }

            return null

        }

        suspend fun diyabetVeriHazirla(fromId : Int) : Pair<ArrayList<FloatArray>,ArrayList<FloatArray>>? {
            var diyabet_veriler = baglan?.get_new_diyabet_datas(supabase, fromId)
            if (diyabet_veriler != null) {
                var inputs: ArrayList<FloatArray> = arrayListOf()
                var outputs: ArrayList<FloatArray> = arrayListOf()
                for (i in diyabet_veriler) {
                    var girdiler: MutableList<Float> = mutableListOf(
                        i.Pregnancies_0.toFloat(),
                        i.Pregnancies_1.toFloat(),
                        i.Pregnancies_2.toFloat(),
                        i.Glucose_0.toFloat(),
                        i.Glucose_1.toFloat(),
                        i.BloodPressure_0.toFloat(),
                        i.BloodPressure_1.toFloat(),
                        i.BloodPressure_2.toFloat(),
                        i.BloodPressure_3.toFloat(),
                        i.SkinThickness_0.toFloat(),
                        i.SkinThickness_1.toFloat(),
                        i.SkinThickness_2.toFloat(),
                        i.Insulin_0.toFloat(),
                        i.Insulin_1.toFloat(),
                        i.Insulin_2.toFloat(),
                        i.Insulin_3.toFloat(),
                        i.BMI_0.toFloat(),
                        i.BMI_1.toFloat(),
                        i.BMI_2.toFloat(),
                        i.BMI_3.toFloat(),
                        i.Age_1.toFloat(),
                        i.Age_2.toFloat(),
                        i.Age_3.toFloat(),
                        i.Age_4.toFloat(),
                    )
                    var cikti = FloatArray(1) { i.Outcome.toFloat() }

                    Log.i("update-info", "diyabet class : $i")
                    Log.i("update-info", "diyabet $girdiler ve $cikti ")

                    inputs.add(girdiler.toFloatArray())
                    outputs.add(cikti)
                }

                return Pair(inputs, outputs)
            }
            return null
        }

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

        suspend fun doUpdate(hangimodel: Models,fromId:Int) : Boolean{
            if(hangimodel == Models.GENEL){
                var genel_checkpoints : String? = getLastCheckpoint(Models.GENEL,cx)
                Log.i("interpreter-checkpoint","genel checkpoints alındı : ${genel_checkpoints}")
                if(genel_checkpoints!=null) {
                    Log.i("update-info", " genel $fromId den itibaren güncellenecek.")
                    var veriler = genelVeriHazirla(fromId)
                    if (veriler != null) {
                        Log.i("update-info", "genel veriler başarıyla geldi.")
                        val sonuc = updateGenel(veriler.first, veriler.second, genel_checkpoints, false)
                        return false
                    }
                }else {
                    Log.i("update-info","genel checkpoints kaydı bulunamadı")
                }
            }
            if(hangimodel == Models.DIYABET){
                var diyabet_checkpoints : String? = getLastCheckpoint(Models.DIYABET,cx)
                if(diyabet_checkpoints!=null) {
                    Log.i("update-info", " diyabet $fromId den itibaren güncellenecek.")
                    var veriler = diyabetVeriHazirla(fromId)
                    if (veriler != null) {
                        Log.i("update-info", "diyabet veriler başarıyla geldi.")
                        val sonuc = updateDiyabet(veriler.first, veriler.second, diyabet_checkpoints, false)
                        return false
                    }
                }else {
                    Log.i("update-info","diyabet checkpoints kaydı bulunamadı")
                }
            }
            if(hangimodel == Models.KALP){
                var kalp_checkpoints :String? = getLastCheckpoint(Models.KALP,cx)
                if(kalp_checkpoints!=null) {
                    Log.i("update-info", " kalp $fromId den itibaren güncellenecek.")
                    var veriler = kalpVeriHazirla(fromId)
                    if (veriler != null) {
                        Log.i("update-info", "diyabet veriler başarıyla geldi.")
                        val sonuc = updateKalp(veriler.first, veriler.second, kalp_checkpoints, false)
                        return false
                    }
                }else {
                    Log.i("update-info","kalp checkpoints kaydı bulunamadı")
                }
            }

            return false

        }

        suspend fun doUpdateFirstTime(checkpointname:String, hangimodel:Models) : Boolean{

            if(hangimodel == Models.GENEL){
                Log.i("update-info","genel first update girdi.")
                var veriler = genelVeriHazirla(0)
                if(veriler!=null){
                    Log.i("update-info","genel veriler başarıyla geldi.")
                    val sonuc = updateGenel(veriler.first,veriler.second, checkpointname,true)
                    return sonuc
                }
            }else if(hangimodel == Models.KALP){
                    Log.i("update-info","kalp first update girdi.")
                    var veriler = kalpVeriHazirla(0)
                    if(veriler!=null){
                    Log.i("update-info","kalp first update girdi.")
                    val sonuc = updateKalp(veriler.first,veriler.second,checkpointname,true)
                    return sonuc
                    }

            }else if(hangimodel == Models.DIYABET){
                Log.i("update-info","diyabet first update girdi.")
                var veriler = diyabetVeriHazirla(0)
                if(veriler!=null){
                    Log.i("update-info","diyabet first update girdi.")
                    val sonuc = updateDiyabet(veriler.first,veriler.second,checkpointname,true)
                    return sonuc
                }
            }

            return false
        }

        fun updateDiyabet(inputArr:ArrayList<FloatArray>,outputArr:ArrayList<FloatArray>,checkpoint:String,firstTime:Boolean) : Boolean{
            return try {
                var d = ModelActions(cx,"diyabetmodel.tflite")
                val sonuc = d.restoreAndDotrain(cx,inputArr,outputArr,50,inputArr.size,checkpoint,firstTime)
                sonuc
            }catch (e:Exception){
                Log.i("interpreter-hata-diyabet",e.toString())
                false
            }
        }

        fun updateGenel(inputArr:ArrayList<FloatArray>,outputArr:ArrayList<FloatArray>,checkpoint:String,firstTime: Boolean) : Boolean{
            return try {
                var d = ModelActions(cx,"genelmodel.tflite")
               val sonuc =  d.restoreAndDotrain(cx,inputArr,outputArr,10,inputArr.size,checkpoint,firstTime)
                sonuc
            }catch (e:Exception){
                Log.i("interpreter-hata-genel",e.toString())
                false
            }
        }

        fun updateKalp(inputArr:ArrayList<FloatArray>,outputArr:ArrayList<FloatArray>,checkpoint:String,firstTime: Boolean) : Boolean{
            return try {
                var d = ModelActions(cx,"heartmodel.tflite")
                val sonuc = d.restoreAndDotrain(cx,inputArr,outputArr,50,inputArr.size,checkpoint,firstTime)
                sonuc
            }catch (e:Exception){
                Log.i("interpreter-hata-kalp",e.toString())
                false
            }
        }

    }





