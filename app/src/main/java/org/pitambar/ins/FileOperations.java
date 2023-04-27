package org.pitambar.ins;

import android.os.Environment;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class FileOperations {

    static  public void writeToFile(String fname,String data) {
        try {
            String storageState = Environment.getExternalStorageState();
            if (storageState.equals(Environment.MEDIA_MOUNTED)) {
                String filePath = Environment.getExternalStorageDirectory().toString()+"/Documents/"+fname+".txt";
//                Log.d("filepath", filePath);
                File file=new File(filePath);
                FileWriter f = new FileWriter(file, true);
                f.write(data);
                f.flush();
                f.close();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    static public int read(String fname,String[] data) throws FileNotFoundException {
        BufferedReader br = null;
        String response = null;
        try {
            StringBuffer output = new StringBuffer();
            String fpath = Environment.getExternalStorageDirectory().toString()+"/Documents/"+fname+".txt";
            br = new BufferedReader(new FileReader(fpath));
            String line = "";
            int i=0;
            while ((line = br.readLine()) != null) {
                output.append(line +"\n");
                data[i]=line;
                i++;

            }

            response = output.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return 0;
        }
        return 1;
    }

    static public int read_file(String fname, ArrayList<String> data){
        BufferedReader br = null;
        String response = null;
        try {
            StringBuffer output = new StringBuffer();
            String fpath = Environment.getExternalStorageDirectory().toString()+"/Documents/"+fname+".txt";
            br = new BufferedReader(new FileReader(fpath));
            String line = "";
            int i=0;
            while ((line = br.readLine()) != null) {
                output.append(line +"\n");
                data.add(line);
                i++;

            }

            response = output.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return 0;
        }
        return 1;
    }
}
