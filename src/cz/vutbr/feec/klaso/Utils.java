package cz.vutbr.feec.klaso;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Random;
//util functions to make main shorter
public class Utils {
    static String bytesToHex(byte[] bytes) {

        char[] hexArray = "0123456789ABCDEF".toCharArray();
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }
    public static boolean isCommand(byte[] a, byte[] b){
        if(a[1]==b[1] && a[0]== b[0])
            return true;
        else return false;


    }
    public static boolean isEqual(byte[] a, byte[] b) {
        if (a.length != b.length) {
            return false;
        }

        int result = 0;
        for (int i = 0; i < a.length; i++) {
            result |= a[i] ^ b[i];
        }
        return result == 0;
    }
    public static byte[] bytesFromBigInteger(BigInteger n) {

        byte[] b = n.toByteArray();

        if(Options.SECURITY_LEVEL==2) {
            if (b.length == 32) {
                return b;
            } else if (b.length > 32) {
                if (b[0] == 0)
                {
                    byte[] tmp = new byte[b.length - 1];
                    System.arraycopy(b, 1, tmp, 0, tmp.length);
                    b = tmp;
                }
                return b;
            } else {
                byte[] buf = new byte[32];
                System.arraycopy(b, 0, buf, buf.length - b.length, b.length);
                return buf;
            }
        }
        else
        {
            if(b.length>28) {
                if (b[0] == 0) {
                    byte[] tmp = new byte[b.length - 1];
                    System.arraycopy(b, 1, tmp, 0, tmp.length);
                    b = tmp;
                }

            }
            return b;
        }
    }
    public static BigInteger bigIntFromBytes(byte[] b) {
        return new BigInteger(1, b);
    }

    public byte [] randomBytes()
    {
        byte[] b = new byte[32];
        new Random().nextBytes(b);
        return b;
    }
    //function to generate hash to send to prover
    public byte [] generateHashToSend(byte [] RandPoint, byte[] PubKey) throws NoSuchAlgorithmException, IOException {
        byte [] randMsg=randomBytes(); //first generate random bytes, so challenge is unique
        MessageDigest digest = null;
        digest = MessageDigest.getInstance("SHA-256");
        byte[] randMsgHash = digest.digest(randMsg); //hash random bytes
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
        outputStream.write(RandPoint);
        outputStream.write(PubKey);
        outputStream.write(randMsgHash); //make array of random point, public key, random bytes hash and hash that
        byte connectedBytes[] = outputStream.toByteArray( );
        byte [] hashToReturn = digest.digest(connectedBytes); //we are hashing different byte array each time, so replay attack should not work
        outputStream.close();
        return hashToReturn;
    }
    public static byte[] addFirstToByteArr(byte first, byte[] arr2) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        output.write(first);
        output.write(arr2);
        byte[] result=output.toByteArray();
        return result;
    }


}

