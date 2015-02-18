/* 
 * Copyright (C) 2008 Naom Nisan, Benny Pinkas, Assaf Ben-David.
 * See full copyright license terms in file ../GPL.txt
 * @author Assaf Ben-David 
 */

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Vector;

import BGW.BGW;

import circuit.*;
import communication.*;
import config.*;
import utils.*;
import players.*;

// Main program.
public class FairplayMP {
	
	static double _version = 1.1; 
		
	static Thread _ipThread = null; 
	static Thread _cpThread = null; 
	static Thread _rpThread = null;
	static Thread _server = null;
	
	static String[] _CP = null;
	static ConfigFile _conf = null;
	static Circuit _cir = null;
	static String _name;
	static String _ip;
	static BigInteger _MOD = null;
	
	public static void main(String[] args) {

		parseArgs(args);

		init(args[0]);
		
		runPlayers();
		
		waitForPlayers();
		
		Client.finishSending();

		System.exit(0);
	}

	private static void parseArgs(String[] args) {
		if (args.length == 1) {
			return;
		}
		if ((args.length == 2) && args[1].equals("-v")) {
			Utils.v = true;
			return;
		}		
		if ((args.length == 3) && args[1].equals("Test")) {
			Utils.v = true;
			InputPlayer._injectedInput = new BigInteger(args[2]);
			return;
		}

		Utils.printErr("Usage (version " + _version + "): FairplayMP <random seed> [-v]");
	}

	protected static void init(String seed) {
		getIPAddress();		
		
		Vector<SortedMap<String, Msg>> msgs = 
			new Vector<SortedMap<String, Msg>>();
		for (int i = 0 ; i < 8 ; i++) {
			msgs.add(new TreeMap<String, Msg>());
		}
	
		if (_conf == null)
			_conf = new XMLParser("config.xml");
		_cir = new SHDLCircuitCnv(_conf.getCircuit());
		BigInteger MOD = _conf.getModulo();

		int port = _conf.getPort();
		_server = new Thread(new Server(msgs, port));		
		Hashtable<String, String> Players = _conf.getPlayers();
		getName(Players);
		
		_CP = _conf.getCP();
		Arrays.sort(_CP);
		int n = _CP.length;
		if (n % 2 == 0) {
			Utils.printErr("The number of CP players should be odd");
		}
		
		initSSL();
		CPMsgs.init(n);
		PRG.init(seed.getBytes(), MOD, _conf.getPRGProtocol());
		String[] lambdas = _conf.getLambdas();				

		BGW.init(MOD, n, lambdas);
		Client.init(Players, _CP, _cir.getResultPlayers(), port);
		Player.init(_name, _cir, _conf, msgs, n);
	}

	protected static void runPlayers() {
		String[] IP = _cir.getInputPlayers();
		String[] RP = _cir.getResultPlayers();
		int id;

		if (isIn(_name, IP) > -1) {
			_ipThread = new Thread(new InputPlayer()); 
			_ipThread.start();
		}
		
		if ((id = isIn(_ip, _CP)) != -1) {
			_cpThread = new Thread(new ComputationPlayer(id, RP)); 
			_cpThread.start();
			_server.start();
		}

		if (isIn(_name, RP) > -1) {
			_rpThread = new Thread(new ResultPlayer()); 
			_rpThread.start();
			if (id == -1)
				_server.start();
		}	
	}

	protected static void waitForPlayers() {		
		try {
			if (_ipThread != null)
				_ipThread.join();
						
			if (_cpThread != null)
				_cpThread.join();

			if (_rpThread != null)
				_rpThread.join();					
		} catch (InterruptedException e) {
			e.printStackTrace();
			System.exit(1);
		}				
	}
	
	protected static void getIPAddress() {
		try {
			_ip = InetAddress.getLocalHost().getHostAddress().trim();
		} catch (UnknownHostException e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		Utils.printMsg("The ip address found for this computer is: " + _ip);
	}
	
	protected static void getName(Hashtable<String, String> players) {
		String name = null;
		Iterator<String> it = players.keySet().iterator();
		while(it.hasNext()) {
			
			name = it.next();
			
			if (_ip.equals(players.get(name))) {
				_name = name;
				return;
			}
		}
	}
	
	protected static int isIn(String s, String[] sArray) {
		if (s == null)
			return -1;
		
		for (int i = 0 ; i < sArray.length ; i++)
			if (sArray[i].equals(s))
				return i;
		
		return -1;
	}
	
	protected static void initSSL() {
		System.setProperty("javax.net.ssl.keyStore", _conf.getKeyStore());
		System.setProperty("javax.net.ssl.keyStorePassword",
				_conf.getKeyStorePassword());
		System.setProperty("javax.net.ssl.trustStore", _conf.getTrustStore());
		System.setProperty("javax.net.ssl.trustStorePassword", 
				_conf.getTrustStorePassword());
	}
}
