package en.menghui.android.gameengine.shadows;

import java.util.ArrayList;
import java.util.List;
import en.menghui.android.gameengine.entities.Camera;
import en.menghui.android.gameengine.renderEngine.MasterRenderer;
import en.menghui.android.gameengine.toolbox.Maths;
import android.annotation.TargetApi;
import android.os.Build;
import android.renderscript.Matrix4f;

/**
 * Represents the 3D cuboidal area of the world in which objects will cast
 * shadows (basically represents the orthographic projection area for the shadow
 * render pass). It it updated each frame to optimise the area, making it as
 * small as possible (to allow for optimal shadow map resolution) while not
 * being too small to avoid objects not having shadows when they should.
 * Everything inside the cuboidal area represented by this object will be
 * rendered to the shadow map in the shadow render pass. Everything outside the
 * area won't be.
 */

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class ShadowBox {
	private static final float OFFSET = 10;
	private static final float[] UP = {0, 1, 0, 0};
	private static final float[] FORWARD = {0, 0, -1, 0};
	private static final float SHADOW_DISTANCE = 100;
	
	private float minX, maxX;
	private float minY, maxY;
	private float minZ, maxZ;
	private Matrix4f lightViewMatrix;
	private Camera cam;
	
	private float farHeight, farWidth, nearHeight, nearWidth;
	
	/**
	 * Creates a new shadow box and calculates some initial values relating to
	 * the camera's view frustum, namely the width and height of the near plane
	 * and (possibly adjusted) far plane.
	 * 
	 * @param lightViewMatrix
	 *             - basically the "view matrix" of the light. Can be used to
	 *             transform a point from world space into "light" space (i.e.
	 *             changes a point's coordinates from being in relation to the
	 *             world's axis to being in terms of the light's local axis).
	 *             
	 * @param camera
	 *             - the in-game camera.
	 */
	protected ShadowBox(Matrix4f lightViewMatrix, Camera camera) {
		this.lightViewMatrix = lightViewMatrix;
		this.cam = camera;
		calculateWidthsAndHeights();
	}
	
	/**
	 * Updates the bounds of the shadow box based on the light direction and the
	 * camera's view frustum, to make sure that the box covers the smallest area
	 * possible while still ensuring that everything inside the camera's view
	 * (within a certain range) will cast shadows.
	 */
	protected void update() {
		Matrix4f rotation = calculateCameraRotationMatrix();
		float[] vec = Maths.transformMatrix(rotation, FORWARD);
		float[] forwardVector = {vec[0], vec[1], vec[2]};
		
		float[] toFar = forwardVector.clone();
		toFar = Maths.scaleArray(toFar, SHADOW_DISTANCE);
		float[] toNear = forwardVector.clone();
		toNear = Maths.scaleArray(toNear, MasterRenderer.NEAR_PLANE);
		float[] centerNear = Maths.addArrays(toNear, cam.getPosition());
		float[] centerFar = Maths.addArrays(toFar, cam.getPosition());
		
		List<float[]> points = calculateFrustumVertices(rotation, forwardVector, centerNear, centerFar);
		
		boolean first = true;
		for (float[] point : points) {
			if (first) {
				minX = point[0];
				maxX = point[0];
				minY = point[1];
				maxY = point[1];
				minZ = point[2];
				maxZ = point[2];
				first = false;
				continue;
			}
			if (point[0] > maxX) {
				maxX = point[0];
			} else if (point[0] < minX) {
				minX = point[0];
			}
			if (point[1] > maxY) {
				maxY = point[1];
			} else if (point[1] < minY) {
				minY = point[1];
			}
			if (point[2] > maxZ) {
				maxZ = point[2];
			} else if (point[2] < minZ) {
				minZ = point[2];
			}
		} 
		maxZ += OFFSET;
	}
	
	/**
	 * Calculates the center of the "view cuboid" in light space first, and then
	 * converts this to world space using the inverse light's view matrix.
	 * 
	 * @return The center of the "view cuboid" in world space.
	 */
	protected float[] getCenter() {
		float x = (minX + maxX) / 2f;
		float y = (minY + maxY) / 2f;
		float z = (minZ + maxZ) / 2f;
		float[] cen = {x, y, z, 1};
		Matrix4f invertedLight = new Matrix4f(lightViewMatrix.getArray());
		invertedLight.inverse();
		float[] vec = Maths.transformMatrix(invertedLight, cen);
		float[] ret = {vec[0], vec[1], vec[2]};
		return ret;
	}
	
	/**
	 * @return The width of the "view cuboid" (orthographic projection area).
	 */
	protected float getWidth() {
		return maxX - minX;
	}
	
	/**
	 * @return The height of the "view cuboid" (orthographic projection area).
	 */
	protected float getHeight() {
		return maxY - minY;
	}
	
	/**
	 * @return The length of the "view cuboid" (orthographic projection area).
	 */
	protected float getLength() {
		return maxZ - minZ;
	}
	
	/**
	 * Calculates the position of the vertex at each corner of the view frustum
	 * in light space (8 vertices in total, so this return 8 positions).
	 * 
	 * @param rotation - camera's rotation.
	 * 
	 * @param forwardVector - the direction that the camera is aiming, and thus the direction of the frustum.
	 * 
	 * @param centerNear - the center point of the frustum's near plane.
	 * 
	 * @param centerFar - the center point of the frustum's (possibly adjusted) far plane.
	 * 
	 * @return The positions of the vertices of the frustum in light space.
	 */
	private List<float[]> calculateFrustumVertices(Matrix4f rotation, float[] forwardVector, float[] centerNear, float[] centerFar) {
		float[] vec = Maths.transformMatrix(rotation, UP);
		float[] upVector = {vec[0], vec[1], vec[2]};
		float[] rightVector = Maths.crossMulVec3Arrays(forwardVector, upVector);
		float[] downVector = {-upVector[0], -upVector[1], -upVector[2]};
		float[] leftVector = {-rightVector[0], -rightVector[1], -rightVector[2]};
		
		float[] vec1 = {upVector[0] * farHeight, upVector[1] * farHeight, upVector[2] * farHeight};
		float[] farTop = Maths.addArrays(centerFar, vec1);
		float[] vec2 = {downVector[0] * farHeight, downVector[1] * farHeight, downVector[2] * farHeight};
		float[] farBottom = Maths.addArrays(centerFar, vec2);
		float[] vec3 = {upVector[0] * nearHeight, upVector[1] * nearHeight, upVector[2] * nearHeight};
		float[] nearTop = Maths.addArrays(centerNear, vec3);
		float[] vec4 = {downVector[0] * nearHeight, downVector[1] * nearHeight, downVector[2] * nearHeight};
		float[] nearBottom = Maths.addArrays(centerNear, vec4);
		List<float[]> points = new ArrayList<float[]>(8);
		points.add(0, calculateLightSpaceFrustumCorner(farTop, rightVector, farWidth));
		points.add(1, calculateLightSpaceFrustumCorner(farTop, leftVector, farWidth));
		points.add(2, calculateLightSpaceFrustumCorner(farBottom, rightVector, farWidth));
		points.add(3, calculateLightSpaceFrustumCorner(farBottom, leftVector, farWidth));
		points.add(4, calculateLightSpaceFrustumCorner(nearTop, rightVector, nearWidth));
		points.add(5, calculateLightSpaceFrustumCorner(nearTop, leftVector, nearWidth));
		points.add(6, calculateLightSpaceFrustumCorner(nearBottom, rightVector, nearWidth));
		points.add(7, calculateLightSpaceFrustumCorner(nearBottom, leftVector, nearWidth));
		
		/* points.add(calculateLightSpaceFrustumCorner(farTop, rightVector, farWidth));
		points.add(calculateLightSpaceFrustumCorner(farTop, leftVector, farWidth));
		points.add(calculateLightSpaceFrustumCorner(farBottom, rightVector, farWidth));
		points.add(calculateLightSpaceFrustumCorner(farBottom, leftVector, farWidth));
		points.add(calculateLightSpaceFrustumCorner(nearTop, rightVector, nearWidth));
		points.add(calculateLightSpaceFrustumCorner(nearTop, leftVector, nearWidth));
		points.add(calculateLightSpaceFrustumCorner(nearBottom, rightVector, nearWidth));
		points.add(calculateLightSpaceFrustumCorner(nearBottom, leftVector, nearWidth)); */
		
		return points;
	}
	
	/**
	 * Calculates one of the corner vertices of the view frustum in world space
	 * and converts it to light space.
	 * 
	 * @param startPoint - the starting center point on the view frustum.
	 * 
	 * @param direction - the direction of the corner from the start point.
	 * 
	 * @param width - the distance of the corner from the start point.
	 * 
	 * @return - The relevant corner vertex of the view frustum in light space.
	 */
	private float[] calculateLightSpaceFrustumCorner(float[] startPoint, float[] direction, float width) {
		float[] vec = {direction[0] * width, direction[1] * width, direction[2] * width};
		float[] point = Maths.addArrays(startPoint, vec);
		float[] point4f = {point[0], point[1], point[2], 1f};
		float[] ret = Maths.transformMatrix(lightViewMatrix, point4f);
		
		return ret;
	}
	
	/**
	 * @return The rotation of the camera represented as a matrix.
	 */
	private Matrix4f calculateCameraRotationMatrix() {
		Matrix4f rotation = new Matrix4f();
		rotation.rotate(-cam.getYaw(), 0, 1, 0);
		rotation.rotate(-cam.getPitch(), 1, 0, 0);
		
		return rotation;
	}
	
	/**
	 * Calculates the width and height of the near and far planes of the
	 * camera's view frustum. However, this doesn't have to use the "actual" far
	 * plane of the view frustum. It can use a shortened view frustum if desired
	 * by bringing the far-plane closer, which would increase shadow resolution
	 * but means that distant objects wouldn't cast shadows.
	 */
	private void calculateWidthsAndHeights() {
		farWidth = (float) (SHADOW_DISTANCE * Math.tan(Math.toRadians(MasterRenderer.FOV)));
		nearWidth = (float) (MasterRenderer.NEAR_PLANE * Math.tan(Math.toRadians(MasterRenderer.FOV)));
		farHeight = farWidth / getAspectRatio();
		nearHeight = nearWidth / getAspectRatio();
	}
	
	/**
	 * @return The aspect ratio of the display (width:height ratio)
	 */
	private float getAspectRatio() {
		return (float) MasterRenderer.mWidth / (float) MasterRenderer.mHeight;
	}
	
	
}
