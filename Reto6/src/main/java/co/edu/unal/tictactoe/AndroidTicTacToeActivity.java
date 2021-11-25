package co.edu.unal.tictactoe;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class AndroidTicTacToeActivity extends AppCompatActivity {

    private TicTacToeGame mGame;

    private Button mBoardButtons[];
    static final int DIALOG_DIFFICULTY_ID = 0;
    static final int DIALOG_QUIT_ID = 1;
    private BoardView mBoardView;
    private boolean mGameOver;
    MediaPlayer mHumanMediaPlayer;
    MediaPlayer mComputerMediaPlayer;
    Handler handler = new Handler();
    private int mHumanWins = 0;
    private int mComputerWins = 0;
    private int mTies = 0;
    private char mGoFirst = TicTacToeGame.HUMAN_PLAYER;
    // Various text displayed
    private TextView mInfoTextView;
    private TextView mHumanScoreTextView;
    private TextView mComputerScoreTextView;
    private TextView mTieScoreTextView;
    private SharedPreferences mPrefs;
    private Button ButtonNewGame;
    private Button ButtonReiniciar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportActionBar().hide();

        mBoardButtons = new Button[TicTacToeGame.BOARD_SIZE];
        mInfoTextView = (TextView) findViewById(R.id.information);
        mGame = new TicTacToeGame();
        mBoardView = (BoardView) findViewById(R.id.board);
        mBoardView.setGame(mGame);
        mHumanScoreTextView = (TextView) findViewById(R.id.player_score);
        mComputerScoreTextView = (TextView) findViewById(R.id.computer_score);
        mTieScoreTextView = (TextView) findViewById(R.id.tie_score);
        // Listen for touches on the board
        mBoardView.setOnTouchListener(mTouchListener);
        ButtonNewGame = findViewById(R.id.NewGame);
        ButtonReiniciar = findViewById(R.id.reiniciar_puntaje);
        mPrefs = getSharedPreferences("ttt_prefs", MODE_PRIVATE);
        //mPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        mHumanWins = mPrefs.getInt("mHumanWins", 0);
        mComputerWins = mPrefs.getInt("mComputerWins", 0);
        mTies = mPrefs.getInt("mTies", 0);

        ButtonNewGame.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startNewGame();
            }
        });

       ButtonReiniciar.setOnClickListener(v -> {
           mHumanWins = 0;
           mComputerWins = 0;
           mTies = 0;
           displayScores();
       } );

        if (savedInstanceState == null) {
            startNewGame();
        }
        else {
// Restore the game's state
            mGame.setBoardState(savedInstanceState.getCharArray("board"));
            mGameOver = savedInstanceState.getBoolean("mGameOver");
            mInfoTextView.setText(savedInstanceState.getCharSequence("info"));
            mHumanWins = savedInstanceState.getInt("mHumanWins");
            mComputerWins = savedInstanceState.getInt("mComputerWins");
            mTies = savedInstanceState.getInt("mTies");
            mGoFirst = savedInstanceState.getChar("mGoFirst");
        }
        displayScores();
    }

    private void startNewGame() {
        mGame.clearBoard();
        mBoardView.invalidate();

// Human goes first
        mInfoTextView.setText(R.string.first_human);
    }

    private class ButtonClickListener implements View.OnClickListener {
        int location;
        public ButtonClickListener(int location) {
            this.location = location;
        }
        public void onClick(View view) {
            if (mBoardButtons[location].isEnabled()) {
                setMove(TicTacToeGame.HUMAN_PLAYER, location);
// If no winner yet, let the computer make a move
                int winner = mGame.checkForWinner();
                if (winner == 0) {
                    mInfoTextView.setText(R.string.turn_computer);
                    int move = mGame.getComputerMove();
                    setMove(TicTacToeGame.COMPUTER_PLAYER, move);
                    winner = mGame.checkForWinner();
                }
                if (winner == 0){
                    mInfoTextView.setText(R.string.turn_human);
                    mHumanMediaPlayer.start(); // Play the sound effect
                }
                else if (winner == 1) {
                    mTies++;
                    displayScores();
                    mInfoTextView.setText(R.string.result_tie);
                }

                else if (winner == 2) {
                    mHumanWins++;
                    displayScores();
                    mInfoTextView.setText(R.string.result_human_wins);
                }
                else {
                    mComputerWins++;
                    displayScores();
                    mInfoTextView.setText(R.string.result_computer_wins);
                }
            }
        }
    }

    // Listen for touches on the board
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        int location;
        public boolean onTouch(View v, MotionEvent event) {
// Determine which cell was touched
            int col = (int) event.getX() / mBoardView.getBoardCellWidth();
            int row = (int) event.getY() / mBoardView.getBoardCellHeight();
            int pos = row * 3 + col;
            if (!mGameOver && setMove(TicTacToeGame.HUMAN_PLAYER, pos)){
// If no winner yet, let the computer make a move
                int winner = mGame.checkForWinner();
                if (winner == 0) {
                    mInfoTextView.setText(R.string.turn_computer);

                    handler.postDelayed(new Runnable() {
                        public void run() {
                            mComputerMediaPlayer.start(); // Play the sound effect
                        }
                    }, 300);
                    int move = mGame.getComputerMove();
                    setMove(TicTacToeGame.COMPUTER_PLAYER, move);
                    winner = mGame.checkForWinner();
                }
                if (winner == 0){
                    mInfoTextView.setText(R.string.turn_human);
                    mHumanMediaPlayer.start(); // Play the sound effect
                }
                else if (winner == 1) {
                    mTies++;
                    displayScores();
                    mInfoTextView.setText(R.string.result_tie);
                }

                else if (winner == 2) {
                    mHumanWins++;
                    displayScores();
                    mInfoTextView.setText(R.string.result_human_wins);
                }
                else {
                    mComputerWins++;
                    displayScores();
                    mInfoTextView.setText(R.string.result_computer_wins);
                }
            }
// So we aren't notified of continued events when finger is moved
            return false;
        }
    };

    private boolean setMove(char player, int location) {
        if (mGame.setMove(player, location)) {
            mBoardView.invalidate(); // Redraw the board
            return true;
        }
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.new_game:
                startNewGame();
                return true;
            case R.id.ai_difficulty:
                showDialog(DIALOG_DIFFICULTY_ID);
                return true;
            case R.id.quit:
                showDialog(DIALOG_QUIT_ID);
                return true;
        }
        return false;
    }

    public void sendMessage(View view) {
        startNewGame();
    }

    protected Dialog onCreateDialog(int id) {
        Dialog dialog = null;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        switch(id) {
            case DIALOG_DIFFICULTY_ID:
                builder.setTitle(R.string.difficulty_choose);
                final CharSequence[] levels = {
                        getResources().getString(R.string.difficulty_easy),
                        getResources().getString(R.string.difficulty_harder),
                        getResources().getString(R.string.difficulty_expert)};
                int selected = 0;
                builder.setSingleChoiceItems(levels, selected,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int item) {
                                dialog.dismiss(); // Close dialog
                                switch(item){
                                    case 1:
                                        mGame.setDifficultyLevel(TicTacToeGame.DifficultyLevel.Harder);
                                        break;
                                    case 2:
                                        mGame.setDifficultyLevel(TicTacToeGame.DifficultyLevel.Expert);
                                        break;
                                    default:
                                        mGame.setDifficultyLevel(TicTacToeGame.DifficultyLevel.Easy);
                                        break;
                                }
                                Toast.makeText(getApplicationContext(), levels[item],

                                        Toast.LENGTH_SHORT).show();

                            }
                        });
                dialog = builder.create();
                break;
            case DIALOG_QUIT_ID:
// Create the quit confirmation dialog
                builder.setMessage(R.string.quit_question)
                        .setCancelable(false)
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                AndroidTicTacToeActivity.this.finish();
                            }
                        })
                        .setNegativeButton(R.string.no, null);
                dialog = builder.create();
                break;
        }
        return dialog;
    }

    @Override
    protected void onResume() {
        super.onResume();
        mHumanMediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.human);
        mComputerMediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.pc);
    }
    @Override
    protected void onPause() {
        super.onPause();
        mHumanMediaPlayer.release();
        mComputerMediaPlayer.release();
    }
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putCharArray("board", mGame.getBoardState());
        outState.putBoolean("mGameOver", mGameOver);
        outState.putInt("mHumanWins", Integer.valueOf(mHumanWins));
        outState.putInt("mComputerWins", Integer.valueOf(mComputerWins));
        outState.putInt("mTies", Integer.valueOf(mTies));
        outState.putCharSequence("info", mInfoTextView.getText());
        outState.putChar("mGoFirst", mGoFirst);
    }

    private void displayScores() {
        mHumanScoreTextView.setText(Integer.toString(mHumanWins));
        mComputerScoreTextView.setText(Integer.toString(mComputerWins));
        mTieScoreTextView.setText(Integer.toString(mTies));
    }

    @Override
    protected void onStop() {
        super.onStop();
// Save the current scores
        SharedPreferences.Editor ed = mPrefs.edit();
        ed.putInt("mHumanWins", mHumanWins);
        ed.putInt("mComputerWins", mComputerWins);
        ed.putInt("mTies", mTies);
        ed.commit();
    }

}