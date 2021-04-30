package cz.vutbr.feec.klaso;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.io.*;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Optional;

public class PassClass {
    private static String adminPassFile="AdminPass.ser";
    private static String clientPassFile="clientPass.ser";
    private static HashMap<String, String[]> adminPassMap=new HashMap<String, String[]>();
    private static HashMap<String, String[]> ClientPassMap=new HashMap<String, String[]>();
    private static boolean AdminIn=false;
    private static boolean CurrentUserLegit=false;
    public static String hashPass(char[] pass, String salt)
    {

        byte[] saltBytes=salt.getBytes();
        PBEKeySpec spec = new PBEKeySpec(pass, saltBytes, 65536, 512);
        try {
            SecretKeyFactory fac = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512");
            byte[] securePassword = fac.generateSecret(spec).getEncoded();
            return Base64.getEncoder().encodeToString(securePassword);

        } catch (NoSuchAlgorithmException | InvalidKeySpecException ex) {
            System.err.println("Exception encountered in hashPassword()");
            return null;

        } finally {
            spec.clearPassword();
        }
    }
    private static final SecureRandom RAND = new SecureRandom();

    public static String generateSalt (final int length) {
        byte[] salt = new byte[length];
        RAND.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }
    public static boolean LoadAdminPass(){
        try{
            FileInputStream fileIn = new FileInputStream(adminPassFile);
            ObjectInputStream in = new ObjectInputStream(fileIn);
            adminPassMap=(HashMap<String, String[]>)in.readObject();
            in.close();
            fileIn.close();
            if (adminPassMap.isEmpty())
                return false;
            else
                return true;
        }
        catch (IOException | ClassNotFoundException e)
        {
            System.out.println("Admin pass not found");
        }
        return false;
    }
    public static boolean SaveAdminPassToFile(String HashedPass, String Salt) throws IOException {

        try {
            String[] passAndSalt=new String[2];
            passAndSalt[0]=HashedPass;
            passAndSalt[1]=Salt;
            adminPassMap.put("000000",passAndSalt);
            FileOutputStream fileOut = new FileOutputStream(adminPassFile);
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(adminPassMap);
            out.close();
            fileOut.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        System.out.println("Key saved to file ");
        LoadAdminPass();
        return true;
    }
    public static boolean newAdminPass(char[] pass)
    {
        String Salt=generateSalt(64);
        String HashedPass=hashPass(pass,Salt);
        try {
            SaveAdminPassToFile(HashedPass,Salt);

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
    public static boolean verAdminPass(char[] pass)
    {
        String[] passStrings=adminPassMap.get("000000");
        String newHashedPass=hashPass(pass,passStrings[1]);
        if (passStrings[0].equals(newHashedPass)) {
            AdminIn=true;
            return true;
        }
        else
            return false;
    }


    public static boolean isAdminIn() {
        return AdminIn;
    }
    public static boolean addUserPass(String ID, char[] pass)
    {
        loadClientPasswords();
        String Salt=generateSalt(64);
        String HashedPass=hashPass(pass,Salt);
        try {
            String[] saltAndPass=new String[2];
            saltAndPass[0]=HashedPass;
            saltAndPass[1]=Salt;
            ClientPassMap.put(ID,saltAndPass);
            FileOutputStream fileOut = new FileOutputStream(clientPassFile);
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(ClientPassMap);
            out.close();
            fileOut.close();

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
    public static boolean verifyUserPass(String ID,char[] pass)
    {

        CurrentUserLegit=false;
        loadClientPasswords();
        try {
            String[] passAndSalt=ClientPassMap.get(ID);
            String nowPass=hashPass(pass,passAndSalt[1]);
            if(nowPass.equals(passAndSalt[0])) {
                CurrentUserLegit=true;
                return true;
            }
            else
                return false;
        }
        catch (Exception e)
        {
            System.out.println("Exception in verify pass");
        }
        return false;
    }
    public static boolean loadClientPasswords()
    {
        try{
            FileInputStream fileIn = new FileInputStream(clientPassFile);
            ObjectInputStream in = new ObjectInputStream(fileIn);
            ClientPassMap=(HashMap<String, String[]>)in.readObject();
            in.close();
            fileIn.close();
            return true;
        }
        catch (IOException | ClassNotFoundException e)
        {
            System.out.println("No client Passes yet");
        }
        return false;
    }

    public static boolean isCurrentUserLegit() {
        return CurrentUserLegit;
    }
}
