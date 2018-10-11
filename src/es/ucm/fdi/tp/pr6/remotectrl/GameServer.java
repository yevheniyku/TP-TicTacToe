package es.ucm.fdi.tp.pr6.remotectrl;


import java.util.ArrayList;
import java.util.List;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import java.net.ServerSocket;
import java.net.Socket;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import es.ucm.fdi.tp.basecode.bgame.model.Board;
import es.ucm.fdi.tp.basecode.bgame.model.Game;
import es.ucm.fdi.tp.basecode.bgame.model.GameObserver;
import es.ucm.fdi.tp.basecode.bgame.model.Piece;
import es.ucm.fdi.tp.basecode.bgame.model.Game.State;
import es.ucm.fdi.tp.basecode.bgame.model.GameError;

import es.ucm.fdi.tp.basecode.bgame.control.Controller;
import es.ucm.fdi.tp.basecode.bgame.control.GameFactory;
import es.ucm.fdi.tp.basecode.bgame.control.Player;
import es.ucm.fdi.tp.basecode.bgame.control.commands.Command;
import es.ucm.fdi.tp.pr6.remotectrl.responses.Response;
import es.ucm.fdi.tp.pr6.remotectrl.responses.ChangeTurnResposne;
import es.ucm.fdi.tp.pr6.remotectrl.responses.ErrorResponse;
import es.ucm.fdi.tp.pr6.remotectrl.responses.GameOverResponse;
import es.ucm.fdi.tp.pr6.remotectrl.responses.GameStartResponse;
import es.ucm.fdi.tp.pr6.remotectrl.responses.MoveEndResponse;
import es.ucm.fdi.tp.pr6.remotectrl.responses.MoveStartResponse;


/*
                           LA CLASE GAMESERVER
                      
     1. 'GameServer' es un 'Controller' (tiene "makeMove", etc.) y tambien
        es un 'GameObserver' (tiene "onGameStart", "onMoveStart", etc.).
        
     2. Cuando la Vista invoca "makeMove" de su 'GameClient', el 'GameClient'
        reenvia la peticion a 'GameServer' para ejecutar su "makeMove".
        
     3. Cuando 'GameServer' recibe una notificacion del Modelo, la reenv�a a
        todos los 'GameClient', que a su vez la reenvian a sus Observadores
        (las Vistas).
        
     4. Ejecutamos 'GameServer' con un juego, por ejemplo 'Ataxx', y una lista
        de 'Piece(s)' (usamos la opcion '-p' o la lista de fichas por defecto).
        Los Clientes no saben a que juego van a jugar.
        
     5. Una vez hay suficientes Clientes conectados, 'GameServer' inicia el
        juego.
        
     6. Cuando acaba el juego, 'GameServer' desconecta a todos los Clientes
        y espera a otros Clientes para comenzar un nuevo juego.
                            
 */

public class GameServer extends Controller implements GameObserver {

	// El puerto usado por el servidor:
	
	private int port;
	
	// El numero de jugadores necesario para iniciar el juego:
	
	private int numPlayers;
	
	// El numero de jugadores conectados (se usa para saber cuando
	// hay que iniciar el juego):
	
	private int numOfConnectedPlayers;
	
	// El 'GameFactory' que se usa para crear 'GameRules':
	
	private GameFactory gameFactory;
	
	// Lista de Clientes conectados:
	
	private List<Connection> clients;

	// Una referencia al Servidor:
	
	volatile private ServerSocket server;
	
	// Indica si el Servidor ha sido "apagado":
	
	volatile private boolean stopped;
	
	// Indica si el juego ha terminado:
	
	private boolean gameOver;
	
	private JTextArea infoArea;
	
	
	/*                 LA CONSTRUCTORA DE GAMESERVER
	 
	   La constructora recibe un 'GameFactory' para poder crear el 'GameRules',
	   la lista de fichas 'pieces', y un puerto 'port':
	   
	 */
	
	public GameServer(GameFactory gameFactory, List<Piece> pieces, int port) {
		
		// Crea el juego y lo pasa (junto con las fichas) a la superclase,
		// donde seran almacenados en atributos correspondientes:
		// (no inicia el juego todavia)
		
		super(new Game(gameFactory.gameRules()), pieces);
		
		// Initialise the fields with corresponding values:		
		// ...
		this.port = port;
		this.numPlayers = pieces.size();
		this.numOfConnectedPlayers = 0;
		this.gameFactory = gameFactory;
		this.clients = new ArrayList <Connection>();
		// Register the 'Controller' as an 'Observer' in the 'game':
		
		game.addObserver(this);
		
	}

	
	/*                LOS METODOS DE "CONTROLLER"
	  
	   Estos metodos ya estan definidos en la superclase "Controller".
	   Los sobreescribimos aqui para poder capturar excepciones
	   (podemos perder la conexion si se lanza una excepcion).
	   
	 */

	
	// We override "makeMove", capture exception, and make it synchronized:

	@Override
	public synchronized void makeMove(Player player) {
		
		try {
			super.makeMove(player);
		} catch (GameError e) {
		}
		
	}

	
	// We override "stop", capture exception, and make it synchronized:

	@Override
	public synchronized void stop() {
		
		try {
			super.stop();
		} catch (GameError e) {
		}
		
	}

	
	// We override "restart", capture exception, and make it synchronized:

	@Override
	public synchronized void restart() {
		
		try {
			super.restart();
		} catch (GameError e) {
		}
		
	}
	
	
	/*                       EL METODO START
	   
	   Ejecutamos el Controlador 'GameServer', construimos la "GUI de control"
	   (para poder mostrar mensajes y parar el Servidor, etc.), y luego iniciar
	   el Servidor:
	   
	 */
	
	@Override
	public void start() {
		
		controlGUI();
		startServer();
		
	}
	
	
	/*                      LA 'GUI DE CONTROL'
	 
	   Usamos "invokeAndWait" en lugar de "invokeLater". Asi, al salir de
	   "controlGUI", sabemos que es seguro usar el metodo "log" para a�adir
	   mensajes:
	   
	*/
	
	private void controlGUI() {
		
		try {
			
			SwingUtilities.invokeAndWait(new Runnable() {

				@Override
				public void run() {
					constructGUI();
				}
				
			});
			
		} catch (InvocationTargetException | InterruptedException e) {
			
			throw new GameError("Something went wrong while creating the GUI");
		
		}
		
	}
	
	
	private void constructGUI() {
		
		JFrame window = new JFrame("Game Server");
		JPanel panel = new JPanel(new BorderLayout());
		

		// Create a text area "infoArea" for printing messages:
		infoArea = new JTextArea();
		
		// Quit button:
		
		JButton quitButton = new JButton("Stop Sever");
		
		// Cuando se hace "clic" en este boton, tenemos que parar el servidor
		// y salir de la aplicacion:
		quitButton.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				try{
					stop();
					desconectarCliente();
					//stopped = true;
					//se podria hacer en un metodo auxiliar con el try catch del close,sobraria el try catch donde esta metido
					server.close();
					System.exit(0);
				}
				catch(IOException er){
					
				}
			}
			
		});
		
		panel.add(infoArea);
		panel.add(quitButton);
		
		window.add(panel);
		window.setPreferredSize(new Dimension(400, 250));
		window.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		window.pack();
		window.setVisible(true);
		
	}
	
	private void desconectarCliente(){
		gameOver = true;
		
		for(Connection c : clients){
			try{
				c.stop();
			}
			catch(GameError | IOException e){
				
			}
		}
		clients.clear();
		numOfConnectedPlayers = 0;
	}
	
	// Usamos este metodo desde todas las partes del 'GameServer' para
	// a�adir mensajes a "infoArea":
	
	private void log(final String msg) {
		
		SwingUtilities.invokeLater(new Runnable() {
			
			@Override
			public void run() {
				
				infoArea.append("* " + msg + "\n");	
				
			}
			
		});
		
	}

	
	
	/*
	 *               EL BUCLE PRINCIPAL DEL SERVIDOR   
	 */
	
	private void startServer() {

		// Try to create the Server, if failed then we throw a 'GameError'
		// exception with a corresponding error message.

		// Iniciamos el Servidor (necesitamos capturar excepciones si es
		// necesario):
		
		try {
			
			server = new ServerSocket(port);
			
		} catch (IOException e) {
			
			throw new GameError("Cannot start the sever at port " + port + " (" + e.getMessage() + ")");
		
		}

		// Indicamos que el juego no ha terminado, ya que estamos a punto de
		// empezar:
		
		stopped = false;
		
		// Important to set it to 'false' outside the thread, otherwise the
		// control loop might be reached before the value is set to 'false'.

		
		/*                    EL BUCLE DEL SERVIDOR
		 
		   Esperamos a que un Cliente se conecte, y pasamos el Socket 
		   correspondiente a "handleRequest" para responder a su peticion:
		   
		 */
		
		while (!stopped) {
			
			
			
			try {
			
				Socket s = server.accept();
				log("New client accepted");
				handleRequest(s);
				 
			} catch (IOException e) {
			
				if (!stopped) {
					log("error while waiting for a connection: " + e.getMessage());
				}
				
			}
			
			
			
		}

	}
	
	
	/*
	 *               MANEJAR PETICIONES DE LOS CLIENTES
	 */
	
	
	// This method should not execute in parallel, connection requests must be
	// handled sequentially.

	private void handleRequest(Socket s) {
		
		try {
			
			// Envolvemos el Socket con "Connection", para que sea mas
			// facil usarlo:
			
			Connection c = new Connection(s);
			
			// The first thing we should receive from a Client is a String with
			// the text "Connect":

			Object clientRequest = c.getObject();
			
			if (!(clientRequest instanceof String) && !((String) clientRequest).equalsIgnoreCase("Connect")) {
				
				c.sendObject(new GameError("Invalid Request"));
				c.stop();
				
				return;

			}

			// 1. If the number of players already reached the maximum, send back a
			// corresponding error:
			if(numPlayers == numOfConnectedPlayers){
				c.sendObject(new GameError("We are already playing"));
				c.stop();
				return;
			}
	
			// 2. Increase the number of players:
			numOfConnectedPlayers++;
			
			// and add the Client to the list of Clients:
			clients.add(c);

			// 3. Enviar el string "OK" al Cliente, seguido por el 'GameFactory'
			// y el 'Piece' a usar. Asignamos al i-esimo Cliente la i-esima
			// ficha (de la lista "pieces"):
			c.sendObject("OK");
			c.sendObject(gameFactory);
			c.sendObject(pieces.get(numOfConnectedPlayers - 1));

			// 4. Si hay un numero suficiente de Clientes, iniciar el juego
			// (la primera vez usando "start", despues usando "restart"):
			if(numPlayers == numOfConnectedPlayers){
				gameOver = false;
				if(game.getState() == State.Starting){
					log("Starting...");
					game.start(pieces);
				}
				else{
					//restart
					log("Restarting the game...");
					game.restart();
				}
			}

			// 5. Invocar a "startClientListener" para iniciar una hebra con la
			// que recibir los comandos del Cliente:
			
			startClientListener(c);
			
		} catch (IOException | ClassNotFoundException _e) {}
		
	}

	
	/*
	 *               RECIBIR COMANDOS DEL CLIENTE
	 */

	
	private void startClientListener(final Connection c) {
		
		// Ponemos "gameOver" a 'false' para indicar que el juego no ha
		// terminado:
		
		gameOver = false;
		
		// Iniciamos una hebra para ejecutar el bucle de abajo mientras
		// el juego no haya terminado y el Servidor no haya sido parado:
		
		Thread t = new Thread() {
			
			@Override
			public void run() {
				
				while (!stopped && !gameOver) {	
					
					
					try {
					
						Command cmd;
						cmd = (Command)c.getObject();
						cmd.execute(GameServer.this);
						
						 
					} catch (ClassNotFoundException | IOException e) {
					
						if (!stopped && !gameOver) {
						
							stopTheGame();
						
						}
					}
					
					
				}
				
			};
			
		};
		
		t.start();
		
	}

	
	protected void stopTheGame() {
		if (this.game.getState() == State.InPlay) {
			stop();
		}
	}


	/*
	                ENVIAR NOTIFICACIONES A LOS CLIENTES:
	                
	    1. Cuando el 'GameServer' recibe una notificacion del Modelo,
	       crea un objeto 'Response' que representa la notificacion,
	       y lo envia a todos los Clientes.
	       
	    2. Cuando el 'GameClient' recibe un 'Response', lo que va a hacer
	       es ejecutar "r.run(o)" para todo Observador 'o'. En particular,
	       la Vista seria un Observador, asi que "run" llamara a su metodo
	       correspondiente.
	
	*/
	
	
	/**
	 * The controller register as an Observer in the game, when it receives a
	 * notification, among other things, it will forward it to the Clients.
	 */

	@Override
	public void onGameStart(Board board, String gameDesc, List<Piece> pieces, Piece turn) {
		forwardNotification(new GameStartResponse(board, gameDesc, pieces, turn));
	}

	@Override
	public void onGameOver(Board board, State state, Piece winner) {
		forwardNotification(new GameOverResponse(board, state, winner));
		// Stop the game:
		desconectarCliente();
		
		
	}

	@Override
	public void onMoveStart(Board board, Piece turn) {
		forwardNotification(new MoveStartResponse(board, turn));
	}

	@Override
	public void onMoveEnd(Board board, Piece turn, boolean success) {
		 forwardNotification(new MoveEndResponse(board, turn, success));
	}

	@Override
	public void onChangeTurn(Board board, Piece turn) {
		 forwardNotification(new ChangeTurnResposne(board, turn));
	}

	@Override
	public void onError(String msg) {
		 forwardNotification(new ErrorResponse(msg));
	}
	
	
	private void forwardNotification(Response r) {
		
		// Call "c.sendObject(r)" for each Client connection 'c'.
		
		for(Connection c : clients){
			try {
				c.sendObject(r);
			} catch (IOException e) {
				stopTheGame();
			}
		}
		
	}

}
