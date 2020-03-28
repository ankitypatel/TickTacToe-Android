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

    class Box{
        ImageView xImage;
        ImageView oImage;
        ImageView activeImage;
        boolean boxFilled;
        Symbol symbolSet;

        public Box(int xImageId, int oImageId)
        {
            xImage = findViewById(xImageId);
            oImage = findViewById(oImageId);
            xImage.setAlpha(0f);
            oImage.setAlpha(0f);
            boxFilled = false;
            symbolSet = Symbol.UNDEFINED;
        }

        public void setX() {
            if (!boxFilled) {
                xImage.setAlpha(1f);
                oImage.setAlpha(0f);
                boxFilled = true;
                activeImage = xImage;
                symbolSet = Symbol.X_SELECTED;
                filledBoxes++;
            }
        }

        public void setO(){
            if (!boxFilled) {
                xImage.setAlpha(0f);
                oImage.setAlpha(1f);
                boxFilled = true;
                activeImage = oImage;
                symbolSet = Symbol.O_SELECTED;
                filledBoxes++;
            }
        }

        public boolean isBoxFilled()
        {
            return boxFilled;
        }

        public ImageView getActiveImage()
        {
            return activeImage;
        }

        public Symbol getSymbol()
        {
            return symbolSet;
        }

        public void setSymbol(Symbol symbol)
        {
            if (!boxFilled){
                if (Symbol.X_SELECTED == symbol){
                    setX();
                } else if (Symbol.O_SELECTED == symbol){
                    setO();
                }
            }
        }

        public void resetBox()
        {
            boxFilled = false;
            symbolSet = Symbol.UNDEFINED;
            xImage.setAlpha(0.0f);
            oImage.setAlpha(0.0f);
        }
    }

    class Player{
        ///boolean xSelected;
        // boolean oSelected;
        Symbol symbol;
        boolean isActive;
        int     score;

        public Player(){
            symbol = Symbol.UNDEFINED;
            isActive = false;
            score = 0;
        }

        public boolean isActive() {
            return isActive;
        }

        public void selectSymbol(Symbol selectedSymbol){
            symbol = selectedSymbol;
        }

        public void setActive() {
            isActive = true;
        }

        public void setInactive(){
            isActive = false;
        }

        public Symbol isSymbolSelected() {
            return symbol;
        }

        public void resetPlayer()
        {
            symbol = Symbol.UNDEFINED;
            isActive = false;
        }
    }

    public enum Symbol{
        X_SELECTED,
        O_SELECTED,
        UNDEFINED
    };

    // Create object for each box in board (or frame)
    // Row 0
    Box boxAtPos00;
    Box boxAtPos01;
    Box boxAtPos02;

    // Row 1
    Box boxAtPos10;
    Box boxAtPos11;
    Box boxAtPos12;

    //Row 2
    Box boxAtPos20;
    Box boxAtPos21;
    Box boxAtPos22;

    int[][] xImage = { {R.id.ximage00, R.id.ximage01, R.id.ximage02},
                       {R.id.ximage10, R.id.ximage11, R.id.ximage12},
                       {R.id.ximage20, R.id.ximage21, R.id.ximage22} };

    int[][] oImage = { {R.id.oimage00, R.id.oimage01, R.id.oimage02},
                       {R.id.oimage10, R.id.oimage11, R.id.oimage12},
                       {R.id.oimage20, R.id.oimage21, R.id.oimage22} };

    Box[][] board = { {boxAtPos00, boxAtPos01, boxAtPos02},
                      {boxAtPos10, boxAtPos11, boxAtPos12},
                      {boxAtPos20, boxAtPos21, boxAtPos22} };

    boolean gameCompleted = false;
    boolean gameIsDraw = false;
    boolean row0 = false;
    boolean row1 = false;
    boolean row2 = false;
    boolean column0 = false;
    boolean column1 = false;
    boolean column2 = false;
    boolean diag0 = false;
    boolean diag1 = false;
    int filledBoxes = 0;

    // Create player object
    Player playerA = null;
    Player playerB = null;

    public Player getPlayer(Symbol symbol )
    {
        // If symbols aren't assigned to player, it means that game isn't started
        if (symbol.UNDEFINED == playerA.isSymbolSelected() && symbol.UNDEFINED == playerB.isSymbolSelected()){
            playerA.selectSymbol(symbol);
            return playerA;
        } else if (symbol.UNDEFINED == playerB.isSymbolSelected()){
            playerB.selectSymbol(symbol);
            return playerB;
        } else if (symbol == playerA.isSymbolSelected())
        {
            return playerA;
        } else if (symbol == playerB.isSymbolSelected())
        {
            return playerB;
        } else
        {
            return null;
        }
    }

    public Symbol getSymbol(){
        Symbol symbolToDisplay;
        if (playerA.isActive())
        {
            symbolToDisplay = playerA.isSymbolSelected();
        } else if (playerB.isActive())
        {
            symbolToDisplay = playerB.isSymbolSelected();
        } else
        {
            symbolToDisplay = Symbol.UNDEFINED;
        }

        return symbolToDisplay;
    }

    public void toggleWiningBoxes(final ImageView image0, final ImageView image1, final ImageView image2)
    {
        image0.animate().rotationYBy(720).setDuration(1000).start();
        image1.animate().rotationYBy(720).setDuration(1000).start();
        image2.animate().rotationYBy(720).setDuration(1000).start();
        Log.i("INFO", "animate image 0");
    }

    public void displayWiningBoxes()
    {
        if (row0) {
            toggleWiningBoxes( board[0][0].getActiveImage(), board[0][1].getActiveImage(), board[0][2].getActiveImage());
        } else if (row1) {
            toggleWiningBoxes( board[1][0].getActiveImage(), board[1][1].getActiveImage(), board[1][2].getActiveImage());
        } else if (row2) {
            toggleWiningBoxes( board[2][0].getActiveImage(), board[2][1].getActiveImage(), board[2][2].getActiveImage());
        } else if (column0) {
            toggleWiningBoxes( board[0][0].getActiveImage(), board[1][0].getActiveImage(), board[2][0].getActiveImage());
        } else if (column1) {
            toggleWiningBoxes( board[0][1].getActiveImage(), board[1][1].getActiveImage(), board[2][1].getActiveImage());
        } else if (column2) {
            toggleWiningBoxes( board[0][2].getActiveImage(), board[1][2].getActiveImage(), board[2][2].getActiveImage());
        } else if (diag0) {
            toggleWiningBoxes( board[0][0].getActiveImage(), board[1][1].getActiveImage(), board[2][2].getActiveImage());
        } else if (diag1) {
            toggleWiningBoxes( board[0][2].getActiveImage(), board[1][1].getActiveImage(), board[2][0].getActiveImage());
        }
    }

    public void restartGame()
    {
        gameCompleted = false;
        gameIsDraw = false;
        row0 = false;
        row1 = false;
        row2 = false;
        column0 = false;
        column1 = false;
        column2 = false;
        diag0 = false;
        diag1 = false;
        filledBoxes = 0;

        ImageView oSelectImage = findViewById(R.id.oSelected);
        ImageView xSelectImage = findViewById(R.id.xSelected);
        xSelectImage.animate().alpha(1f).start();
        oSelectImage.animate().alpha(1f).start();

        for (int x = 0; x < 3; x++){
            for (int y = 0; y < 3; y++){
                board[x][y].resetBox();
            }
        }

        // Players
        playerA.resetPlayer();
        playerB.resetPlayer();
        Log.i("INFO", "game restarted!!");
    }

    public void processGameStatus()
    {
        Log.i("INFO", "processGameStatus");
        // Check 1st row and column
        if ( ( column0 = (Symbol.UNDEFINED != board[0][0].getSymbol()) && ( board[1][0].getSymbol() == board[0][0].getSymbol()) && (board[2][0].getSymbol() == board[0][0].getSymbol()) )||
             ( row0 = (Symbol.UNDEFINED != board[0][0].getSymbol()) && ( board[0][1].getSymbol() == board[0][0].getSymbol()) && (board[0][2].getSymbol() == board[0][0].getSymbol()) ) ) {
            gameCompleted = true;
            Log.i("INFO", "row0"+row0+"column0"+column0);
        } // Check 2nd row and column
        else if ( (column1 = (Symbol.UNDEFINED != board[1][1].getSymbol()) && ( board[0][1].getSymbol() == board[1][1].getSymbol()) && (board[2][1].getSymbol() == board[1][1].getSymbol())) ||
                  (row1 = (Symbol.UNDEFINED != board[1][1].getSymbol()) && ( board[1][0].getSymbol() == board[1][1].getSymbol()) && (board[1][2].getSymbol() == board[1][1].getSymbol()))) {
            gameCompleted = true;
            Log.i("INFO", "row1"+row0+"column0"+column1);
        } // check 3rd row and column
        else if ( (column2 = (Symbol.UNDEFINED != board[2][2].getSymbol()) && ( board[0][2].getSymbol() == board[2][2].getSymbol()) && (board[1][2].getSymbol() == board[2][2].getSymbol())) ||
                  (row2 = (Symbol.UNDEFINED != board[2][2].getSymbol()) && ( board[2][0].getSymbol() == board[2][2].getSymbol()) && (board[2][1].getSymbol() == board[2][2].getSymbol())) ) {
            gameCompleted = true;
            Log.i("INFO", "row2"+row2+"column2"+column2);
        } // check both diagonals
        else if ( (diag0 = (Symbol.UNDEFINED != board[1][1].getSymbol()) && ( board[0][0].getSymbol() == board[1][1].getSymbol()) && (board[2][2].getSymbol() == board[1][1].getSymbol()) )||
                  (diag1 = (Symbol.UNDEFINED != board[1][1].getSymbol()) && ( board[0][2].getSymbol() == board[1][1].getSymbol()) && (board[2][0].getSymbol() == board[1][1].getSymbol()) )) {
            gameCompleted = true;
            Log.i("INFO", "diag0"+diag0+"diag1"+diag1);
        }

        if (gameCompleted) {
            Log.i("INFO", "game completed!!");
            displayWiningBoxes();
        } else{
            if (9 == filledBoxes)
            {
                gameIsDraw = true;
            }
        }

        if ( gameCompleted || gameIsDraw ){
            restartGame();
        }
    }

    public void selectedPos00(View view){
        Symbol symbolAt = getSymbol();

        board[0][0].setSymbol(symbolAt);

        if (Symbol.X_SELECTED == symbolAt) {
            oSelected(view);
        } else  if (Symbol.O_SELECTED == symbolAt) {
            xSelected(view);
        }

    }

    public void selectedPos01(View view){
        Symbol symbolAt = getSymbol();
        board[0][1].setSymbol(symbolAt);
        if (Symbol.X_SELECTED == symbolAt) {
            oSelected(view);
        } else  if (Symbol.O_SELECTED == symbolAt) {
            xSelected(view);
        }
    }

    public void selectedPos02(View view){
        Symbol symbolAt = getSymbol();

        board[0][2].setSymbol(symbolAt);
        if (Symbol.X_SELECTED == symbolAt) {
            oSelected(view);
        } else  if (Symbol.O_SELECTED == symbolAt) {
            xSelected(view);
        }
    }

    public void selectedPos10(View view){
        Symbol symbolAt = getSymbol();

        board[1][0].setSymbol(symbolAt);
        if (Symbol.X_SELECTED == symbolAt) {
            oSelected(view);
        } else  if (Symbol.O_SELECTED == symbolAt) {
            xSelected(view);
        }

    }

    public void selectedPos11(View view){
        Symbol symbolAt = getSymbol();

        board[1][1].setSymbol(symbolAt);
        if (Symbol.X_SELECTED == symbolAt) {
            oSelected(view);
        } else  if (Symbol.O_SELECTED == symbolAt) {
            xSelected(view);
        }
    }

    public void selectedPos12(View view){
        Symbol symbolAt = getSymbol();

        board[1][2].setSymbol(symbolAt);
        if (Symbol.X_SELECTED == symbolAt) {
            oSelected(view);
        } else  if (Symbol.O_SELECTED == symbolAt) {
            xSelected(view);
        }
    }

    public void selectedPos20(View view){
        Symbol symbolAt = getSymbol();

        board[2][0].setSymbol(symbolAt);
        if (Symbol.X_SELECTED == symbolAt) {
            oSelected(view);
        } else  if (Symbol.O_SELECTED == symbolAt) {
            xSelected(view);
        }
    }

    public void selectedPos21(View view){
        Symbol symbolAt = getSymbol();

        board[2][1].setSymbol(symbolAt);
        if (Symbol.X_SELECTED == symbolAt) {
            oSelected(view);
        } else  if (Symbol.O_SELECTED == symbolAt) {
            xSelected(view);
        }
    }

    public void selectedPos22(View view){
        Symbol symbolAt = getSymbol();

        board[2][2].setSymbol(symbolAt);
        if (Symbol.X_SELECTED == symbolAt) {
            oSelected(view);
        } else  if (Symbol.O_SELECTED == symbolAt) {
            xSelected(view);
        }
    }

    public void xSelected(View view){
        Player playerWithX = getPlayer(Symbol.X_SELECTED);
        Player playerWithO = getPlayer(Symbol.O_SELECTED);
        if (null != playerWithX && null != playerWithO) {
            playerWithX.setActive();
            playerWithO.setInactive();
            ImageView xSelectImage = findViewById(R.id.xSelected);
            xSelectImage.animate().alpha(0.5f);
            ImageView oSelectImage = findViewById(R.id.oSelected);
            oSelectImage.animate().alpha(1f);
        }
    }

    public void oSelected(View view){
        Player playerWithX = getPlayer(Symbol.X_SELECTED);
        Player playerWithO = getPlayer(Symbol.O_SELECTED);
        if (null != playerWithX && null != playerWithO) {
            playerWithO.setActive();
            playerWithX.setInactive();
            ImageView oSelectImage = findViewById(R.id.oSelected);
            oSelectImage.animate().alpha(0.5f);
            ImageView xSelectImage = findViewById(R.id.xSelected);
            xSelectImage.animate().alpha(1f);
        }
    }
    private Handler myHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        for (int x = 0; x < 3; x++ ){
            for (int y = 0; y < 3; y++){
                board[x][y] = new Box(xImage[x][y], oImage[x][y]);
            }
        }

        // Players
        playerA = new Player();
        playerB = new Player();

        MyRunnable gameStatus = new MyRunnable();
        new Thread(gameStatus).start();
    }

    private class MyRunnable implements Runnable{
        int previousFilledBoxes = 0;
        @Override
        public void run() {
            Handler threadHandler = new Handler(Looper.getMainLooper());
            while(true) {
                threadHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        Log.i("INFO", "previousFilledBoxes "+previousFilledBoxes+"filledBoxes "+filledBoxes);
                        if (filledBoxes == (previousFilledBoxes+1)){
                            previousFilledBoxes = filledBoxes;
                            processGameStatus();
                        }

                        if (filledBoxes == 0)
                            previousFilledBoxes = 0;
                    }
                });
                /*runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        processGameStatus();
                    }
                });*/
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
           }
        }
    }
}
/*
    private class MyAsyncTask extends AsyncTask<Integer,Integer,String>{
        @Override
        protected String doInBackground(Integer... integers) {
            while(true) {
                processGameStatus();
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    break;
                }
            }
            return "finished";
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if ( gameCompleted || gameIsDraw ){
                restartGame();
            }
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            displayWiningBoxes();

        }
    }
}*/
