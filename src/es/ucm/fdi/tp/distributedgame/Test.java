package es.ucm.fdi.tp.distributedgame;


import java.io.IOException;


public class Test {

	private static int numPlayers = 5;
	private static int port = 2222;
	
	public static void launchServer() {
		
		new Thread(){
			
				public void run(){
					
					GameServer server;
					server = new GameServer(numPlayers, port);
					server.start();
					
				}
				
		}.start();	
		
	}
	
	
	public static void launchClient(final int id){
		
		new Thread(){
			
			public void run(){
				
				GameClient client = new GameClient("localhost", port, id, numPlayers);
				
				try {
					
					client.start();
					
				} catch (ClassNotFoundException | IOException | InterruptedException e) {
					e.printStackTrace();
				}
				
			}
			
		}.start();
		
	}
	
	
	public static void main(String[] args) throws InterruptedException {

		launchServer();
		
		for(int i = 0; i < numPlayers; i++){
			
			Thread.sleep(1500);
			launchClient(i);
			
		}

	}

}
