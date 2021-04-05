package cz.vutbr.feec.klaso;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class Old {
    //send first command to get random point from prover
/*
            ResponseAPDU response2 = channel.transmit(new CommandAPDU(instructions.getCOM1()));

            byte[] byteResponse2 = response2.getBytes();
            byte [] RandPoint= Arrays.copyOfRange(byteResponse2,0,33);

            //byte[] RandPoint = byteResponse2;

            System.out.println("Random point (compressed) received from the prover is : " + utils.bytesToHex(RandPoint));
            byte[] byteResponse22 = null;
            do {
                byte[] com22 = instructions.WATCHTHISRAND;
                ResponseAPDU response22 = channel.transmit(new CommandAPDU(com22));

                byteResponse22 = response22.getBytes();
                System.out.println("in loop");

                sleep(50);
            }while(utils.isCommand(instructions.NOTYET,byteResponse22));
            System.out.println("Second RAND is: " + utils.bytesToHex(byteResponse22));

            //for second command we will generate challenge - hash of our random message|random point| public key and send it to card
            byte[] HashToSign = utils.generateHashToSend(RandPoint, PubKey);
            System.out.println("Hash we want signed is: " + utils.bytesToHex(HashToSign));

            byte[] com2 = instructions.generateHashCOM(HashToSign);
            ResponseAPDU response3 = channel.transmit(new CommandAPDU(com2));
            byte[] byteResponse3 = null;
            byteResponse3 = response3.getBytes();
            System.out.println("Received signature is : " + utils.bytesToHex(byteResponse3));

            byte[] byteResponse4 = null;
            do {
                byte[] com4 = instructions.getCOM3();
                ResponseAPDU response4 = channel.transmit(new CommandAPDU(com4));
                System.out.println("in loop");
                byteResponse4 = response4.getBytes();

                sleep(50);
            }while(utils.isCommand(instructions.NOTYET,byteResponse4));
            // Disconnect the card, for next operation connection is not needed
            System.out.println("Second Signature is: " + utils.bytesToHex(byteResponse4));
            long EndTime = System.nanoTime();

 */
    /*  System.out.println("The communication took: " + (EndTime-StartTime)/1000000+"ms");
            ECOperations ecOperations= new ECOperations();

            BigInteger Signature =new BigInteger(1,byteResponse3);
            boolean isLegit=ecOperations.signVer(Signature,RandPoint,PubKey,HashToSign);
            System.out.println("Is the signature legit?: " + isLegit);
            BigInteger Signature2 =new BigInteger(1,byteResponse4);
            boolean isLegit2=ecOperations.signVer(Signature2,byteResponse22,PubKeyWatch,HashToSign);
            System.out.println("Is the signature legit?: " + isLegit2);*/
//we add hash to the command we want to send
   /* public byte[] generateHashCOM(byte [] hash) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
        outputStream.write(COM2);
        outputStream.write(hash);
        outputStream.write((byte)0x20);
        byte [] com2=outputStream.toByteArray();
        outputStream.close();
        return com2;
    }*/
    //computes sig*G
    /*public ECPoint computeP1(BigInteger S){
        cs=new CurveSpecifics();
        ECPoint P1= G.multiply(S);
        return P1;
    }*/
    //computes R+P*c
    /*public ECPoint computeP2(byte[] r, byte[] pub,byte[] hash) throws IOException {
        ECPoint pubKey= ellipticCurve.decodePoint(pub);
        BigInteger hashInt= utils.bigIntFromBytes(hash).mod(n);
        ECPoint mid= pubKey.multiply(hashInt);
        ECPoint R= ellipticCurve.decodePoint(r);
        ECPoint result = R.add(mid);
        return result;
    }*/

    /*//verifies the signature, if sig*G== R+P*c it is a proof that the prover knows the private key
    public boolean signVer(BigInteger S,byte[] r, byte[] pub,byte[] hash) throws IOException {
        ECPoint P1 = computeP1(S);
        ECPoint P2 = computeP2(r,pub,hash);
        if(P1.equals(P2)) return true;
        else return false;
    }*/
}
