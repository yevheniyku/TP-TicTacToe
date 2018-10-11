package es.ucm.fdi.tp.pr5.attt;

import java.util.List;

import es.ucm.fdi.tp.basecode.attt.AdvancedTTTMove;
import es.ucm.fdi.tp.basecode.bgame.control.Player;
import es.ucm.fdi.tp.basecode.bgame.model.Board;
import es.ucm.fdi.tp.basecode.bgame.model.GameMove;
import es.ucm.fdi.tp.basecode.bgame.model.GameRules;
import es.ucm.fdi.tp.basecode.bgame.model.Piece;

public class AdvancedTTTSwingPlayer extends  Player {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		private int oRow;
		private int oCol;
		private int dRow;
		private int dCol;

		public AdvancedTTTSwingPlayer() {
		}

		@Override
		public GameMove requestMove(Piece p, Board board, List<Piece> pieces, GameRules rules) {
			return createMove(oRow, oCol, dRow, dCol, p);
		}

		private GameMove createMove(int oRow, int oCol, int dRow, int dCol, Piece p) {
			return new AdvancedTTTMove(oRow, oCol, dRow, dCol, p);
		}

		public void setMoveValue(int oRow, int oCol, int dRow, int dCol) {
			this.oRow = oRow;
			this.oCol = oCol;
			this.dRow = dRow;
			this.dCol = dCol;
		}
		

	}
