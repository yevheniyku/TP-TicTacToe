package es.ucm.fdi.tp.pr5.common;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;

import es.ucm.fdi.tp.basecode.bgame.Utils;
import es.ucm.fdi.tp.basecode.bgame.model.Board;
import es.ucm.fdi.tp.basecode.bgame.model.GameObserver;
import es.ucm.fdi.tp.basecode.bgame.model.Observable;
import es.ucm.fdi.tp.basecode.bgame.model.Piece;
import es.ucm.fdi.tp.basecode.bgame.model.Game.State;

public class JBoard extends JComponent implements GameObserver {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Board board;

	private int _CELL_HEIGHT = 50;
	private int _CELL_WIDTH = 50;

	private HashMap<Piece, Color> pieceColors = new HashMap<Piece, Color>();
	private Iterator<Color> colorsIter = Utils.colorsGenerator();

	public enum Shapes {
		CIRCLE, RECTANGLE
	}

	public JBoard(final Observable<GameObserver> game) {
		initGUI();
		game.addObserver(JBoard.this);
	}

	private void initGUI() {
		setBorder(BorderFactory.createRaisedBevelBorder());

		addMouseListener(new MouseListener() {

			@Override
			public void mouseReleased(MouseEvent e) {
			}

			@Override
			public void mousePressed(MouseEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void mouseExited(MouseEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void mouseEntered(MouseEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void mouseClicked(MouseEvent e) {
				int col = (e.getX() / _CELL_WIDTH);
				int row = (e.getY() / _CELL_HEIGHT);

				int mouseButton = 0;

				if (SwingUtilities.isLeftMouseButton(e))
					mouseButton = 1;
				else if (SwingUtilities.isMiddleMouseButton(e))
					mouseButton = 2;
				else if (SwingUtilities.isRightMouseButton(e))
					mouseButton = 3;

				if (mouseButton == 0)
					return; // Unknown button, don't know if it is possible!

				JBoard.this.mouseClicked(row, col, e.getClickCount(), mouseButton);
			}
		});

		this.setPreferredSize(new Dimension(200, 200));
		repaint();
	}

	protected void mouseClicked(int row, int col, int clickCount, int mouseButton) {
	}

	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		fillBoard(g);
	}

	private void fillBoard(Graphics g) {
		if (board == null) {
			g.setColor( Color.red );
			g.drawString("Waiting for game to start!", 20,  this.getHeight()/2);
			return;
		}
		int numCols = board.getCols();
		int numRows = board.getRows();

		_CELL_WIDTH = this.getWidth() / numCols;
		_CELL_HEIGHT = this.getHeight() / numRows;

		for (int i = 0; i < numRows; i++)
			for (int j = 0; j < numCols; j++)
				drawCell(i, j, g);
	}

	private void drawCell(int row, int col, Graphics g) {
		int x = col * _CELL_WIDTH;
		int y = row * _CELL_HEIGHT;

		g.setColor(Color.LIGHT_GRAY);
		g.fillRect(x + 2, y + 2, _CELL_WIDTH - 4, _CELL_HEIGHT - 4);

		Piece p = board.getPosition(row, col);

		if (p != null) {
			Color c = getPieceColor(p);
			Shapes s = getPieceShape(p);

			g.setColor(c);
			switch (s) {
			case CIRCLE:
				g.fillOval(x + 4, y + 4, _CELL_WIDTH - 8, _CELL_HEIGHT - 8);
				g.setColor(Color.black);
				g.drawOval(x + 4, y + 4, _CELL_WIDTH - 8, _CELL_HEIGHT - 8);
				break;
			case RECTANGLE:
				g.fillRect(x + 4, y + 4, _CELL_WIDTH - 8, _CELL_HEIGHT - 8);
				g.setColor(Color.black);
				g.drawRect(x + 4, y + 4, _CELL_WIDTH - 8, _CELL_HEIGHT - 8);
				break;
			default:
				break;

			}
		}

	}

	private void changeBoard(Board board) {
		if (board == null) {
			return;
		}

		this.board = board;
		int numCols = board.getCols();
		int numRows = board.getRows();
		this.setPreferredSize(new Dimension(numCols * _CELL_WIDTH + 1, numRows * _CELL_HEIGHT + 1));
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				repaint();
			}
		});
	}

	protected Shapes getPieceShape(Piece p) {
		return Shapes.CIRCLE;
	}

	protected Color getPieceColor(Piece p) {
		Color c = pieceColors.get(p);

		if (c == null) {
			pieceColors.put(p, colorsIter.next());
		}
		return c;
	}

	@Override
	public void onGameStart(Board board, String gameDesc, List<Piece> pieces, Piece turn) {
		changeBoard(board);
	}

	@Override
	public void onGameOver(Board board, State state, Piece winner) {
		changeBoard(board);
	}

	@Override
	public void onMoveStart(Board board, Piece turn) {
	}

	@Override
	public void onMoveEnd(Board board, Piece turn, boolean success) {
		changeBoard(board);
	}

	@Override
	public void onChangeTurn(Board board, Piece turn) {
		changeBoard(board);
	}

	@Override
	public void onError(String msg) {
	}

}
