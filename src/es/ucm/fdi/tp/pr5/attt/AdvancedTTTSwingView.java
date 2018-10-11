package es.ucm.fdi.tp.pr5.attt;

import es.ucm.fdi.tp.basecode.bgame.control.Controller;
import es.ucm.fdi.tp.basecode.bgame.control.Player;
import es.ucm.fdi.tp.basecode.bgame.model.BasicBoard;
import es.ucm.fdi.tp.basecode.bgame.model.GameObserver;
import es.ucm.fdi.tp.basecode.bgame.model.Observable;
import es.ucm.fdi.tp.basecode.bgame.model.Piece;
import es.ucm.fdi.tp.pr5.common.FiniteRectBoardSwingView;

public class AdvancedTTTSwingView extends FiniteRectBoardSwingView {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private AdvancedTTTSwingPlayer player;
	private boolean active;
	private boolean originAvailable;
	private boolean hasPieces;

	private int oRow;
	private int oCol;
	private int dRow;
	private int dCol;

	public AdvancedTTTSwingView(Observable<GameObserver> g, Controller c, Piece localPiece, Player randomPlayer,
			Player autoPlayer) {
		super(g, c, localPiece, randomPlayer, autoPlayer);
		player = makeManualPlayer();
		active = false;
		originAvailable = false;
		hasPieces = false;
	}

	private AdvancedTTTSwingPlayer makeManualPlayer() {
		return new AdvancedTTTSwingPlayer();
	}

	@Override
	protected void handleMouseClick(int row, int col, int clickCount, int mouseButton) {
		if (!active) {
			return;
		}

		if (hasPieces) {
			player.setMoveValue(-1, -1, row, col);
			decideMakeManualMove(player);
		} else {
			if (mouseButton != 1) {
				if (originAvailable) {
					addContentToStatusArea("Origin cell selection canceled");
					addContentToStatusArea("Click on an origin cell");
					originAvailable = false;
				}
				return;
			}

			if (originAvailable) {
				this.dRow = row;
				this.dCol = col;
				originAvailable = false;
				player.setMoveValue(oRow, oCol, dRow, dCol);
				addContentToStatusArea("You have selected (" + row + "," + col + ") as destination");
				decideMakeManualMove(player);
			} else {
				this.oRow = row;
				this.oCol = col;
				originAvailable = true;
				addContentToStatusArea("You have selected (" + row + "," + col + ") as origin");
				addContentToStatusArea("Click on an destination cell");
			}
		}
	}

	@Override
	protected void activateBoard() {
		active = true;
		hasPieces = getBoard().getPieceCount(getTurn()) > 0;
		if ( hasPieces ) {
			addContentToStatusArea("Click on an empty cell");
		} else {
			addContentToStatusArea("Click on an origin cell");		
		}
	}

	@Override
	protected void deActivateBoard() {
		active = false;
		if (originAvailable) {
			addContentToStatusArea("Origin cell selection calnceled");
			originAvailable = false;
		}
	}

}
