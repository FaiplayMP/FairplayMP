/* 
 * Copyright (C) 2008 Naom Nisan, Benny Pinkas, Assaf Ben-David.
 * See full copyright license terms in file ../GPL.txt
 * @author Assaf Ben-David 
 */

package config;

import java.math.BigInteger;
import java.util.Hashtable;

public interface ConfigFile {

	Hashtable<String, String> getPlayers();

	String[] getCP();

	String[] getLambdas();

	String getCircuit();

	int getPort();

	String getPRGProtocol();
	
	String getKeyStore();

	String getKeyStorePassword();

	String getTrustStore();

	String getTrustStorePassword();

	BigInteger getModulo();
}
