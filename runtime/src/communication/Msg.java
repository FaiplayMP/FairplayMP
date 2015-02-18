/* 
 * Copyright (C) 2008 Naom Nisan, Benny Pinkas, Assaf Ben-David.
 * See full copyright license terms in file ../GPL.txt
 * @author Assaf Ben-David 
 */

package communication;

import java.math.BigInteger;
import java.util.Iterator;
import java.util.LinkedList;

import com.google.protobuf.ByteString;

import communication.Messages.BasicMsg;

import utils.Utils;

public class Msg {
	
	int _id;
	protected LinkedList<BigInteger> _msg = null;
	protected Iterator<ByteString> _data = null;
	
	public Msg(int id) {
		_id = id;
		_msg = new LinkedList<BigInteger>();
	}

	public Msg(Msg msg) {
		_id = msg._id;
		_msg = new LinkedList<BigInteger>(msg._msg);
	}

	public Msg(BasicMsg msg) {
		_id = msg.getMsgId();
		_data = msg.getDataList().iterator();;
	}

	public void append(BigInteger data) {
		_msg.add(data);
	}

	public BigInteger pop() {
		if (!_data.hasNext()) {
			Utils.printErr("Reached the end of the msg");
		}
		
		return new BigInteger(_data.next().toByteArray());
	}

	public int getID() {
		return _id;
	}
	
	public BasicMsg getBasicMsg() {
		BasicMsg.Builder bm = BasicMsg.newBuilder();
		bm.setMsgId(_id);
		for (BigInteger data : _msg) {
			bm.addData(ByteString.copyFrom(data.toByteArray()));
		}
		_msg = null;
		return bm.build();
	}
	
	public String toString() {
		Utils.printErr("Msg has no toString");
		return null;
	}
}
