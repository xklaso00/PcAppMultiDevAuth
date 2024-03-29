package cz.vutbr.feec.klaso;

import java.io.*;
import java.math.BigInteger;
import java.util.HashMap;

public class ClientKeyFile {
    private static String keyFile224="files/ClientKeys224.ser";
    private static String keyFile256="files/ClientKeys256.ser";
    private static String keyFile160="files/ClientKeys160.ser";
    public static void WriteHashMapToFile(HashMap<String, BigInteger> IdKeyPairs,int Security)
    {
        String keyFile;
        if(Security==1)
            keyFile=keyFile224;
        else if(Security==2)
            keyFile=keyFile256;
        else
            keyFile=keyFile160;
        try {
            FileOutputStream fileOut = new FileOutputStream(keyFile);
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(IdKeyPairs);
            out.close();
            fileOut.close();
            System.out.println("Keys saved to file "+keyFile);
        }
        catch (IOException e)
        {
            System.out.println(e.toString());
        }
    }
    public static HashMap<String,BigInteger> LoadHashMapFromFile(int Security)
    {
        HashMap<String,BigInteger> IdKeyPairs=new HashMap<String, BigInteger>();
        String keyFile;
        if(Security==1)
            keyFile=keyFile224;
        else if(Security==2)
            keyFile=keyFile256;
        else
            keyFile=keyFile160;
        try
        {
            FileInputStream fileIn = new FileInputStream(keyFile);
            ObjectInputStream in = new ObjectInputStream(fileIn);
            IdKeyPairs=(HashMap<String,BigInteger>)in.readObject();
            in.close();
            fileIn.close();
            System.out.println("Key pars has been loaded from "+keyFile);

        }
        catch (IOException | ClassNotFoundException e){
            System.out.println(e.toString());
        }
        return IdKeyPairs;
    }
}
