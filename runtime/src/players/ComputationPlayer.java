/* 
 * Copyright (C) 2008 Naom Nisan, Benny Pinkas, Assaf Ben-David.
 * See full copyright license terms in file ../GPL.txt
 * @author Assaf Ben-David 
 */

package players;

import java.math.BigInteger;

import BGW.BGW;

import utils.PRG;
import utils.Utils;

import communication.CPMsgs;
import communication.Msg;

public class ComputationPlayer extends Player {
	
	protected static BigInteger INV2 = null;
	protected static BigInteger _FOUR = BigInteger.valueOf(4);
	
	Thread _msg0 = null;
	BigInteger[] _lambda = null;
	BigInteger[] _myS = null;
	BigInteger[] _s = null;
	int _ID = 0;
	String[] _RP = null;

	public ComputationPlayer(int id, String[] RP) {
		INV2 = BigInteger.valueOf(2).modInverse(_MOD);
		_ID = id;
		initCircuit();
		startClient();
		_lambda = new BigInteger[_wiresNum];
		_myS = new BigInteger[_wiresNum * 2];
		_s = new BigInteger[_wiresNum * 2];
		_RP = RP;
	}
	
	@Override
	public void run() {
		Utils.printMsg("Starting Computation player");
		parseMsg0();
		sendMsg1();
		parseMsg1();
		sendMsg2();		
		parseMsg2();
		sendMsg3();
		parseMsg3();
		sendMsg4();
		parseMsg4();
		sendMsg5();
		parseMsg5();
		sendMsg6();
		Utils.printMsg("Computation player is done.");
	}

	protected void parseMsg0() {
		_msg0 = new Thread(new Runnable(){
			@Override
			public void run() {
				parseMsg(0,new MsgParser(){
					@Override
					protected void parse(int playerID, Msg msg) {
						
						int firstInput = 
							_circuit.getPlayerFirstInputWire(_IP[playerID]);
						int maxWire = firstInput +  
							_circuit.getPlayerInputSize(_IP[playerID]);
						
						for (int w = firstInput ; w < maxWire ; w++) {				
							_lambda[w] = msg.pop();
						}

						for (int w = firstInput ; w < maxWire ; w++){				
							_myS[2*w] = msg.pop();
							_myS[2*w+1] = msg.pop();
						}
					}
				});
			}
		});
		_msg0.start();
	}

	protected void sendMsg1() {
		CPMsgs cpMsgs1 = new CPMsgs(1);
		
		for (int w = _firstNonInputWire ; w < _wiresNum ; w++) {
			cpMsgs1.append(BGW.share(PRG.getRandomBigInt()));
			cpMsgs1.append(BGW.share(BigInteger.ZERO));
		}
		
		for (int w = _firstNonInputWire * 2 ; w < _wiresNum * 2 ; w++){
			_myS[w] = PRG.getRandomBits(_K, false);			
			cpMsgs1.append(BGW.share(_myS[w].shiftLeft(_ID*_K).mod(_MOD)));			
		}
		
		_client.sendToCP(cpMsgs1);
	}

	protected void parseMsg1() {
		for (int w = _firstNonInputWire ; w < _wiresNum ; w++) {				 
			_lambda[w] = BigInteger.ZERO;
		}
		
		for (int w = _firstNonInputWire * 2 ; w < _wiresNum * 2 ; w++) {
			_s[w] = BigInteger.ZERO;
		}
		
		parseMsg(1, new MsgParser(){
			@Override
			protected void parse(int playerID, Msg msg) {
				for (int w = _firstNonInputWire ; w < _wiresNum ; w++) {
					_lambda[w] = 
						_lambda[w].add(msg.pop()).add(msg.pop()).mod(_MOD);
				}
				
				for (int w = _firstNonInputWire * 2 ; w < (_wiresNum * 2) ; w++) {
					_s[w] = _s[w].add(msg.pop()).mod(_MOD);			
				}
			}
		});
		
		for (int w = _firstNonInputWire * 2 ; w < (_wiresNum * 2) ; w++) {
			_s[w] = _s[w].shiftLeft(1).mod(_MOD);
		}
	}

	protected void sendMsg2() {
		CPMsgs msg2 = new CPMsgs(2);		
		
		for (int w = _firstNonInputWire ; w < _wiresNum ; w++ ){
			//System.out.println("basic" + ":" + _lambda[w]);
			msg2.append(_lambda[w].multiply(_lambda[w]).mod(_MOD));
		}
			 
		_client.sendToCP(msg2);
	}

	protected void parseMsg2() {
		_shares = new BigInteger[1][_wiresNum - _firstNonInputWire][_n];
		
		parseMsg(2, new MsgParser(){
			@Override
			protected void parse(int playerID, Msg msg) {
				for (int i = 0 ; i < _wiresNum - _firstNonInputWire ; i++) {					
					_shares[0][i][playerID] = msg.pop();
				}
			}
		});
		
		for (int w = _firstNonInputWire , i = 0  ; w < _wiresNum ; w++ , i++){
 			BigInteger square = BGW.reconstruct(_shares[0][i]);

 			// TODO(abenda): remove.
 			if (square.equals(BigInteger.ZERO)) {
 				_lambda[w] = BigInteger.ZERO;
 				continue;
 			}
 			
			BigInteger smallInv = smallerRoot(square, _MOD).modInverse(_MOD);	
			_lambda[w] = _lambda[w].multiply(smallInv).mod(_MOD).add(
					BigInteger.ONE).mod(_MOD).multiply(INV2).mod(_MOD);			 				
		}
	}
	
	protected void sendMsg3() {
		try {
			_msg0.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		CPMsgs cpMsgs3 = new CPMsgs(3);
		
		for (int g = 0 ; g < _gatesNum ; g++){
			
			int[] in = _circuit.getGateInputWires(g);
			int a = in[0];
			int b = in[1];
			// System.out.println(a + ":" + _lambda[a]);
			// System.out.println(b + ":" + _lambda[b]);
			
			BigInteger[][] A = createGH(a, g);
			BigInteger[][] B = createGH(b, g);
			for (int p = 0 ; p < 2 ; p ++)
				for (int q = 0 ; q < 2 ; q ++)					
					cpMsgs3.append(BGW.share(A[p][q].add(B[q][p]).mod(_MOD)));
			
			BigInteger notLambdaA = 
				_lambda[a].negate().add(BigInteger.ONE).mod(_MOD);
			BigInteger notLambdaB = 
				_lambda[b].negate().add(BigInteger.ONE).mod(_MOD);
					
			//A
			cpMsgs3.append(
					BGW.share(_lambda[a].multiply(_lambda[b]).mod(_MOD)));
			//B
			cpMsgs3.append(
					BGW.share(_lambda[a].multiply(notLambdaB).mod(_MOD)));
			//C
			cpMsgs3.append(
					BGW.share(notLambdaA.multiply(_lambda[b]).mod(_MOD)));
			//D
			cpMsgs3.append(
					BGW.share(notLambdaA.multiply(notLambdaB).mod(_MOD)));
		}
		
		_client.sendToCP(cpMsgs3);
		_shares = null;
	}

	protected void parseMsg3() {
		_shares = new BigInteger[_gatesNum][4][_n];

		for (int i = 0 ; i < _gatesNum ; i++)
			for (int j = 0 ; j < 4 ; j++)
				_ABCD[i][j] = BigInteger.ZERO;

		parseMsg(3, new MsgParser(){
			@Override
			protected void parse(int playerID, Msg msg) {
			
				for (int g = 0 ; g < _gatesNum ; g++) {
					for (int i = 0 ; i < 4 ; i++)
						_ABCD[g][i] = _ABCD[g][i].add(msg.pop()).mod(_MOD);

					for (int i = 0 ; i < 4 ; i++)
						_shares[g][i][playerID] = msg.pop();				
				}
			}
		});		
	}

	protected void sendMsg4() {
		CPMsgs cpMsgs4 = new CPMsgs(4);
		
		BigInteger[] andLambdas = new BigInteger[4];
		BigInteger lambda;
		
		for (int g = 0 ; g < _gatesNum ; g++) {

			// System.out.println("-----------");
			for (int i = 0 ; i < 4 ; i++) {
				andLambdas[i] = BGW.reshare(_shares[g][i]);
			}

			for (int i = 0 ; i < 4 ; i++) {
				lambda = computeGate(g ,i ,andLambdas);
				int c = _circuit.getGateOutputWire(g);
				//System.out.println(i + ":" + _lambda[c]);
				lambda = lambda.subtract(_lambda[c]).mod(_MOD);
				lambda = lambda.multiply(lambda).mod(_MOD);
				cpMsgs4.append(BGW.share(lambda));				
			}
		}
		
		_client.sendToCP(cpMsgs4);
	}
	
	protected void parseMsg4() {
		parseMsg(4, new MsgParser(){
			@Override
			protected void parse(int playerID, Msg msg) {
				for (int g = 0 ; g < _gatesNum ; g++)
					for (int i = 0 ; i < 4 ; i++)
						_shares[g][i][playerID] = msg.pop();				
			}
		});
	}

	protected void sendMsg5() {
		CPMsgs cpMsgs5 = new CPMsgs(5);
		
		for (int g = 0 ; g < _gatesNum ; g++){
			int c = _circuit.getGateOutputWire(g) * 2;
			for (int i = 0 ; i < 4 ; i++){
				BigInteger resultOfNotLambdaI = BGW.reshare(_shares[g][i]);
				//System.out.println(i + ":" + resultOfNotLambdaI);
				BigInteger lambdaI = 
					resultOfNotLambdaI.negate().add(BigInteger.ONE).mod(_MOD);				
				BigInteger s = lambdaI.multiply(_s[c]).add(
					resultOfNotLambdaI.multiply(_s[c+1].add(BigInteger.ONE)));
				cpMsgs5.append(BGW.share(_ABCD[g][i].add(s).mod(_MOD)));
			}
		}
			
		_client.sendToCP(cpMsgs5);		
	}

	protected void parseMsg5() {
		parseMsg(5, new MsgParser(){
			@Override
			protected void parse(int playerID, Msg msg) {
				for (int g = 0 ; g < _gatesNum ; g++)
					for (int i = 0 ; i < 4 ; i++)
						_shares[g][i][playerID] = msg.pop();					
			}
		});
	}

	protected void sendMsg6() {
		Msg msg6 = new Msg(6);

		for (int g = 0 ; g < _gatesNum ; g++)
			for (int i = 0 ; i < 4 ; i++)
				msg6.append(BGW.reshare(_shares[g][i]));

		for (String RP : _RP) {			
			Msg msg = new Msg(msg6);
			for (int[] wires : _circuit.getPlayerOutputs(RP).values()) {				
				for (int wire : wires) {
					if (wire > -1) {
						msg.append(_lambda[wire]);
					}
				}
			}
			
			_client.sendToRP(RP, msg);
		}
		Utils.printMsg("Sending message 6");
	}
		
	// Create the g and h using the usePRG.
	protected BigInteger[][] createGH(int pos, int gateIndex){
		BigInteger[][] res = new BigInteger[2][];
		for (int i = 0 ; i < 2 ; i++)
			res[i] = usePRG(_myS[2*pos + i], gateIndex);	
		
		return res;
	}

	protected static BigInteger computeGate(int g, int i, BigInteger[] andLambdas) {
		boolean not = false;
		BigInteger res = null;
		
		switch(_circuit.getGateTruthTable(g)){
		case 14://1110 - NAND
			not = true;
		case 1: //0001 - AND
			res = andLambdas[i];
			break;
			
		case 13://1101
			not = true;
		case 2: //0010
			res = andLambdas[((i % 2 == 0)?(2):(4))-i];			
			break;
					
		case 11://1011
			not = true;
		case 4: //0100
			res = andLambdas[i + ((i % 2 == 0)?(1):(-1))];
			break;
			
		case 7: //0111
			not = true;
		case 8: //1000
			res = andLambdas[3-i];
			break;
			
		case 6: //0110
			not = true;
		case 9: //1001
			res = andLambdas[i % 3].add(andLambdas[3 - (i % 3)]).mod(_MOD);
			break;
			
		default:				
			Utils.printErr("The circuit type " + _circuit.getGateTruthTable(g) + 
					" Is not supported");
		}
		
		if (not)
			res = res.negate().add(BigInteger.ONE).mod(_MOD);

		return res;
	}
	
	protected static BigInteger smallerRoot(BigInteger square, BigInteger mod) {
		BigInteger r = 
			square.modPow(mod.add(BigInteger.ONE).divide(_FOUR), mod);		
		return r.min(r.negate().mod(mod));
	}	
}