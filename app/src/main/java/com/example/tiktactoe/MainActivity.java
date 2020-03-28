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

    public enum Symbol{
        X_SELECTED,
        O_SELECTED,
        UNDEFINED
    };

    Symbol[][] boardStatus = { {Symbol.UNDEFINED, Symbol.UNDEFINED, Symbol.UNDEFINED},
                               {Symbol.UNDEFINED, Symbol.UNDEFINED, Symbol.UNDEFINED},
                               {Symbol.UNDEFINED, Symbol.UNDEFINED, Symbol.UNDEFINED} };
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

    class Box{
        ImageView xImage;
        ImageView oImage;
        ImageView activeImage;
        boolean boxFilled;
        int x,y;

        public Box(int xImageId, int oImageId, int posX, int posY)
        {
            xImage = findViewById(xImageId);
            oImage = findViewById(oImageId);
            xImage.setAlpha(0f);
            oImage.setAlpha(0f);
            boxFilled = false;
            x = posX;
            y = posY;
        }

        public void setX() {
            if (!boxFilled) {
                xImage.setAlpha(1f);
                oImage.setAlpha(0f);
                boxFilled = true;
                activeImage = xImage;
                boardStatus[x][y] = Symbol.X_SELECTED;
                Log.i("INFO", "x at"+ x + y);
                filledBoxes++;
            }
        }

        public void setO(){
            if (!boxFilled) {
                xImage.setAlpha(0f);
                oImage.setAlpha(1f);
                boxFilled = true;
                activeImage = oImage;
                boardStatus[x][y] = Symbol.O_SELECTED;
                filledBoxes++;
                Log.i("INFO", "o at"+ x + y);
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

        public void resetBox()
        {
            boxFilled = false;
            xImage.animate().alpha(0f).scaleX(1).scaleY(1).start();
            oImage.animate().alpha(0f).scaleX(1).scaleY(1).start();
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

    // Create object for each box in board (or frame)
    // Row 0
    Box boxAtPos00 = null;
    Box boxAtPos01 = null;
    Box boxAtPos02 = null;

    // Row 1
    Box boxAtPos10 = null;
    Box boxAtPos11 = null;
    Box boxAtPos12 = null;

    //Row 2
    Box boxAtPos20 = null;
    Box boxAtPos21 = null;
    Box boxAtPos22 = null;

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
            toggleWiningBoxes( boxAtPos00.getActiveImage(), boxAtPos01.getActiveImage(), boxAtPos02.getActiveImage());
        } else if (row1) {
            toggleWiningBoxes( boxAtPos10.getActiveImage(), boxAtPos11.getActiveImage(), boxAtPos12.getActiveImage());
        } else if (row2) {
            toggleWiningBoxes( boxAtPos20.getActiveImage(), boxAtPos21.getActiveImage(), boxAtPos22.getActiveImage());
        } else if (column0) {
            toggleWiningBoxes( boxAtPos00.getActiveImage(), boxAtPos10.getActiveImage(), boxAtPos20.getActiveImage());
        } else if (column1) {
            toggleWiningBoxes( boxAtPos01.getActiveImage(), boxAtPos11.getActiveImage(), boxAtPos21.getActiveImage());
        } else if (column2) {
            toggleWiningBoxes( boxAtPos02.getActiveImage(), boxAtPos12.getActiveImage(), boxAtPos22.getActiveImage());
        } else if (diag0) {
            toggleWiningBoxes( boxAtPos00.getActiveImage(), boxAtPos11.getActiveImage(), boxAtPos22.getActiveImage());
        } else if (diag1) {
            toggleWiningBoxes( boxAtPos02.getActiveImage(), boxAtPos11.getActiveImage(), boxAtPos20.getActiveImage());
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

        // reset board
        for (int x = 0; x < 3 ; x++) {
            for (int y = 0; y < 3 ; y++) {
                boardStatus[x][y] = Symbol.UNDEFINED;
            }
        }

        ImageView oSelectImage = findViewById(R.id.oSelected);
        ImageView xSelectImage = findViewById(R.id.xSelected);
        xSelectImage.animate().alpha(1f).start();
        oSelectImage.animate().alpha(1f).start();

        // Row 0
        boxAtPos00.resetBox();
        boxAtPos01.resetBox();
        boxAtPos02.resetBox();

        // Row 1
        boxAtPos10.resetBox();
        boxAtPos11.resetBox();
        boxAtPos12.resetBox();

        // Row 2
        boxAtPos20.resetBox();
        boxAtPos21.resetBox();
        boxAtPos22.resetBox();

        // Players
        playerA.resetPlayer();
        playerB.resetPlayer();
        Log.i("INFO", "game restarted!!");
    }

    public void processGameStatus()
    {
        Log.i("INFO", "processGameStatus");
        // Check 1st row and column
        if ( ( column0 = (Symbol.UNDEFINED != boardStatus[0][0]) && ( boardStatus[1][0] == boardStatus[0][0]) && (boardStatus[2][0] == boardStatus[0][0]) )||
             ( row0 = (Symbol.UNDEFINED != boardStatus[0][0]) && ( boardStatus[0][1] == boardStatus[0][0]) && (boardStatus[0][2] == boardStatus[0][0]) ) ) {
            gameCompleted = true;
            Log.i("INFO", "row0"+row0+"column0"+column0);
        } // Check 2nd row and column
        else if ( (column1 = (Symbol.UNDEFINED != boardStatus[1][1]) && ( boardStatus[0][1] == boardStatus[1][1]) && (boardStatus[2][1] == boardStatus[1][1])) ||
                  (row1 = (Symbol.UNDEFINED != boardStatus[1][1]) && ( boardStatus[1][0] == boardStatus[1][1]) && (boardStatus[1][2] == boardStatus[1][1]))) {
            gameCompleted = true;
            Log.i("INFO", "row1"+row0+"column0"+column1);
        } // check 3rd row and column
        else if ( (column2 = (Symbol.UNDEFINED != boardStatus[2][2]) && ( boardStatus[0][2] == boardStatus[2][2]) && (boardStatus[1][2] == boardStatus[2][2])) ||
                  (row2 = (Symbol.UNDEFINED != boardStatus[2][2]) && ( boardStatus[2][0] == boardStatus[2][2]) && (boardStatus[2][1] == boardStatus[2][2])) ) {
            gameCompleted = true;
            Log.i("INFO", "row2"+row2+"column2"+column2);
        } // check both diagonals
        else if ( (diag0 = (Symbol.UNDEFINED != boardStatus[1][1]) && ( boardStatus[0][0] == boardStatus[1][1]) && (boardStatus[2][2] == boardStatus[1][1]) )||
                  (diag1 = (Symbol.UNDEFINED != boardStatus[1][1]) && ( boardStatus[0][2] == boardStatus[1][1]) && (boardStatus[2][0] == boardStatus[1][1]) )) {
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

        if (!boxAtPos00.isBoxFilled()) {
            if (Symbol.X_SELECTED == symbolAt) {
                boxAtPos00.setX();
                oSelected(view);
            } else  if (Symbol.O_SELECTED == symbolAt) {
                boxAtPos00.setO();
                xSelected(view);
            }
        }
    }

    public void selectedPos01(View view){
        Symbol symbolAt = getSymbol();

        if (!boxAtPos01.isBoxFilled()) {
            if (Symbol.X_SELECTED == symbolAt) {
                boxAtPos01.setX();
                oSelected(view);
            } else  if (Symbol.O_SELECTED == symbolAt) {
                boxAtPos01.setO();
                xSelected(view);
            }
        }
    }

    public void selectedPos02(View view){
        Symbol symbolAt = getSymbol();

        if (!boxAtPos02.isBoxFilled()) {
            if (Symbol.X_SELECTED == symbolAt) {
                boxAtPos02.setX();
                oSelected(view);
            } else  if (Symbol.O_SELECTED == symbolAt) {
                boxAtPos02.setO();
                xSelected(view);
            }
        }
    }

    public void selectedPos10(View view){
        Symbol symbolAt = getSymbol();

        if (!boxAtPos10.isBoxFilled()) {
            if (Symbol.X_SELECTED == symbolAt) {
                boxAtPos10.setX();
                oSelected(view);
            } else  if (Symbol.O_SELECTED == symbolAt) {
                boxAtPos10.setO();
                xSelected(view);
            }
        }

    }

    public void selectedPos11(View view){
        Symbol symbolAt = getSymbol();

        if (!boxAtPos11.isBoxFilled()) {
            if (Symbol.X_SELECTED == symbolAt) {
                boxAtPos11.setX();
                oSelected(view);
            } else  if (Symbol.O_SELECTED == symbolAt) {
                boxAtPos11.setO();
                xSelected(view);
            }
        }
    }

    public void selectedPos12(View view){
        Symbol symbolAt = getSymbol();

        if (!boxAtPos12.isBoxFilled()) {
            if (Symbol.X_SELECTED == symbolAt) {
                boxAtPos12.setX();
                oSelected(view);
            } else  if (Symbol.O_SELECTED == symbolAt) {
                boxAtPos12.setO();
                xSelected(view);
            }
        }
    }

    public void selectedPos20(View view){
        Symbol symbolAt = getSymbol();

        if (!boxAtPos20.isBoxFilled()) {
            if (Symbol.X_SELECTED == symbolAt) {
                boxAtPos20.setX();
                oSelected(view);
            } else  if (Symbol.O_SELECTED == symbolAt) {
                boxAtPos20.setO();
                xSelected(view);
            }
        }
    }

    public void selectedPos21(View view){
        Symbol symbolAt = getSymbol();

        if (!boxAtPos21.isBoxFilled()) {
            if (Symbol.X_SELECTED == symbolAt) {
                boxAtPos21.setX();
                oSelected(view);
            } else  if (Symbol.O_SELECTED == symbolAt) {
                boxAtPos21.setO();
                xSelected(view);
            }
        }
    }

    public void selectedPos22(View view){
        Symbol symbolAt = getSymbol();

        if (!boxAtPos22.isBoxFilled()) {
            if (Symbol.X_SELECTED == symbolAt) {
                boxAtPos22.setX();
                oSelected(view);
            } else if (Symbol.O_SELECTED == symbolAt) {
                boxAtPos22.setO();
                xSelected(view);
            }
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
        // Row 0
        boxAtPos00 = new Box(R.id.ximage00,R.id.oimage00, 0, 0);
        boxAtPos01 = new Box(R.id.ximage01,R.id.oimage01, 0,1);
        boxAtPos02 = new Box(R.id.ximage02,R.id.oimage02, 0,2);

        // Row 1
        boxAtPos10 = new Box(R.id.ximage10,R.id.oimage10, 1,0);
        boxAtPos11 = new Box(R.id.ximage11,R.id.oimage11, 1,1);
        boxAtPos12 = new Box(R.id.ximage12,R.id.oimage12, 1,2);

        // Row 2
        boxAtPos20 = new Box(R.id.ximage20,R.id.oimage20,2,0);
        boxAtPos21 = new Box(R.id.ximage21,R.id.oimage21,2,1);
        boxAtPos22 = new Box(R.id.ximage22,R.id.oimage22,2,2);

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
