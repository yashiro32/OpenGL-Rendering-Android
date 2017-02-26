package en.menghui.android.gameengine.postProcessing;

import android.annotation.TargetApi;
import android.opengl.GLES30;
import android.os.Build;
import en.menghui.android.gameengine.ThreeDConversion.ThreeDConversion;
import en.menghui.android.gameengine.bloom.BrightFilter;
import en.menghui.android.gameengine.bloom.CombineFilter;
import en.menghui.android.gameengine.gaussianBlur.HorizontalBlur;
import en.menghui.android.gameengine.gaussianBlur.VerticalBlur;
import en.menghui.android.gameengine.models.RawModel;
import en.menghui.android.gameengine.renderEngine.Loader;
import en.menghui.android.gameengine.renderEngine.MasterRenderer;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class PostProcessing {
	private static final float[] POSITIONS = { -1, 1, -1, -1, 1, 1, 1, -1 };
	private static RawModel quad;
	
	private static ContrastChanger contrastChanger;
	
	private static HorizontalBlur hBlur;
	private static VerticalBlur vBlur;
	private static HorizontalBlur hBlur2;
	private static VerticalBlur vBlur2;
	
	private static BrightFilter brightFilter;
	private static CombineFilter combineFilter;
	
	private static ThreeDConversion threeDConv;
	
	public static void init(Loader loader) {
		quad = loader.loadToVAO(POSITIONS, 2);
		contrastChanger = new ContrastChanger();
		
		hBlur = new HorizontalBlur(MasterRenderer.mWidth/8, MasterRenderer.mHeight/8);
		vBlur = new VerticalBlur(MasterRenderer.mWidth/8, MasterRenderer.mHeight/8);
		hBlur2 = new HorizontalBlur(MasterRenderer.mWidth/2, MasterRenderer.mHeight/2);
		vBlur2 = new VerticalBlur(MasterRenderer.mWidth/2, MasterRenderer.mHeight/2);
		
		brightFilter = new BrightFilter(MasterRenderer.mWidth /2, MasterRenderer.mHeight / 2);
		combineFilter = new CombineFilter();
		
		threeDConv = new ThreeDConversion(MasterRenderer.mWidth, MasterRenderer.mHeight);
	}
	
	public static void doPostProcessing(int colourTexture, int brightTexture) {
		start();
		/*hBlur2.render(colourTexture);
		vBlur2.render(hBlur2.getOutputTexture());
		hBlur.render(vBlur2.getOutputTexture());
		vBlur.render(hBlur.getOutputTexture());
		contrastChanger.render(vBlur.getOutputTexture()); */
		
		/* brightFilter.render(colourTexture);
		// hBlur.render(brightTexture);
		hBlur.render(brightFilter.getOutputTexture());
		vBlur.render(hBlur.getOutputTexture());
		combineFilter.render(colourTexture, vBlur.getOutputTexture()); */
		
		threeDConv.render(colourTexture);
		// contrastChanger.render(threeDConv.getOutputTexture());
		
		// contrastChanger.render(colourTexture);
		end();
	}
	
	public static void cleanUp() {
		contrastChanger.cleanUp();
		
		hBlur.cleanUp();
		vBlur.cleanUp();
		hBlur2.cleanUp();
		vBlur2.cleanUp();
		
		brightFilter.cleanUp();
		combineFilter.cleanUp();
		
		threeDConv.cleanUp();
	}
	
	private static void start() {
		GLES30.glBindVertexArray(quad.getVaoID());
		GLES30.glEnableVertexAttribArray(0);
		GLES30.glDisable(GLES30.GL_DEPTH_TEST);
	}
	
	private static void end() {
		GLES30.glEnable(GLES30.GL_DEPTH_TEST);
		GLES30.glDisableVertexAttribArray(0);
		GLES30.glBindVertexArray(0);
	}
}
