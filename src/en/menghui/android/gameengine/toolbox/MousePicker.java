package en.menghui.android.gameengine.toolbox;

import en.menghui.android.gameengine.MainActivity;
import en.menghui.android.gameengine.entities.Camera;
import en.menghui.android.gameengine.renderEngine.MasterRenderer;
import en.menghui.android.gameengine.terrains.Terrain;
import android.annotation.TargetApi;
import android.os.Build;
import android.renderscript.Matrix4f;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class MousePicker {
	private static final int RECURSION_COUNT = 200;
	private static final float RAY_RANGE = 600;
	
	private float[] currentRay;
	
	private Matrix4f projectionMatrix;
	private Matrix4f viewMatrix;
	private Camera camera;
	
	private Terrain terrain;
	private float[] currentTerrainPoint;
	
	public MousePicker(Camera cam, Matrix4f projection, Terrain terrain) {
		this.camera = cam;
		this.projectionMatrix = projection;
		this.viewMatrix = Maths.createViewMatrix(camera);
		this.terrain = terrain;
	}
	
	public float[] getCurrentTerrainPoint() {
		return currentTerrainPoint;
	}
	
	public float[] getCurrentRay() {
		return currentRay;
	}
	
	public void update() {
		viewMatrix = Maths.createViewMatrix(camera);
		currentRay = calculateMouseRay();
		if (intersectionInRange(0, RAY_RANGE, currentRay)) {
			currentTerrainPoint = binarySearch(0, 0, RAY_RANGE, currentRay);
		} else {
			currentTerrainPoint = null;
		}
	}
	
	private float[] calculateMouseRay() {
		float mouseX = MainActivity.touchX;
		float mouseY = MainActivity.touchY;
		float[] normalizedCoords = getNormalizedDeviceCoords(mouseX, mouseY);
		float[] clipCoords = {normalizedCoords[0], normalizedCoords[1], -1f, 1f};
		float[] eyeCoords = toEyeCoords(clipCoords);
		float[] worldRay = toWorldCoords(eyeCoords);
		return worldRay;
	}
	
	private float[] toWorldCoords(float[] eyeCoords) {
		Matrix4f invertedView = new Matrix4f(viewMatrix.getArray());
		invertedView.inverse();
		invertedView.multiply(new Matrix4f(eyeCoords));
		float[] mouseRay = {invertedView.get(0,0), invertedView.get(0, 1), invertedView.get(0, 2)};
		mouseRay = Maths.normalizeArray(mouseRay);
		return mouseRay;
	}
	
	private float[] toEyeCoords(float[] clipCoords) {
		Matrix4f invertedProjection = new Matrix4f(projectionMatrix.getArray());
		invertedProjection.inverse();
		invertedProjection.multiply(new Matrix4f(clipCoords));
		// System.out.println("" + invertedProjection);
		float[] eyeCoords = {invertedProjection.get(0,0), invertedProjection.get(0, 1), -1f, 0f};
		return eyeCoords;
	}
	
	private float[] getNormalizedDeviceCoords(float mouseX, float mouseY) {
		float x = (2f * mouseX) / MasterRenderer.mWidth;
		float y = (2f * mouseY) / MasterRenderer.mHeight;
		float[] position = {x, -y};
		return position;
	}
	
	//**********************************************************
	
	private float[] getPointOnRay(float[] ray, float distance) {
		float[] camPos = camera.getPosition();
		float[] start = {camPos[0], camPos[1], camPos[2]};
		float[] scaledRay = {ray[0] * distance, ray[1] * distance, ray[2] * distance};
		
		for (int i = 0; i < start.length; i++) {
			start[i] += scaledRay[i];
		}
		
		return start;
	}
	
	private float[] binarySearch(int count, float start, float finish, float[] ray) {
		float half = start + ((finish - start) / 2f);
		if (count >= RECURSION_COUNT) {
			float[] endPoint = getPointOnRay(ray, half);
			Terrain terrain = getTerrain(endPoint[0], endPoint[2]);
			if (terrain != null) {
				return endPoint;
			} else {
				return null;
			}
		}
		if (intersectionInRange(start, half, ray)) {
			return binarySearch(count + 1, start, half, ray);
		} else {
			return binarySearch(count + 1, half, finish, ray);
		}
	}
	
	private boolean intersectionInRange(float start, float finish, float[] ray) {
		float[] startPoint = getPointOnRay(ray, start);
		float[] endPoint = getPointOnRay(ray, finish);
		if (!isUnderGround(startPoint) && isUnderGround(endPoint)) {
			return true;
		} else {
			return false;
		}
	}
	
	private boolean isUnderGround(float[] testPoint) {
		Terrain terrain = getTerrain(testPoint[0], testPoint[2]);
		float height = 0;
		if (terrain != null) {
			height = terrain.getHeightOfTerrain(testPoint[0], testPoint[2]);
		}
		if (testPoint[1] < height) {
			return true;
		} else {
			return false;
		}
	}
	
	private Terrain getTerrain(float worldX, float worldZ) {
		return terrain;
	}
	
	
}
