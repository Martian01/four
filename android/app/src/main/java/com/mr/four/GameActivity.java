package com.mr.four;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.*;

import android.animation.*;
import android.annotation.*;
import android.content.*;
import android.graphics.drawable.*;
import android.media.*;
import android.os.*;
import android.preference.*;
import android.util.*;
import android.view.*;
import android.view.animation.*;
import android.widget.*;

import java.util.*;

public class GameActivity extends AppCompatActivity implements Runnable, Animation.AnimationListener {

	private final String PREF_LEVEL = "PrefLevel";
	private byte maxLevel;
	private byte currentLevel;

	private final Game game = new Game();

	private byte gameState;
	private byte color;
	private byte computerPlayer;

	private Spinner levelSpinner;
	private ArrayAdapter<Integer> levelAdapter;
	private TextView messageView;
	private View busyIndicator;
	private final ImageView[][] images = new ImageView[7][7];

	private final Drawable[] drawables = new Drawable[3];
	private final int[] colors = new int[3];

	private ValueAnimator winAnimation;

	private final DisplayMetrics displayMetrics = new DisplayMetrics();

	private MediaPlayer mpWon;
	private MediaPlayer mpLost;
	private MediaPlayer mpLevelUp0;
	private MediaPlayer mpLevelUp1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		((WindowManager) getSystemService(WINDOW_SERVICE)).getDefaultDisplay().getMetrics(displayMetrics);
		setContentView(R.layout.game);
		levelSpinner = findViewById(R.id.level);
		messageView = findViewById(R.id.message);
		busyIndicator = findViewById(R.id.busy);
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
		mpWon = MediaPlayer.create(this, R.raw.won);
		mpLost = MediaPlayer.create(this, R.raw.lost);
		mpLevelUp0 = MediaPlayer.create(this, R.raw.level_up_0);
		mpLevelUp1 = MediaPlayer.create(this, R.raw.level_up_1);
		loadLevel();
		game.maxLevel = currentLevel;
		//levelAdapter = new ArrayAdapter<Integer>(this, android.R.layout.simple_spinner_item);
		levelAdapter = new ArrayAdapter<Integer>(this, R.layout.spinner_item, R.id.spinnerItem);
		for (int i = 1 ; i <= maxLevel; i++)
			levelAdapter.add(i);
		levelSpinner.setAdapter(levelAdapter);
		levelSpinner.setSelection(currentLevel - 1);
		levelSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				game.maxLevel = currentLevel = (byte) (position + 1);
				saveLevel();
			}
			@Override
			public void onNothingSelected(AdapterView<?> parent) { }
		});
		//
		gameState = 4;
		color = Game.WHITE;
		computerPlayer = Game.BLACK;
		setMessage();
	}

	private void loadLevel() {
		int packedLevel = PreferenceManager.getDefaultSharedPreferences(this).getInt(PREF_LEVEL, 0);
		maxLevel = (byte) (packedLevel >> 8);
		currentLevel = (byte) (packedLevel & 0xff);
		if (maxLevel < 1)
			maxLevel = 1;
		if (currentLevel > maxLevel)
			currentLevel = maxLevel;
	}

	private void saveLevel() {
		SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
		editor.putInt(PREF_LEVEL, (maxLevel << 8) | currentLevel);
		editor.apply();
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
		if (gameState == Game.RUNNING && color == computerPlayer) {
			messageView.setVisibility(View.INVISIBLE);
			busyIndicator.setVisibility(View.VISIBLE);
		} else {
			messageView.setText(messageText[gameState]);
			messageView.setVisibility(View.VISIBLE);
			busyIndicator.setVisibility(View.INVISIBLE);
		}
	}

	private void nextMove(byte column) {
		drop(color, column, game.getTop(column));
		game.drop(column, color);
		gameState = winner(column);
		switch (gameState) {
			case Game.RUNNING:
				color = Game.opposite(color);
				setMessage();
				if (color == computerPlayer)
					new SearchTask().execute();
				break;
			case Game.WHITE:
				messageView.postDelayed(this, 750);
				setMessage();
				if (currentLevel >= maxLevel) {
					(currentLevel % 2 == 0 ? mpLevelUp0 : mpLevelUp1).start();
					game.maxLevel = currentLevel = ++maxLevel;
					saveLevel();
					levelAdapter.add((int) maxLevel);
					levelAdapter.notifyDataSetChanged();
					levelSpinner.setSelection(currentLevel - 1);
					// TODO: Level up animation
				} else
					mpWon.start();
				break;
			case Game.BLACK:
				messageView.postDelayed(this, 750);
				setMessage();
				mpLost.start();
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

	private void clearBoard() {
		for (int row = 6; row >= 0; row--)
			for (int column = 0; column < 7; column++) {
				images[row][column].setAlpha(1F);
				images[row][column].setImageDrawable(null);
			}
		// TODO: any effects?
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

	private final LinkedList<Integer> drops = new LinkedList<>();

	private boolean canDrop = true;

	@Override
	public void onAnimationStart(Animation animation) { }

	@Override
	public void onAnimationEnd(Animation animation) {
		synchronized (drops) {
			canDrop = true;
			dropNext(drops.poll());
		}
	}

	@Override
	public void onAnimationRepeat(Animation animation) { }

	private void dropNext(Integer stone) {
		if (stone == null)
			return;
		if (canDrop) {
			canDrop = false;
			byte color = (byte) (stone >> 16);
			byte column = (byte) ((stone >> 8) & 0xff);
			byte row = (byte) (stone & 0xff);
			ImageView image = images[row][column];
			image.setImageDrawable(drawables[color]);
			image.setColorFilter(colors[color]);
			Animation fallAnimation = getFallAnimation(row);
			fallAnimation.setAnimationListener(this);
			image.startAnimation(fallAnimation);
		} else
			drops.offer(stone);
	}

	private void drop(byte color, byte column, byte row) {
		synchronized (drops) {
			dropNext((color << 16) | (column << 8) | row);
		}
	}

	private void tryColumn(byte column) {
		if (gameState == 4)
			startGame();
		if (gameState == Game.RUNNING && color != computerPlayer && game.isOption(column))
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
		drops.clear();
		canDrop = true;
		game.init();
		gameState = Game.RUNNING;
		color = Game.WHITE;
		clearBoard();
		if (color == computerPlayer)
			new SearchTask().execute();
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
