package es.ucm.fdi.tp.pr6.remotectrl;


import java.util.ArrayList;
import java.util.List;
import java.io.IOException;
import java.net.Socket;

import es.ucm.fdi.tp.basecode.bgame.model.Board;
import es.ucm.fdi.tp.basecode.bgame.model.GameError;
import es.ucm.fdi.tp.basecode.bgame.model.Game.State;
import es.ucm.fdi.tp.basecode.bgame.model.GameObserver;
import es.ucm.fdi.tp.basecode.bgame.model.Observable;
import es.ucm.fdi.tp.basecode.bgame.model.Piece;
import es.ucm.fdi.tp.pr6.remotectrl.responses.Response;
import es.ucm.fdi.tp.basecode.bgame.control.Controller;
import es.ucm.fdi.tp.basecode.bgame.control.GameFactory;
import es.ucm.fdi.tp.basecode.bgame.control.Player;
import es.ucm.fdi.tp.basecode.bgame.control.commands.Command;
import es.ucm.fdi.tp.basecode.bgame.control.commands.PlayCommand;
import es.ucm.fdi.tp.basecode.bgame.control.commands.QuitCommand;
import es.ucm.fdi.tp.basecode.bgame.control.commands.RestartCommand;


/*
                          LA CLASE GAMECLIENT
                          
     1. 'GameClient' es un 'Controller' (tiene "makeMove", etc.) y tambien es
        un 'Observable' de tipo 'GameObserver' (tiene "addObserver", etc.).
        Las Vistas ven a GameClient tanto como 'Controller' como 'Observable'
        (Modelo).
       
     2. Es un 'Controller' para que la Vista pueda usar "makeMove", etc.
        Es un 'Observable' para que la Vista se registre para recibir 
        notificaciones del Modelo.
    	La Vista usa 'GameClient' como Modelo y como Controlador, pues 
        no sabe si las notificaciones vienen del Servidor o si las llamadas 
        a "makeMove" van al Servidor.
       
     3. Cuando ejecutamos un 'GameClient', tiene que mandar el string "Connect"
        a 'GameServer' para decir que quiere entrar en el juego. Recibe de
        vuelva un 'GameFactory' y un 'Piece', que se usan para construir la
        Vista.           
                     
 */

public class GameClient extends Controller implements Observable<GameObserver>, GameObserver {

	private String host;
	private int port;
	
	// Las notificaciones que manda el Servidor se reenvian a todos los Observadores:
	
	private List<GameObserver> observers;
	
	// Los 'Piece' y 'GameFactory' que tiene que usar el Cliente (vienen del Servidor):
	
	private Piece localPiece;
	private GameFactory gameFactory;
	
	// La conexion al Servidor:
	
	private Connection connectioToServer;
	
	// Indica si el juego ha terminado:
	
	private boolean gameOver;

	// Indica si ha ocurrido algun error:
	
	private boolean errOccured;
	

	// Consultar el valor de 'GameFactory':
	
	public GameFactory getGameFactoty() {
		return gameFactory;
	}
	
	// Consultar el valor de 'localPiece':

	public Piece getPlayerPiece() {
		return localPiece;
	}

	
	
	/*     
	 *          LA CONSTRUCTORA DE LA CLASE GAMECLIENT
	 */
	
	public GameClient(String host, int port) throws Exception {
		
		super(null, null);
		
		// Inicializar los atributos y llamar a "connect" para establecer
		// la conexion con el Servidor:
		
		// initialise the fiels:
		
		this.host = host;
		this.port = port;
		observers = new ArrayList<GameObserver>();
		
		
		connect();
		
	}
	
	
	/*
	 *                   CONECTAR AL SERVIDOR
	 */
	
	private void connect() throws Exception {
		
		// Creamos una conexion con el Servidor:
		
		connectioToServer = new Connection(new Socket(host, port));
		
		// Enviamos el string "Connect" para expresar su interes en jugar:
		
		connectioToServer.sendObject("Connect");
		
		// Leemos el primer objeto en la respuesta del Servidor:

		Object response = connectioToServer.getObject();
		
		// Si es una instancia de 'Exception' entonces lanzamos una excepcion,
		// porque eso significa que el Servidor ha rechazado la peticion:
		
		if (response instanceof Exception) {
			
			throw (Exception) response;
			
		}

		try {
			
//			   Si no es instancia de 'Exception', seria el string "OK" seguido
//			   por los 'GameFactory' y 'Piece' que el Cliente tiene que usar:
					
					gameFactory = (GameFactory) connectioToServer.getObject();
					localPiece  = (Piece) connectioToServer.getObject();
			
					
		} catch (Exception e) {
			
			throw new GameError("Unknown server response: " + e.getMessage());
		
		}
		
	}


	/*
                      LLAMADAS REMOTAS A GAMESERVER:
                    
        1. Cuando la Vista llama a los metodos "makeMove" del 'GameClient'
           ("MakeMove", "stop", etc.), queremos hacer una llamada remota al
           metodo correspondiente del 'GameServer'.
            
        2. Implementamos dichas llamadas remotas usando las clases que
           implementan la interfaz "Command". Ver el paquete:
            
                      basecode.bgame.control.commands
                      
        3. Cuando el 'GameServer' recibe un objeto 'cmd' de tipo "Command",
           invocara a su "execute" pasandole a si mismo:
           
                        cmd.execute(GameServer.this)
                      
           El metodo "execute", a su vez, llamara al metodo correspondiente
           de "GameServer.this" (por ejemplo, "makeMove").
           
	 */
	
	
	@Override
	public void makeMove(final Player p) {
		forwardCommand(new PlayCommand(p));
	}

	@Override
	public void stop() {
		forwardCommand(new QuitCommand());
	}

	@Override
	public void restart() {
		forwardCommand(new RestartCommand());
	}
	
	
	private void forwardCommand(Command cmd) {
		
		// If the game is over do nothing, otherwise
		// send the object 'cmd' to the Server:
		if(!gameOver){
			try {
				connectioToServer.sendObject(cmd);
			} catch (IOException e) {

			}
		}
		
	}
	
	
	/*
     *             EL BUCLE PRINCIPAL DE GAMECLIENT
	 */
	
	@Override
	public void start() {
		
		// Creamos una instancia anonima de 'GameObserver' y la registramos
		// como Observador en 'GameClient':
		
		this.observers.add(this);
		
		// Esta instancia tiene que cambiar 'gameOver' a 'true' y cerrar la
		// conexion con el servidor en su metodo "onGameOver". Asi podemos
		// salir del bucle principal cuando termina el juego:
		
		gameOver = false;
		errOccured = false;
		
		while (!gameOver && !errOccured) {
			
			
			
			try {
				
				// Read a response:
				 Response res = (Response)connectioToServer.getObject();  
				
				for (GameObserver o : observers) {
					
					// Execute the response on the Observer 'o'.
					
					// Mientras que el juego no haya terminado, leemos
					// un 'Response' enviado por el Servidor y ejecutamos
					// "res.run(o)" para cada Observador, para pasarle asi
					// la notificacion correspondiente.
					res.run(o);
				}
				
			} catch (ClassNotFoundException | IOException e) {
				
				if (!gameOver) {
					errOccured = true;
				}
				
			}
			
			
			
		}
		
		System.out.println("CLIENT " + getPlayerPiece() + ": disconnecting !");
		
	}
	
	
	/*
	 *         LOS METODOS DE LA INTERFAZ OBSERVABLE
	 */
	
	@Override
	public void addObserver(GameObserver o) {
		observers.add(o);
	}

	@Override
	public void removeObserver(GameObserver o) {
		observers.remove(o);
	}
	
	
	/*
	 *         LOS METODOS DE LA INTERFAZ GAMEOBSERVER
	 */

	@Override
	public void onGameStart(Board board, String gameDesc, List<Piece> pieces, Piece turn) {
		
	}

	@Override
	public void onGameOver(Board board, State state, Piece winner) {
		
		try {
			gameOver = true;
			connectioToServer.stop();
		} 
		catch (IOException e) {

		}
	}

	@Override
	public void onMoveStart(Board board, Piece turn) {
		
	}

	@Override
	public void onMoveEnd(Board board, Piece turn, boolean success) {
		
	}

	@Override
	public void onChangeTurn(Board board, Piece turn) {
		
	}

	@Override
	public void onError(String msg) {
		
	}

}
