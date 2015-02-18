/* 
 * Copyright (C) 2008 Naom Nisan, Benny Pinkas, Assaf Ben-David.
 * See full copyright license terms in file ../GPL.txt
 * @author Assaf Ben-David 
 */

package utils;

import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

// Implements Pseudo Random Generator.
public class PRG{

	protected static String _PRG_PROTOCOL; 
	protected static BigInteger _MOD = null;	
	protected static final BigInteger _TWO = BigInteger.valueOf(2);
	protected static SecureRandom _SR = new SecureRandom();
			
	public static void init(byte[] seed, BigInteger modulo, String prgProtocol){
		_PRG_PROTOCOL = prgProtocol;
		_SR.setSeed(seed);
		//_SR.setSeed(_SR.generateSeed(128));
		
		setMod(modulo);
	}
	
	protected static void setMod(BigInteger modulo) {
		BigInteger three = BigInteger.valueOf(3);
		BigInteger four = BigInteger.valueOf(4);
		if (modulo.mod(four).compareTo(three) != 0)
			Utils.printErr(modulo + " % 4 != 3");
		
		_MOD = modulo;		
	}

	// Generate random bits	mask with 1 in the left.
	public static BigInteger getRandomBits(int bitNum, boolean useMask) {
		BigInteger mask = (useMask)?(BigInteger.ONE):(BigInteger.ZERO);
		return mask.shiftLeft(bitNum).add(new BigInteger(bitNum , _SR));
	}

	// Generate random BigInteger in the MOD
	public static BigInteger getRandomBigInt() {
		BigInteger rnd = new BigInteger(_MOD.bitLength() * 2 , _SR);
		return rnd.mod(_MOD);
	}
	
	// Generate random BigInteger according to a seed (PRG).
	public static BigInteger generate(BigInteger seed) {
		SecureRandom sr = null;
		try {
			sr = SecureRandom.getInstance(_PRG_PROTOCOL);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			System.exit(1);
		}	
		
		sr.setSeed(seed.toByteArray());
		byte[] b = new byte[_MOD.bitLength()*2];
		sr.nextBytes(b);
		return new BigInteger(b).mod(_MOD);		
	}
}
