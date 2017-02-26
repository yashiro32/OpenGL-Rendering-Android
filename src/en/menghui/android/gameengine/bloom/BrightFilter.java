package en.menghui.android.gameengine.bloom;

import android.annotation.TargetApi;
import android.opengl.GLES30;
import android.os.Build;
import en.menghui.android.gameengine.postProcessing.ImageRenderer;

public class BrightFilter {
	private ImageRenderer renderer;
	private BrightFilterShader shader;
	
	public BrightFilter(int width, int height) {
		shader = new BrightFilterShader();
		renderer = new ImageRenderer(width, height);
	}
	
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
	public void render(int texture) {
		shader.start();
		GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
		GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, texture);
		renderer.renderQuad();
		shader.stop();
	}
	
	public int getOutputTexture() {
		return renderer.getOutputTexture();
	}
	
	public void cleanUp() {
		renderer.cleanUp();
		shader.cleanUp();
	}
}
