package en.menghui.android.gameengine.gaussianBlur;

import en.menghui.android.gameengine.shaders.ShaderProgram;

public class HorizontalBlurShader extends ShaderProgram {
	private static final String VERTEX_FILE = "/en/menghui/android/gameengine/gaussianBlur/horizontal_blur_vertex_shader.txt";
	private static final String FRAGMENT_FILE = "/en/menghui/android/gameengine/gaussianBlur/blur_fragment_shader.txt";
     
    private int location_targetWidth;
     
    protected HorizontalBlurShader() {
        super(VERTEX_FILE, FRAGMENT_FILE);
    }
 
    protected void loadTargetWidth(float width){
        super.loadFloat(location_targetWidth, width);
    }
     
    @Override
    protected void getAllUniformLocations() {
        location_targetWidth = super.getUniformLocation("targetWidth");
    }
 
    @Override
    protected void bindAttributes() {
        super.bindAttribute(0, "position");
    }
    
    
}
