/* 
 * Copyright (C) 2008 Naom Nisan, Benny Pinkas, Assaf Ben-David.
 * See full copyright license terms in file ../GPL.txt 
 */

package BGW;

import java.math.BigInteger;
import java.security.SecureRandom;

/**
 * This is an implementation of the BGW protocol for semi-honest players:
 * www. (link)
 * Also used:
 * www. (link)
 * 
 * @author Assaf Ben-David (abenda).
 */
public class BGW {

	protected static BigInteger[] _LAMBDA;
	protected static BigInteger _MOD;
	protected static int _n;
	protected static SecureRandom _SR = new SecureRandom();
	
	public static void init(BigInteger modulo, int n, String[] lambdas) {
		if (n % 2 == 0) {
			throw new IllegalArgumentException("n should be odd");
		}
		
		_MOD = modulo;
		_n = n;		
		_LAMBDA = (lambdas != null) ? parseString(lambdas) : buildLambdas();
	}

	// Share  a secret to n players.
	public static BigInteger[] share(BigInteger secret) {
		
		if (secret.compareTo(_MOD) == 1) {
			throw new IllegalArgumentException(
				"The secret: " + secret + " is larger than the MOD: " + _MOD);
		}
					
		int t = (_n - 1)/2;
		BigInteger[] coefficient = new BigInteger[t];
		for (int i = 0 ; i < t ; i++) {
			coefficient[i] = new BigInteger(_MOD.bitLength() * 2 , _SR).mod(_MOD);
		}
		
		BigInteger[] shares = new BigInteger[_n];
		for (int j = 0 ; j < _n ; j++){
			BigInteger X = BigInteger.valueOf(j + 1).mod(_MOD);
			BigInteger share = BigInteger.ZERO;
			
			// Horner
			for (int i = t-1 ; i >= 0 ; i--) {
				share = share.add(coefficient[i]).multiply(X).mod(_MOD);
			}
			
			shares[j] = share.add(secret).mod(_MOD);
		}

		return shares;
	}

	// Build the share after multiply two shares.
	public static BigInteger reshare(BigInteger[] shares) {
		if (shares.length != _n) {
			throw new IllegalArgumentException(
				"Shares length: " + shares.length + " doesn't match n: " + _n);
		}
		
		BigInteger sum = BigInteger.ZERO;
		for (int i = 0 ; i < shares.length ; i++) {
			sum = sum.add(_LAMBDA[i].multiply(shares[i]).mod(_MOD)).mod(_MOD);
		}
		return sum;
	}

	public static BigInteger reconstruct(BigInteger[] shares) {
		int t = (_n + 1) / 2;
		return reconstruct(shares, t);
	}
	
	// Reconstruct the shares to reveal the secret.
	public static BigInteger reconstruct(BigInteger[] shares, int t) {					
		BigInteger s = BigInteger.ZERO;
		for (int i = 0 ;  i < t ; i++){
			BigInteger c = BigInteger.ONE;
			for (int j = 0 ; j < t ; j++){
				if (i == j)
					continue;
					
				BigInteger J = BigInteger.valueOf(j+1).mod(_MOD);
				BigInteger Inv = BigInteger.valueOf(j-i).modInverse(_MOD);					
				c = c.multiply(J).mod(_MOD).multiply(Inv).mod(_MOD);
			}
			s = s.add(c.multiply(shares[i])).mod(_MOD);
		}
		return s;
	}
	
	public static BigInteger[] buildLambdas(){
		int cols = 2*_n;
		BigInteger[][] A = new BigInteger[_n][cols];
		for (int r = 0 ; r < _n ; r++){
			BigInteger v = BigInteger.ONE;
			BigInteger R = BigInteger.valueOf(r+1);
			for (int c = 0 ; c < _n ; c++){				
				A[r][c] = v;
				v = v.multiply(R).mod(_MOD);
			}
			for (int c = 0 ; c < _n ; c++){
				if (c == r) {
					A[r][c+_n] = BigInteger.ONE;
				} else {
					A[r][c+_n] = BigInteger.ZERO;
				}
			}
		}
		
		for (int r = 0 ; r < _n ; r++) {

			BigInteger inv = A[r][r].modInverse(_MOD);
			for (int c = r ; c < cols ; c++) {
				A[r][c] = A[r][c].multiply(inv).mod(_MOD);
			}
						
			for (int r2 = 0 ; r2 < _n ; r2++) {
				if (r2 == r) {
					continue;
				}
				
				BigInteger val = A[r2][r];
				for (int c = r ; c < cols ; c++){
					A[r2][c] = A[r2][c].subtract(val.multiply(A[r][c])).mod(_MOD);
				}
			}			
		}
		
		BigInteger[] res = new BigInteger[_n];
		for (int i = 0 ; i < _n ; i++) {
			res[i] = A[0][_n+i];
		}
		
		return res;
	}
	
	protected static BigInteger[] parseString(String[] lambdas) {
		if (lambdas.length != _n) {
			throw new IllegalArgumentException(
				"lambdas length + " + lambdas.length + " doesn't match n: " + _n);
		}
		BigInteger[] res = new BigInteger[lambdas.length];
		for (int i = 0 ; i < lambdas.length ; i++) {
			res[i] = new BigInteger(lambdas[i]);
		}
		
		return res;
	}
	
	public static void main(String[] args) {
		BigInteger MOD = new BigInteger("103");
		int n = 37;
		BGW.init(MOD, n, null);
		
		BigInteger[] shares = BGW.share(new BigInteger("17"));
		System.out.println(BGW.reconstruct(shares));
		System.out.println(BGW.reconstruct(shares, 18));
		System.out.println(BGW.reconstruct(shares, 19));
	}

}