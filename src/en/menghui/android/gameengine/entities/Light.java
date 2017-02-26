package en.menghui.android.gameengine.entities;

public class Light {
	private float[] position;
	private float[] colour;
	private float[] attenuation = {1, 0, 0};
	
	public Light(float[]position,  float[] colour) {
		this.position = position;
		this.colour = colour;
	}
	
	public Light(float[]position,  float[] colour, float[] attenuation) {
		this.position = position;
		this.colour = colour;
		this.attenuation = attenuation;
	}
	
	public float[] getAttenuation() {
		return this.attenuation;
	}

	public float[] getPosition() {
		return position;
	}

	public void setPosition(float[] position) {
		this.position = position;
	}

	public float[] getColour() {
		return colour;
	}

	public void setColour(float[] colour) {
		this.colour = colour;
	}
}
