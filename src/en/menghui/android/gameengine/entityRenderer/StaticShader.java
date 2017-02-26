package en.menghui.android.gameengine.entityRenderer;

import android.annotation.TargetApi;
import android.os.Build;
import android.renderscript.Matrix4f;
import en.menghui.android.gameengine.entities.Camera;
import en.menghui.android.gameengine.shaders.ShaderProgram;
import en.menghui.android.gameengine.toolbox.Maths;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class StaticShader extends ShaderProgram {
	private static final String VERTEX_FILE = "/en/menghui/android/gameengine/entityRenderer/vertex_shader.txt";
	private static final String FRAGMENT_FILE = "/en/menghui/android/gameengine/entityRenderer/fragment_shader.txt";
    
    private int location_transformationMatrix;
    private int location_projectionMatrix;
    private int location_viewMatrix;
    private int location_modelTexture;
    private int location_cameraPosition;
    private int location_enviroMap;
 
    public StaticShader() {
        super(VERTEX_FILE, FRAGMENT_FILE);
    }
 
    @Override
    protected void bindAttributes() {
        super.bindAttribute(0, "position");
        super.bindAttribute(1, "textureCoordinates");
        super.bindAttribute(2, "normal");
    }
 
    @Override
    protected void getAllUniformLocations() {
        location_transformationMatrix = super.getUniformLocation("transformationMatrix");
        location_projectionMatrix = super.getUniformLocation("projectionMatrix");
        location_viewMatrix = super.getUniformLocation("viewMatrix");
        location_modelTexture = super.getUniformLocation("modelTexture");
        this.location_cameraPosition = super.getUniformLocation("cameraPosition");
        location_enviroMap = super.getUniformLocation("enviroMap");
         
    }
     
    protected void connectTextureUnits(){
        super.loadInt(location_modelTexture, 0);
        super.loadInt(location_enviroMap, 1);
    }
     
    public void loadTransformationMatrix(float[] matrix){
        super.loadMatrix(location_transformationMatrix, matrix);
    }
     
    public void loadViewMatrix(Camera camera){
        Matrix4f viewMatrix = Maths.createViewMatrix(camera);
        super.loadMatrix(location_viewMatrix, viewMatrix.getArray());
        super.loadVector(location_cameraPosition, camera.getPosition());
    }
    
    public void loadProjectionMatrix(float[] projection){
        super.loadMatrix(location_projectionMatrix, projection);
    }
}
