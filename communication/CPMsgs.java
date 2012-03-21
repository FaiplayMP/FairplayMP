/* 
 * Copyright (C) 2008 Naom Nisan, Benny Pinkas, Assaf Ben-David.
 * See full copyright license terms in file ../GPL.txt
 * @author Assaf Ben-David 
 */

package communication;

import java.math.BigInteger;


public class CPMsgs {
	
	static int _n = 0;
	Msg[] _msg = null;

	public static void init(int n) {
		_n = n;
	}
	
	public CPMsgs(int id) {
		_msg = new Msg[_n];
		for (int i = 0 ; i < _n ; i++)
			_msg[i] = new Msg(id);
	}

	public void append(BigInteger[] data) {
		for (int i = 0 ; i < _n ; i++)
			_msg[i].append(data[i]);
	}

	public void append(BigInteger data) {
		for (int i = 0 ; i < _n ; i++)
			_msg[i].append(data);
	}

	public Msg pop(int i) {
		return _msg[i];
	}
}
