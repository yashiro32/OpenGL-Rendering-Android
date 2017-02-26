package en.menghui.android.gameengine.shadows;

import android.annotation.TargetApi;
import android.os.Build;
import android.renderscript.Matrix4f;
import en.menghui.android.gameengine.shaders.ShaderProgram;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class ShadowShader extends ShaderProgram {
	private static final String VERTEX_FILE = "/en/menghui/android/gameengine/shadows/shadow_vertex_shader.txt";
	private static final String FRAGMENT_FILE = "/en/menghui/android/gameengine/shadows/shadow_fragment_shader.txt";
	
	private int location_mvpMatrix;

	public ShadowShader() {
		super(VERTEX_FILE, FRAGMENT_FILE);
	}

	@Override
	protected void getAllUniformLocations() {
		location_mvpMatrix = super.getUniformLocation("mvpMatrix");
	}
	
	protected void loadMvpMatrix(Matrix4f mvpMatrix) {
		super.loadMatrix(location_mvpMatrix, mvpMatrix.getArray());
	}

	@Override
	protected void bindAttributes() {
		super.bindAttribute(0, "in_position");
		super.bindAttribute(1, "in_textureCoords");
	}
	
}
