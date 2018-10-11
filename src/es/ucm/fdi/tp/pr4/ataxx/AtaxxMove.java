package es.ucm.fdi.tp.pr4.ataxx;

import java.util.List;

import es.ucm.fdi.tp.basecode.bgame.model.Board;
import es.ucm.fdi.tp.basecode.bgame.model.GameError;
import es.ucm.fdi.tp.basecode.bgame.model.GameMove;
import es.ucm.fdi.tp.basecode.bgame.model.Piece;

public class AtaxxMove extends GameMove {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private int oRow;
	private int oCol;
	private int dRow;
	private int dCol;

	public AtaxxMove() {
	}

	public AtaxxMove(int oRow, int oCol, int dRow, int dCol, Piece p) {
		super(p);
		this.oRow = oRow;
		this.oCol = oCol;
		this.dRow = dRow;
		this.dCol = dCol;
	}

	@Override
	public void execute(Board board, List<Piece> pieces) {
		// out of board
		if (oRow < 0 || oRow >= board.getRows() || dRow < 0 || dRow >= board.getRows() || oCol < 0
				|| oCol >= board.getCols() || dCol < 0 || dCol >= board.getCols()) {
			throw new GameError("Invalid Ataxx move: " + toString());
		}

		// there is no piece at (oRow, oCol) or (dRow,dCol) is not empty
		if (!getPiece().equals(board.getPosition(oRow, oCol)) || board.getPosition(dRow, dCol) != null) {
			throw new GameError("Invalid Ataxx move: " + toString());
		}

		int distance = Math.max(Math.abs(oRow - dRow), Math.abs(oCol - dCol));

		// invalid distance
		if (distance < 1 || distance > 2) {
			throw new GameError("Invalid Ataxx move: " + toString());
		}

		board.setPosition(dRow, dCol, getPiece());
		if (distance == 2) {
			board.setPosition(oRow, oCol, null);
		}

		int left = Math.max(dCol - 1, 0);
		int right = Math.min(dCol + 1, board.getCols() - 1);
		int top = Math.max(dRow - 1, 0);
		int bot = Math.min(dRow + 1, board.getRows() - 1);

		for (int i = top; i <= bot; i++) {
			for (int j = left; j <= right; j++) {
				if (board.getPosition(i, j) != null && pieces.indexOf(board.getPosition(i, j)) != -1) {
					board.setPosition(i, j, getPiece());
				}
			}
		}

	}

	@Override
	public GameMove fromString(Piece p, String str) {

		String[] words = str.split(" ");
		if (words.length != 4) {
			return null;
		}

		try {
			int oRow = Integer.parseInt(words[0]);
			int oCol = Integer.parseInt(words[1]);
			int dRow = Integer.parseInt(words[2]);
			int dCol = Integer.parseInt(words[3]);
			return new AtaxxMove(oRow, oCol, dRow, dCol, p);
		} catch (NumberFormatException e) {
			return null;
		}

	}

	@Override
	public String help() {
		return "'origRow origCol destRow destCol', to move the piece at (origRow,origCol) to (destRow,destCol).";
	}

	@Override
	public String toString() {
		return "Move piece " + getPiece() + " from (" + oRow + "," + oCol + ") to (" + dRow + "," + dCol + ")";
	}

}
