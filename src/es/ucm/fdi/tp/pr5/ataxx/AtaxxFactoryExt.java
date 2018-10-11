package es.ucm.fdi.tp.pr5.ataxx;

import java.lang.reflect.InvocationTargetException;

import javax.swing.SwingUtilities;

import es.ucm.fdi.tp.basecode.bgame.control.Controller;
import es.ucm.fdi.tp.basecode.bgame.control.Player;
import es.ucm.fdi.tp.basecode.bgame.model.GameError;
import es.ucm.fdi.tp.basecode.bgame.model.GameObserver;
import es.ucm.fdi.tp.basecode.bgame.model.Observable;
import es.ucm.fdi.tp.basecode.bgame.model.Piece;
import es.ucm.fdi.tp.pr4.ataxx.AtaxxFactory;

public class AtaxxFactoryExt extends AtaxxFactory {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public AtaxxFactoryExt() {
	}

	public AtaxxFactoryExt(int dim) {
		super(dim);
	}

	public AtaxxFactoryExt(int dim, int obstacles) {
		super(dim, obstacles);
	}

	public AtaxxFactoryExt(int obstacles, boolean dummy) {
		super(obstacles, dummy);
	}

	@Override
	public void createSwingView(final Observable<GameObserver> g, final Controller c, final Piece viewPiece,
			final Player randomPlayer, final Player aiPlayer) {
		try {
			SwingUtilities.invokeAndWait(new Runnable() {
				@Override
				public void run() {
					new AtaxxSwingView(g, c, viewPiece, randomPlayer, aiPlayer);
				}
			});
		} catch (InvocationTargetException | InterruptedException e) {
			throw new GameError("Somthing went wrong while creating the view");
		}
	}

}
