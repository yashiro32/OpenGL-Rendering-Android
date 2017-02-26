package en.menghui.android.gameengine.entities;

public class Camera {
	private float distanceFromPlayer = 50;
	private float angleAroundPlayer = 0;
	
	private float[] position = {0, 0, 0};
	private float pitch = 20;
	private float yaw;
	private float roll;
	
	private Player player;
	
	public Camera(Player player) {
		this.player = player;
	}
	
	public void move(String direction, float offset) {
		/* if (direction.equals("W")) {
			position[2] -= offset;
		}
		
		if (direction.equals("D")) {
			position[0] += offset;
		}
		
		if (direction.equals("A")) {
			position[0] -= offset;
		}
		
		if (direction.equals("S")) {
            position[1] += offset;
        }
        if (direction.equals("L")) {
            position[1] -= offset;
        } */
		
		calculateZoom(0.1f);
		calculatePitch(true, 0.1f);
		calculateAngleAroundPlayer(true, 0.1f);
		
		float horizontalDistance = calculateHorizontalDistance();
		float verticalDistance = calculateVerticalDistance();
		
		calculateCameraPosition(horizontalDistance, verticalDistance);
		
		this.yaw = 180 - (player.getRotY() + angleAroundPlayer);
		this.yaw %= 360;
	}
	
	public void invertPitch() {
		this.pitch = -pitch;
	}
	
	public float[] getPosition() {
		return position;
	}

	public float getPitch() {
		return pitch;
	}

	public float getYaw() {
		return yaw;
	}

	public float getRoll() {
		return roll;
	}
	
	private void calculateCameraPosition(float horizDistance, float verticDistance) {
		float theta = player.getRotY() + angleAroundPlayer;
		float offsetX = (float) (horizDistance * Math.sin(Math.toRadians(theta)));
		float offsetZ = (float) (horizDistance * Math.cos(Math.toRadians(theta)));
		position[0] = player.getPosition()[0] - offsetX;
		position[2] = player.getPosition()[2] - offsetZ;
		position[1] = player.getPosition()[1] + verticDistance;
	}
	
	private float calculateHorizontalDistance() {
		return (float) (distanceFromPlayer * Math.cos(Math.toRadians(pitch)));
	}
	
	private float calculateVerticalDistance() {
		return (float) (distanceFromPlayer * Math.sin(Math.toRadians(pitch)));
	}
	
	private void calculateZoom(float rotation) {
		float zoomLevel = rotation * 0.1f;
		distanceFromPlayer -= zoomLevel;
	}
	
	private void calculatePitch(boolean shiftPressed, float distance) {
		if (shiftPressed) {
			float pitchChange = distance * 0.1f;
			pitch -= pitchChange;
		}
	}
	
	private void calculateAngleAroundPlayer(boolean upPressed, float distance) {
		if (upPressed) {
			float angleChange = distance * 0.3f;
			angleAroundPlayer -= angleChange;
		}
	}
	
	
}
