package en.menghui.android.gameengine.water;

import java.util.List;

import android.annotation.TargetApi;
import android.opengl.GLES30;
import android.os.Build;
import android.renderscript.Matrix4f;
import en.menghui.android.gameengine.R;
import en.menghui.android.gameengine.entities.Camera;
import en.menghui.android.gameengine.entities.Light;
import en.menghui.android.gameengine.models.RawModel;
import en.menghui.android.gameengine.renderEngine.Loader;
import en.menghui.android.gameengine.renderEngine.MasterRenderer;
import en.menghui.android.gameengine.toolbox.Maths;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class WaterRenderer {
	private static final int DUDV_MAP = R.drawable.water_dudv;
	
	private static final int NORMAL_MAP = R.drawable.normal_map;
	
	private static final float WAVE_SPEED = 0.03f;
	
	private RawModel quad;
	private WaterShader shader;
	private WaterFrameBuffers fbos;
	
	private float moveFactor = 0;
	
	private int dudvTexture;
	
	private int normalMap;
	
	public WaterRenderer(Loader loader, WaterShader shader, Matrix4f projectionMatrix, WaterFrameBuffers fbos) {
		this.shader = shader;
		this.fbos = fbos;
		dudvTexture = loader.loadTexture(DUDV_MAP);
		normalMap = loader.loadTexture(NORMAL_MAP);
		shader.start();
		shader.connectTextureUnits();
		shader.loadProjectionMatrix(projectionMatrix);
		shader.stop();
		setUpVAO(loader);
	}
	
	public void render(List<WaterTile> water, Camera camera, Light sun) {
		prepareRender(camera, sun);
		for (WaterTile tile : water) {
			float[] position = {tile.getX(), tile.getHeight(), tile.getZ()};
			Matrix4f modelMatrix = Maths.createTransformationMatrix(position, 0, 0, 0, WaterTile.TILE_SIZE);
			shader.loadModelMatrix(modelMatrix);
			GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, quad.getVertexCount());
		}
		unbind();
	}
	
	private void prepareRender(Camera camera, Light sun) {
		shader.start();
		shader.loadViewMatrix(camera);
		moveFactor += WAVE_SPEED * MasterRenderer.getFrameTimeSeconds();
		moveFactor %= 1;
		shader.loadMoveFactor(moveFactor);
		shader.loadLight(sun);
		GLES30.glBindVertexArray(quad.getVaoID());
		GLES30.glEnableVertexAttribArray(0);
		GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
		GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, fbos.getReflectionTexture());
		GLES30.glActiveTexture(GLES30.GL_TEXTURE1);
		GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, fbos.getRefractionTexture());
		GLES30.glActiveTexture(GLES30.GL_TEXTURE2);
		GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, dudvTexture);
		GLES30.glActiveTexture(GLES30.GL_TEXTURE3);
		GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, normalMap);
		GLES30.glActiveTexture(GLES30.GL_TEXTURE4);
		GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, fbos.getRefractionDepthTexture());
		
		GLES30.glEnable(GLES30.GL_BLEND);
		GLES30.glBlendFunc(GLES30.GL_SRC_ALPHA, GLES30.GL_ONE_MINUS_SRC_ALPHA);
	}
	
	private void unbind() {
		GLES30.glDisable(GLES30.GL_BLEND);
		GLES30.glDisableVertexAttribArray(0);
		GLES30.glBindVertexArray(0);
		shader.stop();
	}
	
	private void setUpVAO(Loader loader) {
		// Just x and z vertex positions here, y is set to 0 in v.shader.
		float[] vertices = { -1, -1, -1, 1, 1, -1, 1, -1, -1, 1, 1, 1 };
		quad = loader.loadToVAO(vertices, 2);
	}
	
	
}
