package com.example.tiktactoe;

import androidx.appcompat.app.AppCompatActivity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import java.lang.ref.WeakReference;

public class MainActivity extends AppCompatActivity {

    //////////////////////////////////////////////////////////////////////
    // Definition of Box class:
    // This class defines Box in TickTacToe board, where you put X or O
    /////////////////////////////////////////////////////////////////////
    class Box{
        private ImageView xImage;      // Image X
        private ImageView oImage;      // Image O
        private ImageView activeImage; // Displayed Image
        private boolean   boxFilled;   // Flag to indicate box is filled
        private Symbol    symbolSet;   // Symbol set on this box X or O
        private int x, y;

        // Constructor
        public Box(int xImageId, int oImageId, int posX, int posY)
        {
            xImage = findViewById(xImageId); // Init X image
            oImage = findViewById(oImageId); // Init O image
            xImage.setAlpha(0f);             // Hide X
            oImage.setAlpha(0f);             // Hide O
            boxFilled = false;               // Box is not filled yet
            symbolSet = Symbol.UNDEFINED;    // No symbol for this box
            x = posX;
            y = posY;
        }

        // Method to put X in box
        private void setX() {
            xImage.setAlpha(1f);           // Show X
            oImage.setAlpha(0f);           // Hide O (Might be redundant)
            boxFilled = true;              // Indicate box is filled
            activeImage = xImage;          // Active image is of X
            symbolSet = Symbol.X_SELECTED; // Set symbol to X
            mFilledBoxes++;                 // Increment filled boxes
        }

        // Method to put O in box
        private void setO(){
            xImage.setAlpha(0f);           // Show O
            oImage.setAlpha(1f);           // Hide X (Might be redundant)
            boxFilled = true;              // Indicate box is filled
            activeImage = oImage;          // Active image is of O
            symbolSet = Symbol.O_SELECTED; // Set symbol to O
            mFilledBoxes++;                 // Increment filled boxes
        }

        // Method to return active image
        public ImageView getActiveImage()
        {
            return activeImage;
        }

        // Method to return set symbol
        /*public Symbol getSymbol()
        {
            return symbolSet;
        }*/

        // Method to set X or O in box
        public boolean setSymbol(Symbol symbol)
        {
            boolean status = false;

            if (!boxFilled){
                statusBoard[x][y] = symbol;
                if (Symbol.X_SELECTED == symbol){
                    setX();
                    status = true;
                } else if (Symbol.O_SELECTED == symbol){
                    setO();
                    status = true;
                }
            }
            return status;
        }

        // Reset Box (Make it blank, and show it's unfilled)
        public void resetBox()
        {
            boxFilled = false;
            symbolSet = Symbol.UNDEFINED;
            statusBoard[x][y] = symbolSet;
            xImage.animate().alpha(0f).setDuration(1000).start();
            oImage.animate().alpha(0f).setDuration(1000).start();
        }
    }

    //////////////////////////////////////////////////////////////////////
    // Definition of Player class:
    // This class defines Players in TickTacToe game.
    /////////////////////////////////////////////////////////////////////
    class Player{
        private boolean isActive; // To show player is active or not (In play)
        private int     score;    // Score

        // Constructor
        public Player(){
            isActive = false;  // Set player inactive
            score = 0;         // Set score to 0
        }

        // This method returns active state of player
        public boolean isActive() {
            return isActive;
        }

        // This method sets player active
        public void setActive() {
            isActive = true;
        }

        // This method sets player inactive
        public void setInactive(){
            isActive = false;
        }

        // Increment the score
        public void incrementScore()
        {
            score++;
        }

        // Reset the player
        public void resetPlayer()
        {
            setInactive();
        }
    }

    //////////////////////////////////////////////////////////////////////
    // Define symbols:
    // This enums defines X, O and blank
    /////////////////////////////////////////////////////////////////////
    public enum Symbol{
        X_SELECTED,         // Symbol X
        O_SELECTED,         // Symbol O
        UNDEFINED           // Blank box
    }

    //////////////////////////////////////////////////////////////////////
    // Define Game Status:
    // This enums defines game status
    /////////////////////////////////////////////////////////////////////
    public enum GameStatus{
        GAME_NOT_STARTED,     // Game is not started
        GAME_STARTED,         // Game is started
        GAME_INCOMPLETE,      // Game is incomplete
        GAME_COMPLETE,        // Game is completed (winner is decided)
        GAME_IS_DRAW          // Game is started (Nobody won)
    }

    // Create object for each box in 3 x 3 board (or frame)
    // Row 0
    private Box boxAtPos00; // Box (0,0)
    private Box boxAtPos01; // Box (0,1)
    private Box boxAtPos02; // Box (0,2)

    // Row 1
    private Box boxAtPos10; // Box (1,0)
    private Box boxAtPos11; // Box (1,1)
    private Box boxAtPos12; // Box (1,2)

    //Row 2
    private Box boxAtPos20; // Box (2,0)
    private Box boxAtPos21; // Box (2,1)
    private Box boxAtPos22; // Box (2,2)

    // Xs to put in each box of TickTacToe board
    private int[][] xImage = { {R.id.ximage00, R.id.ximage01, R.id.ximage02},
                               {R.id.ximage10, R.id.ximage11, R.id.ximage12},
                               {R.id.ximage20, R.id.ximage21, R.id.ximage22} };

    // Os to put in each box of TickTacToe board
    private int[][] oImage = { {R.id.oimage00, R.id.oimage01, R.id.oimage02},
                               {R.id.oimage10, R.id.oimage11, R.id.oimage12},
                               {R.id.oimage20, R.id.oimage21, R.id.oimage22} };

    Symbol[][] statusBoard =  { {Symbol.UNDEFINED, Symbol.UNDEFINED, Symbol.UNDEFINED},
                                {Symbol.UNDEFINED, Symbol.UNDEFINED, Symbol.UNDEFINED},
                                {Symbol.UNDEFINED, Symbol.UNDEFINED, Symbol.UNDEFINED} };

    // Create 3 x 3 TickTacToe board
    private Box[][] mBoard = { {boxAtPos00, boxAtPos01, boxAtPos02},
                              {boxAtPos10, boxAtPos11, boxAtPos12},
                              {boxAtPos20, boxAtPos21, boxAtPos22} };

    private Symbol currentSymbol, previousSymbol = Symbol.UNDEFINED;   // To keep track of which symbol is in play
    private boolean row0, row1, row2 = false;          // Flags to indicate winning row
    private boolean column0, column1, column2 = false; // Flags to indicate winning column
    private boolean diag0, diag1 = false;              // Flags to indicate winning diagonals
    private int mFilledBoxes = 0;                       // Number to keep track of filled boxes

    // Create player object
    private Player playerA = null; // First player
    private Player playerB = null; // Second player

    public Symbol[][] copyBoard( Symbol[][] board )
    {
        Symbol[][] newBoard = new Symbol[3][3];

        if (board != null) {
            for (int x = 0; x < 3; x++) {
                for (int y = 0; y < 3; y++) {
                    newBoard[x][y] = board[x][y];
                }
            }
        }

        return newBoard;
    }
    //////////////////////////////////////////////////////////////////////
    // This method displays winning boxes (rows, columns, or diagonals)
    //
    // Params : ImageView of winning boxes
    // Return : Void
    /////////////////////////////////////////////////////////////////////
    public void toggleWiningBoxes(final ImageView image0, final ImageView image1, final ImageView image2)
    {
        // Rotate images on winning boxes
        image0.animate().rotationYBy(720).setDuration(1000).start();
        image1.animate().rotationYBy(720).setDuration(1000).start();
        image2.animate().rotationYBy(720).setDuration(1000).start();
        Log.i("INFO", "animate image 0");
    }

    /////////////////////////////////////////////////////////////////////////////
    // This method decides which boxes to animate, and calls toggleWiningBoxes()
    //
    // Params : Void
    // Return : Void
    //////////////////////////////////////////////////////////////////////////////
    public void displayWiningBoxes()
    {
        if (row0) {
            toggleWiningBoxes( mBoard[0][0].getActiveImage(), mBoard[0][1].getActiveImage(), mBoard[0][2].getActiveImage()); // Row 0
        } else if (row1) {
            toggleWiningBoxes( mBoard[1][0].getActiveImage(), mBoard[1][1].getActiveImage(), mBoard[1][2].getActiveImage()); // Row 1
        } else if (row2) {
            toggleWiningBoxes( mBoard[2][0].getActiveImage(), mBoard[2][1].getActiveImage(), mBoard[2][2].getActiveImage()); // Row 2
        } else if (column0) {
            toggleWiningBoxes( mBoard[0][0].getActiveImage(), mBoard[1][0].getActiveImage(), mBoard[2][0].getActiveImage()); // Column 0
        } else if (column1) {
            toggleWiningBoxes( mBoard[0][1].getActiveImage(), mBoard[1][1].getActiveImage(), mBoard[2][1].getActiveImage()); // Column 1
        } else if (column2) {
            toggleWiningBoxes( mBoard[0][2].getActiveImage(), mBoard[1][2].getActiveImage(), mBoard[2][2].getActiveImage()); // Column 2
        } else if (diag0) {
            toggleWiningBoxes( mBoard[0][0].getActiveImage(), mBoard[1][1].getActiveImage(), mBoard[2][2].getActiveImage()); // Diagonal 0 (\)
        } else if (diag1) {
            toggleWiningBoxes( mBoard[0][2].getActiveImage(), mBoard[1][1].getActiveImage(), mBoard[2][0].getActiveImage()); // Diagonal 1 (/)
        }
    }

    //////////////////////////////////////////////////////////////////////
    // This method restarts game by resetting all flags, selections, boxes
    // and players
    //
    // Params : Void
    // Return : Void
    /////////////////////////////////////////////////////////////////////
    public void restartGame()
    {
        currentSymbol = Symbol.UNDEFINED;
        previousSymbol = Symbol.UNDEFINED;
        row0 = false;
        row1 = false;
        row2 = false;
        column0 = false;
        column1 = false;
        column2 = false;
        diag0 = false;
        diag1 = false;
        mFilledBoxes = 0;

        // Reset X and O selection buttons
        ImageView oSelectImage = findViewById(R.id.oSelected);
        ImageView xSelectImage = findViewById(R.id.xSelected);
        xSelectImage.animate().alpha(1f).start();
        oSelectImage.animate().alpha(1f).start();

        // Reset all boxes in board
        for (int x = 0; x < 3; x++){
            for (int y = 0; y < 3; y++){
                mBoard[x][y].resetBox();
            }
        }

        // Reset both players
        playerA.resetPlayer();
        playerB.resetPlayer();
    }

    //////////////////////////////////////////////////////////////////////
    // This method increments score for winning player
    //
    // Params : Void
    // Return : Void
    /////////////////////////////////////////////////////////////////////
    public void updateScore(){
        // Get active player and increment score of active player
        Player player = getActivePLayer();
        player.incrementScore();
    }

    //////////////////////////////////////////////////////////////////////
    // This method increments score for winning player
    //
    // Params : Void
    // Return : Void
    /////////////////////////////////////////////////////////////////////
    public boolean isGameDraw(Symbol[][] passedBoard){
        boolean status = false;
        int boxesFilled = 0;

        for ( int x = 0; x < 3; x++){
            for (int y = 0; y < 3; y++){
                if ( Symbol.UNDEFINED != passedBoard[x][y]){
                    boxesFilled++;
                }
            }
        }

        if (boxesFilled == 9)
        {
            status = true;
        }

        return  status;
    }

    //////////////////////////////////////////////////////////////////////
    // This method decides whether game is completed or it's draw. This
    // method is called from different thread
    //
    // Params : Void
    // Return : Void
    /////////////////////////////////////////////////////////////////////
    public GameStatus getGameStatus(Symbol[][] passedBoard)
    {
        GameStatus status = GameStatus.GAME_INCOMPLETE;

        // Decide if game is completed by checking boxes
        // Check 1st row and column
        if ( ( column0 = (Symbol.UNDEFINED != passedBoard[0][0]) && ( passedBoard[1][0] == passedBoard[0][0]) && (passedBoard[2][0] == passedBoard[0][0]) )||
                ( row0 = (Symbol.UNDEFINED != passedBoard[0][0]) && ( passedBoard[0][1] == passedBoard[0][0]) && (passedBoard[0][2] == passedBoard[0][0]) ) ) {
            status = GameStatus.GAME_COMPLETE;
        } // Check 2nd row and column
        else if ( (column1 = (Symbol.UNDEFINED != passedBoard[1][1]) && ( passedBoard[0][1] == passedBoard[1][1]) && (passedBoard[2][1] == passedBoard[1][1])) ||
                (row1 = (Symbol.UNDEFINED != passedBoard[1][1]) && ( passedBoard[1][0] == passedBoard[1][1]) && (passedBoard[1][2] == passedBoard[1][1]))) {
            status = GameStatus.GAME_COMPLETE;
        } // check 3rd row and column
        else if ( (column2 = (Symbol.UNDEFINED != passedBoard[2][2]) && ( passedBoard[0][2] == passedBoard[2][2]) && (passedBoard[1][2] == passedBoard[2][2])) ||
                (row2 = (Symbol.UNDEFINED != passedBoard[2][2]) && ( passedBoard[2][0] == passedBoard[2][2]) && (passedBoard[2][1] == passedBoard[2][2])) ) {
            status = GameStatus.GAME_COMPLETE;
        } // check both diagonals
        else if ( (diag0 = (Symbol.UNDEFINED != passedBoard[1][1]) && ( passedBoard[0][0] == passedBoard[1][1]) && (passedBoard[2][2] == passedBoard[1][1]) )||
                (diag1 = (Symbol.UNDEFINED != passedBoard[1][1]) && ( passedBoard[0][2] == passedBoard[1][1]) && (passedBoard[2][0] == passedBoard[1][1]) )) {
            status = GameStatus.GAME_COMPLETE;
        }


        // Check if all boxes are filled and winner hasn't decided yet,
        // if so game is draw
        if ( ( status == GameStatus.GAME_INCOMPLETE ) &&
             ( true == isGameDraw(passedBoard)) )
        {
            status = GameStatus.GAME_IS_DRAW;
        }

        return status;
    }

    /////////////////////////////////////////////////////////////////////////
    // This method does post processing after game is completed or it's draw
    //
    // Params : Void
    // Return : Void
    ////////////////////////////////////////////////////////////////////////
    public void processGameStatus()
    {
        GameStatus status = getGameStatus(statusBoard);

        // Check if game is completed
        if ( status == GameStatus.GAME_COMPLETE ) {
            updateScore();         // update winner's score
            displayWiningBoxes();  // Animate winning boxes
        }

        // If game is completed or it's draw then time to restart game
        if ( status == GameStatus.GAME_COMPLETE || status == GameStatus.GAME_IS_DRAW ){
            restartGame();
        }
    }

    /////////////////////////////////////////////////////////////////////////
    // This method finds best move for maximizing player
    //
    // Params : Box[][] : Game board to be processed to find best move
    //          isMaximizingPlayer : Maximizing player who is looking for best move
    // Return : Void
    ////////////////////////////////////////////////////////////////////////
    public int minMaxAlgorithm( Symbol[][] currentBoardStatus, boolean isMaximizingPlayer, Symbol maxiMizingSymbol, Symbol miniMizingSymbol )
    {
        GameStatus status = getGameStatus(currentBoardStatus);
        // Check if game is completed or draw
        if ( status == GameStatus.GAME_COMPLETE ){
            if (isMaximizingPlayer){
                return -1;
            }else{
                return 1;
            }
        } else if (status == GameStatus.GAME_IS_DRAW){
            return 0;
        }

            if (isMaximizingPlayer) {
                int bestValue = -1;
                for (int x = 0; x < 3; x++){
                    for (int y = 0; y < 3; y++){
                        // Create copy of board
                        Symbol[][] tempBoardStatus = copyBoard(currentBoardStatus);
                        if ( Symbol.UNDEFINED == tempBoardStatus[x][y] ) {
                            int retValue = 0;
                            tempBoardStatus[x][y] = maxiMizingSymbol;
                            retValue = minMaxAlgorithm( tempBoardStatus, false, maxiMizingSymbol, miniMizingSymbol  );
                            bestValue = Math.max(retValue, bestValue);
                        }
                    }
                }
                return  bestValue;

            } else {
                int bestValue = 1;
                for (int x = 0; x < 3; x++){
                    for (int y = 0; y < 3; y++){
                        Symbol[][] tempBoardStatus = copyBoard(currentBoardStatus);
                        if ( Symbol.UNDEFINED == tempBoardStatus[x][y] ) {
                            int retValue = 0;
                            tempBoardStatus[x][y] = miniMizingSymbol;
                            retValue = minMaxAlgorithm( tempBoardStatus, true, maxiMizingSymbol, miniMizingSymbol );
                            bestValue = Math.min(retValue, bestValue);
                        }
                    }
                }
                return bestValue;
            }
    }

    public void playNextMove()
    {
        int     bestValue = -1;
        int     tempReturn;
        Symbol  maximizingSymbol = currentSymbol;
        Symbol  minimizingSymbol = previousSymbol;
        int bestPosX = -1;
        int bestPosY = -1;

        for (int x = 0; x < 3; x++){
            for (int y = 0; y < 3; y++){
                Symbol[][] tempBoardStatus = copyBoard(statusBoard);
                if ( Symbol.UNDEFINED == tempBoardStatus[x][y] ) {
                    tempBoardStatus[x][y] = maximizingSymbol;
                    tempReturn = minMaxAlgorithm(tempBoardStatus, false, maximizingSymbol, minimizingSymbol );
                    if (bestValue < tempReturn){
                        bestValue = tempReturn;
                        bestPosX = x;
                        bestPosY = y;
                    }
                }
            }
        }
        Log.i("INFO", "bestPosX = "+ bestPosX +"bestPosY = "+bestPosY);
    }

    //////////////////////////////////////////////////////////////////////
    // This method toggles player to pass the turn
    //
    // Params : Void
    // Return : Void
    /////////////////////////////////////////////////////////////////////
    public void togglePlayer()
    {
        // If player A is active then
        // set Player A inactive and Player B active, and vice versa
        if (playerA.isActive()) {
            playerA.setInactive();
            playerB.setActive();
        } else  if (playerB.isActive()) {
            playerB.setInactive();
            playerA.setActive();
        }
    }

    //////////////////////////////////////////////////////////////////////
    // This method is used to toggle symbol selection between X and O
    //
    // Params : Void
    // Return : Void
    /////////////////////////////////////////////////////////////////////
    public void toggleSymbol(View view)
    {
        // If current symbol is X then select O, and vice versa
        if (Symbol.X_SELECTED == currentSymbol) {
            oSelected(view);
        } else  if (Symbol.O_SELECTED == currentSymbol) {
            xSelected(view);
        }
    }

    //////////////////////////////////////////////////////////////////////
    // OnClick method when user selects box (0,0)
    //
    // Params : Void
    // Return : Void
    /////////////////////////////////////////////////////////////////////
    public void selectedPos00(View view){
        if (mBoard[0][0].setSymbol(currentSymbol)) { // If box is selected, set current symbol
            togglePlayer();                         // Pass turn to another player
            toggleSymbol(view);                     // Toggle selected symbol
        }
    }

    //////////////////////////////////////////////////////////////////////
    // OnClick method when user selects box (0,1)
    //
    // Params : Void
    // Return : Void
    /////////////////////////////////////////////////////////////////////
    public void selectedPos01(View view){
        if (mBoard[0][1].setSymbol(currentSymbol)){ // If box is selected, set current symbol
            togglePlayer();                        // Pass turn to another player
            toggleSymbol(view);                    // Toggle selected symbol
        }
    }

    //////////////////////////////////////////////////////////////////////
    // OnClick method when user selects box (0,2)
    //
    // Params : Void
    // Return : Void
    /////////////////////////////////////////////////////////////////////
    public void selectedPos02(View view){
        if ( mBoard[0][2].setSymbol(currentSymbol)){ // If box is selected, set current symbol
            togglePlayer();                         // Pass turn to another player
            toggleSymbol(view);                     // Toggle selected symbol
        }
    }

    //////////////////////////////////////////////////////////////////////
    // OnClick method when user selects box (1,0)
    //
    // Params : Void
    // Return : Void
    /////////////////////////////////////////////////////////////////////
    public void selectedPos10(View view){
        if ( mBoard[1][0].setSymbol(currentSymbol)){ // If box is selected, set current symbol
            togglePlayer();                         // Pass turn to another player
            toggleSymbol(view);                     // Toggle selected symbol
        }
    }

    //////////////////////////////////////////////////////////////////////
    // OnClick method when user selects box (1,1)
    //
    // Params : Void
    // Return : Void
    /////////////////////////////////////////////////////////////////////
    public void selectedPos11(View view){
        if (mBoard[1][1].setSymbol(currentSymbol)) { // If box is selected, set current symbol
            togglePlayer();                         // Pass turn to another player
            toggleSymbol(view);                     // Toggle selected symbol
        }
    }

    //////////////////////////////////////////////////////////////////////
    // OnClick method when user selects box (1,2)
    //
    // Params : Void
    // Return : Void
    /////////////////////////////////////////////////////////////////////
    public void selectedPos12(View view){
        if (mBoard[1][2].setSymbol(currentSymbol)) { // If box is selected, set current symbol
            togglePlayer();                         // Pass turn to another player
            toggleSymbol(view);                     // Toggle selected symbol
        }
    }

    //////////////////////////////////////////////////////////////////////
    // OnClick method when user selects box (2,0)
    //
    // Params : Void
    // Return : Void
    /////////////////////////////////////////////////////////////////////
    public void selectedPos20(View view){
        if (mBoard[2][0].setSymbol(currentSymbol)) { // If box is selected, set current symbol
            togglePlayer();                         // Pass turn to another player
            toggleSymbol(view);                     // Toggle selected symbol
        }
    }

    //////////////////////////////////////////////////////////////////////
    // OnClick method when user selects box (2,1)
    //
    // Params : Void
    // Return : Void
    /////////////////////////////////////////////////////////////////////
    public void selectedPos21(View view){
        if (mBoard[2][1].setSymbol(currentSymbol)) { // If box is selected, set current symbol
            togglePlayer();                         // Pass turn to another player
            toggleSymbol(view);                     // Toggle selected symbol
        }
    }

    //////////////////////////////////////////////////////////////////////
    // OnClick method when user selects box (2,2)
    //
    // Params : Void
    // Return : Void
    /////////////////////////////////////////////////////////////////////
    public void selectedPos22(View view){
        if (mBoard[2][2].setSymbol(currentSymbol)) { // If box is selected, set current symbol
            togglePlayer();                         // Pass turn to another player
            toggleSymbol(view);                     // Toggle selected symbol
        }
    }

    //////////////////////////////////////////////////////////////////////
    // This method returns active player, who is in play
    //
    // Params : Void
    // Return : Void
    /////////////////////////////////////////////////////////////////////
    public Player getActivePLayer(){
        // Return active player
        // If no player is active then set Player A active, and return player A
        if (playerA.isActive()){
            return  playerA;
        }else if (playerB.isActive()){
           return playerB;
        }else{
            playerA.setActive();
            return playerA;
        }
    }

    //////////////////////////////////////////////////////////////////////
    // OnClick method when user selects X
    //
    // Params : Void
    // Return : Void
    /////////////////////////////////////////////////////////////////////
    public void xSelected(View view){
        previousSymbol = currentSymbol;
        currentSymbol = Symbol.X_SELECTED;                            // Set current active symbol X
        ImageView xSelectImage = findViewById(R.id.xSelected);        // Get X image and,
        xSelectImage.animate().alpha(0.5f);                           // set it to 0.5 alpha to show it's selected
        ImageView oSelectImage = findViewById(R.id.oSelected);        // Get O image and,
        oSelectImage.animate().alpha(1f);                             // set it to alpha 1 in case
    }

    //////////////////////////////////////////////////////////////////////
    // OnClick method when user selects O
    //
    // Params : Void
    // Return : Void
    /////////////////////////////////////////////////////////////////////
    public void oSelected(View view){
        previousSymbol = currentSymbol;
        currentSymbol = Symbol.O_SELECTED;                     // Set current active symbol O
        playNextMove();
        ImageView oSelectImage = findViewById(R.id.oSelected); // Get O image and,
        oSelectImage.animate().alpha(0.5f);                    // set it to 0.5 alpha to show it's selected
        ImageView xSelectImage = findViewById(R.id.xSelected); // Get X image and,
        xSelectImage.animate().alpha(1f);                      // set it to alpha 1 in case
    }

    private Handler myHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize whole board
        for (int x = 0; x < 3; x++ ){
            for (int y = 0; y < 3; y++){
                mBoard[x][y] = new Box(xImage[x][y], oImage[x][y], x, y);
            }
        }

        // Initialize Players
        playerA = new Player();
        playerB = new Player();

        // Initialize Runnable to process game status (processGameStatus())
        MyRunnable gameStatus = new MyRunnable();
        new Thread(gameStatus).start(); // Start thread


    }

    //////////////////////////////////////////////////////////////////////
    // Define Runnable to call processGameStatus()
    // TO DO: find better solution to run it on thread other than UI
    /////////////////////////////////////////////////////////////////////
    private class MyRunnable implements Runnable{
        int previousFilledBoxes = 0;  // Previous filled boxes
        @Override
        public void run() {
            Handler threadHandler = new Handler(Looper.getMainLooper());

            // Infinite loop
            while(true) {
                threadHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        // Check if filled boxes are changed, to only call processGameStatus() when new move has been made
                        if (mFilledBoxes == (previousFilledBoxes+1)){
                            // Assign filled boxes to previous boxes
                            previousFilledBoxes = mFilledBoxes;

                            // Process game status
                            processGameStatus();
                        }

                        // If game is restarted
                        if (mFilledBoxes == 0)
                            previousFilledBoxes = 0;
                    }
                });

                // Adding 100 ms delay, becuase this runnable runs on UI thread, so
                // without delay it will slowdown app on real device
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
           }
        }
    }
}