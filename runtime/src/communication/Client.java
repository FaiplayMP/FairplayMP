/* 
 * Copyright (C) 2008 Naom Nisan, Benny Pinkas, Assaf Ben-David.
 * See full copyright license terms in file ../GPL.txt
 * @author Assaf Ben-David 
 */

package communication;

import java.net.Socket;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import javax.net.SocketFactory;
import javax.net.ssl.SSLSocketFactory;

//import javax.net.ssl.SSLSocketFactory;

import utils.Utils;

public class Client {

	// static final SocketFactory _socketFactory = SocketFactory.getDefault();		
	static final SocketFactory _socketFactory = SSLSocketFactory.getDefault();
	static Hashtable<String, String> _players = null;
	static String[] _CP = null;
	static String[] _RP = null;
	static Collection<Thread> _senders = new LinkedList<Thread>();
	static int _port; 

	public static void init(Hashtable<String, String> players, String[] CP,
			String[] RP, int port) {
		_players = players;
		_CP = CP;
		_RP = RP;
		_port = port;
	}

	public static void finishSending() {
		Iterator<Thread> it = _senders.iterator();
		while (it.hasNext()) {
			try {
				it.next().join();
			} catch (InterruptedException e) {
				Utils.printErr("Exception while waiting for a sending thread to finisn.");
			}
		}
	}

	public void sendToCP(CPMsgs msgs) {
		for (int i = 0 ; i < _CP.length ; i++)
			Send(_CP[i], msgs.pop(i));
		
		Utils.printMsg("Sending message " + msgs.pop(0).getID());
	}

	public void sendToRP(String name, Msg msg) {
		String ip = _players.get(name);
		Send(ip, msg);
	}

	public void sendToRP(Msg msg) {
		for (int i = 0 ; i < _RP.length ; i++)
			Send(_players.get(_RP[i]), msg);
		
		Utils.printMsg("Sending message " + msg.getID());
	}

	protected void Send(String ip, Msg msg) {
		Thread sender = new Thread(new Sender(ip, msg));
		sender.start();
		_senders.add(sender);
	}
	
	protected class Sender implements Runnable {
		
		String _ip;
		Msg _msg;
		
		public Sender(String ip, Msg msg) {
			_ip = ip;
			_msg = msg;
		}

		@Override
		public void run() {
			
			while (true){//for (int i = 0 ; i < 10 ; i++) { 
				try {
				    Socket socket = _socketFactory.createSocket(_ip, _port);
				    _msg.getBasicMsg().writeTo(socket.getOutputStream());
				
					// Close the connection and finish
					socket.close();
					return;
				}
				catch(Exception e){
					try {
						Thread.sleep(100);
					} 
					catch (InterruptedException e1) {}						
				}
			}			
		}
	}
}
