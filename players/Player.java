/* 
 * Copyright (C) 2008 Naom Nisan, Benny Pinkas, Assaf Ben-David.
 * See full copyright license terms in file ../GPL.txt
 * @author Assaf Ben-David 
 */

package players;

import java.math.BigInteger;
import java.util.Iterator;
import java.util.SortedMap;
import java.util.Vector;

import circuit.Circuit;
import utils.*;
import communication.*;
import config.ConfigFile;

public abstract class Player implements Runnable {

	public static BigInteger _mask = BigInteger.ZERO;

	BigInteger[][][] _shares = null;
	BigInteger[][] _ABCD = null;
	
	static Vector<SortedMap<String, Msg>> _msgs = null;
	static int _wiresNum = 0;
	static int _gatesNum = 0;
	static int _firstNonInputWire = 0;
	static boolean _init = false;
	static String[] _IP = null;
	static Client _client = null;
	static Object _clientSync = new Object();
	static Circuit _circuit = null;
	static String _name = null;
	static int _n = 0;
	static int _K = 0;
	static int _sl = 0;
	static BigInteger _MOD = null;
		
	public Player() {
		if (!_init)
			Utils.printErr("Player must be init first");
	}
	
	public static void init(String name, Circuit cir, ConfigFile _par, 
			Vector<SortedMap<String, Msg>> msgs, int n) {
		_name = name;
		_circuit = cir;
		_msgs = msgs;

		_n = n;
		_MOD = _par.getModulo();
		_K = (_MOD.bitLength()-2)/n;
		
		_init = true;
	}
	
	// Only for CP and RP
	protected void initCircuit() {
		_IP = _circuit.getInputPlayers();
		_wiresNum = _circuit.getWiresCount();
		_gatesNum = _circuit.getGatesCount();
		_firstNonInputWire = _circuit.getFirstNonInputWirePos();
		_ABCD = new BigInteger[_gatesNum][4];
		_sl = Integer.bitCount(_gatesNum);
	}
	
	protected void startClient() { 
		if (_client == null)
			synchronized(_clientSync){
				if (_client == null)
					_client = new Client();
			}
	}
	
	protected void waitForMsgs(int i) {
		//System.err.println("Waiting for msg " + i);
		//long t = System.currentTimeMillis();
		int size = (i == 0 || i == 7)?_IP.length:_n;
		SortedMap<String, Msg> msgs = _msgs.elementAt(i); 
		synchronized(msgs){
			while (msgs.size() != size){
				try {
					msgs.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
					System.exit(1);
				}				
			}
		}
		//System.err.println("For msg " + i + " waited: " + (System.currentTimeMillis() - t));
	}
	
	protected void parseMsg(int msgID, MsgParser mp){
		waitForMsgs(msgID);
		mp.parseMsg(_msgs.elementAt(msgID));
		//System.err.println("Parsed " + msgID);
	}
	
	protected abstract class MsgParser {
		
		public void parseMsg(SortedMap<String, Msg> msgs) {
			Iterator<Msg> it = msgs.values().iterator();
			for (int i = 0 ; it.hasNext() ; i++)
				parse(i, it.next());				
		}
		
		abstract protected void parse(int playerID, Msg msg);
	}	 

	protected BigInteger[] usePRG(BigInteger seed, int gateIndex) {
		return split(PRG.generate(
				seed.shiftLeft(_sl).add(BigInteger.valueOf(gateIndex))), 2);		
	}

	public static BigInteger[] split(BigInteger bi, int n) {
		
		int size = ((bi.bitLength()) - 1 )/ n; 
			
		BigInteger[] res = new BigInteger[n];
		if (_mask.bitLength() != size)
			_mask = BigInteger.valueOf(-1).mod(BigInteger.ONE.shiftLeft(size));
		
		for (int i = 0 ; i < n ; i++) {
			res[i] = bi.and(_mask);
			bi = bi.shiftRight(size);
		}
		
		return res;
	}	
}
