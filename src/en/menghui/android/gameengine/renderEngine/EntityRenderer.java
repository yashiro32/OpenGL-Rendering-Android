package en.menghui.android.gameengine.renderEngine;

import java.util.List;
import java.util.Map;

import android.annotation.TargetApi;
import android.opengl.GLES30;
import android.os.Build;
import android.renderscript.Matrix4f;
import en.menghui.android.gameengine.entities.Entity;
import en.menghui.android.gameengine.models.RawModel;
import en.menghui.android.gameengine.models.TexturedModel;
import en.menghui.android.gameengine.shaders.StaticShader;
import en.menghui.android.gameengine.textures.ModelTexture;
import en.menghui.android.gameengine.toolbox.Maths;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class EntityRenderer {
	private StaticShader shader;
	
	public EntityRenderer(StaticShader shader, Matrix4f projectionMatrix) {
		this.shader = shader;
        shader.start();
        shader.loadProjectionMatrix(projectionMatrix.getArray());
        shader.stop();
	}
	
	public void render(Map<TexturedModel, List<Entity>> entities) {
		for (TexturedModel model : entities.keySet()) {
			prepareTexturedModel(model);
			List<Entity> batch = entities.get(model);
			for (Entity entity : batch) {
				prepareInstance(entity);
				GLES30.glDrawElements(GLES30.GL_TRIANGLES, model.getRawModel().getVertexCount(), GLES30.GL_UNSIGNED_INT, 0);
			}
			unbindTexturedModel();
		}
	}
	
	private void prepareTexturedModel(TexturedModel model) {
		RawModel rawModel = model.getRawModel();
		GLES30.glBindVertexArray(rawModel.getVaoID());
		GLES30.glEnableVertexAttribArray(0);
		GLES30.glEnableVertexAttribArray(1);
		GLES30.glEnableVertexAttribArray(2);
		ModelTexture texture = model.getTexture();
		shader.loadNumberOfRows(texture.getNumberOfRows());
		if (texture.isHasTransparency()) {
			MasterRenderer.disableCulling();
		}
		shader.loadFakeLightingVariable(texture.isUseFakeLighting());
		shader.loadShineVariables(texture.getShineDamper(), texture.getReflectivity());
		GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
		GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, model.getTexture().getID());
		
		shader.loadUseSpecularMap(texture.hasSpecularMap());
		if (texture.hasSpecularMap()) {
			GLES30.glActiveTexture(GLES30.GL_TEXTURE1);
			GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, texture.getSpecularMap());
		}
	}
	
	private void unbindTexturedModel() {
		MasterRenderer.enableCulling();
		GLES30.glDisableVertexAttribArray(0);
		GLES30.glDisableVertexAttribArray(1);
		GLES30.glDisableVertexAttribArray(2);
		GLES30.glBindVertexArray(0);
	}
	
	private void prepareInstance(Entity entity) {
		Matrix4f transformationMatrix = Maths.createTransformationMatrix(entity.getPosition(), entity.getRotX(), entity.getRotY(), entity.getRotZ(), entity.getScale());
		shader.loadTransformationMatrix(transformationMatrix.getArray());
		shader.loadOffset(entity.getTextureXOffset(), entity.getTextureYOffset());
	}
	
	
}
