package en.menghui.android.gameengine.environmentMapRenderer;

import en.menghui.android.gameengine.entities.Camera;
import en.menghui.android.gameengine.toolbox.Maths;
import android.annotation.TargetApi;
import android.opengl.GLES30;
import android.os.Build;
import android.renderscript.Matrix4f;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class SkyboxRenderer {
	private SkyboxShader shader;
    private Matrix4f projectionMatrix;
    private CubeMap cubeMap;
     
    public SkyboxRenderer(CubeMap cubeMap, Matrix4f projectionMatrix){
        this.projectionMatrix = projectionMatrix;
        shader = new SkyboxShader();
        this.cubeMap = cubeMap;
    }
     
    public void render(Camera camera){
        shader.start();
        loadProjectionViewMatrix(camera);
        bindTexture();
        bindCubeVao();
        GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, cubeMap.getCube().getVertexCount());
        unbindCubeVao();
        shader.stop();
    }
    
    public void cleanUp(){
        shader.cleanUp();
    }
     
    private void bindCubeVao(){
        GLES30.glBindVertexArray(cubeMap.getCube().getVaoID());
        GLES30.glEnableVertexAttribArray(0);
    }
     
    private void unbindCubeVao(){
        GLES30.glDisableVertexAttribArray(0);
        GLES30.glBindVertexArray(0);
    }
     
    private void bindTexture(){
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
        GLES30.glBindTexture(GLES30.GL_TEXTURE_CUBE_MAP, cubeMap.getTexture());
    }
     
    private void loadProjectionViewMatrix(Camera camera){
        Matrix4f viewMatrix = Maths.createViewMatrix(camera);
        // Matrix4f projectionViewMatrix = Matrix4f.mul(projectionMatrix, viewMatrix, null);
        Matrix4f projectionViewMatrix = projectionMatrix;
        projectionViewMatrix.multiply(viewMatrix);
        shader.loadProjectionViewMatrix(projectionViewMatrix.getArray());
    }   
}
