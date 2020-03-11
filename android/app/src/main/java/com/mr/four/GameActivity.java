package com.mr.four;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.*;

import android.animation.*;
import android.annotation.*;
import android.content.*;
import android.graphics.drawable.*;
import android.os.*;
import android.preference.*;
import android.util.*;
import android.view.*;
import android.view.animation.*;
import android.widget.*;

public class GameActivity extends AppCompatActivity implements Runnable {

	private final String PREF_LEVEL = "PrefLevel";

	private final Game game = new Game();

	private byte gameState;
	private byte color;

	private TextView level;
	private TextView message;
	private View busy;
	private final ImageView[][] images = new ImageView[7][7];

	private final Drawable[] drawables = new Drawable[3];
	private final int[] colors = new int[3];

	private ValueAnimator winAnimation;

	private final DisplayMetrics displayMetrics = new DisplayMetrics();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		((WindowManager) getSystemService(WINDOW_SERVICE)).getDefaultDisplay().getMetrics(displayMetrics);
		game.maxLevel = (byte) PreferenceManager.getDefaultSharedPreferences(this).getInt(PREF_LEVEL, 1);
		setContentView(R.layout.game);
		level = findViewById(R.id.level);
		message = findViewById(R.id.message);
		busy = findViewById(R.id.busy);
		ViewGroup box = findViewById(R.id.box);
		for (int row = 0; row < 7; row++) {
			ViewGroup rowView = (ViewGroup) box.getChildAt(6 - row);
			for (int column = 0; column < 7; column++)
				images[row][column] = (ImageView) rowView.getChildAt(column);
		}
		drawables[Game.WHITE] = ContextCompat.getDrawable(this, R.drawable.naught);
		drawables[Game.BLACK] = ContextCompat.getDrawable(this, R.drawable.cross);
		colors[Game.WHITE] = ContextCompat.getColor(this, R.color.f);
		colors[Game.BLACK] = ContextCompat.getColor(this, R.color.m);
		winAnimation = ValueAnimator.ofFloat(1F, 0F);
		winAnimation.setDuration(500);
		winAnimation.setRepeatMode(ValueAnimator.REVERSE);
		winAnimation.setRepeatCount(ValueAnimator.INFINITE);
		//
		gameState = 4;
		color = Game.WHITE;
		setMessage();
	}

	private static final int[] messageText = {
			R.string.msg_turn,
			R.string.msg_won,
			R.string.msg_lost,
			R.string.msg_draw,
			R.string.msg_welcome
	};

	@SuppressLint("SetTextI18n")
	private void setMessage() {
		if (gameState == Game.RUNNING && color == Game.BLACK) {
			message.setVisibility(View.INVISIBLE);
			busy.setVisibility(View.VISIBLE);
		} else {
			message.setText(messageText[gameState]);
			message.setVisibility(View.VISIBLE);
			busy.setVisibility(View.INVISIBLE);
		}
		level.setText(Integer.toString(game.maxLevel));
	}

	private void nextMove(byte column) {
		drop(column, color);
		game.drop(column, color);
		gameState = winner(column);
		switch (gameState) {
			case Game.RUNNING:
				color = Game.opposite(color);
				setMessage();
				if (color == Game.BLACK)
					new SearchTask().execute();
				break;
			case Game.WHITE:
				level.postDelayed(this, 600);
				setMessage();
				game.maxLevel++;
				saveLevel();
				break;
			case Game.BLACK:
				level.postDelayed(this, 600);
				setMessage();
				if (game.maxLevel > 0) {
					game.maxLevel--;
					saveLevel();
				}
				break;
			default:
				setMessage();
		}
	}

	private byte winner(byte column) {
		try {
			return game.winner(column);
		} catch(Exception ignored) { }
		return 5;
	}

	private ImageView[] flashing = new ImageView[49];
	private int coordinateNumber;

	public void run() {
		coordinateNumber = 0;
		try {
			for (byte row = 0; row < 7; row++)
				for (byte column = 0; column < 7; column++)
					if (game.winner(row, column) == gameState)
						flashing[coordinateNumber++] = images[row][column];
			winAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
				@Override
				public void onAnimationUpdate(ValueAnimator animator) {
					for (int i = 0; i < coordinateNumber; i++)
						flashing[i].setAlpha((float) animator.getAnimatedValue());
				}
			});
			winAnimation.start();
		} catch(Exception ignored) { }
	}

	private void saveLevel() {
		SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
		editor.putInt(PREF_LEVEL, game.maxLevel);
		editor.apply();
	}

	private void clearBoard() { // TODO: any effects?
		for (int row = 6; row >= 0; row--)
			for (int column = 0; column < 7; column++) {
				images[row][column].setAlpha(1F);
				images[row][column].setImageDrawable(null);
			}
	}

	private Animation getFallAnimation(int row) {
		float deltaPixels = (6 - row) * 36 * displayMetrics.density;
		int deltaMillis = (int) Math.sqrt(50000D * (6 - row));
		AlphaAnimation fadeInAnimation = new AlphaAnimation(0f, 1f);
		fadeInAnimation.setDuration(250);
		TranslateAnimation fallAnimation = new TranslateAnimation(0f, 0f, - deltaPixels, 0f);
		fallAnimation.setDuration(deltaMillis);
		fallAnimation.setInterpolator(new AccelerateInterpolator(8f));
		AnimationSet animationSet = new AnimationSet(true);
		animationSet.addAnimation(fadeInAnimation);
		animationSet.addAnimation(fallAnimation);
		return animationSet;
	}

	private void drop(byte column, byte color) { // TODO: more effects?
		int row = game.getTop(column);
		ImageView image = images[row][column];
		image.setImageDrawable(drawables[color]);
		image.setColorFilter(colors[color]);
		image.startAnimation(getFallAnimation(row));
	}

	private void tryColumn(byte column) {
		if (gameState == 4)
			startGame();
		if (gameState == Game.RUNNING && color == Game.WHITE && game.isOption(column))
			nextMove(column);
	}

	public void tryColumn0(View v) {
		tryColumn((byte) 0);
	}

	public void tryColumn1(View v) {
		tryColumn((byte) 1);
	}

	public void tryColumn2(View v) {
		tryColumn((byte) 2);
	}

	public void tryColumn3(View v) {
		tryColumn((byte) 3);
	}

	public void tryColumn4(View v) {
		tryColumn((byte) 4);
	}

	public void tryColumn5(View v) {
		tryColumn((byte) 5);
	}

	public void tryColumn6(View v) {
		tryColumn((byte) 6);
	}

	public void startGame() {
		winAnimation.removeAllUpdateListeners();
		if (winAnimation.isRunning())
			winAnimation.end();
		game.init();
		gameState = Game.RUNNING;
		color = Game.WHITE;
		clearBoard();
	}

	public void click(View v) {
		if (gameState > Game.RUNNING) {
			startGame();
			setMessage();
		}
	}

	private class SearchTask extends AsyncTask<Void, Void, Byte> {
		@Override
		protected Byte doInBackground(Void... voids) {
			try {
				return game.search(Game.BLACK);
			} catch (Exception ignored) { }
			return null;
		}
		@Override
		protected void onPostExecute(Byte result) {
			if (result == null) {
				gameState = 5;
				setMessage();
			} else
				nextMove(result);
		}
	}
}
