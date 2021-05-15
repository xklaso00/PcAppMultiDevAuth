package cz.vutbr.feec.klaso;

import javax.smartcardio.*;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static java.lang.Thread.sleep;

public class Terminal {

    private static long T1time;
    private static byte [] ID;
    private static Card card = null;
    private static CardChannel channel = null;
    private static Utils utils= new Utils();
    private static Instructions instructions = new Instructions();
    private static byte [] PubKey;
    private static ECOperations ecOperations = new ECOperations();

    public static void TrySendFailCom(boolean isAuthCall){
        byte[] ResultCom=Instructions.returnDoneCOM(false);
        if (!isAuthCall){
            ResultCom=Instructions.ReturnRegisterConformationCOM(false);
        }
        try {
            System.out.println("EXCEPTION sending "+Utils.bytesToHex(ResultCom));
            ResponseAPDU responseResultOfCom=channel.transmit(new CommandAPDU(ResultCom));
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }


    public static boolean InitializeCardConnection()
    {
        try {
            TerminalFactory factory = TerminalFactory.getDefault();
            List<CardTerminal> terminals = factory.terminals().list();
            System.out.println("Terminals: " + terminals);
            CardTerminal terminal = terminals.get(0);

            try {
                while (!terminal.isCardPresent()) ;
                // Connect wit the card, using supported protocol, for some reason T=0 not working
                card = terminal.connect("*");
                System.out.println("Card: " + card);
                channel = card.getBasicChannel();
            } catch (CardException ce) {
                ce.printStackTrace();
            }
            //send choose AID command and get a response
            long StartTime = System.nanoTime();
            T1time=System.nanoTime();
            ResponseAPDU response1 = channel.transmit(new CommandAPDU(instructions.getAID()));
            System.out.println("Sending: " + utils.bytesToHex(instructions.getAID()));
            byte[] byteResponse1 = null;
            byteResponse1 = response1.getBytes();
            System.out.println("Card response for choose AID command: " + utils.bytesToHex(byteResponse1));
            if(byteResponse1.length==2)
                return true;
            ID = Arrays.copyOfRange(byteResponse1, 0, byteResponse1.length - 2);
            Options.setID(ID);
            System.out.println("To get apdu answer it took " + (System.nanoTime() - StartTime) / 1000000 + " ms");
            PubKey = Utils.bytesFromBigInteger(Options.GetKey(0));
            Options.setID(ID);
            return true;
        }
        catch (Exception e)
        {
            System.out.println("Exception in card connection");

        }
        TrySendFailCom(true);
        return false;
    }

    public static boolean SingleDevAuth(boolean disconnectCard, boolean isRegistrationCall)
    {
        if(!isRegistrationCall)
        {
            boolean cont=InitializeCardConnection();
            if(!cont)
                return false;
        }

        try {
            BigInteger hashOfServer=ecOperations.hashOfProver(ID);
            byte[] byteHash= utils.bytesFromBigInteger(hashOfServer);
            byte[] SigOfServer= ecOperations.generateSignatureOfServer(hashOfServer);
            byte[] CommandToSave=instructions.generateCOMSERVERSIG(SigOfServer,byteHash);
            ResponseAPDU responseServerSig = channel.transmit(new CommandAPDU(CommandToSave));
            byte[] byteResponseServerSig = responseServerSig.getBytes();
            System.out.println("Answer is: "+utils.bytesToHex(byteResponseServerSig));
            if(utils.isEqual(byteResponseServerSig,Instructions.UNKNOWN_CMD_SW))
            {
                card.disconnect(false);
                return false;
            }
            byte [] ClientID= Arrays.copyOfRange(byteResponseServerSig,0,5);
            byte [] ClientHash= Arrays.copyOfRange(byteResponseServerSig,5,Options.BYTELENGHT+5);
            byte [] ClientSig= Arrays.copyOfRange(byteResponseServerSig,Options.BYTELENGHT+5,byteResponseServerSig.length);
            System.out.println(Utils.bytesToHex(Utils.bytesFromBigInteger(Options.GetKey(0))));
            boolean isItTrue=ecOperations.verifyClientSig2(ClientID,ClientHash,ClientSig,Utils.bytesFromBigInteger(Options.GetKey(0)),CommandToSave);
            System.out.println("SIG is: "+utils.bytesToHex(ClientSig));
            System.out.println("is it legit tho? "+isItTrue);


            if(disconnectCard) {
                byte [] ResultCom;
                if(isRegistrationCall)
                    ResultCom=Instructions.ReturnRegisterConformationCOM(isItTrue);
                else
                    ResultCom=Instructions.returnDoneCOM(isItTrue);
                ResponseAPDU responseResultOfCom=channel.transmit(new CommandAPDU(ResultCom));
                byte [] resRes=responseResultOfCom.getBytes();
                System.out.println("Answer last is "+Utils.bytesToHex(resRes));
                card.disconnect(false);
            }

            return  isItTrue;

        }
        catch (Exception e)
        {
            System.out.println(e.toString());

        }
        TrySendFailCom(true);

        return false;
    }

    public static boolean MultiDevAuth(boolean isRegistrationCall)
    {
        InitializeCardConnection();

        try {
            //System.out.println("To get apdu answer it took "+(System.nanoTime()-StartTime)/1000000+" ms");
            long ST1=System.nanoTime();
            byte [] PubKey=Utils.bytesFromBigInteger(Options.GetKey(0));
            byte [] PubKeyWatch=Utils.bytesFromBigInteger(Options.GetKey(1));
            BigInteger hashOfServer=ecOperations.hashOfProver(ID);
            byte[] byteHash= utils.bytesFromBigInteger(hashOfServer);
            byte[] SigOfServer= ecOperations.generateSignatureOfServer(hashOfServer);
            byte[] CommandToSave=instructions.generateCOMSERVERSIGWITHWATCH(SigOfServer,byteHash);
            long dur1=System.nanoTime()-ST1;
            System.out.println("our signature took "+dur1+" ns");
            long timeForApduRes;
            long TimeToGetSign=System.nanoTime();
            byte[] byteResponseServerSig;
            byte[] CommandToSave2=CommandToSave;
            System.out.println("T1 time is  " + (System.nanoTime() - T1time) / 1000000 + " ms");
            int timer=0;
            do {
                timeForApduRes=System.nanoTime();
                ResponseAPDU responseServerSig = channel.transmit(new CommandAPDU(CommandToSave2));
                byteResponseServerSig = responseServerSig.getBytes();
                CommandToSave2=Instructions.COMPLACEHOLDERCOM7;
                System.out.println("Answer is: " + utils.bytesToHex(byteResponseServerSig));
                timer++;
                sleep(100);
            }while(utils.isCommand(instructions.NOTYET,byteResponseServerSig)&&timer<15);

            long T5time=System.nanoTime();

            System.out.println("To get back proof after successful command took "+ (System.nanoTime()-timeForApduRes)/1000000+" ms");
            if(utils.isEqual(byteResponseServerSig,Instructions.UNKNOWN_CMD_SW)||Utils.isEqual(byteResponseServerSig,Instructions.NOTYET))
            {
                ResponseAPDU responseResultOfCom=channel.transmit(new CommandAPDU(Instructions.returnDoneCOM(false)));
                card.disconnect(false);
                return false;
            }
            long EndTime = System.nanoTime();
            System.out.println("To Get Proof after sending req it took "+(EndTime-TimeToGetSign)/1000000+" ms");
            //System.out.println("Only communitaction took "+(EndTime-StartTime)/1000000+" ms");
            long StartTime=System.nanoTime();
            byte [] ClientID= Arrays.copyOfRange(byteResponseServerSig,0,5);
            byte [] ClientHash= Arrays.copyOfRange(byteResponseServerSig,5,Options.BYTELENGHT+5);
            byte [] ClientSig= Arrays.copyOfRange(byteResponseServerSig,Options.BYTELENGHT+5,Options.BYTELENGHT*2+5);
            byte[] WatchSig=Arrays.copyOfRange(byteResponseServerSig,Options.BYTELENGHT*2+5,byteResponseServerSig.length);
            System.out.println("Client sig is? "+ utils.bytesToHex(ClientSig));
            System.out.println("Watch sig is? "+ utils.bytesToHex(WatchSig));
            ecOperations.CreateKeyForBoth(PubKey,PubKeyWatch);
            boolean isItTrue=ecOperations.VerifyWithWatch(ClientID,ClientHash,ClientSig,WatchSig,CommandToSave);
            EndTime=System.nanoTime();
            System.out.println("is it legit tho? "+isItTrue);
            System.out.println("Only Verification took "+(EndTime-StartTime)/1000000+" ms");

            System.out.println("T5 took " + (System.nanoTime() - T5time) / 1000000 + " ms");
            //AES test
            long aesTimer=System.nanoTime();
            byte[] secPart=ecOperations.generateSecMsg(ClientHash);
            byte[] aesInc=instructions.generateDecryptMe(secPart);
            System.out.println("Aes crypt took "+(System.nanoTime()-aesTimer)/1000000 +"ms");
            StartTime=System.nanoTime();
            ResponseAPDU aesResponse = channel.transmit(new CommandAPDU(aesInc));
            byte[] aesResponseBytes=aesResponse.getBytes();
            System.out.println("Answer is: "+utils.bytesToHex(aesResponseBytes));
            EndTime=System.nanoTime();
            System.out.println("Aes APDU took "+(EndTime-StartTime)/1000000+" ms");
            byte [] ResultCom;
            if(isRegistrationCall)
                ResultCom=Instructions.ReturnRegisterConformationCOM(isItTrue);
            else
                ResultCom=Instructions.returnDoneCOM(isItTrue);
            System.out.println("sending "+Utils.bytesToHex(ResultCom));
            ResponseAPDU responseResultOfCom=channel.transmit(new CommandAPDU(ResultCom));
            card.disconnect(false);

            return isItTrue;

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        TrySendFailCom(true);
        return false;
    }
    public static boolean RegisterUser()
    {
        boolean cc=InitializeCardConnection();
        if(!cc)
            return false;
        try {
            byte [] newID= new byte[5];
            do {
                new Random().nextBytes(newID);
            } while (Options.HasID(newID));
            System.out.println("New ID is "+Utils.bytesToHex(newID));
            Options.setID(newID);
            byte[] PubKeysCom=Instructions.GeneratePubKeysCOM();
            ResponseAPDU responsePubKeys=channel.transmit(new CommandAPDU(PubKeysCom));
            byte[] responsePubKeysBytes=responsePubKeys.getBytes();
            System.out.println("Answer to pub keys is "+Utils.bytesToHex(responsePubKeysBytes));


            byte[] IDCom=Instructions.generateRegisterDeviceCOM(newID);
            ResponseAPDU responseRegister = channel.transmit(new CommandAPDU(IDCom));
            byte[] responseRegisterBytes=responseRegister.getBytes();
            System.out.println("Response for register"+Utils.bytesToHex(responseRegisterBytes));
            if(Utils.isCommand(responseRegisterBytes,Instructions.NOTYET)) {
                TrySendFailCom(false);
                card.disconnect(false);
                return false;
            }
            byte[] NewPub32=Arrays.copyOfRange(responseRegisterBytes,0,33);
            byte[] NewPub28=Arrays.copyOfRange(responseRegisterBytes,33,62);
            byte[] NewPub20=Arrays.copyOfRange(responseRegisterBytes,62,responseRegisterBytes.length);
            ID=newID;
            BigInteger Pub32=new BigInteger(1,NewPub32);
            BigInteger Pub28=new BigInteger(1,NewPub28);
            BigInteger Pub20=new BigInteger(1,NewPub20);
            byte[] devID= Utils.addFirstToByteArr((byte)0x00,newID);
            Options.addKeys(devID,Pub28,Pub32,Pub20);

            boolean AllRes=SingleDevAuth(true,true);
            //card.disconnect(false);
            return AllRes;
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
        }
        TrySendFailCom(false);
        return false;
    }
    public static boolean registerAnotherDevice()
    {
        boolean singleDone= SingleDevAuth(false,false);
        if(!singleDone)
            return false;
        //this is here for testing to delete last watch key
        /*try {
            byte[] IDtoDel=Utils.addFirstToByteArr((byte)0x01,Options.ActiveID);
            Options.DelID(IDtoDel);
        }
        catch (Exception e)
        {

        }*/

        int newIndex= Options.numOfDevWithActiveID();
        String hex = Integer.toHexString(newIndex);
        byte byteIndex=Byte.parseByte(hex,16);
        byte[] NewDevCom=Instructions.generateAddDevCOM(byteIndex);
        System.out.println("Command is "+Utils.bytesToHex(NewDevCom));
        try {
            byte[] byteResponseReg;
            do {
                ResponseAPDU newDeviceResponse= channel.transmit(new CommandAPDU(NewDevCom));
                byteResponseReg = newDeviceResponse.getBytes();
                System.out.println("Answer is: " + utils.bytesToHex(byteResponseReg));
                //sleep(100);
            }while(utils.isCommand(instructions.NOTYET,byteResponseReg));

            if(byteResponseReg.length<3) {
                TrySendFailCom(false);
                card.disconnect(false);
                return false;
            }
            byte[] NewPub32=Arrays.copyOfRange(byteResponseReg,0,33);
            byte[] NewPub28=Arrays.copyOfRange(byteResponseReg,33,62);
            byte[] NewPub20=Arrays.copyOfRange(byteResponseReg,62,byteResponseReg.length);
            System.out.println("Watch Pub 20 is "+Utils.bytesToHex(NewPub20));
            byte[] newDevID=Utils.addFirstToByteArr(byteIndex,Options.ActiveID);
            Options.addKeys(newDevID,new BigInteger(1,NewPub28),new BigInteger(1,NewPub32),new BigInteger(1,NewPub20));
            boolean working=MultiDevAuth(true);
            //card.disconnect(false);
            if (working)
                return true;
            else{
                try {
                    byte[] IDtoDel=Utils.addFirstToByteArr(byteIndex,Options.ActiveID);
                    Options.DelID(IDtoDel);
                }catch (Exception e){

                }
            }


        } catch (CardException | IOException e) {
            e.printStackTrace();
        }
        TrySendFailCom(false);
        return false;
    }
    public static boolean DeregisterSecondaryDevice(byte deviceNumber)
    {
        try {
            byte[] IDtoDel=Utils.addFirstToByteArr(deviceNumber,Options.ActiveID);
            Options.DelID(IDtoDel);
            System.out.println("Secondary Device has been removed");
            return true;
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
            System.out.println("Could not delete device, maybe that device is not registered");
            return false;
        }

    }
}
