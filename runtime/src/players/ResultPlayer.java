/* 
 * Copyright (C) 2008 Naom Nisan, Benny Pinkas, Assaf Ben-David.
 * See full copyright license terms in file ../GPL.txt
 * @author Assaf Ben-David 
 */

package players;

import java.math.BigInteger;
import java.util.Map;

import BGW.BGW;

import utils.Utils;

import communication.Msg;

public class ResultPlayer extends Player{
		
	Thread _msg6 = null;
	BigInteger[][] _lambdas = null;
	BigInteger[][] _s = null;
	int[] _lambda = null;
	int[] _externalValue = null;
	
	public ResultPlayer() {
		initCircuit();
		_s = new BigInteger[_wiresNum][_n];
		_lambda = new int[_wiresNum];
		_externalValue = new int[_wiresNum];
	}
	
	@Override
	public void run() {
		Utils.printMsg("Starting Result player.");
		
		parseMsg6();
		parseMsg7();
		
		try {
			_msg6.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		compileCircuit();
		printResult();
		
		Utils.printMsg("Result player is done.");
	}
	
	protected void parseMsg6() {
		_msg6 = new Thread(new Runnable(){
			@Override
			public void run() {
				_shares = new BigInteger[_gatesNum][4][_n];
				_lambdas = new BigInteger[_wiresNum][_n];
				
				parseMsg(6,new MsgParser(){
					@Override
					protected void parse(int playerID, Msg msg) {
						for (int g = 0 ; g < _gatesNum ; g++) {
							for (int i = 0 ; i < 4 ; i++) {
								_shares[g][i][playerID] = msg.pop(); {									
								}
							}
						}
									
						for (int[] wires : _circuit.getPlayerOutputs(_name).values()) {				
							for (int wire : wires) {
								if (wire > -1) {
									_lambdas[wire][playerID] = msg.pop();
								}
							}
						}
					}
				});
				
				for (int g = 0 ; g < _gatesNum ; g++) {
					for (int i = 0 ; i < 4 ; i++) {
						_ABCD[g][i] = BGW.reconstruct(_shares[g][i], _n);
					}
				}
				
				for (int[] wires : _circuit.getPlayerOutputs(_name).values()) {
					for (int wire : wires) {
						if (wire > -1) {
							_lambda[wire] = BGW.reconstruct(_lambdas[wire], _n).intValue();
						}
					}
				}
			}
		});
		_msg6.start();
	}
	
	protected void parseMsg7() {
		parseMsg(7, new MsgParser(){
			@Override
			protected void parse(int playerID, Msg msg) {
				String name = _IP[playerID];
				int pos = _circuit.getPlayerFirstInputWire(name);
				int size = _circuit.getPlayerInputSize(name);
				for (int i = 0 ; i < size ; i++) {
					splitAndComputePos((pos+i), msg.pop());
				}
			}
		});
	}

	protected void compileCircuit() {
		for (int g = 0 ; g < _gatesNum ; g++){	
			int[] in = _circuit.getGateInputWires(g);
			int a = in[0];
			int b = in[1];
			int c = _circuit.getGateOutputWire(g);
			int p = _externalValue[a];
			int q = _externalValue[b];
			
			BigInteger[] A = createGH(a, g);
			BigInteger[] B = createGH(b, g);
			splitAndComputePos(c, BigInteger.ONE.shiftLeft(_n*_K+1).add(
					_ABCD[g][2*p + q].subtract(A[q]).subtract(B[p]).mod(_MOD)));
		}
	}

	protected void splitAndComputePos(int pos, BigInteger s) {
		_externalValue[pos] = s.testBit(0)?1:0;
		_s[pos] = split(s.shiftRight(1), _n);
	}

	protected void printResult() {
		Map<String,int[]> res = _circuit.getPlayerOutputs(_name);
		for (String key : res.keySet()) {
			int value = buildValue(res.get(key));
			System.out.println("Value for " + key + " is: " + value);
		}
	}
	
	protected int buildValue(int[] bits) {
		int result = 0;
		int val = 1;
		for (int bit : bits) {
			switch (bit) {
			case -2:
				break;
			case -1:
				result += val;
				break;
			default:
				if ((_externalValue[bit]^_lambda[bit]) == 1) {
					result += val;
				}
				break;
			}
			val *= 2;
		}
			
		return result;
	}

	protected BigInteger[] createGH(int pos, int gateIndex) {
		BigInteger[] gh ,res = new BigInteger[2];
		res[0] = BigInteger.ZERO;
		res[1] = BigInteger.ZERO;
		for (int id = 0 ; id < _n ; id++){
			gh = usePRG(_s[pos][id], gateIndex);
			
			for (int i = 0 ; i < 2 ; i++) {
				res[i]=res[i].add(gh[i]).mod(_MOD);
			}
		}		
		return res;
	}
}