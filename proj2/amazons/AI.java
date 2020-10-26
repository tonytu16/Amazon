package amazons;

/**
NOTICE:
This file is a SUGGESTED skeleton.  NOTHING here or in any other source
file is sacred.  If any of it confuses you, throw it out and do it your way.
*/
/**
lalala
*/
import static java.lang.Math.*;
import java.util.Iterator;
import static amazons.Piece.*;

/** A Player that automatically generates moves.
 *  @author Tony_Tu
 */
class AI extends Player {

    /** A position magnitude indicating a win (for white if positive, black
     *  if negative). */
    private static final int WINNING_VALUE = Integer.MAX_VALUE - 1;
    /** A magnitude greater than a normal value. */
    private static final int INFTY = Integer.MAX_VALUE;

    /** A new AI with no piece or controller (intended to produce
     *  a template). */
    AI() {
        this(null, null);
    }

    /** A new AI playing PIECE under control of CONTROLLER. */
    AI(Piece piece, Controller controller) {
        super(piece, controller);
    }

    @Override
    Player create(Piece piece, Controller controller) {
        return new AI(piece, controller);
    }

    @Override
    String myMove() {
        Move move = findMove();
        _controller.reportMove(move);
        return move.toString();
    }

    /** Return a move for me from the current position, assuming there
     *  is a move. */
    private Move findMove() {
        Board b = new Board(board());
        if (_myPiece == WHITE) {
            findMove(b, maxDepth(b), true, 1, -INFTY, INFTY);
        } else {
            findMove(b, maxDepth(b), true, -1, -INFTY, INFTY);
        }
        return _lastFoundMove;
    }

    /** The move found by the last call to one of the ...FindMove methods
     *  below. */
    private Move _lastFoundMove;

    /** Find a move from position BOARD and return its value, recording
     *  the move found in _lastFoundMove iff SAVEMOVE. The move
     *  should have maximal value or have value > BETA if SENSE==1,
     *  and minimal value or value < ALPHA if SENSE==-1. Searches up to
     *  DEPTH levels.  Searching at level 0 simply returns a static estimate
     *  of the board value and does not set _lastMoveFound. */
    private int findMove(Board board, int depth, boolean saveMove, int sense,
                         int alpha, int beta) {
        if (depth == 0 || board.winner() != null) {
            return staticScore(board);
        }
        Move bestmove = Move.mv("a1 b1 c1");
        int bestScore;
        if (sense == 1) {
            Iterator<amazons.Move> m = board.legalMoves(WHITE);
            bestScore = -INFTY;
            while (m.hasNext()) {

                Board temp = new Board(board);
                Move curr = m.next();
                temp.makeMove(curr);
                int resp = findMove(temp, depth - 1, false, -1, alpha, beta);
                if (resp >= bestScore) {
                    bestScore = resp;
                    bestmove = curr;
                    alpha = max(alpha, resp);
                    if (alpha >= beta) {
                        break;
                    }
                }
            }
        } else {
            Iterator<amazons.Move> m = board.legalMoves(BLACK);
            bestScore = INFTY;
            while (m.hasNext()) {
                Board temp = new Board(board);
                Move curr = m.next();
                temp.makeMove(curr);
                int resp = findMove(temp, depth - 1, false, 1, alpha, beta);
                if (resp <= bestScore) {
                    bestScore = resp;
                    bestmove = curr;
                    beta = min(beta, resp);
                    if (alpha >= beta) {
                        break;
                    }
                }
            }
        }
        if (saveMove) {
            _lastFoundMove = bestmove;
        }
        return bestScore;
    }

    /** Return a heuristically determined maximum search depth
     *  based on characteristics of BOARD. */
    private int maxDepth(Board board) {
        int N = board.numMoves();
        int coefficient2 = 10 * 2,
                coefficient3 = 5 * 11,
                coefficient4 = 7 * 10 + 5,
                coefficient5 = 8 * 10 + 5;
        if (N > coefficient5) {
            return 5;
        } else if (N > coefficient4) {
            return 4;
        } else if (N > coefficient3) {
            return 3;
        } else if (N > coefficient2) {
            return 2;
        }
        return 1;
    }


    /** Return a heuristic value for BOARD. */
    private int staticScore(Board board) {
        Piece winner = board.winner();
        if (winner == WHITE) {
            return WINNING_VALUE;
        } else if (winner == BLACK) {
            return -WINNING_VALUE;
        } else {
            int a = 0;
            Iterator<amazons.Move> m = board.legalMoves(WHITE);
            while (m.hasNext()) {
                a++;
                m.next();
            }
            int b = 0;
            Iterator<amazons.Move> x = board.legalMoves(WHITE);
            while (m.hasNext()) {
                b++;
                x.hasNext();
            }
            return a - b;
        }
    }
}
