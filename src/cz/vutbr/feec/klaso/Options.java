package cz.vutbr.feec.klaso;

import java.math.BigInteger;

public class Options {
    public static int SECURITY_LEVEL=2;
    private static BigInteger SecKey256= new BigInteger("C89F0B6DF429D18ED46D8C0F91A7D5EFFFF5B620514ECEC0D9ED3728A3B2008D",16);
    private static BigInteger SecKey224= new BigInteger("AF1C20A86D38DB16B2E99BEF51A0EA1962EE0A85BA831A2BDE94DE0A",16);
    private static BigInteger PubMobile224=new BigInteger("0228D68E9EF4AFE5FB144C8883D10BBB233AA1E00258ACC9B9600B63D0",16);
    private static BigInteger PubWatch224=new BigInteger("02B6848FAE55DC9BE96E4E456439F9A48EB35452A74548A1E2A041DDC5",16);
    private static BigInteger PubMobile256=new BigInteger("02DFF1D77F2A671C5F36183726DB2341BE58FEAE1DA2DECED843240F7B502BA659",16);
    private static BigInteger PubWatch256=new BigInteger("02726E54885BFA6595DBB16FEE753E6B685CABE85F26B23D5A7B4863FFAEE60C2C",16);
    public static int BYTELENGHT=32;

    public static  void setSecurityLevel(int level)
    {
        if (level>0&&level<5)
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
        if(SECURITY_LEVEL==1)
            return SecKey224;
        else
            return SecKey256;
    }
    public static BigInteger getWatchKey()
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
    }


}
