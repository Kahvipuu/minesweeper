
package minesweeper.model;

import org.junit.Test;
import org.junit.After;
import org.junit.Before;
import static org.junit.Assert.assertEquals;

import minesweeper.generator.MinefieldGenerator;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.function.Function;

public class BoardTest {
    private Board board;
    private MinefieldGenerator generator;

    @Before
    public void setUp() {
        generator = new MinefieldGenerator();
        board = new Board(generator, 10, 10, 3);
        board.firstMove = false;
    } 

    @After
    public void tearDown() {
    // empty method
    }

    @Test
    public void boardInitializesAllSquares() {
        for (int y = 0; y < 10; y++) {
            for (int x = 0; x < 10; x++) {
                assertTrue(board.board[y][x] != null);
            }
        }
    }

    @Test
    public void boardIsInitializedWithCorrectWidthAndHeight() {
        board = new Board(generator, 10, 10, 3);

        assertEquals(10, board.length);
        assertEquals(10, board.width);
    }

    @Test
    public void withinBoardReturnsTrueForWithinBoard() {
        for (int y = 0; y < 10; y++) {
            for (int x = 0; x < 10; x++) {
                assertTrue(board.withinBoard(x, y));
            }
        }
    }

    @Test
    public void withinBoardReturnsFalseForOutsideBoard() {
        assertEquals(false, board.withinBoard(20, 5));
        assertEquals(false, board.withinBoard(5, 20));
        assertEquals(false, board.withinBoard(-5, 5));
        assertEquals(false, board.withinBoard(5, -5));
    }

    @Test
    public void addingAMineWorks() {
        board.addSquare(new Square(true), 5, 5);
        Move move = new Move(MoveType.OPEN, 5, 5);
        board.makeMove(move);

        assertTrue(board.board[5][5].isMine());
    }

    @Test
    public void clickinOnAMineReturnsFalse() {
        board.addSquare(new Square(true), 5, 5);

        assertEquals(false,  board.makeMove(new Move(MoveType.OPEN, 5, 5)));
    }

    @Test
    public void clickingOnAnEmptySquareReturnsTrue() {
        board.addSquare(new Square(true), 5, 5);
        
        Move move = new Move(MoveType.OPEN, 2, 2);
        assertEquals(true, board.makeMove(move));
    }

    @Test
    public void openingEmptyFieldOpensAllSquares() {
        
        Move move = new Move(MoveType.OPEN, 5, 5);
        board.makeMove(move);

        for (int y = 0; y < 10; y++) {
            for (int x = 0; x < 10; x++) {
                assertTrue(board.board[y][x].isOpened());
            }
        }
    }

    @Test
    public void openingASquareDoesNotOpenMines() {
        board.addSquare(new Square(true), 2, 2);
        board.incrementAdjacentSquares(2,2);

        Move move = new Move(MoveType.OPEN, 5, 5);
        board.makeMove(move);

        assertEquals(false, board.board[2][2].isOpened());
    }

    @Test
    public void openingASquareDoesNotOpenFlagged() {
        board.board[4][5].toggleFlagged();
        
        assertEquals(true, board.board[4][5].getFlagged());

        Move move = new Move(MoveType.OPEN, 5, 5);
        board.makeMove(move);

        assertEquals(false, board.board[4][5].isOpened());
    }

    @Test
    public void flaggedSquareReturnsTrueWhenOpened() {
        board.board[5][5].toggleFlagged();
        Move move = new Move(MoveType.OPEN, 5, 5);
        assertTrue(board.makeMove(move));
    }
    
    @Test
    public void openingASquareAddsToOpenedSquares() {
        Move move = new Move(MoveType.OPEN, 5, 5);
        board.makeMove(move);

        assertTrue(board.getOpenSquares().contains(board.getSquareAt(5, 5)));
    }

    @Test
    public void chordedOpenDoesNotOpenFlagged() {
        board.board[4][5].setMine();
        board.board[4][5].toggleFlagged();
        board.incrementAdjacentSquares(4, 5);

        board.board[4][4].setMine();
        board.board[4][4].toggleFlagged();
        board.incrementAdjacentSquares(4, 5);

        board.board[6][5].setMine();
        board.board[6][5].toggleFlagged();
        board.incrementAdjacentSquares(4, 5);


        Move move = new Move(MoveType.OPEN, 5, 5);
        board.makeMove(move);

        
        Move chordedMove = new Move(MoveType.CHORD, 5, 5);
        board.makeMove(chordedMove);

        assertEquals(false, board.board[4][5].isOpened());
        assertEquals(false, board.board[4][4].isOpened());
        assertEquals(false, board.board[6][5].isOpened());
    }

    @Test
    public void chordedOpenOpensUnflagged() {
        board.board[4][5].setMine();
        board.board[4][5].toggleFlagged();
        board.incrementAdjacentSquares(4, 5);

        board.board[4][4].setMine();
        board.board[4][4].toggleFlagged();
        board.incrementAdjacentSquares(4, 5);

        board.board[6][5].setMine();
        board.board[6][5].toggleFlagged();
        board.incrementAdjacentSquares(4, 5);

        
        Move move = new Move(MoveType.OPEN, 5, 5);
        board.makeMove(move);


        Move chordedMove = new Move(MoveType.CHORD, 5, 5);
        board.makeMove(chordedMove);

        for (int xInc = -1; xInc <= 1; xInc++) {
            for (int yInc = -1; yInc <= 1; yInc++) {
                if (!board.board[5 + xInc][5 + yInc].getFlagged()) {
                    assertEquals(true, board.board[5 + xInc][5 + yInc].isOpened());
                }
            }
        }
    }

    @Test
    public void chordedOpenReturnsFalseForMineHit() {
        board.board[4][5].setMine();
        board.board[4][5].toggleFlagged();
        board.incrementAdjacentSquares(4, 5);

        board.board[4][4].setMine();
        board.incrementAdjacentSquares(4, 5);

        board.board[6][6].toggleFlagged();

        board.board[6][5].setMine();
        board.board[6][5].toggleFlagged();
        board.incrementAdjacentSquares(4, 5);

        board.board[5][5].open();
        assertEquals(false, board.makeMove(new Move(MoveType.CHORD, 5, 5)));
    }

    @Test
    public void chordedOpenWillNotRunIfSurroundingMinesAndFlagsDoNotMatch() {
        board.board[4][5].setMine();
        board.board[4][5].toggleFlagged();
        board.incrementAdjacentSquares(4, 5);

        board.board[4][4].setMine();
        board.incrementAdjacentSquares(4, 5);

        board.board[6][5].setMine();
        board.board[6][5].toggleFlagged();
        board.incrementAdjacentSquares(4, 5);

        Move move = new Move(MoveType.OPEN, 5, 5);
        board.makeMove(move);


        Move chordedMove = new Move(MoveType.CHORD, 5, 5);
        assertEquals(true, board.makeMove(chordedMove));

        assertEquals(false, board.board[6][6].isOpened());
    }

    @Test
    public void boardHighlightsCanBeCleared() {
        board.board[5][5].highlight = Highlight.RED;

        board.clearHighlights();

        for (int y = 0; y < board.length; y++) {
            for (int x = 0; x < board.width; x++) {
                assertEquals(Highlight.NONE, board.board[x][y].highlight);
            }
        }
    }

    @Test
    public void makingOpenMoveOpensSquare() {
        board.makeMove(new Move(MoveType.OPEN, 5, 5));

        assertTrue(board.getSquareAt(5, 5).isOpened());
    }

    @Test
    public void makingChordMoveOpensSquares() {
        board.firstMove = false;

        board.board[4][5].setMine();
        board.board[4][5].toggleFlagged();
        board.incrementAdjacentSquares(4, 5);
        
        Move move = new Move(MoveType.OPEN, 5, 5);
        board.makeMove(move);
        
        board.makeMove(new Move(MoveType.CHORD, 5, 5));

        for (int xInc = -1; xInc <= 1; xInc++) {
            for (int yInc = -1; yInc <= 1; yInc++) {
                if (!board.board[5 + xInc][5 + yInc].getFlagged()) {
                    assertEquals(true, board.board[5 + xInc][5 + yInc].isOpened());
                }
            }
        }
    }

    @Test
    public void makingFlagMoveFlagsSquare() {
        board.makeMove(new Move(MoveType.FLAG, 5, 5));

        assertTrue(board.getSquareAt(5, 5).getFlagged());
    }

    @Test
    public void makingHighlightMoveHighlightsSquare() {
        board.makeMove(new Move(5, 5, Highlight.RED));

        assertEquals(Highlight.RED, board.getSquareAt(5, 5).highlight);
    }

    @Test
    public void observedBoardSendsNeededCallbacks(){
        this.board = new Board(generator, 2, 2, 0);
        Square[] neededSquares = new Square[]{this.board.board[0][0], this.board.board[0][1], this.board.board[1][0]};
        ArrayList<Square> gotSquares = new ArrayList<Square>();
        Function<Square,Void> callback = new Function<Square,Void>() {
            @Override
            public Void apply(Square t) {
                gotSquares.add(t);
                return null;
            }
        };
        board.setChangeObserver(callback);
       
        Move move = new Move(MoveType.OPEN, 1, 1);
        board.makeMove(move);
        assertTrue(gotSquares.containsAll(Arrays.asList(neededSquares)));
    }

    @Test
    public void findUnopenedSquareFindsOne() {
        // at first, all squares are closed
        for (int i=0; i < 10; i++) {
            for (int j=0; j < 8; j++) {
                board.board[i][j].open();
            }
        }
        // leave some unopened squares so that random finding does not take too long
        int value = board.findUnopenedSquare();
        int x = value / 1000;
        int y = value % 1000;
        assertEquals(false, board.board[5][8].isOpened());
        assertEquals(true, board.board[5][7].isOpened());
    }
}
