package es.ucm.fdi.tp.pr6.remotectrl.responses;

import es.ucm.fdi.tp.basecode.bgame.model.Board;
import es.ucm.fdi.tp.basecode.bgame.model.GameObserver;
import es.ucm.fdi.tp.basecode.bgame.model.Piece;

public class ChangeTurnResposne implements Response{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private Board board;
	private Piece turn;

	public ChangeTurnResposne(Board board, Piece turn) {
		this.board = board;
		this.turn = turn;
	}

	@Override
	public void run(GameObserver o) {
		o.onChangeTurn(board, turn);
	}

}
