package es.ucm.fdi.tp.distributedgame;


import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Random;


public class GameClient {

	private String url;
	
	private int port;
	private int id;
	private int numPlayers;
	
	private MyNumber number;
	
	private ObjectOutputStream out;
	
	Random r;
	
	
	public GameClient(String url, int port, int id, int numPlayers){
		
		this.url = url;
		this.port = port;
		this.id = id;
		this.numPlayers = numPlayers;
		
		this.number = null;
		
		r = new Random();
		
	}
	
	private boolean myTurn(){
		
		if(number == null){
			return false;
		}
		else{
			return number.getNumber() == id;
		}
		
	}
	
	private boolean gameFinished(){
		return this.number != null && this.number.isWinner();
	}
	
	private MyNumber updateNumber(){
		int n = 1 + r.nextInt(numPlayers - 1);
		return new MyNumber((n + number.getNumber()) % numPlayers, false);
	}
	
	
	public void makeMove(){
		
		MyNumber newNumber = updateNumber();
		
		try {
			
			System.out.println("Client " + id + " sending " + newNumber.getNumber());
			
			out.writeObject(newNumber);
			out.flush();
			out.reset();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	
	public void start() throws UnknownHostException, IOException, InterruptedException, ClassNotFoundException {
		
		Socket s = new Socket(url, port);
		this.out = new ObjectOutputStream(s.getOutputStream());
		ObjectInputStream in = new ObjectInputStream(s.getInputStream());
		
		while(!gameFinished()){
			
			this.number = (MyNumber)in.readObject();
			
			Thread.sleep(300);
			
			if(!gameFinished() && myTurn()){
				makeMove();
			}
			
		}
		
		if(myTurn()){
			System.out.println("I win !! (Client " + id + ")");
		}
		
		in.close();
		s.close();
		
	}
	
}
