/* 
 * Copyright (C) 2008 Naom Nisan, Benny Pinkas, Assaf Ben-David.
 * See full copyright license terms in file ../GPL.txt
 * @author Assaf Ben-David 
 */

package players;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigInteger;

import BGW.BGW;

import communication.*;
import circuit.Circuit;
import utils.*;

// Implements the Input Player protocol.
public class InputPlayer extends Player {

	CPMsgs _msg0 = new CPMsgs(0);
	Msg _msg7 = new Msg(7);
	
	// TODO(abenda) - remove:
	public static BigInteger _injectedInput = null;
	BigInteger _input = BigInteger.ZERO;
	BigInteger _lambda = null;

	int _inputSize = 0;
	
	public InputPlayer() {
		startClient();
		_inputSize = _circuit.getPlayerInputSize(_name);
	}	

	@Override
	public void run() {		
		Utils.printMsg("Starting Input player.");

		readInput();
		
		createAndShareLambda();
		
		createAndShareSeeds();		
				
		_client.sendToCP(_msg0);
		_client.sendToRP(_msg7);
		
		Utils.printMsg("Input player is done.");
	}

	protected void readInput() {
		Circuit.PlayerInput[] inputs = _circuit.getPlayerInputs(_name);
		for (int i = 0 ; i < inputs.length ; i++){
			BigInteger in = readBigInteger(inputs[i]._name, inputs[i]._size);
			Utils.printMsg("Input " + inputs[i]._name + "=" + in);
			_input = _input.shiftLeft(inputs[i]._size).add(in);
		}
	}
	
	protected void createAndShareLambda() {
		_lambda = PRG.getRandomBits(_inputSize, true);
		for (int i = 0 ; i < _inputSize ; i++){
			BigInteger bit = 
				_lambda.testBit(i)?(BigInteger.ONE):(BigInteger.ZERO);
				
			BigInteger[] shares = BGW.share(bit);
			_msg0.append(shares);
		}
	}
	
	protected void createAndShareSeeds() {
		BigInteger xor = _lambda.xor(_input);
		
		for (int i = 0 ; i < _inputSize ; i++){
			BigInteger si0 = PRG.getRandomBits(_n*_K,true);
			BigInteger si1 = PRG.getRandomBits(_n*_K,true);

			_msg0.append(split(si0, _n));
			_msg0.append(split(si1, _n));

			_msg7.append(xor.testBit(i)?
					(si1.shiftLeft(1).setBit(0)):(si0.shiftLeft(1)));
		}
	}
	
	protected BigInteger readBigInteger(String inputName, int size) {
		if (_injectedInput != null) {
			return _injectedInput;
		}
		
	    String input = null;
	    System.out.println("Please inset your input for " + inputName +
	    		", it should be up to " + size + " bits long");
		try {
		    BufferedReader stdin = 
		    	new BufferedReader(new InputStreamReader(System.in));
			input = stdin.readLine().trim();
			stdin.close();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
		 		
		BigInteger i = new BigInteger(input);
		
		if (i.bitLength() > size)
			Utils.printErr("Input " + input + 
				" has more than " + size + " bits.");

		return i;	
	}
}
