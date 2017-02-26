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
import en.menghui.android.gameengine.shaders.TerrainShader;
import en.menghui.android.gameengine.terrains.Terrain;
import en.menghui.android.gameengine.textures.ModelTexture;
import en.menghui.android.gameengine.textures.TerrainTexturePack;
import en.menghui.android.gameengine.toolbox.Maths;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class TerrainRenderer {
	private TerrainShader shader;
	
	public TerrainRenderer(TerrainShader shader, Matrix4f projectionMatrix) {
		this.shader = shader;
		shader.start();
		shader.loadProjectionMatrix(projectionMatrix.getArray());
		shader.connectTextureUnits();
		shader.stop();
	}
	
	public void render(List<Terrain> terrains, Matrix4f toShadowSpace) {
		shader.loadToShadowMapSpaceMatrix(toShadowSpace.getArray());
		for (Terrain terrain : terrains) {
			prepareTerrain(terrain);
			loadModelMatrix(terrain);
			GLES30.glDrawElements(GLES30.GL_TRIANGLES, terrain.getModel().getVertexCount(), GLES30.GL_UNSIGNED_INT, 0);
			
			unbindTexturedModel();
		}
	}
	
	private void prepareTerrain(Terrain terrain) {
		RawModel rawModel = terrain.getModel();
		GLES30.glBindVertexArray(rawModel.getVaoID());
		GLES30.glEnableVertexAttribArray(0);
		GLES30.glEnableVertexAttribArray(1);
		GLES30.glEnableVertexAttribArray(2);
		bindTextures(terrain);
		shader.loadShineVariables(1, 0);
	}
	
	private void bindTextures(Terrain terrain) {
		TerrainTexturePack texturePack = terrain.getTexturePack();
		GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
		GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, texturePack.getBackgroundTexture().getTextureID());
		GLES30.glActiveTexture(GLES30.GL_TEXTURE1);
		GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, texturePack.getrTexture().getTextureID());
		GLES30.glActiveTexture(GLES30.GL_TEXTURE2);
		GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, texturePack.getgTexture().getTextureID());
		GLES30.glActiveTexture(GLES30.GL_TEXTURE3);
		GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, texturePack.getbTexture().getTextureID());
		GLES30.glActiveTexture(GLES30.GL_TEXTURE4);
		GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, terrain.getBlendMap().getTextureID());
	}
	
	private void unbindTexturedModel() {
		GLES30.glDisableVertexAttribArray(0);
		GLES30.glDisableVertexAttribArray(1);
		GLES30.glDisableVertexAttribArray(2);
		GLES30.glBindVertexArray(0);
	}
	
	private void loadModelMatrix(Terrain terrain) {
		float[] position = {terrain.getX(), 0, terrain.getZ()};
		Matrix4f transformationMatrix = Maths.createTransformationMatrix(position, 0, 0, 0, 1);
		shader.loadTransformationMatrix(transformationMatrix.getArray());
	}
}
