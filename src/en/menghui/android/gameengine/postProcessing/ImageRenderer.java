package en.menghui.android.gameengine.postProcessing;

import android.annotation.TargetApi;
import android.opengl.GLES30;
import android.os.Build;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class ImageRenderer {
	private Fbo fbo;
	
	public ImageRenderer(int width, int height) {
		this.fbo = new Fbo(width, height, Fbo.NONE);
	}
	
	public ImageRenderer() {
		
	}
	
	public void renderQuad() {
		if (fbo != null) {
			fbo.bindFrameBuffer();
		}
		GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT);
		GLES30.glDrawArrays(GLES30.GL_TRIANGLE_STRIP, 0, 4);
		if (fbo != null) {
			fbo.unbindFrameBuffer();
		}
	}
	
	public int getOutputTexture() {
		return fbo.getColourTexture();
	}
	
	public void cleanUp() {
		if (fbo != null) {
			fbo.cleanUp();
		}
	}
}
