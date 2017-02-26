package en.menghui.android.gameengine.particles;

public class ParticleTexture {
	private int textureID;
	private int numberOfRows;
	
	private boolean additive = false;
	
	public ParticleTexture(int textureID, int numberOfRows) {
		this.textureID = textureID;
		this.numberOfRows = numberOfRows;
	}

	public int getTextureID() {
		return textureID;
	}

	public int getNumberOfRows() {
		return numberOfRows;
	}
	
	public boolean usesAdditiveBlending() {
		return this.additive;
	}
	
	
}
