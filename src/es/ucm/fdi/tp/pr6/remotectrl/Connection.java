package es.ucm.fdi.tp.pr6.remotectrl;


import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import java.net.Socket;


/*
                          LA CLASE CONNECTION
       
   Para simplificar el uso de Sockets, vamos a definir una clase "Connection"
   que nos permita enviar y recibir objetos usando un Socket.
       
 */


public class Connection {
	
	private volatile ObjectInputStream in;
	private volatile ObjectOutputStream out;
	private volatile Socket s;

	
	public Connection(Socket s) throws IOException {
		
		// Almacenamos los Streams de entrada y salida en atributos:
		
		this.s = s;
		this.out = new ObjectOutputStream(s.getOutputStream());
		this.in = new ObjectInputStream(s.getInputStream());
		
	}

	
	public void sendObject(Object r) throws IOException {
		
		// Enviamos un objeto:
		
		out.writeObject(r);
		out.flush();
		out.reset();
		
	}

	
	public Object getObject() throws IOException, ClassNotFoundException {
		
		// Recibimos un objeto:
		
		return in.readObject();
		
	}

	
	public void stop() throws IOException {
		
		// Cerramos el Socket:
		
		s.close();
		
	}

}
