package en.menghui.android.gameengine.guis;

import java.util.List;

import android.annotation.TargetApi;
import android.opengl.GLES30;
import android.os.Build;
import android.renderscript.Matrix4f;
import en.menghui.android.gameengine.models.RawModel;
import en.menghui.android.gameengine.renderEngine.Loader;
import en.menghui.android.gameengine.toolbox.Maths;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class GuiRenderer {
	private final RawModel quad;
	private GuiShader shader;
	
	public GuiRenderer(Loader loader) {
		float[] positions = {-1, 1, -1, -1, 1, 1, 1, -1};
		quad = loader.loadToVAO(positions, 2);
		shader = new GuiShader();
	}
	
	public void render(List<GuiTexture> guis) {
		shader.start();
		GLES30.glBindVertexArray(quad.getVaoID());
		GLES30.glEnableVertexAttribArray(0);
		GLES30.glEnable(GLES30.GL_BLEND);
		GLES30.glBlendFunc(GLES30.GL_SRC_ALPHA, GLES30.GL_ONE_MINUS_SRC_ALPHA);
		GLES30.glDisable(GLES30.GL_DEPTH_TEST);
		// Render
		for (GuiTexture gui : guis) {
			GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
			GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, gui.getTexture());
			Matrix4f matrix = Maths.createTransformationMatrix(gui.getPosition(), gui.getScale());
			shader.loadTransformation(matrix.getArray());
			GLES30.glDrawArrays(GLES30.GL_TRIANGLE_STRIP, 0, quad.getVertexCount());
		}
		GLES30.glEnable(GLES30.GL_DEPTH_TEST);
		GLES30.glDisable(GLES30.GL_BLEND);
		GLES30.glDisableVertexAttribArray(0);
		GLES30.glBindVertexArray(0);
		shader.stop();
	}
	
	public void cleanUp() {
		shader.cleanUp();
	}
}
