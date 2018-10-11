package es.ucm.fdi.tp.pr6.remotectrl.responses;

import es.ucm.fdi.tp.basecode.bgame.model.Board;
import es.ucm.fdi.tp.basecode.bgame.model.GameObserver;
import es.ucm.fdi.tp.basecode.bgame.model.Piece;

public class MoveEndResponse implements Response{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private Board board;
	private Piece turn;
	private boolean success;

	public MoveEndResponse(Board board, Piece turn, boolean success) {
		this.board = board;
		this.turn = turn;
		this.success = success;
	}

	@Override
	public void run(GameObserver o) {
		o.onMoveEnd(board, turn, success);
	}

}
