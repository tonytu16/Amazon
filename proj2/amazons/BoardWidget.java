package amazons;

import ucb.gui2.Pad;

import java.io.IOException;

import java.util.concurrent.ArrayBlockingQueue;

import java.awt.Color;
import java.awt.Graphics2D;

/**
import java.awt.Polygon;
 */

import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

import static amazons.Piece.*;
import static amazons.Square.sq;
import static amazons.Move.mv;


/** A widget that displays an Amazons game.
 *  @author Tony Tu
 */
class BoardWidget extends Pad {

    /* Parameters controlling sizes, speeds, colors, and fonts. */

    /**
     * Colors of empty squares and grid lines.
     */
    static final Color
            SPEAR_COLOR = new Color(64, 64, 64),
            LIGHT_SQUARE_COLOR = new Color(238, 207, 161),
            DARK_SQUARE_COLOR = new Color(205, 133, 63);

    /**
     * Locations of images of white and black queens.
     */
    private static final String
            WHITE_QUEEN_IMAGE = "wq4.png",
            BLACK_QUEEN_IMAGE = "bq4.png";

    /**
     * Size parameters.
     */
    private static final int
            SQUARE_SIDE = 30,
            BOARD_SIDE = SQUARE_SIDE * 10;

    /**
     * A graphical representation of an Amazons board that sends commands
     * derived from mouse clicks to COMMANDS.
     */
    BoardWidget(ArrayBlockingQueue<String> commands) {
        _commands = commands;
        setMouseHandler("click", this::mouseClicked);
        setPreferredSize(BOARD_SIDE, BOARD_SIDE);

        try {
            _whiteQueen = ImageIO.read(Utils.getResource(WHITE_QUEEN_IMAGE));
            _blackQueen = ImageIO.read(Utils.getResource(BLACK_QUEEN_IMAGE));
        } catch (IOException excp) {
            System.err.println("Could not read queen images.");
            System.exit(1);
        }
        _acceptingMoves = false;
    }

    /**
     * Draw the bare board G.
     */
    private void drawGrid(Graphics2D g) {
        g.setColor(LIGHT_SQUARE_COLOR);
        g.fillRect(0, 0, BOARD_SIDE, BOARD_SIDE);
        g.setColor(LIGHT_SQUARE_COLOR);
        g.fillRect(0, 0, BOARD_SIDE, BOARD_SIDE);
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                if ((i + j) % 2 == 0) {
                    g.setColor(LIGHT_SQUARE_COLOR);
                } else {
                    g.setColor(DARK_SQUARE_COLOR);
                }
                if (sq(i, j) == queenpot) {
                    g.setColor(Color.BLUE);
                }
                if (sq(i, j) == targetpot) {
                    g.setColor(Color.GREEN);
                }
                if (sq(i, j) == spearpot) {
                    g.setColor(Color.YELLOW);
                }
                g.fillRect(cx(i), cy(j), SQUARE_SIDE, SQUARE_SIDE);
            }

        }
    }

    /**a program that prints the components.*/
    @Override
    public synchronized void paintComponent(Graphics2D g) {
        drawGrid(g);
        for (int i = 0; i < 10; i++) {
            Piece x = _board.get(sq(i));
            if (x == WHITE || x == BLACK) {
                drawQueen(g, sq(i), x);
            } else if (x == SPEAR) {
                g.setColor(Color.pink);
                g.fillRect(cx(i % 10), cy(i / 10), SQUARE_SIDE, SQUARE_SIDE);
            }
        }
    }


    /** Draw a queen for side PIECE at square S on G.  */
    private void drawQueen(Graphics2D g, Square s, Piece piece) {
        g.drawImage(piece == WHITE ? _whiteQueen : _blackQueen,
                cx(s.col()) + 2, cy(s.row()) + 4, null);
    }

    /**helper value 1.*/
    private Square queenpot = null;

    /**helper value 2.*/
    private Square targetpot = null;

    /**helper value 3.*/
    private Square spearpot = null;

    /**helper value 4.*/
    private boolean queenSel = false;

    /**helper value 5.*/
    private boolean targetSel = false;

    /**helper value 6.*/
    private boolean spearSel = false;

    /**helper value 7.*/
    private String qsq = "";

    /**helper value 8.*/
    private String ssq = "";

    /**helper value 9.*/
    private String tsq = "";


    /** Handle a click on S. */
    private void click(Square s) {
        if (!queenSel) {
            Piece curr = _board.get(s);
            if (_board.turn() == curr) {
                queenSel = true;
                qsq = s.toString();
                queenpot = s;
            }
        } else if (!targetSel) {
            tsq = s.toString();
            targetSel = true;
            targetpot = s;
        } else if (!spearSel) {
            ssq = s.toString();
            spearSel = true;
            spearpot = s;
            Move created = mv(String.format(("%s-%s(%s"), qsq, tsq, ssq));
            if (created != null
                && _board.turn() == _board.get(created.from())
                && _board.isLegal(created)
                && _board.winner() == null) {
                _commands.offer(qsq + " " + tsq + " " + ssq);
            }
            queenSel = false;
            targetSel = false;
            spearSel = false;
            queenpot = null;
            spearpot = null;
            targetpot = null;
        }
        update(_board);
    }

    /** Handle mouse click event E. */
    private synchronized void mouseClicked(String unused, MouseEvent e) {
        int xpos = e.getX(), ypos = e.getY();
        int x = xpos / SQUARE_SIDE,
                y = (BOARD_SIDE - ypos) / SQUARE_SIDE;
        if (_acceptingMoves
                && x >= 0
                && x < Board.SIZE
                && y >= 0
                && y < Board.SIZE) {
            click(sq(x, y));
        }
    }

    /** Revise the displayed board according to BOARD. */
    synchronized void update(Board board) {
        _board.copy(board);
        repaint();
    }

    /** Turn on move collection iff COLLECTING, and clear any current
     *  partial selection.   When move collection is off, ignore clicks
     *  on
     *  the board. */

    void setMoveCollection(boolean collecting) {
        _acceptingMoves = collecting;
        repaint();
    }

    /** Return x-pixel coordinate of the left corners of column X
    *  relative to the upper-left corner of the board. */
    private int cx(int x) {
        return x * SQUARE_SIDE;
    }

    /** Return y-pixel coordinate of the upper corners of row Y
    *  relative to the upper-left corner of the board. */
    private int cy(int y) {
        return (Board.SIZE - y - 1) * SQUARE_SIDE;
    }

    /** Return x-pixel coordinate of the left corner of S
    *  relative to the upper-left corner of the board. */
    private int cx(Square s) {
        return cx(s.col());
    }

    /** Return y-pixel coordinate of the upper corner of S
    *  relative to the upper-left corner of the board. */
    private int cy(Square s) {
        return cy(s.row());
    }

    /** Queue on which to post move commands (from mouse clicks). */
    private ArrayBlockingQueue<String> _commands;
    /** Board being displayed. */
    private final Board _board = new Board();

    /** Image of white queen. */
    private BufferedImage _whiteQueen;
    /** Image of black queen. */
    private BufferedImage _blackQueen;

    /** True iff accepting moves from user. */
    private boolean _acceptingMoves;
}
