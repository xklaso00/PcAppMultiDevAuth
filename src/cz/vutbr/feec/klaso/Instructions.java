package cz.vutbr.feec.klaso;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
//class that stores instructions and can generate some
public class Instructions {
    public static final byte[] WATCHTHIS = new byte[]{(byte)0x80, //start of command signthis
            (byte)0x03};;
    public static final byte[] WATCHTHISRAND = new byte[]{(byte)0x80,
            (byte)0x04,
            (byte)0x00,
            (byte)0x00,
            (byte)0x20,};;
    public static final byte[] NOTYET = new byte[]{(byte)0x88, //start of command signthis
            (byte)0x88};;
    //choose AID byte
    private byte[] AID = new byte[]{(byte)0x00, // CLA	- Class - Class of instruction
            (byte)0xA4, // INS	- Instruction - Instruction code
            (byte)0x04, // P1	- Parameter 1 - Instruction parameter 1
            (byte)0x00, // P2	- Parameter 2 - Instruction parameter 2
            (byte)0x07, // Lc field	- Number of bytes present in the data field of the command
            (byte)0xF0, (byte)0x39, (byte)0x41, (byte)0x48, (byte)0x14, (byte)0x81, (byte)0x00, // NDEF Tag Application name
            (byte)0x00 };
    byte [] COM1 = new byte[]{(byte)0x80, //mby make a bit different? with this command we want prover to give us rand point
            (byte)0x01,
            (byte)0x00,
            (byte)0x00,


            (byte)0x21,};

    private byte [] COM2 = new byte[]{(byte)0x80,//we modify this command, we add 32bytes of hash and end byte how many bytes we expect
            (byte)0x02,
            (byte)0x00,
            (byte)0x00,
            (byte)0x20,
    };
    private byte [] COM3 = new byte[]{(byte)0x80,//we modify this command, we add 32bytes of hash and end byte how many bytes we expect
            (byte)0x03,
            (byte)0x00,
            (byte)0x00,
            (byte)0x20,
    };
    private byte [] COMSERVERSIG= new byte[]{(byte)0x80,
            (byte)0x05,
            (byte)0x02,
            (byte)0x00,
            (byte)0x40,
    };
    private byte [] COMSERVERSIG28= new byte[]{(byte)0x80,
            (byte)0x05,
            (byte)0x01,
            (byte)0x00,
            (byte)0x38,
    };
    private byte [] COMSERVERSIGWATCH= new byte[]{(byte)0x80,
            (byte)0x07,
            (byte)0x02,
            (byte)0x00,
            (byte)0x40,
    };
    private byte [] COMSERVERSIGWATCH28= new byte[]{(byte)0x80,
            (byte)0x07,
            (byte)0x01,
            (byte)0x00,
            (byte)0x38,
    };
    public static byte[] COMPLACEHOLDERCOM7=
            new byte[]{(byte)0x80,
                    (byte)0x07,
                    (byte)0x01,
                    (byte)0x00,
                    (byte)0x20
            };
    private byte[] DECRYPTME= new byte[]{
            (byte)0x80,
            (byte)0x06,
            (byte)0x00,
            (byte)0x00,
            //(byte)0x3C,
    };
    private static byte [] REGISTERID=
            new byte[]{(byte)0x80,
                    (byte)0x09,
                    (byte)0x00,
                    (byte)0x00,
                    (byte)0x05
            };
    private static byte [] GIVEPUBS=
            new byte[]{(byte)0x80,
                    (byte)0x11,
                    (byte)0x00,
                    (byte)0x00,
                    (byte)0x3E
            };

    public static final byte[] UNKNOWN_CMD_SW = { (byte)0x00,
            (byte)0x00};

    public Instructions(){

    }

    public byte[] generateCOMSERVERSIG(byte[] sig, byte [] hash) throws IOException {
        byte[] startOfCom;
        if(Options.SECURITY_LEVEL==1)
            startOfCom=COMSERVERSIG28;
        else
            startOfCom=COMSERVERSIG;
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
        outputStream.write(startOfCom);
        outputStream.write(hash);
        outputStream.write(sig);
        outputStream.write((byte)0x20);
        byte [] com=outputStream.toByteArray();
        outputStream.close();
        System.out.println("COM IS "+Utils.bytesToHex(com));
        return com;
    }
    public byte[] generateCOMSERVERSIGWITHWATCH(byte[] sig, byte [] hash) throws IOException {
        byte[] startOfCom;
        if(Options.SECURITY_LEVEL==1)
            startOfCom=COMSERVERSIGWATCH28;
        else
            startOfCom=COMSERVERSIGWATCH;

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
        outputStream.write(startOfCom);
        outputStream.write(hash);
        outputStream.write(sig);
        outputStream.write((byte)0x20);
        byte [] com=outputStream.toByteArray();
        outputStream.close();
        return com;
    }
    public byte []generateDecryptMe(byte[] msg) throws IOException {
        String msgLength=Integer.toHexString(msg.length);
        byte dataLength=Byte.parseByte(msgLength,16);
        byte [] com=Utils.mergeWithDataLength(DECRYPTME,dataLength,msg,(byte)0x20);

        return com;
    }
    public static byte[] generateRegisterDeviceCOM(byte[] ID) throws IOException {
        return Utils.mergeThreeByteArrays(REGISTERID,ID,(byte)0x21);
    }
    public static byte[] generateAddDevCOM(byte numOfDev)
    {
        return new byte[]{(byte)0x80, (byte)0x10,numOfDev,(byte)0x00,(byte)0x42};
    }
    public static byte[] returnDoneCOM(boolean isItTrue)
    {
        if (isItTrue)
            return new byte[]{(byte)0x80, (byte)0x20,(byte)0x00,(byte)0x00,(byte)0x00};
        else
            return new byte[]{(byte)0x80, (byte)0x20,(byte)0x01,(byte)0x00,(byte)0x00};
    }

    public static byte[] ReturnRegisterConformationCOM(boolean isItTrue)
    {
        if (isItTrue)
            return new byte[]{(byte)0x80, (byte)0x21,(byte)0x00,(byte)0x00,(byte)0x00};
        else
            return new byte[]{(byte)0x80, (byte)0x21,(byte)0x01,(byte)0x00,(byte)0x00};
    }
    public static byte [] GeneratePubKeysCOM() throws IOException {
        return Utils.mergeFourByteArrays(GIVEPUBS,Options.GetPubKey(2),Options.GetPubKey(1),(byte)0x00);
    }
    public byte[] getAID() {
        return AID;
    }

    public byte[] getCOM1() {
        return COM1;
    }

    public byte[] getCOM3() {
        return COM3;
    }
    public byte[] getNotyet() {
        return NOTYET;
    }
}
