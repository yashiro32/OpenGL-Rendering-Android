package en.menghui.android.gameengine.ThreeDConversion;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.opengl.GLES30;
import android.os.Build;
import en.menghui.android.gameengine.gaussianBlur.VerticalBlurShader;
import en.menghui.android.gameengine.postProcessing.ImageRenderer;

@SuppressLint("NewApi")
public class ThreeDConversion {
	private ImageRenderer renderer;
	private ThreeDConversionShader shader;
	
	public ThreeDConversion(int targetFboWidth, int targetFboHeight) {
		shader = new ThreeDConversionShader();
		renderer = new ImageRenderer();
		shader.start();
		shader.loadTargetWidth(targetFboWidth);
		shader.loadTargetHeight(targetFboHeight);
		shader.loadShift(8);
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
