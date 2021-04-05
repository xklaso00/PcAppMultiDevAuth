package cz.vutbr.feec.klaso;

import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.spec.ECParameterSpec;
import org.bouncycastle.math.ec.ECCurve;
import org.bouncycastle.math.ec.ECPoint;

import java.math.BigInteger;

public class CurveSpecifics {
    ECParameterSpec ecSpec224 = ECNamedCurveTable.getParameterSpec("secp224r1");
    ECCurve curve224 = ecSpec224.getCurve();
    ECParameterSpec ecSpec256 = ECNamedCurveTable.getParameterSpec("secp256k1");
    ECCurve curve256=ecSpec256.getCurve();

    BigInteger Gx224= new BigInteger("B70E0CBD6BB4BF7F321390B94A03C1D356C21122343280D6115C1D21", 16);
    BigInteger Gy224= new BigInteger("BD376388B5F723FB4C22DFE6CD4375A05A07476444D5819985007E34", 16);
    ECPoint G224 = curve224.createPoint(Gx224,Gy224);

    BigInteger Gx256= new BigInteger("79BE667EF9DCBBAC55A06295CE870B07029BFCDB2DCE28D959F2815B16F81798", 16);
    BigInteger Gy256= new BigInteger("483ADA7726A3C4655DA4FBFC0E1108A8FD17B448A68554199C47D08FFB10D4B8", 16);
    ECPoint G256 = curve256.createPoint(Gx256,Gy256);
    public ECCurve getCurve()
    {
        switch (Options.SECURITY_LEVEL) {
            case 1:
                return curve224;
            case 2:
                return curve256;
            default:
                return  curve256;
        }
    }
    public ECPoint getG()
    {
        switch (Options.SECURITY_LEVEL){
            case 1:
                return G224;

            case 2:
                return G256;
            default:
                return  G256;
        }
    }
    public BigInteger getN()
    {
        switch (Options.SECURITY_LEVEL){
            case 1:
                return curve224.getOrder();

            case 2:
                return curve256.getOrder();
            default:
                return  curve256.getOrder();
        }
    }
}
