package en.menghui.android.gameengine.shadows;

import java.util.List;
import java.util.Map;
import en.menghui.android.gameengine.entities.Camera;
import en.menghui.android.gameengine.entities.Entity;
import en.menghui.android.gameengine.entities.Light;
import en.menghui.android.gameengine.models.TexturedModel;
import en.menghui.android.gameengine.toolbox.Maths;
import android.annotation.TargetApi;
import android.opengl.GLES30;
import android.os.Build;
import android.renderscript.Matrix4f;

/**
 * This class is in charge of using all of the classes in the shadows package to
 * carry out the shadow render pass, i.e. rendering the scene to the shadow map
 * texture. This is the only class in the shadows package which needs to be
 * referenced from outside the shadows package.
 * 
 * @author Meng Hui
 *
 */

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class ShadowMapMasterRenderer {
	private static final int SHADOW_MAP_SIZE = 70;
	
	private ShadowFrameBuffer shadowFbo;
	private ShadowShader shader;
	private ShadowBox shadowBox;
	private Matrix4f projectionMatrix = new Matrix4f();
	private Matrix4f lightViewMatrix = new Matrix4f();
	private Matrix4f projectionViewMatrix = new Matrix4f();
	private Matrix4f offset = createOffset();
	
	private ShadowMapEntityRenderer entityRenderer;
	
	/**
	 * Creates instances of the important objects needed for rendering the scene
	 * to the shadow map. This includes the {@link ShadowBox} which calculates
	 * the position and size of the "view cuboid", the simple renderer and
	 * shader program that are used to render objects to the shadow map, and the
	 * {@link ShadowFrameBuffer} to which the scene is rendered. The size of the
	 * shadow map is determined here.
	 * 
	 * @param camera - the camera being used in the scene.
	 */
	public ShadowMapMasterRenderer(Camera camera) {
		shader = new ShadowShader();
		shadowBox = new ShadowBox(lightViewMatrix, camera);
		shadowFbo = new ShadowFrameBuffer(SHADOW_MAP_SIZE, SHADOW_MAP_SIZE);
		entityRenderer = new ShadowMapEntityRenderer(shader, projectionViewMatrix);
	}
	
	/**
	 * Carries out the shadow render pass. This renders the entities to the
	 * shadow map. First the shadow box is updated to calculate the size and
	 * position of the "view cuboid". The light direction is assumed to be
	 * "-lightPosition" which will be fairly accurate assuming that the light is
	 * very far from the scene, It then prepars to render, renders the entities
	 * to the shadow map, and finishes rendering.
	 * 
	 * @param entities
	 *            - the lists of entities to be rendered. Each list is
	 *            associated with the {@link TexturedModel} that all of the
	 *            entities in that list use.
	 *            
	 * @param sun
	 *            - the light acting as the sun in the scene.
	 */
	public void render(Map<TexturedModel, List<Entity>> entities, Light sun) {
		shadowBox.update();
		float[] sunPosition = sun.getPosition();
		float[] lightDirection = {-sunPosition[0], -sunPosition[1], -sunPosition[2]};
		prepare(lightDirection, shadowBox);
		entityRenderer.render(entities);
		finish();
	}
	
	/**
	 * This biased projection-view matrix is used to convert fragments into
	 * "shadow map space" when rendering the main render pass. It converts a 
	 * world space position into a 2D coordinate on the shadow map. This is
	 * needed for the second part of shadow mapping.
	 * 
	 * @return The to-shadow-map-space matrix.
	 */
	public Matrix4f getToShadowMapSpaceMatrix() {
		Matrix4f mat = new Matrix4f(offset.getArray());
		mat.loadMultiply(offset, projectionViewMatrix);
		return mat;
	}
	
	/**
	 * Clean up the shader and FBO on closing.
	 */
	public void cleanUp() {
		shader.cleanUp();
		shadowFbo.cleanUp();
	}
	
	/**
	 * @return The ID of the shadow map texture. The ID will always stay the
	 *         same, even when the contents of the shadow map texture change
	 *         each frame.
	 */
	public int getShadowMap() {
		return shadowFbo.getShadowMap();
	}
	
	public float getShadowMapSize() {
		return SHADOW_MAP_SIZE;
	}
	
	/**
	 * @return The light's "view" matrix.
	 */
	protected Matrix4f getLightSpaceTransform() {
		return lightViewMatrix;
	}
	
	/**
	 * Prepare for the shadow render pass. This first updates the dimensions of
	 * the orthographic "view cuboid" based on the information that was
	 * calculated in the {@link ShadowBox} class. The light's "view" matrix is
	 * also calculated based on the light's direction and the center position of
	 * the "view cuboid" which was also calculated in the {@link ShadowBox}
	 * class. These two matrices are multiplied together to create the
	 * projection-view matrix. This matrix determines the size, position, and
	 * orientation of the "view cuboid" in the world. This method also binds the
	 * shadows FBO so that everything rendered after this gets rendered to the
	 * FBO. It also enables depth testing, and clears any data that is in the
	 * FBOs depth attachment from last frame. The simple shader program is also
	 * started.
	 * 
	 * @param lightDirection - the direction of the light rays coming from the sun.
	 * 
	 * @param box - the shadow box, which contains all the info about the "view cuboid".
	 */
	private void prepare(float[] lightDirection, ShadowBox box) {
		updateOrthoProjectionMatrix(box.getWidth(), box.getHeight(), box.getLength());
		updateLightViewMatrix(lightDirection, box.getCenter());
		projectionViewMatrix.loadMultiply(projectionMatrix, lightViewMatrix);
		shadowFbo.bindFrameBuffer();
		GLES30.glEnable(GLES30.GL_DEPTH_TEST);
		GLES30.glClear(GLES30.GL_DEPTH_BUFFER_BIT);
		shader.start();
	}
	
	/**
	 * Finish the shadow render pass. Stops the shader and unbinds the shadow
	 * FBO, so everything rendered after this point is rendered to the screen,
	 * rather than to the shadow FBO.
	 */
	private void finish() {
		shader.stop();
		shadowFbo.unbindFrameBuffer();
	}
	
	/**
	 * Updates the "view" matrix of the light. This creates a view matrix which
	 * will line up the direction of the "view cuboid" with the direction of the
	 * light. The light itself has no position, so the "view" matrix is centered
	 * at the center of the "view cuboid". The created view matrix determines
	 * where and how the "view cuboid" is positioned in the world. The size of
	 * the view cuboid, however, is determined by the projection matrix.
	 * 
	 * @param direction - the light direction, and therefore the direction that the "view cuboid" should be pointing.
	 * 
	 * @param center - the center of the "view cuboid" in world space.
	 */
	private void updateLightViewMatrix(float[] direction, float[] center) {
		direction = Maths.normaliseArray(direction);
		center = Maths.negateArray(center);
		lightViewMatrix.loadIdentity();
		float[] vec = {direction[0], direction[2]};
		float pitch = (float) Math.acos(Maths.arrayLength(vec));
		lightViewMatrix.rotate(pitch, 1, 0, 0);
		float yaw = (float) Math.toDegrees((float) Math.atan(direction[0] / direction[2]));
		yaw = direction[2] > 0 ? yaw - 180 : yaw;
		lightViewMatrix.rotate(yaw, 0, 1, 0);
		lightViewMatrix.translate(center[0], center[1], center[2]);
	}
	
	/**
	 * Creates the orthographic projection matrix. This projection matrix
	 * basically sets the width, length and height of the "view cuboid", based
	 * on the values that were calculated in the {@link ShadowBox} class.
	 * 
	 * @param width - shadow box width.
	 * 
	 * @param height - shadow box height.
	 * 
	 * @param length - shadow box length.
	 */
	private void updateOrthoProjectionMatrix(float width, float height, float length) {
		projectionMatrix.loadIdentity();
		projectionMatrix.set(0, 0, 2f / width);
		projectionMatrix.set(1, 1, 2f / height);
		projectionMatrix.set(2, 2, -2f / length);
		projectionMatrix.set(3, 3, 1);
	}
	
	/**
	 * Creates the offset for part of the conversion to shadow map space. This
	 * conversion is necessary to convert from one coordinate system to the
	 * coordinate system that we can use to sample to shadow map.
	 * 
	 *  @return The offset as a matrix (so that it's easy to apply to other matrices).
	 */
	private static Matrix4f createOffset() {
		Matrix4f offset = new Matrix4f();
		offset.translate(0.5f, 0.5f, 0.5f);
		offset.scale(0.5f, 0.5f, 0.5f);
		
		return offset;
	}
	
	
}
