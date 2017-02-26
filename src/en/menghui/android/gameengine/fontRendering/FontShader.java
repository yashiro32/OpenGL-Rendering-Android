package en.menghui.android.gameengine.fontRendering;

import en.menghui.android.gameengine.shaders.ShaderProgram;

public class FontShader extends ShaderProgram {
	
	private static final String VERTEX_FILE = "/en/menghui/android/gameengine/fontRendering/font_vertex_shader.txt";
	private static final String FRAGMENT_FILE = "/en/menghui/android/gameengine/fontRendering/font_fragment_shader.txt";
	
	private int location_colour;
	private int location_translation;
	
	private int location_width;
	private int location_edge;
	
	private int location_borderWidth;
	private int location_borderEdge;
	private int location_outlineColour;
	private int location_offset;

	public FontShader() {
		super(VERTEX_FILE, FRAGMENT_FILE);
	}

	@Override
	protected void getAllUniformLocations() {
		location_colour = super.getUniformLocation("colour");
		location_translation = super.getUniformLocation("translation");
		
		location_width = super.getUniformLocation("width");
		location_edge = super.getUniformLocation("edge");
		location_borderWidth = super.getUniformLocation("borderWidth");
		location_borderEdge = super.getUniformLocation("borderEdge");
		location_outlineColour = super.getUniformLocation("outlineColour");
		location_offset = super.getUniformLocation("offset");
	}

	@Override
	protected void bindAttributes() {
		super.bindAttribute(0, "position");
		super.bindAttribute(1, "textureCoords");
	}
	
	protected void loadColour(float[] colour) {
		super.loadVector(location_colour, colour);
	}
	
	protected void loadTranslation(float[] translation) {
		super.load2DVector(location_translation, translation);
	}
	
	
	public void loadWidth(float width) {
		super.loadFloat(location_width, width);
	}
	
	public void loadEdge(float edge) {
		super.loadFloat(location_edge, edge);
	}
	
	public void loadBorderWidth(float width) {
		super.loadFloat(location_borderWidth, width);
	}
	
	public void loadBorderEdge(float edge) {
		super.loadFloat(location_borderEdge, edge);
	}
	
	public void loadOutlineColour(float[] color) {
		super.loadVector(location_outlineColour, color);
	}
	
	public void loadOffset(float[] offset) {
		super.load2DVector(location_offset, offset);
	}
	
	
}
