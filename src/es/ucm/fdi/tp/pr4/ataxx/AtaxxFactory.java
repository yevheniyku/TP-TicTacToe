package es.ucm.fdi.tp.pr4.ataxx;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import es.ucm.fdi.tp.basecode.bgame.control.ConsolePlayerFromListOfMoves;
import es.ucm.fdi.tp.basecode.bgame.control.Controller;
import es.ucm.fdi.tp.basecode.bgame.control.DummyAIPlayer;
import es.ucm.fdi.tp.basecode.bgame.control.GameFactory;
import es.ucm.fdi.tp.basecode.bgame.control.Player;
import es.ucm.fdi.tp.basecode.bgame.model.AIAlgorithm;
import es.ucm.fdi.tp.basecode.bgame.model.GameError;
import es.ucm.fdi.tp.basecode.bgame.model.GameObserver;
import es.ucm.fdi.tp.basecode.bgame.model.GameRules;
import es.ucm.fdi.tp.basecode.bgame.model.Observable;
import es.ucm.fdi.tp.basecode.bgame.model.Piece;
import es.ucm.fdi.tp.basecode.bgame.views.GenericConsoleView;

public class AtaxxFactory implements GameFactory {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected int dim;
	protected int numObstacles;

	public AtaxxFactory() {
		this(7, 0);
	}

	public AtaxxFactory(int dim) {
		this(dim, 0);
	}

	public AtaxxFactory(int obstacles, boolean dummy) {
		this(7, obstacles);
	}

	public AtaxxFactory(int dim, int numObstacles) {
		if (dim < 5 || (dim % 2) == 0) {
			throw new GameError("Invalid dimension " + dim + " for Attax. The board size must be odd (at least 5).");
		}
		if (numObstacles < 0) {
			throw new GameError("Invalid number of obstacles " + numObstacles + " for Attax. It not be non-negative.");
		}
		this.dim = dim;
		this.numObstacles = numObstacles;
	}

	@Override
	public GameRules gameRules() {
		return new AtaxxRules(dim, numObstacles);
	}

	@Override
	public Player createConsolePlayer() {
	//	ArrayList<GameMove> possibleMoves = new ArrayList<GameMove>();
	//	possibleMoves.add(new AtaxxMove());
	//	return new ConsolePlayer(new Scanner(System.in), possibleMoves);
		return new ConsolePlayerFromListOfMoves(new Scanner(System.in) );
	}

	@Override
	public Player createRandomPlayer() {
		return new AtaxxRandomPlayer();
	}

	@Override
	public Player createAIPlayer(AIAlgorithm alg) {
		return new DummyAIPlayer(createRandomPlayer(), 1000);
	}

	@Override
	public List<Piece> createDefaultPieces() {
		List<Piece> pieces = new ArrayList<Piece>();
		pieces.add(new Piece("X"));
		pieces.add(new Piece("O"));
		return pieces;
	}

	@Override
	public void createConsoleView(Observable<GameObserver> game, Controller ctrl) {
		new GenericConsoleView(game, ctrl);
	}

	@Override
	public void createSwingView(Observable<GameObserver> game, Controller ctrl, Piece viewPiece, Player randPlayer,
			Player aiPlayer) {
		// TODO Auto-generated method stub

	}

}
