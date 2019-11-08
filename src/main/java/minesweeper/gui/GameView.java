package minesweeper.gui;

import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.scene.input.MouseButton;
import minesweeper.model.Board;
import minesweeper.generator.MinefieldGenerator;

import minesweeper.bot.TestBot;
import minesweeper.bot.Bot;

public class GameView {
    private GridPane gameGP;
    private Board board;
    private VBox vbox;
    private int sizeX;
    private int sizeY;
    private int remainingUnflaggedMines;
    private Bot bot;
    private Label endLabel = new Label("Mines: ");

    private Button botButton;
    private Button botGame;
    public GameView(int x, int y, VBox vbox, int mines) {
        MinefieldGenerator generator;
        this.vbox = vbox;
        sizeX = x;
        sizeY = y;
        remainingUnflaggedMines = mines;
        this.endLabel.setText(this.endLabel.getText() + remainingUnflaggedMines);

        this.bot = new TestBot();

        botButton = new Button("Help (bot)");
        botButton.setOnMouseClicked(e -> {
            if (this.bot.makeMove(this.board)) {
                this.updateGameGP(false);
            } else {
                this.gameOver();
            }
        });
        botGame = new Button("Bot Game");
        botGame.setOnMouseClicked(e -> {
            this.botGameLoop();
        });
        vbox.getChildren().add(botButton);
        vbox.getChildren().add(botGame);
        vbox.getChildren().add(this.endLabel);

        gameGP = new GridPane();
        gameGP.setMaxWidth(sizeX * 30);
        gameGP.getStyleClass().add("custom-gridpane");
        vbox.getChildren().add(gameGP);
        generator = new MinefieldGenerator();
        board = new Board(generator, x, y, mines);

        for (int i = 0; i < x; i++) {
            for (int j = 0; j < y; j++) {
                Button button = buildButton(30, i, j);
                gameGP.add(button, i, j);
            }
        }
    }
    /**
     * Returns a VBox with the view including a GridPane with the gamestate
     */
    public VBox getView() {
        return this.vbox;
    }

    /**
     * Builds a new button with the required functionality for flagging and opening squares
     */
    public Button buildButton(int size, int x, int y) {
        Button button = new Button();
        button.setMinWidth(size);
        button.setMaxWidth(size);
        button.setMinHeight(size);
        button.setMaxHeight(size);
        button.setOnMouseClicked((e) -> {
            if ((e.getButton() == MouseButton.PRIMARY && e.isSecondaryButtonDown()
                || (e.getButton() == MouseButton.SECONDARY && e.isPrimaryButtonDown()))
                && board.board[x][y].getOpen()) {
                if (!board.chordedOpen(x, y)) {
                    gameOver();
                } else {
                    updateGameGP(false);
                }
            } else if (e.getButton() == MouseButton.PRIMARY) {
                //If the first click of the game, generate a new board
                if (!board.open(x, y)) {
                    button.getStyleClass().add("mine");
                    gameOver();
                    return; 
                } else if (board.gameWon) {
                    gameWon();
                } else {
                    updateGameGP(false);
                }

            } else if (e.getButton() == MouseButton.SECONDARY) {
                //If a right click, flag or unflag a Square
                if (!board.board[x][y].getOpen()) {
                    board.board[x][y].toggleFlagged();
                    if (board.board[x][y].getFlagged()) {
                        button.getStyleClass().add("flagged-button");
                        this.remainingUnflaggedMines--;
                        this.endLabel.setText("Mines: " + this.remainingUnflaggedMines);
                    } else {
                        button.getStyleClass().remove("flagged-button");
                        this.remainingUnflaggedMines++;
                        this.endLabel.setText("Mines: " + this.remainingUnflaggedMines);
                    }
                }
               
            }
        });

        return button;
    }
    /**
     * Updates the view to show that the game has been won.
     */
    public void gameWon() {
        this.endLabel.setText("You won. Congratulations!");
        updateGameGP(true);
    }
    /**
     * Updates the view to show that the game has been lost.
     */
    public void gameOver() {
        this.endLabel.setText("You lost. Get rekt");
        updateGameGP(true);
    }
    
    /**
     * Updates the view with the current boardstate. If the game is over buttons are disabled.
     */
    public void updateGameGP(Boolean end) {
        GridPane originalGP = this.gameGP;
        this.gameGP = new GridPane();
        gameGP.setMaxWidth(sizeX * 30);
        gameGP.getStyleClass().add("custom-gridpane");
        for (int i = 0; i < sizeX; i++) {
            for (int j = 0; j < sizeY; j++) {
                //Builds new buttons for each Square on the board.
                Button newButton = new Button();
                if (end) {
                    //Disabled buttons if the game is over.
                    newButton.setMinHeight(30);
                    newButton.setMaxHeight(30);
                    newButton.setMinWidth(30);
                    newButton.setMaxWidth(30);
                    botButton.setDisable(true);
                } else {
                    //Functional buttons when game is underway.
                    newButton = buildButton(30, i, j);
                }
                
                //Updates the button in the current location with the correct 
                //visual representation of the Square.
                switch (board.board[i][j].highlight) {
                    case RED: 
                        newButton.getStyleClass().add("red-highlight");
                        break;
                    case GREEN: 
                        newButton.getStyleClass().add("green-highlight");
                        break;
                    default:
                        break;
                }

                if (board.board[i][j].getOpen()) {

                    newButton.getStyleClass().add("opened-button");
                    if (board.board[i][j].isMine()) {
                        newButton.getStyleClass().add("mine");

                    } else if (board.board[i][j].surroundingMines() != 0) {
                        newButton.setText("" + board.board[i][j].surroundingMines());
                        setOpenedButtonColor(newButton, board.board[i][j].surroundingMines());
            
                    }
                } else {
                    if (board.board[i][j].getFlagged()) {
                        newButton.getStyleClass().add("flagged-button");
                    }
                }

                gameGP.add(newButton, i, j);
            }
        }
        this.vbox.getChildren().remove(originalGP);
        this.vbox.getChildren().add(gameGP);
    }

    private void botGameLoop() {
        //Called as if game is over to disable human input
        updateGameGP(true); 
        //Start the bot thread
        /* while loop commented out so program wont freeze before implementation
        while(!board.gameEnd) { 
            //Poll the queue for the next move, might need try catch if bot is slow
            //Update the board
            //Update the GridPane
            //
        }*/
    }
    private void setOpenedButtonColor(Button button, int mines) {
        String labelStyle = "custom-label-";
        labelStyle = labelStyle.concat("" + mines);
        button.getStyleClass().add(labelStyle);
    }
}
