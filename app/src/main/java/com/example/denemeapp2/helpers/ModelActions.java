package com.example.denemeapp2.helpers;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.util.Log;

import org.tensorflow.lite.Interpreter;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ModelActions {

    private MappedByteBuffer modelBuffer = null;


    public ModelActions(Context cx,String modelName) {
        // Interpreter'ı oluştur
        try{
            modelBuffer = Objects.requireNonNull(loadModelFile(cx,modelName));
            Log.i("interpreter",modelName+" buffer'a yüklendi.");
        }catch(Exception ex){
            Log.e("interpreter-hata-1",""+ex.getMessage());
        }

    }

    private MappedByteBuffer loadModelFile(Context context,String modelName) throws IOException {
        try {
            AssetManager assetManager = context.getAssets();
            AssetFileDescriptor fileDescriptor = assetManager.openFd(modelName);
            FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
            FileChannel fileChannel = inputStream.getChannel();
            long startOffset = fileDescriptor.getStartOffset();
            long declaredLength = fileDescriptor.getDeclaredLength();
            return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
        }catch(Exception ex){
            Log.e("interpreter-load","Model dosyası yüklenirken hata oluştu : "+ex);
            return null;
        }
    }



    public static FloatBuffer floatArrayToBuffer(float[] floatArray) {
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(floatArray.length * 4);
        byteBuffer.order(ByteOrder.nativeOrder());

        FloatBuffer floatBuffer = byteBuffer.asFloatBuffer();
        floatBuffer.put(floatArray);
        floatBuffer.position(0);

        return floatBuffer;
    }


    private String generate_checkname(String lastCheckpoint){
            String[] lastchecksplit = lastCheckpoint.split("\\.");
            String filename = lastchecksplit[0];
            String[] filenamesplit = filename.split("-");
            String filefirst = filenamesplit[0];
            String filenumber = filenamesplit[1];
            int filenum = Integer.parseInt(filenumber);
            filenum+=1;

            String ext = lastchecksplit[1];

            return filefirst+"-"+filenum +"."+ext;
    }

    //checkpoint ismi checkpoint-1.ckpt şeklindedir.
    
    public Boolean restoreAndDotrain(Context cx,ArrayList<float[]> inputArr,ArrayList<float[]> outputArr,int NUM_EPOCHS,int NUM_TRAININGS,String lastcheckpoint,Boolean firstTime){
        try (Interpreter interpreter = new Interpreter(modelBuffer)) {

                File outputFile = new File(cx.getFilesDir(), lastcheckpoint);
                String outputFilepath = "";
                if (outputFile.exists()) {
                    outputFilepath = outputFile.getAbsolutePath();
                    Map<String, Object> inputs = new HashMap<>();
                    inputs.put("checkpoint_path", outputFilepath);
                    Map<String, Object> outputs = new HashMap<>();
                    interpreter.runSignature(inputs, outputs, "restore");
                    Log.i("interpreter-restore", lastcheckpoint + " dosyası train için restore edildi");
                }

            List<FloatBuffer> inputsBatches = new ArrayList<>(NUM_TRAININGS);
            List<FloatBuffer> outputsBatches = new ArrayList<>(NUM_TRAININGS);
            for (int i = 0; i < NUM_TRAININGS; ++i) {
                   FloatBuffer example_x = floatArrayToBuffer(inputArr.get(i));
                    FloatBuffer example_y = floatArrayToBuffer(outputArr.get(i));
                    inputsBatches.add(example_x);
                    outputsBatches.add(example_y);
            }
            float[] losses = new float[NUM_EPOCHS];
            for (int epoch = 0; epoch < NUM_EPOCHS; ++epoch) {
                for (int batchIdx = 0; batchIdx < NUM_TRAININGS; ++batchIdx) {
                        Map<String, Object> inputs = new HashMap<>();
                        inputs.put("x", inputsBatches.get(batchIdx));
                        inputs.put("y", outputsBatches.get(batchIdx));

                        Map<String, Object> outputs = new HashMap<>();
                        FloatBuffer loss = FloatBuffer.allocate(1);
                        outputs.put("loss", loss);
                        interpreter.runSignature(inputs, outputs, "train");
                        if (batchIdx == NUM_TRAININGS - 1) losses[epoch] = loss.get(0);
                    }
                // Print the loss output for every 10 epochs.
                if ((epoch + 1) % 10 == 0) {
                    Log.i("interpreter-train",
                            "Finished " + (epoch + 1) + " epochs, current loss: " + losses[epoch]);
                }
            }
            String newcheckpoint = "";
            if(!firstTime) {
                newcheckpoint = generate_checkname(lastcheckpoint);
            }else {
                newcheckpoint = lastcheckpoint;
            }
            File outputFile2 = new File(cx.getFilesDir(), newcheckpoint);
            String outputFilepath2 = "";
            if (!outputFile.exists()) {
                //dosya yoksa
                 if (outputFile2.createNewFile()) {
                        Log.i("interpreter-restore", lastcheckpoint +"oluşturuldu");
                } else {
                        Log.e("interpreter-hata", "Dosya oluşturulurken bir hata oluştu.");
                        return false;
                } 
            }
            outputFilepath2 = outputFile2.getAbsolutePath();

            Map<String, Object> inputs = new HashMap<>();
            inputs.put("checkpoint_path", outputFilepath2);
            Map<String, Object> outputs = new HashMap<>();
            interpreter.runSignature(inputs, outputs, "save");
            Log.i("interpreter-save",outputFilepath2+" kaydedildi.");
            //Burada yerel depolamadaki checkpoint listesine bu checkpointi ekleyecek.
            return true;
        }catch(Exception ex){
            Log.e("interpreter-train-hata",ex.getMessage()+" ve "+ Arrays.toString(ex.getStackTrace()));
            return false;
        }
    }

    public Map<String,Object> restoreAndDoPredict(float[] input,float[] outArr,Context cx,String last_checkpoint){
        try (Interpreter interpreter2 = new Interpreter(modelBuffer)) {
            File outputFile = new File(cx.getFilesDir(), last_checkpoint);
            String outputFilepath = "";
            if (outputFile.exists()) {
                outputFilepath = outputFile.getAbsolutePath();
                Map<String, Object> inputs = new HashMap<>();
                inputs.put("checkpoint_path", outputFilepath);
                Map<String, Object> outputs = new HashMap<>();
                interpreter2.runSignature(inputs, outputs, "restore");
            }

            Map<String, Object> p_inputs = new HashMap<>();
            p_inputs.put("x", floatArrayToBuffer(input).rewind());
            float[] out = Arrays.copyOf(outArr,outArr.length);
            FloatBuffer output = floatArrayToBuffer(out);
            Map<String, Object> p_outputs = new HashMap<>();
            p_outputs.put("output", output);
            interpreter2.runSignature(p_inputs, p_outputs, "infer");
            output.rewind();
            return p_outputs;
        }
    }

    
    public Map<String,Object> doPredict(float[] input,float[] outArr) {
        try (Interpreter interpreter2 = new Interpreter(modelBuffer)) {
            Map<String, Object> inputs = new HashMap<>();
            inputs.put("x", floatArrayToBuffer(input).rewind());
            float[] out = Arrays.copyOf(outArr,outArr.length);
            FloatBuffer output = floatArrayToBuffer(out);
            Map<String, Object> outputs = new HashMap<>();
            outputs.put("output", output);
            interpreter2.runSignature(inputs, outputs, "infer");
            output.rewind();
            return outputs;
        }
    }

    
/**
 *     public static FloatBuffer floatArraysToBuffer(List<float[]> floatArrays) {
        // Toplam eleman sayısını bulmak için listenin boyutunu kullan
        int totalElements = 0;
        for (float[] array : floatArrays) {
            totalElements += array.length;
        }

        // FloatBuffer'ı oluştur
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(totalElements* 4);
        byteBuffer.order(ByteOrder.nativeOrder());
        FloatBuffer floatBuffer = byteBuffer.asFloatBuffer();

        // Liste üzerinde dönerken her bir float dizisini FloatBuffer'a ekler
        for (float[] array : floatArrays) {
            floatBuffer.put(array);
        }

        // Pozisyonu sıfıra ayarla (rewind)
        floatBuffer.rewind();

        return floatBuffer;
    }
 */


}
