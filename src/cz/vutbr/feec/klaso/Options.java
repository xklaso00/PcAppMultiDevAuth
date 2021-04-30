package cz.vutbr.feec.klaso;

import org.bouncycastle.math.ec.ECPoint;

import java.io.*;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.HashMap;

public class Options {
    public static int SECURITY_LEVEL=2;
    private static BigInteger SecKey256;//= new BigInteger("C89F0B6DF429D18ED46D8C0F91A7D5EFFFF5B620514ECEC0D9ED3728A3B2008D",16);
    private static BigInteger SecKey224;//= new BigInteger("AF1C20A86D38DB16B2E99BEF51A0EA1962EE0A85BA831A2BDE94DE0A",16);
    //private static BigInteger PubMobile224=new BigInteger("0228D68E9EF4AFE5FB144C8883D10BBB233AA1E00258ACC9B9600B63D0",16);
    //private static BigInteger PubWatch224=new BigInteger("02B6848FAE55DC9BE96E4E456439F9A48EB35452A74548A1E2A041DDC5",16);
    //private static BigInteger PubMobile256=new BigInteger("02DFF1D77F2A671C5F36183726DB2341BE58FEAE1DA2DECED843240F7B502BA659",16);
    //private static BigInteger PubWatch256=new BigInteger("02726E54885BFA6595DBB16FEE753E6B685CABE85F26B23D5A7B4863FFAEE60C2C",16);
    public static int BYTELENGHT=32;
    public static HashMap<String,BigInteger> KeyPairs224;
    public static HashMap<String,BigInteger> KeyPairs256;
    public static byte[] ActiveID;
    public static String ThreadName="";
    public static byte[] GetPubKey(int secLevel){
        int secOld=SECURITY_LEVEL;
        setSecurityLevel(secLevel);
        CurveSpecifics cs=new CurveSpecifics();
        ECPoint PubPoint=cs.getG().multiply(getSecKey());
        byte [] PubBytes=PubPoint.getEncoded(true);
        setSecurityLevel(secOld);
        return PubBytes;
    }
    public static void DelID(byte[] ID)
    {
        try {
            KeyPairs256.remove(Utils.bytesToHex(ID));
            KeyPairs224.remove(Utils.bytesToHex(ID));
        }catch (Exception e)
        {
            System.out.println("Deletion did not happen, probably not found");
        }

        System.out.println("ID "+Utils.bytesToHex(ID) +"was deleted");
    }
    public static int numOfDevWithActiveID()
    {
        boolean stayInLoop=true;
        int devNum=0;
        while(stayInLoop)
        {
            String hex = Integer.toHexString(devNum);
            byte index=Byte.parseByte(hex,16);
            try
            {
                byte[] toFindByte=Utils.addFirstToByteArr(index,ActiveID);
                String StringID=Utils.bytesToHex(toFindByte);
                stayInLoop =KeyPairs256.containsKey(StringID);
                if(stayInLoop)
                    devNum++;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return  devNum;
    }

    public static void addKeys(byte[] devID,BigInteger key224,BigInteger key256)
    {
        KeyPairs224.put(Utils.bytesToHex(devID),key224);
        KeyPairs256.put(Utils.bytesToHex(devID),key256);
        ClientKeyFile.WriteHashMapToFile(KeyPairs224,1);
        ClientKeyFile.WriteHashMapToFile(KeyPairs256,2);
    }
    public static boolean HasID(byte[] ID) throws IOException {
        byte[] firstID=Utils.addFirstToByteArr((byte)0x00,ID);
        if(KeyPairs256.containsKey(Utils.bytesToHex(firstID)))
            return true;
        else
            return false;
    }
    public static void setMaps()
    {
        KeyPairs224=ClientKeyFile.LoadHashMapFromFile(1);
        KeyPairs256=ClientKeyFile.LoadHashMapFromFile(2);

    }
    public static void setID(byte[] ID)
    {
        ActiveID=ID;
    }
    public static BigInteger GetKey(int DeviceIndex)
    {
        BigInteger key=null;
        String hex = Integer.toHexString(DeviceIndex);
        byte index=Byte.parseByte(hex,16);
        try
        {
            byte[] toFindByte=Utils.addFirstToByteArr(index,ActiveID);
            String StringID=Utils.bytesToHex(toFindByte);
            if(Options.SECURITY_LEVEL==1)
            {
                key=KeyPairs224.get(StringID);
            }
            else
            {
                key=KeyPairs256.get(StringID);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return key;
    }
    public static  void setSecurityLevel(int level)
    {
        if (level>0&&level<3)
        {
            SECURITY_LEVEL=level;
        }
        if(level==1)
            BYTELENGHT=28;
        else if(level==2)
            BYTELENGHT=32;
    }
    public static BigInteger getSecKey()
    {
        loadPrivateKey();
        if(SECURITY_LEVEL==1)
            return SecKey224;

        else
            return SecKey256;
    }
    public static void generateKeys()
    {

        try {
            SECURITY_LEVEL=1;
            byte[] keyByte224=ECOperations.generateSecKey();
            SecKey224=new BigInteger(1, keyByte224);
            savePrivateKey();
            SECURITY_LEVEL=2;
            byte[] keyByte256=ECOperations.generateSecKey();
            SecKey256=new BigInteger(1,keyByte256);
            savePrivateKey();
            System.out.println("Private keys generated");

        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        }
    }
    public static void savePrivateKey()
    {
        BigInteger keyToSave;
        String fileName;
        if(SECURITY_LEVEL==1){
            fileName="meKey224.ser";
            keyToSave=SecKey224;
        }

        else {
            fileName="meKey256.ser";
            keyToSave=SecKey256;
        }
        try {
            FileOutputStream fileOut = new FileOutputStream(fileName);
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(keyToSave);
            out.close();
            fileOut.close();
            System.out.println("Key saved to file "+fileName);
        }
        catch (IOException e)
        {
            System.out.println(e.toString());
        }
    }
    public static void loadPrivateKey()
    {
        String fileName;
        if(SECURITY_LEVEL==1){
            fileName="meKey224.ser";
        }

        else {
            fileName="meKey256.ser";
        }
        try
        {
            FileInputStream fileIn = new FileInputStream(fileName);
            ObjectInputStream in = new ObjectInputStream(fileIn);
            BigInteger Key=(BigInteger) in.readObject();
            in.close();
            fileIn.close();
            System.out.println("Key pars has been loaded from "+"meKey.ser");
            System.out.println("Key is "+Utils.bytesToHex(Utils.bytesFromBigInteger(Key)));
            if(SECURITY_LEVEL==1)
                SecKey224=Key;

            else
                SecKey256=Key;


        }
        catch (IOException | ClassNotFoundException e){
            System.out.println(e.toString());
            generateKeys();
        }
    }
    /*public static BigInteger getWatchKey()
    {
        if(SECURITY_LEVEL==1)
            return PubWatch224;
        else
            return PubWatch256;
    }
    public static BigInteger getMobileKey()
    {
        if(SECURITY_LEVEL==1)
            return PubMobile224;
        else
            return PubMobile256;
    }*/


}
