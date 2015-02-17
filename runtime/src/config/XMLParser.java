/* 
 * Copyright (C) 2008 Naom Nisan, Benny Pinkas, Assaf Ben-David.
 * See full copyright license terms in file ../GPL.txt
 * @author Assaf Ben-David 
 */

package config;
import java.io.File;
import java.math.BigInteger;
import java.util.Hashtable;

import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class XMLParser implements ConfigFile{
		
	Document _doc;
	
	public XMLParser(String configName) {
		try {		
			File file = new File(configName);
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db;
			db = dbf.newDocumentBuilder();
			_doc = db.parse(file);
			_doc.getDocumentElement().normalize();
		}catch (Exception e){
			System.err.println("Problem with opening the XML file: " 
					+ e.toString());
			System.exit(1);
		}
	}
	
	private String get(String tag) {
		//System.err.println("Tag " + tag + "=" + _doc.getElementsByTagName(tag).item(0).getTextContent().trim());
		return _doc.getElementsByTagName(tag).item(0).getTextContent().trim();
	}

	private String[] getArray(String tag) {
		String res = get(tag);
		res = res.replace(" ", "");
		String [] str = res.split(",");
		return str;
	}
	
	public Hashtable<String,String> getPlayers() {
		NodeList nl = _doc.getElementsByTagName("Player");
		Hashtable<String,String> h = new Hashtable<String,String>();
		int size = nl.getLength();
		for (int i = 0 ; i < size ; i++){
			String key = 
				nl.item(i).getAttributes().item(0).getNodeValue().trim();
			String val = nl.item(i).getTextContent().trim();
			h.put(key , val);			
		}
		return h;
	}

	@Override
	public String[] getCP() {
		return getArray("ComputationPlayers");
	}

	@Override
	public String[] getLambdas() {
		String tag = "Lambdas";
		if (_doc.getElementsByTagName(tag).item(0) == null)
			return null;
		
		return getArray(tag);
	}

	@Override
	public String getCircuit() {
		return get("Circuit");
	}

	@Override
	public String getKeyStore() {
		return get("KeyStore");
	}

	@Override
	public String getKeyStorePassword() {
		return get("KeyStorePassword");
	}

	@Override
	public String getPRGProtocol() {
		return get("PRGProtocol");
	}

	@Override
	public int getPort() {
		return Integer.parseInt(get("Port"));
	}

	@Override
	public String getTrustStore() {
		return get("TrustStore");
	}

	@Override
	public String getTrustStorePassword() {
		return get("TrustStorePassword");
	}

	@Override
	public BigInteger getModulo() {
		//System.err.println(new BigInteger(get("Modulo")));
		return new BigInteger(get("Modulo"));
	}
}