package es.ucm.fdi.tp.pr4.ataxx;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import es.ucm.fdi.tp.basecode.bgame.Utils;
import es.ucm.fdi.tp.basecode.bgame.model.Board;
import es.ucm.fdi.tp.basecode.bgame.model.FiniteRectBoard;
import es.ucm.fdi.tp.basecode.bgame.model.GameError;
import es.ucm.fdi.tp.basecode.bgame.model.Game.State;
import es.ucm.fdi.tp.basecode.bgame.model.GameMove;
import es.ucm.fdi.tp.basecode.bgame.model.GameRules;
import es.ucm.fdi.tp.basecode.bgame.model.Pair;
import es.ucm.fdi.tp.basecode.bgame.model.Piece;

public class AtaxxRules implements GameRules {

	// This object is returned by gameOver to indicate that the game is not
	// over. Just to avoid creating it multiple times, etc.
	//
	protected final Pair<State, Piece> gameInPlayResult = new Pair<State, Piece>(State.InPlay, null);

	protected int dim;
	protected int numObstacles;
	protected Piece obstacle;

	/**
	 * A constructor for AttaxRules. The dimension must be positive odd number.
	 */
	public AtaxxRules(int dim, int numObstacles) {
		if (dim < 5 || (dim % 2) == 0) {
			throw new GameError("Invalid dimension " + dim + " for Attax. The board size must be odd (at least 5).");
		}
		if (numObstacles < 0) {
			throw new GameError("Invalid number of obstacles " + numObstacles + " for Attax. It not be non-negative.");
		}
		this.dim = dim;
		this.numObstacles = Math.min(numObstacles, dim * dim / 8);
	}

	@Override
	public String gameDesc() {
		return "Ataxx";
	}

	@Override
	public Board createBoard(List<Piece> pieces) {
		Board b = new FiniteRectBoard(dim, dim);
		obstacle = pickObstaclePiece(pieces);
		intializeBoard(b, pieces);
		return b;
	}

	/**
	 * Returns a piece with identifier different from those of {@code pieces}.
	 * It is used to generate a piece to be used for obstacles.
	 * 
	 * @param pieces
	 *            list of pieces involved in the game
	 * @return
	 */
	private Piece pickObstaclePiece(List<Piece> pieces) {
		Piece p = null;
		int i = 0;
		do {
			String s = "*#AtaxxObstacle#" + i;
			p = new Piece(s);
			i++;
		} while (pieces.contains(p));
		return p;
	}

	/**
	 * Add the initial pieces and obstacles to the board.
	 * 
	 * @param board
	 * @param pieces
	 */
	private void intializeBoard(Board board, List<Piece> pieces) {
		// player 1
		board.setPosition(0, 0, pieces.get(0));
		board.setPosition(dim - 1, dim - 1, pieces.get(0));

		// player 2
		board.setPosition(0, dim - 1, pieces.get(1));
		board.setPosition(dim - 1, 0, pieces.get(1));

		// player 3
		if (pieces.size() >= 3) {
			board.setPosition(dim / 2, 0, pieces.get(2));
			board.setPosition(dim / 2, dim - 1, pieces.get(2));
		}

		// player 4
		if (pieces.size() == 4) {
			board.setPosition(0, dim / 2, pieces.get(3));
			board.setPosition(dim - 1, dim / 2, pieces.get(3));
		}

		// Obstacles
		int quadDim = dim / 2 + 1;
		for (int i = 0; i < numObstacles; i++) {
			int row = Utils.randomInt(quadDim);
			int col = Utils.randomInt(quadDim);
			if (board.getPosition(row, col) == null) {
				board.setPosition(row, col, obstacle);
				board.setPosition(row, dim - col - 1, obstacle);
				board.setPosition(dim - row - 1, col, obstacle);
				board.setPosition(dim - row - 1, dim - col - 1, obstacle);
			}
		}

	}

	@Override
	public Piece initialPlayer(Board board, List<Piece> pieces) {
		return nextPlayer(board, pieces, null);
	}

	@Override
	public int minPlayers() {
		return 2;
	}

	@Override
	public int maxPlayers() {
		return 4;
	}

	@Override
	public Pair<State, Piece> updateState(Board board, List<Piece> pieces, Piece turn) {
		Pair<State, Piece> state = gameInPlayResult;

		// if no one can move, we calculate the next state that can be Draw of
		// Won
		if (nextPlayer(board, pieces, turn) == null) {

			// count the number of pieces on board, for each player
			ArrayList<Integer> numPieces = new ArrayList<>();
			for (Piece p : pieces) {
				numPieces.add(countPieces(board, p));
			}

			// compute the max of numPieces, and see how many players have this
			// max
			int max = numPieces.get(0);
			Piece winner = pieces.get(0);
			int numOccurMax = 1;

			for (int i = 1; i < pieces.size(); i++) {
				if (numPieces.get(i) > max) {
					max = numPieces.get(i);
					winner = pieces.get(i);
					numOccurMax = 1;
				} else if (numPieces.get(i) == max) {
					numOccurMax++;
				}
			}

			if (numOccurMax > 1) {
				state = new Pair<State, Piece>(State.Draw, null);
			} else {
				state = new Pair<State, Piece>(State.Won, winner);
			}
		}
		return state;
	}

	/**
	 * Count the number of occurrences of a given piece on the board;
	 * 
	 * @param board
	 *            A board
	 * @param p
	 *            A piece
	 * @return
	 */
	private Integer countPieces(Board board, Piece p) {
		int n = 0;
		for (int i = 0; i < board.getRows(); i++) {
			for (int j = 0; j < board.getCols(); j++) {
				if (p.equals(board.getPosition(i, j))) {
					n++;
				}
			}
		}
		return n;
	}

	@Override
	public Piece nextPlayer(Board board, List<Piece> pieces, Piece turn) {

		// if the board has only one kind of piece, then no one can move.
		int row = 0;
		int col = 0;
		Set<Piece> ps = new HashSet<>();
		while (row < board.getRows() && ps.size() < 2) {
			col = 0;
			while (col < board.getCols() && ps.size() < 2) {
				if (board.getPosition(row, col) != null && pieces.indexOf(board.getPosition(row, col)) != -1) {
					ps.add(board.getPosition(row, col));
				}
				col++;
			}
			row++;
		}

		if (ps.size() == 1) {
			return null;
		}

		// check if any player can make a move, starting from 'turn' and
		// respecting the order in the list pieces.
		int numPieces = pieces.size();
		int j = (turn == null ? 0 : pieces.indexOf(turn) + 1) % numPieces;
		for (int i = 0; i < pieces.size(); i++) {
			if (canMove(pieces.get(j), board)) {
				return pieces.get(j);
			}
			j = (j + 1) % numPieces;
		}
		return null; // none can move
	}

	/**
	 * Returns true if a given piece can make a move on a given board.
	 * 
	 * @param piece
	 *            A piece to check if it can make a move.
	 * @param board
	 *            A board on which the chcek is done.
	 * @return
	 */
	private boolean canMove(Piece piece, Board board) {
		for (int i = 0; i < board.getRows(); i++) {
			for (int j = 0; j < board.getCols(); j++) {
				if (piece.equals(board.getPosition(i, j)) && canMove(piece, i, j, board)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Check if the piece at position ({@code row},{@code col}) can make a move.
	 * 
	 * @param piece
	 *            A piece.
	 * @param row
	 *            Row number.
	 * @param col
	 *            Columns number.
	 * @param board
	 *            A board.
	 * @return
	 */
	private boolean canMove(Piece piece, int row, int col, Board board) {
		int left = Math.max(col - 2, 0);
		int right = Math.min(col + 2, board.getCols() - 1);
		int top = Math.max(row - 2, 0);
		int bot = Math.min(row + 2, board.getRows() - 1);

		for (int i = top; i <= bot; i++) {
			for (int j = left; j <= right; j++) {
				if (board.getPosition(i, j) == null) {
					return true;
				}
			}
		}

		return false;
	}

	@Override
	public double evaluate(Board board, List<Piece> pieces, Piece turn, Piece p) {

		double total = 0;
		double m = 0;
		double n = 0;

		for (Piece q : pieces) {
			int x = countPieces(board, q);
			total += x;
			if (q.equals(p)) {
				n = x;
			} else {
				m += x;
			}
		}

		return (n / total) - (m / total);
	}

	@Override
	public List<GameMove> validMoves(Board board, List<Piece> playersPieces, Piece turn) {
		List<GameMove> moves = new ArrayList<GameMove>();
		for (int row = 0; row < board.getRows(); row++) {
			for (int col = 0; col < board.getRows(); col++) {
				if (turn.equals(board.getPosition(row, col))) {

					int left = Math.max(col - 2, 0);
					int right = Math.min(col + 2, board.getCols() - 1);
					int top = Math.max(row - 2, 0);
					int bot = Math.min(row + 2, board.getRows() - 1);

					for (int i = top; i <= bot; i++) {
						for (int j = left; j <= right; j++) {
							if (board.getPosition(i, j) == null) {
								moves.add(new AtaxxMove(row, col, i, j, turn));
							}
						}
					}
				}
			}
		}
		return moves;
	}

}
