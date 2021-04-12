package cz.vutbr.feec.klaso;
import org.bouncycastle.jcajce.provider.asymmetric.ec.KeyFactorySpi;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.math.ec.ECCurve;
import org.bouncycastle.math.ec.ECPoint;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.Security;
import java.util.Arrays;
import java.util.Random;

//class that does operations on elliptic curve with the help of bouncy castle
public class ECOperations {
    Utils utils= new Utils();
    //those are parameters of secp256k1 curve so we can create it to do point operations
    final static private BigInteger n = new BigInteger("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFEBAAEDCE6AF48A03BBFD25E8CD0364141", 16);
    BigInteger prime = new BigInteger("115792089237316195423570985008687907853269984665640564039457584007908834671663");
    BigInteger A = new BigInteger("0");
    BigInteger B= new BigInteger("7");
    BigInteger Gx= new BigInteger("79BE667EF9DCBBAC55A06295CE870B07029BFCDB2DCE28D959F2815B16F81798", 16);
    BigInteger Gy= new BigInteger("483ADA7726A3C4655DA4FBFC0E1108A8FD17B448A68554199C47D08FFB10D4B8", 16);
    ECCurve ellipticCurve= new ECCurve.Fp(prime,A,B);
    ECPoint G = ellipticCurve.createPoint(Gx,Gy);;
    byte[] TvEncoded;
    BigInteger rand;
    BigInteger SecKey;
    private SecretKey AESKey;
    private byte[] lastIV;
    private ECPoint PubKeyTogether;
    CurveSpecifics cs;
    public ECOperations()
    {
        cs= new CurveSpecifics();
        SecKey=Options.getSecKey();
        long start=System.nanoTime();
        computeTv();
        System.out.println("Test encoding took "+(System.nanoTime()-start)/1000000+"ms");

    }
    public byte[] computeTv()
    {
        ECPoint tv;

        do {
            Random randNum = new Random();
            rand = new BigInteger(cs.getN().bitLength(), randNum);
        } while (rand.compareTo(cs.getN()) >= 0);

        tv=cs.getG().multiply(rand);
        long start=System.nanoTime();
        TvEncoded=tv.getEncoded(true);
        System.out.println("Tv encoding took "+(System.nanoTime()-start)/1000000+"ms");
        return  TvEncoded;
    }
    public BigInteger hashOfProver(byte[] ID) throws NoSuchAlgorithmException, IOException {

        //ECPoint tv= computeTv();
        //if(TvEncoded==null)
        computeTv();

        MessageDigest digest = null;
        String typeOfHash;
        if(Options.SECURITY_LEVEL==1)
            typeOfHash="SHA-224";
        else
            typeOfHash="SHA-256";
        digest = MessageDigest.getInstance(typeOfHash);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
        outputStream.write(ID);
        outputStream.write(utils.bytesFromBigInteger(cs.getN()));
        outputStream.write(TvEncoded);
        byte connectedBytes[] = outputStream.toByteArray( );
        byte [] hashToReturn = digest.digest(connectedBytes);
        outputStream.close();
        System.out.println("tv is: "+utils.bytesToHex(TvEncoded));
        System.out.println("Hash of us is: "+utils.bytesToHex(hashToReturn));
        BigInteger hash= new BigInteger(1,hashToReturn);
        return hash;
    }
    public byte[] generateSignatureOfServer(BigInteger hash)
    {
        BigInteger mid= (hash.multiply(SecKey)).mod(cs.getN());
        BigInteger sv = (rand.subtract(mid)).mod(cs.getN());
        byte [] signature= utils.bytesFromBigInteger(sv);
        System.out.println("sv is: "+utils.bytesToHex(signature));
        return signature;
    }
    public boolean verifyClientSig2(byte[] clientID, byte[] clientHash, byte [] clientSig, byte[] pubB,byte[] prevMess) throws NoSuchAlgorithmException, IOException {
        BigInteger s= new BigInteger(1,clientSig);
        BigInteger e= new BigInteger(1,clientHash);
        ECPoint mid= G.multiply(s);

        ECPoint pubPoint= ellipticCurve.decodePoint(pubB);
        ECPoint mid2=pubPoint.multiply(e);
        ECPoint t= mid.add(mid2);
        ECPoint tk=t.multiply(rand);

        MessageDigest digest = null;
        digest = MessageDigest.getInstance("SHA-256");
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
        outputStream.write(clientID);

        outputStream.write(t.getEncoded(true));
        outputStream.write(tk.getEncoded(true));

        outputStream.write(prevMess);
        byte connectedBytes[] = outputStream.toByteArray( );
        byte [] hashToCheck = digest.digest(connectedBytes);
        outputStream.close();
        InicializeAES(tk.getEncoded(true));
        if(utils.isEqual(hashToCheck,clientHash))
            return true;
        else
            return false;
    }
    public void InicializeAES(byte[] Tk) throws NoSuchAlgorithmException {
        MessageDigest digest = null;
        digest = MessageDigest.getInstance("SHA-256");
        byte [] hash = digest.digest(Tk);
        byte [] SecretKeyBytes= Arrays.copyOfRange(hash,0,hash.length/2);
        AESKey= new SecretKeySpec(SecretKeyBytes, 0, SecretKeyBytes.length, "AES");
        /* byte [] SecretKeyBytes= Arrays.copyOfRange(Tk,1,Tk.length);
        AESKey= new SecretKeySpec(SecretKeyBytes, 0, SecretKeyBytes.length, "AES");*/
        lastIV= new byte[12];
        SecureRandom random = new SecureRandom();
        random.nextBytes(lastIV);
    }
    public byte[] generateSecMsg(byte[] msg) throws Exception {
        System.out.println("Clear text is "+utils.bytesToHex(msg));
        byte [] encrypted=AESGCMClass.encrypt(msg,AESKey,lastIV);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
        outputStream.write(lastIV);
        outputStream.write(encrypted);
        byte connectedBytes[] = outputStream.toByteArray( );
        System.out.println("connected is "+utils.bytesToHex(connectedBytes));
        outputStream.close();
        return connectedBytes;

    }
    public void CreateKeyForBoth(byte[] pubKey, byte[] pubKeyWatch)
    {
        ECPoint ecPub= cs.getCurve().decodePoint(pubKey);
        ECPoint ecPubWatch= cs.getCurve().decodePoint(pubKeyWatch);
        PubKeyTogether=ecPub.add(ecPubWatch);
    }
    public boolean VerifyWithWatch(byte[] ClientID,byte[] clientHash,byte[] phoneSig, byte[] watchSig, byte[] prevMSG) throws IOException, NoSuchAlgorithmException {
        BigInteger s= new BigInteger(1,phoneSig);
        BigInteger s1= new BigInteger(1,watchSig);
        BigInteger e= new BigInteger(1,clientHash);
        ECPoint gs0=cs.getG().multiply(s);
        ECPoint gs1=cs.getG().multiply(s1);
        ECPoint t= gs0.add(gs1);
        ECPoint pubE=(PubKeyTogether.multiply(e));
        t=t.add(pubE);
        ECPoint tk= t.multiply(rand);

        System.out.println("t is "+utils.bytesToHex(t.getEncoded(true)));
        System.out.println("tk is "+utils.bytesToHex(tk.getEncoded(true)));

        MessageDigest digest = null;
        String typeOfHash;
        if(Options.SECURITY_LEVEL==1)
            typeOfHash="SHA-224";
        else
            typeOfHash="SHA-256";
        digest = MessageDigest.getInstance(typeOfHash);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
        outputStream.write(ClientID);
        outputStream.write(t.getEncoded(true));
        outputStream.write(tk.getEncoded(true));
        outputStream.write(prevMSG);
        byte connectedBytes[] = outputStream.toByteArray( );
        byte [] hashToCheck = digest.digest(connectedBytes);
        outputStream.close();
        InicializeAES(tk.getEncoded(true));
        System.out.println("hash i found is "+utils.bytesToHex(hashToCheck));
        System.out.println("hash i got  is "+utils.bytesToHex(clientHash));
        if(utils.isEqual(hashToCheck,clientHash))
            return true;
        else
            return false;

    }

}
