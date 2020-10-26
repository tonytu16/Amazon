package amazons;


import java.util.Collections;
import java.util.Iterator;
import static amazons.Piece.*;
import java.util.Stack;
/**
 import java.util.ArrayDeque;
 import java.util.NoSuchElementException;
 import java.util.Formatter;
 import static amazons.Move.mv;
 */

/** The state of an Amazons Game.
 *  @author Tony Tu
 */
class Board {

    /** The number of squares on a side of the board. */
    static final int SIZE = 10;

    /** Initializes a game board with SIZE squares on a side in the
     *  initial position. */
    Board() {
        init();
    }

    /** Initializes a copy of MODEL. */
    Board(Board model) {

        copy(model);
    }

    /** Copies MODEL into me. */
    void copy(Board model) {
        _board = new Piece[SIZE][SIZE];
        _moves = new Stack<>();
        this._numMoves = model.numMoves();
        int total = 9 * 11;
        for (int i = 0; i < total; i++) {
            Square sq = Square.sq(i);
            this.setBoard(sq, model.findPiece(sq));
        }
        this.changeTurn(model.turn());
        this._winner = model._winner;
        this._moves.clear();
        Move[] temp = new Move[this.numMoves()];
        for (int i = temp.length - 1; i >= 0; i--) {
            temp[i] = model.moves().pop();
        }
        for (int i = 0; i < temp.length; i++) {
            model.moves().push(temp[i]);
            this.moves().push(temp[i]);
        }

    }

    /** Clears the board to the initial position. */
    void init() {
        _board = new Piece[SIZE][SIZE];
        _turn = WHITE;
        _winner = null;
        _numMoves = 0;
        _moves = new Stack<>();
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                _board[i][j] = EMPTY;
            }
        }
        _board[0][3] = WHITE;
        _board[0][6] = WHITE;
        _board[3][0] = WHITE;
        _board[3][9] = WHITE;
        _board[6][0] = BLACK;
        _board[6][9] = BLACK;
        _board[9][3] = BLACK;
        _board[9][6] = BLACK;
    }

    /** Return the Piece whose move it is (WHITE or BLACK). */
    Piece turn() {
        return _turn;
    }

    /** Return the number of moves (that have not been undone) for this
     *  board. */
    int numMoves() {
        return _numMoves;
    }

    /** Return the winner in the current position, or null if the game is
     *  not yet finished. */
    Piece winner() {
        Piece current = turn();
        if (!legalMoves(current).hasNext()) {
            if (current == BLACK) {
                _winner = WHITE;
            } else {
                _winner = BLACK;
            }

        }
        return _winner;
    }

    /** Return the contents the square at S. */
    final Piece get(Square s) {
        return get(s.col(), s.row());
    }

    /** Return the contents of the square at (COL, ROW), where
     *  0 <= COL, ROW <= 9. */
    final Piece get(int col, int row) {
        return _board[row][col];
    }

    /** Return the contents of the square at COL ROW. */
    final Piece get(char col, char row) {

        return get(col - 'a', row - '1');
    }

    /** Set square S to P. */
    final void put(Piece p, Square s) {
        put(p, s.col(), s.row());
    }

    /** Set square (COL, ROW) to P. */
    final void put(Piece p, int col, int row) {
        _board[row][col] = p;
        _winner = EMPTY;
    }

    /** Set square COL ROW to P. */
    final void put(Piece p, char col, char row) {
        int c = 0;
        for ( ; c < _letters.length; c++) {
            if (_letters[c] == col) {
                break;
            }
        }
        int r = Character.getNumericValue(row);
        put(p, c, r - '1');
    }

    /** Return true iff FROM - TO is an unblocked queen move on the current
     *  board, ignoring the contents of ASEMPTY, if it is encountered.
     *  For this to be true, FROM-TO must be a queen move and the
     *  squares along it, other than FROM and ASEMPTY, must be
     *  empty. ASEMPTY may be null, in which case it has no effect. */
    boolean isUnblockedMove(Square from, Square to, Square asEmpty) {
        assert from.isQueenMove(to);
        int dir = from.direction(to);
        if (get(to) != EMPTY && to != asEmpty) {
            return false;
        }
        int steps = Math.abs(Math.max((to.col() - from.col()),
                (to.row() - from.row())));
        for (int i = 1; i <= steps; i++) {
            Square temp = from.queenMove(dir, i);
            if (((temp != asEmpty) && (get(temp) != EMPTY))) {
                return false;
            }
        }
        return true;
    }

    /** Returns a list of the squares the queen passes in traveling
     * DISTANCE squares, in which we use from and to parameters
     * in a DIRECTION. In this program we use @param from and
     * @param to as our parameters*/
    Square[] findPath(Square from, Square to) {
        int dy = to.col() - from.col();
        int dx = to.row() - from.row();
        int distance = Math.max(Math.abs(dy), Math.abs(dx));
        int[] direction = {dx / distance, dy / distance};

        Square[] result = new Square[distance];
        int c = from.col() + direction[1];
        int r = from.row() + direction[0];
        for (int i = 0; i < distance; i++) {
            result[i] = Square.sq(c, r);
            c += direction[1];
            r += direction[0];
        }
        return result;
    }

    /** Return true iff FROM is a valid starting square for a move. */
    boolean isLegal(Square from) {
        return _board[from.row()][from.col()] == turn();
    }

    /** Return true iff FROM-TO is a valid first part of move, ignoring
     *  spear throwing. */
    boolean isLegal(Square from, Square to) {
        return isLegal(from) && (_board[to.row()][to.col()] == EMPTY)
                && isUnblockedMove(from, to, null);
    }

    /** Return true iff FROM-TO(SPEAR) is a legal move in the current
     *  position. */
    boolean isLegal(Square from, Square to, Square spear) {
        return isLegal(from, to) && isUnblockedMove(to, spear, from);
    }

    /** Return true iff MOVE is a legal move in the current
     *  position. */
    boolean isLegal(Move move) {
        return isLegal(move.from(), move.to(), move.spear());
    }

    /** Move FROM-TO(SPEAR), assuming this is a legal move. */
    void makeMove(Square from, Square to, Square spear) {
        assert isLegal(from, to, spear);
        Piece queen = findPiece(from);
        setBoard(to, queen);
        setBoard(from, EMPTY);
        setBoard(spear, SPEAR);
        changeTurn();
        _numMoves += 1;
        Move m = Move.mv(from, to, spear);
        _moves.push(m);
    }

    /** Move according to MOVE, assuming it is a legal move. */
    void makeMove(Move move) {
        Square from = move.from();
        Square to = move.to();
        Square spear = move.spear();
        makeMove(from, to, spear);
    }

    /** Undo one move.  Has no effect on the initial board. */
    void undo() {
        if (numMoves() != 0) {
            Move m = moves().pop();
            Square from = m.from();
            Square to = m.to();
            Square spear = m.spear();

            setBoard(spear, EMPTY);
            setBoard(from, findPiece(to));
            setBoard(to, EMPTY);

            changeTurn();
            _numMoves -= 1;
        }
    }

    /** Return an Iterator over the Squares that are reachable by an
     *  unblocked queen move from FROM. Does not pay attention to what
     *  piece (if any) is on FROM, nor to whether the game is finished.
     *  Treats square ASEMPTY (if non-null) as if it were EMPTY.  (This
     *  feature is useful when looking for Moves, because after moving a
     *  piece, one wants to treat the Square it came from as empty for
     *  purposes of spear throwing.) */
    Iterator<Square> reachableFrom(Square from, Square asEmpty) {

        return new ReachableFromIterator(from, asEmpty);
    }

    /** Return an Iterator over all legal moves on the current board. */
    Iterator<Move> legalMoves() {
        return new LegalMoveIterator(_turn);
    }

    /** Return an Iterator over all legal moves on the current board for
     *  SIDE (regardless of whose turn it is). */
    Iterator<Move> legalMoves(Piece side) {
        return new LegalMoveIterator(side);
    }

    /** An iterator used by reachableFrom. */
    private class ReachableFromIterator implements Iterator<Square> {

        /** Iterator of all squares reachable by queen move from FROM,
         *  treating ASEMPTY as empty. */
        ReachableFromIterator(Square from, Square asEmpty) {
            _from = from;
            _dir = 0;
            _steps = 0;
            _asEmpty = asEmpty;
            toNext();
        }

        @Override
        public boolean hasNext() {
            return _dir < 8;
        }

        @Override
        public Square next() {
            Square result = _from.queenMove(_dir, _steps);
            toNext();
            return result;
        }

        /** Advance _dir and _steps, so that the next valid Square is
         *  _steps steps in direction _dir from _from. */
        private void toNext() {
            Square temp = _from.queenMove(_dir, _steps + 1);
            if (temp != null && isUnblockedMove(_from, temp, _asEmpty)) {
                _steps += 1;
            } else {
                _dir++;
                _steps = 1;
                while (_dir < 8 && (_from.queenMove(_dir, 1) == null
                        || (get(_from.queenMove(_dir, 1)) != EMPTY)
                        && _from.queenMove(_dir, 1) != _asEmpty)) {
                    _dir += 1;
                }
            }
        }

        /** Starting square. */
        private Square _from;
        /** Current direction. */
        private int _dir;
        /** Current distance. */
        private int _steps;
        /** Square treated as empty. */
        private Square _asEmpty;
    }

    /** An iterator used by legalMoves. */
    private class LegalMoveIterator implements Iterator<Move> {

        /** All legal moves for SIDE (WHITE or BLACK). */
        LegalMoveIterator(Piece side) {
            _startingSquares = Square.iterator();
            _spearThrows = NO_SQUARES;
            _pieceMoves = NO_SQUARES;
            _fromPiece = side;
            toNext();
        }

        @Override
        /** A program that checks if the spear has next */
        public boolean hasNext() {
            Boolean result = _spearThrows.hasNext();
            return result;
        }

        @Override
        /** a program that checks the next move */
        public Move next() {
            Move m = Move.mv(_start, _nextSquare, _spearThrows.next());
            toNext();
            return m;
        }

        /** Advance so that the next valid Move is
         *  _start-_nextSquare(sp), where sp is the next value of
         *  _spearThrows. */
        private void toNext() {
            while (!_spearThrows.hasNext()) {

                while (!_pieceMoves.hasNext()) {

                    if (_startingSquares.hasNext()) {
                        _start = _startingSquares.next();
                    } else {
                        return;
                    }
                    while (_startingSquares.hasNext()
                            && _fromPiece != findPiece(_start)) {
                        _start = _startingSquares.next();
                    }
                    if (_fromPiece != findPiece(_start)) {
                        return;
                    }

                    _pieceMoves = reachableFrom(_start, null);

                }
                _nextSquare = _pieceMoves.next();
                _spearThrows = reachableFrom(_nextSquare, _start);


            }

        }

        /** Color of side whose moves we are iterating. */
        private Piece _fromPiece;
        /** Current starting square. */
        private Square _start;
        /** Remaining starting squares to consider. */
        private Iterator<Square> _startingSquares;
        /** Current piece's new position. */
        private Square _nextSquare;
        /** Remaining moves from _start to consider. */
        private Iterator<Square> _pieceMoves;
        /** Remaining spear throws from _piece to consider. */
        private Iterator<Square> _spearThrows;
        /** Next spear throw. */
        private Square _spearThrow;
    }

    @Override
    public String toString() {
        String result = "";
        for (int i = SIZE - 1; i >= 0; i--) {
            for (int j = 0; j < SIZE; j++) {
                if (j == 0) {
                    result += "   ";
                    result += _board[i][j].toString() + " ";
                } else if (j == SIZE - 1) {
                    result += _board[i][j].toString() + "\n";
                } else {
                    result += _board[i][j].toString() + " ";
                }
            }
        }
        return result;
    }

    /* The following helper methods are mine.
    So they might be wrong. :) */
    /** Returns the piece at the board at COL and ROW. */
    public Piece board(int col, int row) {
        return _board[row][col];
    }

    /** Returns the piece at the board with a given index (MINE)
     * in which the function takes in an index argument. Here
     * we use @param index parameter as an argument. */
    public Piece board(int index) {
        int row = index / SIZE;
        int col = index % SIZE;
        return _board[row][col];
    }

    /** Return the piece at the board with the given square MINE
     * in which the function takes in @param s as argument. */
    public Piece findPiece(Square s) {
        if (s == null) {
            return null;
        }
        return board(s.index());
    }

    /** Sets the piece at S to be P MINE. */
    private void setBoard(Square s, Piece p) {
        int row = s.index() / SIZE;
        int col = s.index() % SIZE;
        _board[row][col] = p;
    }

    /** Changes turn to P MINE. */
    void changeTurn(Piece p) {
        _turn = p;
    }

    /** Changes turn. */
    void changeTurn() {
        if (turn() == WHITE) {
            changeTurn(BLACK);
        } else {
            changeTurn(WHITE);
        }
    }

    /** Returns the current moves made MINE. */
    Stack<Move> moves() {
        return _moves;
    }

    /** An empty iterator for initialization. */
    private static final Iterator<Square> NO_SQUARES =
            Collections.emptyIterator();

    /** Piece whose turn it is (BLACK or WHITE). */
    private Piece _turn;

    /** Cached value of winner on this board, or EMPTY if it has not been
     *  computed. */
    private Piece _winner;

    /* Everything below is mine :) */

    /** The number of moves that have been made. */
    private int _numMoves;

    /** The board itself with each square representing a
     * white, black, spear, or empty piece. */
    private Piece[][] _board;

    /** Represents past moves. */
    private Stack<Move> _moves;

    /** Helps get you the column number from the letter. */
    private final char[] _letters =
    {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j'};
}
