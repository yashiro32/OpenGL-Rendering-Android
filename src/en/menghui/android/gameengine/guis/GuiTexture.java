package en.menghui.android.gameengine.guis;

public class GuiTexture {
	private int texture;
	private float[] position;
	private float[] scale;
	public GuiTexture(int texture, float[] position, float[] scale) {
		this.texture = texture;
		this.position = position;
		this.scale = scale;
	}
	public int getTexture() {
		return texture;
	}
	public float[] getPosition() {
		return position;
	}
	public float[] getScale() {
		return scale;
	}
	
	
}
