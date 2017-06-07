package controllers;

import java.io.FileOutputStream;
import java.io.FileInputStream;
/**
 * Created by Fujitsu on 19/2/2016.
 */
public class FileController {
    private static FileController instance;

    public static String MAP_FILE_NAME = "map.txt";
    public static String PERCEIVED_MAP_NAME = "perceived_map.txt";

    public static FileController getInstance(){
        if(instance==null){
            instance = new FileController();
        }
        return instance;
    }

    public void writeTo(String filename,String text) throws Exception{
        FileOutputStream fos = new FileOutputStream(filename);
        fos.write(text.getBytes());
        fos.close();
    }

    public String readFrom(String filename) throws Exception{
        String result = "";
        int k,i = 0; byte[] b;
        FileInputStream fis = new FileInputStream(filename);
        b = new byte[fis.available()];
        while((k=fis.read())!=-1)
           b[i++] = (byte)k;
        result = new String(b);
        fis.close();
        return result;
    }
}
