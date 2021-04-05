package cz.vutbr.feec.klaso;
import org.bouncycastle.math.ec.ECPoint;

import javax.smartcardio.*;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;

import static java.lang.Thread.sleep;

public class Main {


    public static void main(String[] args) {
	// write your code here
        Card card = null;
        CardChannel channel = null;
        Utils utils= new Utils();
        BigInteger PubBig= new BigInteger("02DFF1D77F2A671C5F36183726DB2341BE58FEAE1DA2DECED843240F7B502BA659",16);
        BigInteger PubWatch= new BigInteger("02726E54885BFA6595DBB16FEE753E6B685CABE85F26B23D5A7B4863FFAEE60C2C",16);
        byte [] ID= new byte[]{(byte)0x10,
                (byte)0x20,
                (byte)0x30,
                (byte)0x40,
                (byte)0x50,
        };
        byte [] PubKey=Options.getMobileKey().toByteArray();
        byte [] PubKeyWatch=Options.getWatchKey().toByteArray();
        Instructions instructions = new Instructions();

        try {
            TerminalFactory factory = TerminalFactory.getDefault();
            List<CardTerminal> terminals = factory.terminals().list();
            System.out.println("Terminals: " + terminals);
            CardTerminal terminal = terminals.get(0);

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
            ECOperations ecOperations= new ECOperations();





            BigInteger hashOfServer=ecOperations.hashOfProver(ID);
            byte[] byteHash= utils.bytesFromBigInteger(hashOfServer);
            byte[] SigOfServer= ecOperations.generateSignatureOfServer(hashOfServer);
            //byte[] CommandToSave=instructions.generateCOMSERVERSIG(SigOfServer,byteHash);
            byte[] CommandToSave=instructions.generateCOMSERVERSIGWITHWATCH(SigOfServer,byteHash);
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
            byte[] byteResponseServerSig;
            do {
                ResponseAPDU responseServerSig = channel.transmit(new CommandAPDU(CommandToSave));
                byteResponseServerSig = responseServerSig.getBytes();
                System.out.println("Answer is: " + utils.bytesToHex(byteResponseServerSig));
                sleep(100);
            }while(utils.isCommand(instructions.NOTYET,byteResponseServerSig));
            if(utils.isEqual(byteResponseServerSig,Instructions.UNKNOWN_CMD_SW))
            {
                card.disconnect(false);
                return;
            }
            long EndTime = System.nanoTime();
            System.out.println("Only communitaction took "+(EndTime-StartTime)/1000000+" ms");
            byte [] ClientID= Arrays.copyOfRange(byteResponseServerSig,0,5);
            byte [] ClientHash= Arrays.copyOfRange(byteResponseServerSig,5,Options.BYTELENGHT+5);
            byte [] ClientSig= Arrays.copyOfRange(byteResponseServerSig,Options.BYTELENGHT+5,Options.BYTELENGHT*2+5);
            byte[] WatchSig=Arrays.copyOfRange(byteResponseServerSig,Options.BYTELENGHT*2+5,byteResponseServerSig.length);
            System.out.println("Client sig is? "+ utils.bytesToHex(ClientSig));
            System.out.println("Watch sig is? "+ utils.bytesToHex(WatchSig));
            ecOperations.CreateKeyForBoth(PubKey,PubKeyWatch);
            boolean isItTrue=ecOperations.VerifyWithWatch(ClientID,ClientHash,ClientSig,WatchSig,CommandToSave);
            System.out.println("is it legit tho? "+isItTrue);
            //AES test
            byte[] secPart=ecOperations.generateSecMsg(ClientHash);
            byte[] aesInc=instructions.generateDecryptMe(secPart);
            ResponseAPDU aesResponse = channel.transmit(new CommandAPDU(aesInc));
            byte[] aesResponseBytes=aesResponse.getBytes();
            System.out.println("Answer is: "+utils.bytesToHex(aesResponseBytes));


            card.disconnect(false);






        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

    }
}
