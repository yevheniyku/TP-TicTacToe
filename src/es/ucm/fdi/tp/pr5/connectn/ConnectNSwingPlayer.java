package es.ucm.fdi.tp.pr5.connectn;

import java.util.List;

import es.ucm.fdi.tp.basecode.bgame.control.Player;
import es.ucm.fdi.tp.basecode.bgame.model.Board;
import es.ucm.fdi.tp.basecode.bgame.model.GameMove;
import es.ucm.fdi.tp.basecode.bgame.model.GameRules;
import es.ucm.fdi.tp.basecode.bgame.model.Piece;
import es.ucm.fdi.tp.basecode.connectn.ConnectNMove;

/**
 * Clase que se ocupa de hacer el movimiento de jugador
 *
 */

@SuppressWarnings("serial")
public class ConnectNSwingPlayer extends Player {

	//ATRIBUTOS
	private int row;
	private int col;

	/**
	 * Constructora por defecto
	 */
	public ConnectNSwingPlayer() {
		
	}

	@Override
	public GameMove requestMove(Piece p, Board board, List<Piece> pieces, GameRules rules) {
		return createMove(row, col, p);
	}
	
	/**
	 * Inicializa los atributos con los datos que se le pasan por parametro 
	 * @param row: fila
	 * @param col: columna
	 */
	public void setMoveValue(int row, int col) {
		this.row = row;
		this.col = col;
	}
	
	protected GameMove createMove(int row, int col, Piece p) {
		//devuelvo nuevo movimiento
		return new ConnectNMove(row, col, p);
	}

}
