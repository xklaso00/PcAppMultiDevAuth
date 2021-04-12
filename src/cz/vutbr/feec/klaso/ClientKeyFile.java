package cz.vutbr.feec.klaso;

import java.io.*;
import java.math.BigInteger;
import java.util.HashMap;

public class ClientKeyFile {
    private static String keyFile224="ClientKeys224.ser";
    private static String keyFile256="ClientKeys256.ser";
    public static void WriteHashMapToFile(HashMap<String, BigInteger> IdKeyPairs,int Security)
    {
        String keyFile;
        if(Security==1)
            keyFile=keyFile224;
        else
            keyFile=keyFile256;
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
        HashMap<String,BigInteger> IdKeyPairs=null;
        String keyFile;
        if(Security==1)
            keyFile=keyFile224;
        else
            keyFile=keyFile256;
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
