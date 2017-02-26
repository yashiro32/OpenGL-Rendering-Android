package en.menghui.android.gameengine;

import en.menghui.android.gameengine.renderEngine.GLRenderer;
import en.menghui.android.gameengine.renderEngine.MasterRenderer;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

public class MainActivity extends Activity {
	private static final String TAG = "Main Activity";
	
	private GLSurfaceView mSurfaceView;
	private GLSurfaceView mGLView;
	
	private final int WIDTH = 1280;
	private final int HEIGHT = 720;
	private final int FPS_CAP = 120;
	
	private MasterRenderer renderer;
	
	public static float touchX;
	public static float touchY;
	public static String touchDirection = "";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// setContentView(R.layout.activity_main);
		
		renderer = new MasterRenderer(this);
		
		createDisplay();
		
		/* while(true) {
			// game logic.
			// render
			updateDisplay();
		} */
		
	}
	
	@SuppressLint("NewApi")
	public void createDisplay() {
		if (hasGLES20()) {
			mGLView = new GLSurfaceView(this);
			mGLView.setEGLContextClientVersion(2);
			mGLView.setPreserveEGLContextOnPause(true);
			// mGLView.setEGLConfigChooser(new ConfigChooser()); // Set Anti-aliasing. Multi-sampling.
			mGLView.setRenderer(renderer);
			
			mGLView.setOnTouchListener(new OnTouchListener() {
				@SuppressLint("ClickableViewAccessibility")
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					switch(event.getAction()) {
						case MotionEvent.ACTION_DOWN:
							float x = event.getX();
							float y = event.getY();
							
							touchX = event.getX();
							touchY = event.getY();
							
							if (y < 320 && x < 240) {
								if (y/320 < x/240) {
									touchDirection = "U";
								} else {
									touchDirection = "L";
								}
							}
							
							if (y < 320 && x >= 240) {
								if (y/320 < (x-240)/240) {
									touchDirection = "U";
								} else {
									touchDirection = "R";
								}
							}
							
							if (y >= 320 && x < 240) {
								if ((y-320)/320 < x/240) {
									touchDirection = "D";
								} else {
									touchDirection = "L";
								}
							}
							
							if (y >= 320 && x >= 240) {
								if ((y-320)/320 > (x-240)/240) {
									touchDirection = "D";
								} else {
									touchDirection = "R";
								}
							}
							
							// Log.d(TAG, "X: " + touchDirection);
							// Log.d(TAG, "Y: " + touchDirection);
							break;
						case MotionEvent.ACTION_MOVE:
							touchDirection = "";
							touchX = event.getX();
							touchY = event.getY();
							break;
						case MotionEvent.ACTION_UP:
							touchDirection = "";
							touchX = -Float.MAX_VALUE;
							touchY = -Float.MAX_VALUE;
							break;
						default:
							touchDirection = "";
					}
					
					return false;
				}
				
			});
		} else {
			// Time to get a new phone, OpenGL ES 2.0 not supported.
		}
		
		setContentView(mGLView);
		
		// GLES11.glViewport(0, 0, mGLView.getWidth(), mGLView.getHeight());
	}
	
	public void updateDisplay() {
		
	}
	
	public void closeDisplay() {
		mGLView.destroyDrawingCache();
	}
	
	private boolean hasGLES20() {
		ActivityManager am = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
		ConfigurationInfo info = am.getDeviceConfigurationInfo();
		
		return info.reqGlEsVersion >= 0x20000;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		/* if (id == R.id.action_settings) {
			return true;
		} */
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		/*
		 * The activity must call the GL surface view's
		 * onResume() on activity onResume().
		 */
		if (mSurfaceView != null) {
			mSurfaceView.onResume();
		}
	}
	
	@Override
	protected void onPause() {
		super.onResume();
		/**
		 * The activity must call the GL surface view's
		 * onPause() on activity onPause()
		 */
		if (mSurfaceView != null) {
			mSurfaceView.onPause();
		}
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		
		// renderer.getLoader().cleanUp();
		
		closeDisplay();
	}
	
	
}
