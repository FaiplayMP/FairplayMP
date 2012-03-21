/* 
 * Copyright (C) 2008 Naom Nisan, Benny Pinkas, Assaf Ben-David.
 * See full copyright license terms in file ../GPL.txt
 * @author Assaf Ben-David 
 */

package circuit;

import java.util.Map;

/**
 * This interface represents a Boolean circuit for FairplayMP project.<br>
 * The circuit has players and gates both are connected with wires.<p>
 * <b>Wires:</b>
 * <ul>
 * <li>All wires have a sequential index starting from 0.</li>
 * <li>The wires are divided into 2 groups: 
 * circuit input wires and non-input wires.</li>
 * <li>All the circuit input wires 
 * will come before any of the non-input wires.</li>
 * <li>Each input wire will be assign to exactly one player.</li>
 * <li>All non-input wires can be also defined as output wires.</li>
 * <li>Each output wire will be assign to at least one player</li>
 * </ul>
 * <b>Players:</b>
 * <ul>
 * <li>A player can have input wires and/or output wires assigned to him.</li>
 * <li>All players' input wires will come in sequential order.</li>
 * <li>A player with inputs 
 * also have a mapping from the bits those wires represent to variables</li>
 * <li>A player with outputs 
 * also have a mapping from the bits those wires represent to variables</li>
 * </ul>
 * <b>Gates:</b>
 * <ul>
 * <li>All gates have a sequential index starting from 0.</li>
 * <li>Each gate will have 2 wires with different index as input.</li>
 * <li>The two input wires of a gate are 
 * either circuit input wires or an output wire of a gate with lower index.</li>
 * <li>Each gate will have one wire as output.</li>
 * <li>Each gate has an int number assign to it, 
 * which in its binary representation is the gate's truth table.
 * <li>The gate's truth table can not be one of the following: 
 * 0, 3, 5, 10, 12 and 15.
 * </ul>
 *  
 * @version 2.0
 * @author Assaf Ben-David
 */
public interface Circuit {

	/**
	 * Gets all the names of the input players.<br>
	 * @return The names of the input players in an array.
	 */
	public String[] getInputPlayers();

	/**
	 * Gets all the names of the result players.<br>
	 * @return The names of the result players in an array.
	 */
	public String[] getResultPlayers();
	
	/**
	 * Gets the index of the players' first input wire.<br>
	 * All the rest of the player's input wires 
	 * will be in the following indexes. 
	 * @param name The name of the player.
	 * @return The index of the player's first input wire.<br>
	 */
	public int getPlayerFirstInputWire(String name);

	/**
	 * Gets the number of the players' input wires, 
	 * which is the players input size in bits.
	 * @param name The name of the player.
	 * @return The player's input size in bits.
	 */
	public int getPlayerInputSize(String name);

	/**
	 * Gets an array of all the players' inputs.<br>
	 * Each entry is an instance of the PlayersInput class which defined below.
	 * Each entry will have a name of the variable and the size in bits.
	 * The entries will be arranged 
	 * from the lsb to the msb of the player input bits. 
	 * The first entry (position 0) lsb will be the players' input lsb.
	 * The last entry (position length-1) msb will be the players' input msb.   
	 * @param name The name of the player.
	 * @return An array of all the player's inputs.
	 */
	public PlayerInput[] getPlayerInputs(String name);
	            
	/**
	 * Returns a mapping of a players' variable name and its wires.<br>
	 * The mapping is returned using the Map interface. 
	 * Each entry contains the variable name (string) as the key, 
	 * and the indexes of the matching wires (int[]) as the value.
	 * In the value (int[]) int[0] will be the lsb.
	 * @param name The name of the player.
	 * @return A Map between the player output variable name (String) 
	 * and it's wires indexes (int[]).
	 */
	public Map<String,int[]> getPlayerOutputs(String name);

	/**
	 * Gets the total number of wires in the Circuit.
	 * @return The number of wires in the Circuit.
	 */
	public int getWiresCount();

	/** 
	 * Gets the first index of a non-input wire.
	 * <ul>
	 * <li>Wires with equal or larger index are non-input wires.</li>
	 * <li>Wires with lower index are circuit input wires.</li>
	 * </ul>
	 * @return The first index of a non-input wire.
	 */
	public int getFirstNonInputWirePos();

	/**
	 * Gets the total number of gates in the Circuit.
	 * @return The number of gates in the Circuit.
	 */
	public int getGatesCount();

	/**
	 * Gets the index of the gates' input wires.
	 * @param g The gate index.
	 * @return An Array with the gate's input wires indexes.
	 */
	public int[] getGateInputWires(int g);

	/**
	 * Return the index of the gates' output wire.
	 * @param g The gate index.
	 * @return The gate's output wire index.
	 */
	public int getGateOutputWire(int g);
	
	/**
	 * Gets the truth table of the gate in its decimal form.<br>
	 * The truth table for a two inputs wires gate range between 
	 * 0 and 15 (inclusive). 
	 * This number in binary representation (four bits) 
	 * is the gate's truth table.
	 * For example: The number 14 is 1110 in its binary form 
	 * and there for will represent a NAND gate.
	 * The following gates are not supported: 0, 3, 5, 10, 12,15 
	 * (the 0/1 consts, the input wires and there negation)<p> 
	 * @param g The gate index.
	 * @return The truth table of the gate.
	 */
	public int getGateTruthTable(int g);
	
	/**
	 * A class to represent a player input.<br>
	 * Contains the input name and size.
	 */
	public class PlayerInput{
		
		/**
		 * The input name.
		 */
		public String _name;
		/**
		 * The input size.
		 */
		public int _size;
	}
}
