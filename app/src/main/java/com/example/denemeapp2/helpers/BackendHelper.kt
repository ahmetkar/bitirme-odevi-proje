package com.example.denemeapp2.helpers


import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Count
import io.github.jan.supabase.postgrest.result.PostgrestResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable


@Serializable
data class UserAdd(val kayit_tarihi:String)

@Serializable
data class UserGet(val id:Int,val kayit_tarihi:String)

@Serializable
data class GetDiyabetDataModel(
    val id:Int,
    val Pregnancies_0:Int,
    val Pregnancies_1:Int,
    val Pregnancies_2:Int,
    val Glucose_0:Int,
    val Glucose_1:Int,
    val BloodPressure_0:Int,
    val BloodPressure_1:Int,
    val BloodPressure_2:Int,
    val BloodPressure_3:Int,
    val SkinThickness_0:Int,
    val SkinThickness_1:Int,
    val SkinThickness_2:Int,
    val Insulin_0:Int,
    val Insulin_1:Int,
    val Insulin_2:Int,
    val Insulin_3:Int,
    val BMI_0:Int,
    val BMI_1:Int,
    val BMI_2:Int,
    val BMI_3:Int,
    val Age_1:Int,
    val Age_2:Int,
    val Age_3:Int,
    val Age_4:Int,
    val Outcome:Int,
    val kid:Int
)

@Serializable
data class DiyabetDataModel(
    val Pregnancies_0:Int,
    val Pregnancies_1:Int,
    val Pregnancies_2:Int,
    val Glucose_0:Int,
    val Glucose_1:Int,
    val BloodPressure_0:Int,
    val BloodPressure_1:Int,
    val BloodPressure_2:Int,
    val BloodPressure_3:Int,
    val SkinThickness_0:Int,
    val SkinThickness_1:Int,
    val SkinThickness_2:Int,
    val Insulin_0:Int,
    val Insulin_1:Int,
    val Insulin_2:Int,
    val Insulin_3:Int,
    val BMI_0:Int,
    val BMI_1:Int,
    val BMI_2:Int,
    val BMI_3:Int,
    val Age_1:Int,
    val Age_2:Int,
    val Age_3:Int,
    val Age_4:Int,
    val Outcome:Int,
    val kid:Int
)

@Serializable
data class KalpDataModel(
    val Age_1:Int,
    val Age_2:Int,
    val Age_3:Int,
    val Age_4:Int,
    val Sex_0:Int,
    val Sex_1:Int,
    val ChestPainType_0:Int,
    val ChestPainType_1:Int,
    val ChestPainType_2:Int,
    val ChestPainType_3:Int,
    val RestingBP_0:Int,
    val RestingBP_1:Int,
    val RestingBP_2:Int,
    val Cholesterol_0:Int,
    val Cholesterol_1:Int,
    val Cholesterol_2:Int,
    val FastingBS_0:Int,
    val FastingBS_1:Int,
    val MaxHR_0:Int,
    val MaxHR_1:Int,
    val MaxHR_2:Int,
    val MaxHR_3:Int,
    val MaxHR_4:Int,
    val ExerciseAngina_0:Int,
    val ExerciseAngina_1:Int,
    val HeartDisease:Int,
    val kid:Int
)

@Serializable
data class GetKalpDataModel(
    val id:Int,
    val Age_1:Int,
    val Age_2:Int,
    val Age_3:Int,
    val Age_4:Int,
    val Sex_0:Int,
    val Sex_1:Int,
    val ChestPainType_0:Int,
    val ChestPainType_1:Int,
    val ChestPainType_2:Int,
    val ChestPainType_3:Int,
    val RestingBP_0:Int,
    val RestingBP_1:Int,
    val RestingBP_2:Int,
    val Cholesterol_0:Int,
    val Cholesterol_1:Int,
    val Cholesterol_2:Int,
    val FastingBS_0:Int,
    val FastingBS_1:Int,
    val MaxHR_0:Int,
    val MaxHR_1:Int,
    val MaxHR_2:Int,
    val MaxHR_3:Int,
    val MaxHR_4:Int,
    val ExerciseAngina_0:Int,
    val ExerciseAngina_1:Int,
    val HeartDisease:Int,
    val kid:Int
)


@Serializable
data class GetGenelDataModel(
    val id:Int,
    val ozellikler:String,
    val prognosis:String,
    val yuzde:String,
    val geribildirim:Int,
    val kid:Int
)

@Serializable
data class GenelDataModel(
    val ozellikler:String,
    val prognosis:String,
    val yuzde:String,
    val geribildirim:Int,
    val kid:Int
)

@Serializable
data class UserUpdateInfoModel(
    val ne_zaman:String,
    val hangi_model:String,
    val alinan_son_tablo_idx:Int,
    val kullanici_id:Int,
    val checkpointname:String
)

@Serializable
data class GetUserUpdateInfoModel(
    val id:Int,
    val ne_zaman:String,
    val hangi_model:String,
    val alinan_son_tablo_idx:Int,
    val kullanici_id:Int,
    val checkpointname:String
)

class BackendHelper {

    class UserDataStore(private val context: Context) {
        companion object {
            private val Context.dataStore : DataStore<Preferences> by preferencesDataStore("user")
            val user_key = stringPreferencesKey("user_id")
        }

        val getUserId : Flow<String?> = context.dataStore.data.map {
                preferences ->
            preferences[user_key] ?: ""
        }

        suspend fun saveID(id : Int){
            context.dataStore.edit { preferences->
                preferences[user_key] = id.toString()
            }
        }

        suspend fun removeID() {
                context.dataStore.edit { preferences ->
                    preferences.remove(user_key)
                }
        }
    }


    class UpdateDataStore(private val context:Context){

        companion object {
            private val Context.dataStore2 : DataStore<Preferences> by preferencesDataStore("update")
            val genel_update_key = stringPreferencesKey("genel_update_path")
            val diyabet_update_key = stringPreferencesKey("diyabet_update_path")
            val kalp_update_key = stringPreferencesKey("kalp_update_path")
        }


        fun getUpdateInfo(hangimodel:Models) : Flow<String?>?{
            try {
                when (hangimodel) {
                    Models.GENEL -> {
                        return context.dataStore2.data.map { preferences ->
                            preferences[genel_update_key] ?: ""
                        }
                    }

                    Models.KALP -> {
                        return context.dataStore2.data.map { preferences ->
                            preferences[diyabet_update_key] ?: ""
                        }
                    }

                    Models.DIYABET -> {
                        return context.dataStore2.data.map { preferences ->
                            preferences[kalp_update_key] ?: ""
                        }
                    }

                    else -> return null
                }
            }catch(ex:Exception){
                Log.i("interpreter-checkpoints-hata",ex.toString()+"ve"+ex.stackTrace.contentToString())
                return null
            }
        }


        suspend fun saveInfo(hangimodel:Models,newcheck:String){
            if(!newcheck.isNullOrEmpty()){
                context.dataStore2.edit { preferences ->
                    if(hangimodel == Models.GENEL) {
                        preferences[genel_update_key] = newcheck
                    }else if(hangimodel == Models.DIYABET){
                        preferences[diyabet_update_key] = newcheck
                    }else if(hangimodel == Models.KALP){
                        preferences[kalp_update_key] = newcheck
                    }
                }
            }
        }

        suspend fun removeInfo(hangimodel: Models) {

            context.dataStore2.edit { preferences ->
                if(hangimodel == Models.GENEL) {
                    preferences.remove(genel_update_key)
                }else if(hangimodel == Models.DIYABET){
                    preferences.remove(diyabet_update_key)
                }else if(hangimodel == Models.KALP){
                    preferences.remove(kalp_update_key)
                }

            }
        }
    }

    class DB {

        fun init() : SupabaseClient? {
            try {
                val supabase = createSupabaseClient(
                    supabaseUrl = "...",
                    supabaseKey = "..."
                ) 
                    install(Auth)
                    install(Postgrest)
                }
                return supabase
            }catch(e :Exception){
                Log.e("veritabani_hata1",e.toString())
                return null
            }

        }


         suspend fun user_get(supabase: SupabaseClient?,id : Int) : UserGet? {
            var sonuc : PostgrestResult? = supabase?.from("kullanicilar")?.
            select(columns = Columns.list( "id","kayit_tarihi")){
                filter {
                    eq("id",id)
                }
            }
            var veri = sonuc?.decodeSingle<UserGet>()
            if (veri != null) {
                Log.e("veritabani","kullanici verisi alındı : ${veri.id}")
                return veri
            }else {
                Log.e("veritabani","nullv")
                return null
            }
        }

        suspend fun user_add(supabase: SupabaseClient?,user : UserAdd) : Int?{
                //Aynı zamanda datastore kullanıcının id si eklenecek.
                var ekle = supabase?.from("kullanicilar")?.insert(user){
                    select(columns = Columns.list( "id","kayit_tarihi"))
                }?.decodeSingle<UserGet>()
                if (ekle != null) {
                    Log.e("veritabani","${ekle.id} eklendi.")
                    return ekle.id
                }else {
                    Log.e("veritabani_hata","null_kullanici")
                    return null
                }
        }

        suspend fun diyabet_feedback(supabase: SupabaseClient?,diyabet:DiyabetDataModel) : Boolean{
            var ekle = supabase?.from("dymodelfeedback")?.insert(diyabet)
            if (ekle != null) {
                Log.e("veritabani","diyabet eklendi.")
                return true
            }else {
                Log.e("veritabani_hata","null_diyabet")
                return false
            }
        }

        suspend fun kalp_feedback(supabase: SupabaseClient?,kalp:KalpDataModel) : Boolean{
            val ekle = supabase?.from("hmodelfeedback")?.insert(kalp)
            if (ekle != null) {
                Log.e("veritabani","kalp eklendi ")
                return true
            }else {
                Log.e("veritabani_hata","null_kalp")
                return false
            }
        }

        suspend fun genel_feedback(supabase: SupabaseClient?,genel:GenelDataModel) : Boolean{
            val ekle = supabase?.from("gnmodelfeedback")?.insert(genel)
            if (ekle != null) {
                Log.e("veritabani","genel eklendi ")
                return true
            }else {
                Log.e("veritabani_hata","null_genel")
                return false
            }
        }

        suspend fun get_last_idx_from_table(supabase:SupabaseClient?,hangi_tablo:Models) : Int?{
            var tablo = ""
            if(hangi_tablo == Models.GENEL){
                tablo = "gnmodelfeedback"
            }else if(hangi_tablo == Models.DIYABET){
                tablo = "dymodelfeedback"
            }else if(hangi_tablo == Models.KALP){
                tablo = "hmodelfeedback"
            }
            val count = supabase?.from(tablo)?.select(head = true) {
                    count(Count.EXACT)
                }
            val countnum = count?.countOrNull()

            return countnum?.toInt()
        }
        suspend fun get_last_update_idx_from_table(supabase:SupabaseClient?,hangi_model:Models,kid:Int) : GetUserUpdateInfoModel?{
            var tablo = ""
            if(hangi_model == Models.GENEL){
                tablo = "genelmodel"
            }else if(hangi_model == Models.DIYABET){
                tablo = "diyabetmodel"
            }else if(hangi_model == Models.KALP){
                tablo = "kalpmodel"
            }
            val sorgu = supabase?.from("alinan_guncellemeler")?.select() {
                filter {
                    and {
                        eq("hangi_model", tablo)
                        eq("kullanici_id", kid)
                    }
                }
            }
            val result = sorgu?.decodeList<GetUserUpdateInfoModel>()
            return result?.get(result.size-1)
        }
        suspend fun get_count_update(supabase:SupabaseClient?,kid:Int,hangimodel:String) : Int? {

            val count = supabase?.from("alinan_guncellemeler")?.select(head = true) {
               filter {
                   UserUpdateInfoModel::kullanici_id eq kid
                   UserUpdateInfoModel::hangi_model eq hangimodel
               }
                count(Count.EXACT)
            }
            val countnum = count?.countOrNull()

            return countnum?.toInt()
        }

        suspend fun add_user_update_info(supabase:SupabaseClient?,userUpdate:UserUpdateInfoModel) : Boolean{
            val ekle = supabase?.from("alinan_guncellemeler")?.insert(userUpdate)
            if (ekle != null) {
                Log.e("veritabani-userupdate","güncelleme bilgisi eklendi ")
                return true
            }else {
                Log.e("veritabani-userupdate-hata","null")
                return false
            }

        }



        suspend fun get_new_kalp_datas(supabase:SupabaseClient?,fromId:Int) : List<GetKalpDataModel>?{
            var sonuc  = supabase?.from("hmodelfeedback")?.select() {
                filter {
                    GetKalpDataModel::id gt fromId
                }
            }?.decodeList<GetKalpDataModel>()
            if (sonuc != null) {
                if(sonuc.isNotEmpty()){
                    Log.e("veritabani-kalp","veriler alındı.")
                    return sonuc
                }else {
                    Log.e("veritabani-kalp","bos")
                    return null
                }
            }else {
                Log.e("veritabani-kalp","null")
                return null
            }
        }
        suspend fun get_new_diyabet_datas(supabase:SupabaseClient?,fromId:Int) : List<GetDiyabetDataModel>?{
            var sonuc  = supabase?.from("dymodelfeedback")?.select() {
                filter {
                    GetDiyabetDataModel::id gt fromId
                }
            }?.decodeList<GetDiyabetDataModel>()
            if (sonuc != null) {
                if(sonuc.isNotEmpty()){
                    Log.e("veritabani-diyabet","veriler alındı.")
                    return sonuc
                }else {
                    Log.e("veritabani-diyabet","bos")
                    return null
                }
            }else {
                Log.e("veritabani-diyabet","null")
                return null
            }
        }
        suspend fun get_new_genel_datas(supabase:SupabaseClient?,fromId:Int) : List<GetGenelDataModel>?{
            var sonuc  = supabase?.from("gnmodelfeedback")?.select() {
                filter {
                    GetGenelDataModel::id gt fromId
                }
            }?.decodeList<GetGenelDataModel>()
            if (sonuc != null) {
                if(sonuc.isNotEmpty()){
                    Log.e("veritabani-genel","veriler alındı.")
                    return sonuc
                }else {
                    Log.e("veritabani-genel","bos")
                    return null
                }
            }else {
                Log.e("veritabani-genel","null")
                return null
            }
        }

    }

}


/*
suspend fun users_get(supabase: SupabaseClient?) : List<UserGet>?{
    var sonuc : List<UserGet>? = supabase?.from("kullanicilar")?.
    select(columns = Columns.list( "id","kayit_tarihi"))?.decodeList<UserGet>()
    if (sonuc != null) {
        if(sonuc.isNotEmpty()){
            Log.e("veritabani","veriler alındı.")
            return sonuc
        }else {
            Log.e("veritabani","bos")
            return null
        }
    }else {
        Log.e("veritabani","nullv")
        return null
    }
} */
