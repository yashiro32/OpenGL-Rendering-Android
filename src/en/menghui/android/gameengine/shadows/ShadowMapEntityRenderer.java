package en.menghui.android.gameengine.shadows;

import java.util.List;
import java.util.Map;

import en.menghui.android.gameengine.entities.Entity;
import en.menghui.android.gameengine.models.RawModel;
import en.menghui.android.gameengine.models.TexturedModel;
import en.menghui.android.gameengine.renderEngine.MasterRenderer;
import en.menghui.android.gameengine.toolbox.Maths;
import android.annotation.TargetApi;
import android.opengl.GLES30;
import android.os.Build;
import android.renderscript.Matrix4f;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class ShadowMapEntityRenderer {
	private Matrix4f projectionViewMatrix;
	private ShadowShader shader;
	
	/**
	 * @param shader - the simple shader program being used for the shadow render pass.
	 * 
	 * @param projectionViewMatrix - the orthographic projection matrix multiplied by the light's "view" matrix.
	 */
	protected ShadowMapEntityRenderer(ShadowShader shader, Matrix4f projectionViewMatrix) {
		this.shader = shader;
		this.projectionViewMatrix = projectionViewMatrix;
	}
	
	/**
	 * Renders entities to the shadow map. Each model is first bound and then all
	 * of the entities using that model are rendered to the shadow map.
	 * 
	 * @param entities - the entities to be rendered to the shadow map.
	 */
	protected void render(Map<TexturedModel, List<Entity>> entities) {
		for (TexturedModel model : entities.keySet()) {
			RawModel rawModel = model.getRawModel();
			bindModel(rawModel);
			GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
			GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, model.getTexture().getID());
			if (model.getTexture().isHasTransparency()) {
				MasterRenderer.disableCulling();
			}
			for (Entity entity : entities.get(model)) {
				prepareInstance(entity);
				GLES30.glDrawElements(GLES30.GL_TRIANGLES, rawModel.getVertexCount(), GLES30.GL_UNSIGNED_INT, 0);
			}
			if (model.getTexture().isHasTransparency()) {
				MasterRenderer.enableCulling();
			}
		}
		GLES30.glDisableVertexAttribArray(0);
		GLES30.glDisableVertexAttribArray(1);
		GLES30.glBindVertexArray(0);
	}
	
	/**
	 * Binds a raw model before rendering. Only the attribute 0 is enabled here
	 * because that is where the positions are stored in the VAO, and only the
	 * positions are required in the vertex shader.
	 * 
	 * @param rawModel - the model to be bound.
	 */
	private void bindModel(RawModel rawModel) {
		GLES30.glBindVertexArray(rawModel.getVaoID());
		GLES30.glEnableVertexAttribArray(0);
		GLES30.glEnableVertexAttribArray(1);
	}
	
	/**
	 * Prepares an entity to be rendered. The model matrix is created in the
	 * usual way and then multiplied with the projection and view matrix (often
	 * in the past we've done this in the vertex shader) to create the
	 * mvp-matrix. This is then loaded to the vertex shader a a uniform.
	 * 
	 * @param entity - the entity to be prepared for rendering.
	 */
	private void prepareInstance(Entity entity) {
		Matrix4f modelMatrix = Maths.createTransformationMatrix(entity.getPosition(), entity.getRotX(), entity.getRotY(), entity.getRotZ(), entity.getScale());
		Matrix4f mvpMatrix = new Matrix4f(projectionViewMatrix.getArray());
		mvpMatrix.loadMultiply(projectionViewMatrix, modelMatrix);
		shader.loadMvpMatrix(mvpMatrix);
	}
}
