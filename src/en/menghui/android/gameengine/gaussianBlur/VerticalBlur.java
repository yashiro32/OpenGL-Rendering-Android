package en.menghui.android.gameengine.gaussianBlur;

import android.annotation.TargetApi;
import android.opengl.GLES30;
import android.os.Build;
import en.menghui.android.gameengine.postProcessing.ImageRenderer;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class VerticalBlur {
	private ImageRenderer renderer;
	private VerticalBlurShader shader;
	
	public VerticalBlur(int targetFboWidth, int targetFboHeight) {
		shader = new VerticalBlurShader();
		renderer = new ImageRenderer(targetFboWidth, targetFboHeight);
		shader.start();
		shader.loadTargetHeight(targetFboHeight);
		shader.stop();
	}
	
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
