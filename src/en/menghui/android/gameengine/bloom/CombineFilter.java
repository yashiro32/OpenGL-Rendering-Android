package en.menghui.android.gameengine.bloom;

import android.annotation.TargetApi;
import android.opengl.GLES30;
import android.os.Build;
import en.menghui.android.gameengine.postProcessing.ImageRenderer;

public class CombineFilter {
	private ImageRenderer renderer;
	private CombineShader shader;
	
	public CombineFilter() {
		shader = new CombineShader();
		shader.start();
		shader.connectTextureUnits();
		shader.stop();
		renderer = new ImageRenderer();
	}
	
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
	public void render(int colourTexture, int highlightTexture) {
		shader.start();
		GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
		GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, colourTexture);
		GLES30.glActiveTexture(GLES30.GL_TEXTURE1);
		GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, highlightTexture);
		renderer.renderQuad();
		shader.stop();
	}
	
	public void cleanUp() {
		renderer.cleanUp();
		shader.cleanUp();
	}
}
