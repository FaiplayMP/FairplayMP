/* 
 * Copyright (C) 2008 Naom Nisan, Benny Pinkas, Assaf Ben-David.
 * See full copyright license terms in file ../GPL.txt
 * @author Assaf Ben-David 
 */

package utils;


public class Utils {
	
	public static boolean v = false;
	
	public static void printErr(String msg) {
		System.err.println("Msg: " + msg);
		System.exit(1);
	}
	
	public static void printMsg(String msg) {
		if (v)
			System.out.println(msg);
	}
}
