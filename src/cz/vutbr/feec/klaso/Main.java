package cz.vutbr.feec.klaso;
import org.bouncycastle.math.ec.ECPoint;

import javax.smartcardio.*;
import javax.swing.*;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static java.lang.Thread.sleep;

public class Main {


    public static void main(String[] args) {
	// write your code here
        //Card card = null;
       // CardChannel channel = null;
        //Utils utils= new Utils();
        //BigInteger PubBig= new BigInteger("02DFF1D77F2A671C5F36183726DB2341BE58FEAE1DA2DECED843240F7B502BA659",16);
        //BigInteger PubWatch= new BigInteger("02726E54885BFA6595DBB16FEE753E6B685CABE85F26B23D5A7B4863FFAEE60C2C",16);
        //byte [] ID;

        //Instructions instructions = new Instructions();
        Options.setSecurityLevel(2);
       /* HashMap<String,BigInteger>IdKeyPairs=new HashMap<String,BigInteger>();
        try {
            byte[] IDMob=Utils.addFirstToByteArr((byte)0x00,ID);
            byte[] IDWatch=Utils.addFirstToByteArr((byte)0x01,ID);
            IdKeyPairs.put(Utils.bytesToHex(IDMob),Options.getMobileKey());
            IdKeyPairs.put(Utils.bytesToHex(IDWatch),Options.getWatchKey());
        }
        catch (IOException e)
        {
            System.out.println(e.toString());
        }
        ClientKeyFile.WriteHashMapToFile(IdKeyPairs,Options.SECURITY_LEVEL);*/
        Options.setMaps();
        AppGUI frame= new AppGUI();
        frame.setContentPane(new AppGUI().PanelMain);

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setSize(700,500);
        frame.setVisible(true);
        //Terminal.MultiDevAuth();
        /*Options.setID(ID);


        byte [] PubKey=Utils.bytesFromBigInteger(Options.GetKey(0));
        byte [] PubKeyWatch=Utils.bytesFromBigInteger(Options.GetKey(1));
        System.out.println("Pub key of dev 0 is "+Utils.bytesToHex(PubKey));
        System.out.println("Pub key of dev 1 is "+Utils.bytesToHex(PubKeyWatch));*/
        /*try {
            TerminalFactory factory = TerminalFactory.getDefault();
            List<CardTerminal> terminals = factory.terminals().list();
            System.out.println("Terminals: " + terminals);
            CardTerminal terminal = terminals.get(0);
            ECOperations ecOperations= new ECOperations();
            //ecOperations.computeTv();
            //while no card is present terminal will try to connect with a card
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
            ResponseAPDU response1 = channel.transmit(new CommandAPDU(instructions.getAID()));

            byte[] byteResponse1 = null;
            byteResponse1 = response1.getBytes();
            System.out.println("Card response for choose AID command: " + utils.bytesToHex(byteResponse1));
            ID=Arrays.copyOfRange(byteResponse1,0,byteResponse1.length-2);
            Options.setID(ID);
            System.out.println("To get apdu answer it took "+(System.nanoTime()-StartTime)/1000000+" ms");
            long ST1=System.nanoTime();
            byte [] PubKey=Utils.bytesFromBigInteger(Options.GetKey(0));
            byte [] PubKeyWatch=Utils.bytesFromBigInteger(Options.GetKey(1));
            System.out.println("Pub key of dev 0 is "+Utils.bytesToHex(PubKey));
            System.out.println("Pub key of dev 1 is "+Utils.bytesToHex(PubKeyWatch));




            BigInteger hashOfServer=ecOperations.hashOfProver(ID);
            byte[] byteHash= utils.bytesFromBigInteger(hashOfServer);
            byte[] SigOfServer= ecOperations.generateSignatureOfServer(hashOfServer);
            //byte[] CommandToSave=instructions.generateCOMSERVERSIG(SigOfServer,byteHash);
            byte[] CommandToSave=instructions.generateCOMSERVERSIGWITHWATCH(SigOfServer,byteHash);
            long dur1=System.nanoTime()-ST1;
            System.out.println("our signature took "+dur1+" ns");*/
            //only phone
            /*
            ResponseAPDU responseServerSig = channel.transmit(new CommandAPDU(CommandToSave));
            byte[] byteResponseServerSig = responseServerSig.getBytes();
            System.out.println("Answer is: "+utils.bytesToHex(byteResponseServerSig));

            byte [] ClientID= Arrays.copyOfRange(byteResponseServerSig,0,5);
            byte [] ClientHash= Arrays.copyOfRange(byteResponseServerSig,5,37);
            byte [] ClientSig= Arrays.copyOfRange(byteResponseServerSig,37,69);
            boolean isItTrue=ecOperations.verifyClientSig2(ClientID,ClientHash,ClientSig,PubKey,CommandToSave);
            System.out.println("is it legit tho? "+isItTrue);
            */
           /* long timeForApduRes;
            long TimeToGetSign=System.nanoTime();
            byte[] byteResponseServerSig;
            byte[] CommandToSave2=CommandToSave;
            do {
                timeForApduRes=System.nanoTime();
                ResponseAPDU responseServerSig = channel.transmit(new CommandAPDU(CommandToSave2));
                byteResponseServerSig = responseServerSig.getBytes();
                CommandToSave2=Instructions.COMPLACEHOLDERCOM7;
                System.out.println("Answer is: " + utils.bytesToHex(byteResponseServerSig));
                //System.out.println("To get back comman "+ (System.nanoTime()-timeForApduRes)/1000000+" ms");
                //if(utils.isCommand(instructions.NOTYET,byteResponseServerSig))
                  // sleep(70);
            }while(utils.isCommand(instructions.NOTYET,byteResponseServerSig));
            System.out.println("To get back proof after succesfull command took "+ (System.nanoTime()-timeForApduRes)/1000000+" ms");
            if(utils.isEqual(byteResponseServerSig,Instructions.UNKNOWN_CMD_SW))
            {
                card.disconnect(false);
                return;
            }
            long EndTime = System.nanoTime();
            System.out.println("To Get Proof after sending req it took "+(EndTime-TimeToGetSign)/1000000+" ms");
            System.out.println("Only communitaction took "+(EndTime-StartTime)/1000000+" ms");
            StartTime=System.nanoTime();
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
            card.disconnect(false);






        }
        catch (Exception e)
        {
            e.printStackTrace();
        }*/

    }
}
