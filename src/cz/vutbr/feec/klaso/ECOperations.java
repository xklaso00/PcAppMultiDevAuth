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
import java.security.*;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECGenParameterSpec;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Random;

//class that does operations on elliptic curve with the help of bouncy castle
public class ECOperations {
    Utils utils= new Utils();
    //those are parameters of secp256k1 curve so we can create it to do point operations
    //final static private BigInteger n = new BigInteger("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFEBAAEDCE6AF48A03BBFD25E8CD0364141", 16);
   /* BigInteger prime = new BigInteger("115792089237316195423570985008687907853269984665640564039457584007908834671663");
    BigInteger A = new BigInteger("0");
    BigInteger B= new BigInteger("7");
    BigInteger Gx= new BigInteger("79BE667EF9DCBBAC55A06295CE870B07029BFCDB2DCE28D959F2815B16F81798", 16);
    BigInteger Gy= new BigInteger("483ADA7726A3C4655DA4FBFC0E1108A8FD17B448A68554199C47D08FFB10D4B8", 16);*/
    //ECCurve ellipticCurve= new ECCurve.Fp(prime,A,B);
    //ECPoint G = ellipticCurve.createPoint(Gx,Gy);;
    public static byte[]  PubKey224;
    public static byte[] PubKey256;
    public static byte[] PubKey160;
    byte[] TvEncoded;
    BigInteger rand;
    BigInteger SecKey;
    private SecretKey AESKey;
    private byte[] lastIV;
    private ECPoint PubKeyTogether;
    private static byte[] lastTimeStamp;
    CurveSpecifics cs;
    public ECOperations()
    {
        cs= new CurveSpecifics();
        SecKey=Options.getSecKey();
        long start=System.nanoTime();
        computeTv();
        System.out.println("Test encoding took "+(System.nanoTime()-start)/1000000+"ms");

    }
    public static byte[] getLastTimeStamp() {
        return lastTimeStamp;
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
        String typeOfHash=Options.getHashName();
        /*if(Options.SECURITY_LEVEL==1)
            typeOfHash="SHA-224";
        else
            typeOfHash="SHA-256";*/
        digest = MessageDigest.getInstance(typeOfHash);
        lastTimeStamp=GenerateTimeStamp();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
        outputStream.write(ID);
        outputStream.write(utils.bytesFromBigInteger(cs.getN()));
        outputStream.write(TvEncoded);
        outputStream.write(lastTimeStamp);
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
        BigInteger mid= (hash.multiply(Options.getSecKey())).mod(cs.getN());
        BigInteger sv = (rand.subtract(mid)).mod(cs.getN());
        byte [] signature= utils.bytesFromBigInteger(sv);
        System.out.println("sv is: "+utils.bytesToHex(signature));
        return signature;
    }
    public boolean verifyClientSig2(byte[] clientID, byte[] clientHash, byte [] clientSig, byte[] pubB,byte[] prevMess) throws NoSuchAlgorithmException, IOException {
        BigInteger s= new BigInteger(1,clientSig);
        BigInteger e= new BigInteger(1,clientHash);
        ECPoint mid= cs.getG().multiply(s);

        ECPoint pubPoint= cs.getCurve().decodePoint(pubB);
        ECPoint mid2=pubPoint.multiply(e);
        ECPoint t= mid.add(mid2);
        ECPoint tk=t.multiply(rand);
        System.out.println("t is "+Utils.bytesToHex(t.getEncoded(true)));
        System.out.println("tk is "+Utils.bytesToHex(tk.getEncoded(true)));
        MessageDigest digest = null;
        String hashFunction=Options.getHashName();
       /* if(Options.SECURITY_LEVEL==1)
            hashFunction="SHA-224";
        else
            hashFunction="SHA-256";*/
        digest = MessageDigest.getInstance(hashFunction);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
        outputStream.write(clientID);

        outputStream.write(t.getEncoded(true));
        outputStream.write(tk.getEncoded(true));

        outputStream.write(prevMess);
        byte connectedBytes[] = outputStream.toByteArray( );
        byte [] hashToCheck = digest.digest(connectedBytes);
        outputStream.close();
        InicializeAES(tk.getEncoded(true));
        System.out.println("hash i found is "+Utils.bytesToHex(hashToCheck));
        System.out.println("hash i got is "+Utils.bytesToHex(clientHash));
        if(utils.isEqual(hashToCheck,clientHash))
            return true;
        else
            return false;
    }
    public void InicializeAES(byte[] Tk) throws NoSuchAlgorithmException {
        if(Options.SECURITY_LEVEL==1||Options.SECURITY_LEVEL==2) {
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
        else
        {
            MessageDigest digest = null;
            digest = MessageDigest.getInstance("SHA-256");
            byte [] hash = digest.digest(Tk);
            byte [] SecretKeyBytes= Arrays.copyOfRange(hash,0,24);
            AESKey= new SecretKeySpec(SecretKeyBytes, 0, SecretKeyBytes.length, "DESede");
            lastIV= new byte[8];
            SecureRandom random = new SecureRandom();
            random.nextBytes(lastIV);
        }

    }
    public byte[] generateSecMsg(byte[] msg) throws Exception {
        System.out.println("Clear text is "+utils.bytesToHex(msg));
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
        if(Options.SECURITY_LEVEL==1||Options.SECURITY_LEVEL==2) {
            byte [] encrypted=AESGCMClass.encrypt(msg,AESKey,lastIV);
            outputStream.write(lastIV);
            outputStream.write(encrypted);

        }
        else {
            byte [] encrypted=DESClass.encrypt(msg,AESKey,lastIV);
            outputStream.write(lastIV);
            outputStream.write(encrypted);
        }
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
        String typeOfHash=Options.getHashName();
       /* if(Options.SECURITY_LEVEL==1)
            typeOfHash="SHA-224";
        else
            typeOfHash="SHA-256";*/
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
    public static byte[] generateSecKey() throws NoSuchProviderException, NoSuchAlgorithmException, InvalidAlgorithmParameterException {

        KeyPairGenerator g = KeyPairGenerator.getInstance("EC");
        String curveName;
        if(Options.SECURITY_LEVEL==1)
            curveName="secp224r1";
        else if(Options.SECURITY_LEVEL==2)
            curveName="secp256k1";
        else
            curveName="secp160r1";
        g.initialize(new ECGenParameterSpec(curveName), new SecureRandom());
        KeyPair aKeyPair = g.generateKeyPair();
        ECPrivateKey SecKeyA= (ECPrivateKey)aKeyPair.getPrivate();
        BigInteger SKA= SecKeyA.getS();
        ECPublicKey PubKeyA= (ECPublicKey)aKeyPair.getPublic();
        java.security.spec.ECPoint PUK=PubKeyA.getW();
        BigInteger pubByte = PUK.getAffineX();
        BigInteger pubByteY= PUK.getAffineY();

        ECPoint PUKA= CurveSpecifics.getCurve().createPoint(PUK.getAffineX(),PUK.getAffineY());

        byte [] publicKeyA= PUKA.getEncoded(true);

        if(Options.SECURITY_LEVEL==1)
            PubKey224=publicKeyA;
        else if(Options.SECURITY_LEVEL==2)
            PubKey256=publicKeyA;
        else
            PubKey160=publicKeyA;
        return Utils.bytesFromBigInteger(SKA);
    }
    public static byte[] GenerateTimeStamp()
    {
        String stamp= ZonedDateTime
                .now( ZoneId.systemDefault() )
                .format( DateTimeFormatter.ofPattern( "uuuu.MM.dd.HH.mm.ss" ) );
        System.out.println("Time stamp is "+stamp);
        System.out.println("Bytes of this are "+Utils.bytesToHex(stamp.getBytes())+" length:"+stamp.getBytes().length);
        byte[] timeBytes=stamp.getBytes();
        return timeBytes;
    }

}
