package en.menghui.android.gameengine.geometryShading;

import android.renderscript.Matrix4f;

public class StaticShader extends ShaderProgram {
	private static final String VERTEX_FILE = "/en/menghui/android/gameengine/geometryShading/vertex_shader.txt";
	private static final String GEOMETRY_FILE = "/en/menghui/android/gameengine/geometryShading/geometry_shader.txt";
    private static final String FRAGMENT_FILE = "/en/menghui/android/gameengine/geometryShading/fragment_shader.txt";
     
    private int location_projectionViewMatrix;
 
    public StaticShader() {
        super(VERTEX_FILE, FRAGMENT_FILE, GEOMETRY_FILE);
    }
    
    @Override
    protected void bindAttributes() {
        super.bindAttribute(0, "position");
    }
    
    @Override
    protected void getAllUniformLocations() {
        location_projectionViewMatrix = super.getUniformLocation("projectionViewMatrix");
         
    }
    
    public void loadProjectionViewMatrix(float[] matrix){
        super.loadMatrix(location_projectionViewMatrix, matrix);
    }
}
