/* 
 * Copyright (C) 2008 Naom Nisan, Benny Pinkas, Assaf Ben-David.
 * See full copyright license terms in file ../GPL.txt
 * @author Assaf Ben-David 
 */

package circuit;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Map;
import java.util.Vector;

public class SHDLCircuitCnv implements Circuit {
	
	String _circuitName;
	int _firstNonInputWirePos;
	Vector<Gate> _gates = new Vector<Gate>();
	Hashtable<String, Player> _players = new Hashtable<String, Player>();
	Vector<String> _ip = new Vector<String>();
	Vector<String> _rp = new Vector<String>();	

	public SHDLCircuitCnv(String cicuitName) {
		_circuitName = cicuitName;
		try {
			parseCircuit();
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	protected void parseCircuit() throws IOException {
		BufferedReader file = new BufferedReader(
				new FileReader(_circuitName + ".cnv"));
		parseInput(file);
		parseGates(file);
		parseOutput(file);
	}

	protected void parseInput(BufferedReader file) throws IOException {
		String line = file.readLine();
		// input,bidder[1],input,8,9,10,11,12,13,14,15
		for (line = file.readLine().trim() ; 
			!line.trim().equals("Gates:") ; line = file.readLine()){
			
			String[] parts = line.split(",");
			if (!(parts[0].equals("input")))
				throw new IOException("Input err");
			
			String playerName = parts[1];
			String inputName = parts[2];
			int[] wires = new int[parts.length-3];
			for (int i = 0 ; i < wires.length ; i++)
				wires[i] = Integer.parseInt(parts[i+3]);
			
			_firstNonInputWirePos = wires[wires.length - 1] + 1;
			
			PlayerInput pi = new PlayerInput();
			pi._name = inputName;
			pi._size = wires.length;
				
			Player player = (Player)_players.get(playerName);			
			if (player == null){
				player = new Player();
				player._name = playerName;
				player._firstInputWire = wires[0];				
				player._inputs.add(pi);
				player._inputSize = wires.length;
				_players.put(playerName, player);
				_ip.add(playerName);
			}
			else{
				player._inputs.add(pi);					
				player._inputSize += wires.length;
			}
		}
	}

	protected void parseGates(BufferedReader file) throws IOException {
		//gate,80,2,0 1 1 0,9,8
		for (String line = file.readLine().trim() ; 
			!line.equals("FMT - Output:") ; line = file.readLine()){
			
			String[] parts = line.split(",");
			if (!(parts[0].equals("gate") && parts[2].equals("2")))
				throw new IOException("Gate err ");
			
			Gate g = new Gate();
			g._output = Integer.parseInt(parts[1]);
			g._type = Integer.parseInt(parts[3].replaceAll(" ", ""),2);
			g._inputs = new int[2]; 
			g._inputs[0] = Integer.parseInt(parts[4]); 
			g._inputs[1] = Integer.parseInt(parts[5]);
			_gates.add(g);
		}			
	}
	protected void parseOutput(BufferedReader file) throws IOException {
		// output,seller,output.winner,1434,1435,1436,1437,-2
		for (String line = file.readLine().trim() ; 
			line != null ; line = file.readLine()){
			
			String[] parts = line.split(",");
			if (!(parts[0].equals("output")))
				throw new IOException("Output err");

			String playerName = parts[1];
			String outputName = parts[2];
			int[] res = new int[parts.length-3];
			for (int i = 0 ; i < res.length ; i++)
				res[i] = Integer.parseInt(parts[i+3]);

			Player pl = (Player)_players.get(playerName);
			if (pl == null){
				pl = new Player();
				pl._name = playerName;
				pl._outputs = new int[res.length];
				for (int i = 0 ; i < pl._outputs.length ; i++)
					pl._outputs[i] = res[i];
					
				_players.put(playerName, pl);				
				_rp.add(playerName);					 
			}
			else {
				if (pl._outputs == null){
					pl._outputs = new int[res.length];
					for (int i = 0 ; i < pl._outputs.length ; i++)
						pl._outputs[i] = res[i];
					
					_rp.add(playerName);					 
				}
				else{
					int[] temp = new int[pl._outputs.length + res.length];
					for (int i = 0 ; i < pl._outputs.length ; i++)
						temp[i] = pl._outputs[i];
					for (int i = pl._outputs.length, j=0 ; 
							i < temp.length ; i++,j++) 						
						temp[i] = res[j];

					pl._outputs = temp;
				}
			}
			
			pl._result.put(outputName, res);				
		}
	}
	
	public int getFirstNonInputWirePos() {
		return _firstNonInputWirePos;
	}

	public int[] getGateInputWires(int g) {		
		return _gates.elementAt(g)._inputs;
	}

	public int getGateOutputWire(int g) {
		return _gates.elementAt(g)._output;
	}

	public int getGateTruthTable(int g) {
		return _gates.elementAt(g)._type;
	}

	public int getGatesCount() {
		return _gates.size();
	}

	public String[] getInputPlayers() {
		String[] ip = new String[0];
		return _ip.toArray(ip);
	}

	public int getPlayerFirstInputWire(String name) {
		return _players.get(name)._firstInputWire;
	}

	public int getPlayerInputSize(String name) {
		return _players.get(name)._inputSize;
	}

	public PlayerInput[] getPlayerInputs(String name) {
		PlayerInput[] pi = new PlayerInput[0];
		return _players.get(name)._inputs.toArray(pi);
	}

	public Map<String, int[]> getPlayerOutputs(String name) {
		return _players.get(name)._result;
	}

	public String[] getResultPlayers() {
		String[] rp = new String[0];
		return _rp.toArray(rp);
	}

	public int getWiresCount() {
		return getGateOutputWire(_gates.size()-1) + 1;
	}
	
	protected class Gate{
		int[] _inputs;
		int _output;
		int _type;
	}
	
	protected class Player{
		String _name;
		int _firstInputWire;
		int _inputSize;
		Vector<PlayerInput> _inputs = new Vector<PlayerInput>();
		int[] _outputs;
		Map<String, int[]> _result = new Hashtable<String, int[]>();
	}
}
