package en.menghui.android.gameengine.entities;

import android.util.Log;
import en.menghui.android.gameengine.MainActivity;
import en.menghui.android.gameengine.models.TexturedModel;
import en.menghui.android.gameengine.renderEngine.MasterRenderer;
import en.menghui.android.gameengine.terrains.Terrain;

public class Player extends Entity {
	private static final String TAG = "Player";
	
	private static final float RUN_SPEED = 20;
	private static final float TURN_SPEED = 160;
	public static final float GRAVITY = -50;
	private static final float JUMP_POWER = 30;
	
	private static final float TERRAIN_HEIGHT = 0;
	
	private float currentSpeed = 0;
	private float currentTurnSpeed = 0;
	private float upwardsSpeed = 0;
	
	private boolean isInAir = false;
	
	public Player(TexturedModel model, float[] position, float rotX, float rotY, float rotZ, float scale) {
		super(model, position, rotX, rotY, rotZ, scale);
	}
	
	public void move(Terrain terrain) {
		checkInputs();
		super.increaseRotation(0, currentTurnSpeed * MasterRenderer.getFrameTimeSeconds(), 0);
		float distance = currentSpeed * MasterRenderer.getFrameTimeSeconds();
		float dx = (float) (distance * Math.sin(Math.toRadians(super.getRotY())));
		float dz = (float) (distance * Math.cos(Math.toRadians(super.getRotY())));
		super.increasePosition(dx, 0, dz);
		upwardsSpeed += GRAVITY * MasterRenderer.getFrameTimeSeconds();
		super.increasePosition(0, upwardsSpeed * MasterRenderer.getFrameTimeSeconds(), 0);
		float terrainHeight = terrain.getHeightOfTerrain(super.getPosition()[0], super.getPosition()[2]);
		if (super.getPosition()[1] < terrainHeight) {
			upwardsSpeed = 0;
			isInAir = false;
			super.getPosition()[1] = terrainHeight;
		}
	}
	
	private void jump() {
		if (!isInAir) {
			this.upwardsSpeed = JUMP_POWER;
			isInAir = true;
		}
	}
	
	private void checkInputs() {
		if (MainActivity.touchDirection.equals("U")) {
			this.currentSpeed = RUN_SPEED;
		} else if (MainActivity.touchDirection.equals("D")) {
			this.currentSpeed = -RUN_SPEED;
		} else {
			this.currentSpeed = 0;
		}
		
		if (MainActivity.touchDirection.equals("L")) {
			this.currentTurnSpeed = -TURN_SPEED;
		} else if (MainActivity.touchDirection.equals("R")) {
			this.currentTurnSpeed = TURN_SPEED;
		} else {
			this.currentTurnSpeed = 0;
		}
		
		if (MainActivity.touchDirection.equals("J")) {
			jump();
		}
	}

}
