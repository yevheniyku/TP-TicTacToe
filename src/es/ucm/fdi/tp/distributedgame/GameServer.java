package es.ucm.fdi.tp.distributedgame;


import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import java.net.ServerSocket;
import java.net.Socket;


public class GameServer {

	private int numPlayers;
	private int numConnections;
	private int port;
	
	private MyNumber number;
	
	private List<ObjectOutputStream> outChannels;
	
	
	public GameServer(int numPlayers, int port){
		
		this.numPlayers = numPlayers;
		this.numConnections = 0;
		this.port = port;
		
		this.number = new MyNumber(0, false);
		
		outChannels = new ArrayList<>();

	}
	
	
	public void start(){
		
		try {
			
			ServerSocket server = new ServerSocket(port);
			System.out.println("Server waiting for connections");
			
			while(numConnections < numPlayers){
				
				Socket s = server.accept();
				
				numConnections++;
				System.out.println("Connection " + numConnections + " accepted by server");
				
				handleRequestInThread(s);
				
			}
			
			startGame();
			server.close();
			
		}
		catch (IOException e) {
			e.printStackTrace();
		}

	}
	
	
	private void notifyAllClients() throws IOException{
		
		for(ObjectOutputStream out : outChannels){
			out.writeObject(this.number);
			out.flush();
			out.reset();
		}
		
	}
	
	
	private void handleRequestInThread(final Socket s) throws IOException {
		
		ObjectOutputStream out = new ObjectOutputStream(s.getOutputStream());
		outChannels.add(out);
		final ObjectInputStream in = new ObjectInputStream(s.getInputStream());
		
		new Thread(){
			
			public void run(){
				
				try{
					
					startListener(in);
					
				}
				catch(IOException | ClassNotFoundException e){}
				
			}
			
		}.start();
		
	}

	private void setWinnerStatus(){
		Random r = new Random();
		this.number.setWinner(r.nextInt(10) < 3);
	}
	
	private void startListener(ObjectInputStream in) throws IOException, ClassNotFoundException {
		
		do{
			
			this.number = (MyNumber)in.readObject();
			
			setWinnerStatus();
			System.out.println("Server receives " + number.getNumber());
			
			notifyAllClients();
			
		} while(!number.isWinner());
		
		in.close();
		
	}
	
	private void startGame() throws IOException{
		notifyAllClients();
	}

}
