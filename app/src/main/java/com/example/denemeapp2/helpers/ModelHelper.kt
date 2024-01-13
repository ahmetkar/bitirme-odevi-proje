package com.example.denemeapp2.helpers;

import android.content.Context
import android.util.Log
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.example.denemeapp2.helpers.BackendHelper
import com.example.denemeapp2.helpers.Models
import kotlinx.coroutines.flow.collect
import java.nio.FloatBuffer

class ModelHelper {

    companion object {
        fun kalpgirdiAl(girdiler: List<Float>) : FloatArray{
            var bosGirdiler : Array<Float> = Array(25){0.0f}

            val age  = girdiler[0]
            if(age >18 && age<=30) bosGirdiler[0] = 1.0f
            else if(age >30 && age<=50) bosGirdiler[1] = 1.0f
            else if(age >50 && age<=70) bosGirdiler[2] = 1.0f
            else if(age >70) bosGirdiler[3] = 1.0f

            val cinsiyet = girdiler[1]
            if(cinsiyet == 0.0f ) bosGirdiler[4] = 1.0f
            else if(cinsiyet == 1.0f ) bosGirdiler[5] = 1.0f

            val cpt = girdiler[2]
            if(cpt == 0.0f) bosGirdiler[6] = 1.0f
            else if(cpt == 1.0f) bosGirdiler[7] = 1.0f
            else if(cpt == 2.0f) bosGirdiler[8] = 1.0f
            else if(cpt == 3.0f) bosGirdiler[9] = 1.0f

            val rbp = girdiler[3]
            if(rbp < 90) bosGirdiler[10] = 1.0f
            else if(rbp >= 90 && rbp<150) bosGirdiler[11] = 1.0f
            else if(rbp >= 150) bosGirdiler[12] = 1.0f

            val chol = girdiler[4]
            if(chol < 100) bosGirdiler[13] = 1.0f
            else if(chol >= 100 && chol<200) bosGirdiler[14] = 1.0f
            else if(chol >= 200) bosGirdiler[15] = 1.0f

            val bs = girdiler[5]
            if(bs >120) bosGirdiler[17] = 1.0f
            else bosGirdiler[16] = 1.0f

            val mxhr = girdiler[6]
            if(mxhr <= 150) bosGirdiler[18] = 1.0f
            else if(mxhr > 150 && mxhr<=170) bosGirdiler[19] = 1.0f
            else if(mxhr > 170 && mxhr<=190) bosGirdiler[20] = 1.0f
            else if(mxhr > 190 && mxhr<=200) bosGirdiler[21] = 1.0f
            else if(mxhr > 200) bosGirdiler[22] = 1.0f

            val exan = girdiler[7]
            if(exan== 0.0f ) bosGirdiler[23] = 1.0f
            else if(exan == 1.0f ) bosGirdiler[24] = 1.0f

            return bosGirdiler.toFloatArray()
        }


        fun diabetgirdiAl(girdiler: List<Float>): FloatArray {
            var bosGirdiler: Array<Float> = Array(24) { 0.0f }

            val preg = girdiler[0]
            if(preg >0 && preg<=3) bosGirdiler[0] = 1.0f
            else if(preg>3 && preg<=6) bosGirdiler[1] = 1.0f
            else if(preg >6) bosGirdiler[2] = 1.0f

            val gluc = girdiler[1]
            if (gluc <= 140) bosGirdiler[3] = 1.0f
            else if (gluc > 140) bosGirdiler[4] = 1.0f

            val dcbp = girdiler[2]
            if (dcbp <= 79) bosGirdiler[5] = 1.0f
            else if (dcbp > 79 && dcbp <= 89) bosGirdiler[6] = 1.0f
            else if (dcbp > 89 && dcbp <= 120) bosGirdiler[7] = 1.0f
            else if (dcbp > 120) bosGirdiler[8] = 1.0f

            val thck = girdiler[3]
            if (thck <= 20) bosGirdiler[9] = 1.0f
            else if (thck > 20 && thck <= 30) bosGirdiler[10] = 1.0f
            else if (thck > 30) bosGirdiler[11] = 1.0f

            val ins = girdiler[4]
            if (ins <= 180) bosGirdiler[12] = 0.0f
            else if (ins > 180 && ins <= 320) bosGirdiler[13] = 1.0f
            else if (ins > 320 && ins <= 520) bosGirdiler[14] = 1.0f
            else if (ins > 520) bosGirdiler[15] = 1.0f

            val bmi = girdiler[5]
            if (bmi <= 18) bosGirdiler[16] = 1.0f
            else if (bmi > 18 && bmi <= 25) bosGirdiler[17] = 1.0f
            else if (bmi > 25 && bmi <= 30) bosGirdiler[18] = 1.0f
            else if (bmi > 30) bosGirdiler[19] = 1.0f

            val age = girdiler[6]
            if (age >18 && age <= 30) bosGirdiler[20] = 1.0f
            else if (age > 30 && age <= 50) bosGirdiler[21] = 1.0f
            else if (age > 50 && age <= 70) bosGirdiler[22] = 1.0f
            else if (age > 70) bosGirdiler[23] = 1.0f

            return bosGirdiler.toFloatArray()
        }

        fun printFloatBuffer(floatBuffer: FloatBuffer?, key:String) {
            floatBuffer!!.rewind()
            while (floatBuffer!!.hasRemaining()) {
                Log.e("interpreter-sonuc","$key -> ${floatBuffer.get()} ")
            }
        }

        fun genelgirdiAl(belirtiler:Array<String>,girdiler : SnapshotStateList<String>) :FloatArray{
            var n_girdiler : MutableList<Int> = mutableListOf()
            girdiler.forEach { item ->
                if(item!="") {
                    val idx = belirtiler.indexOf(item)
                    Log.e("Dene", "$item ve $idx")
                    if (idx != -1) {
                        n_girdiler.add(idx)
                    }
                }
            }
            var bosGirdiler : Array<Float> = Array(132){0.0f}
            n_girdiler.forEach { item->
                bosGirdiler[item] = 1.0f
            }
            return bosGirdiler.toFloatArray()
        }



    }

    

}