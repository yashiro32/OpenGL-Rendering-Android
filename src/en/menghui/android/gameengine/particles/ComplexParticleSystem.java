package en.menghui.android.gameengine.particles;

import java.util.Random;
import android.annotation.TargetApi;
import android.os.Build;
import android.renderscript.Matrix4f;
import en.menghui.android.gameengine.renderEngine.MasterRenderer;
import en.menghui.android.gameengine.toolbox.Maths;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class ComplexParticleSystem {
	private float pps, averageSpeed, gravityComplient, averageLifeLength, averageScale;
	
	private float speedError, lifeError, scaleError = 0;
	private boolean randomRotation = false;
	private float[] direction;
	private float directionDeviation = 0;
	
	private ParticleTexture texture;
	
	private Random random = new Random();
	
	public ComplexParticleSystem(ParticleTexture texture, float pps, float speed, float gravityComplient, float lifeLength, float scale) {
		this.pps = pps;
		this.averageSpeed = speed;
		this.gravityComplient = gravityComplient;
		this.averageLifeLength = lifeLength;
		this.averageScale = scale;
		this.texture = texture;
	}
	
	/**
	 * @param direction - The average direction in which particles are emitted.
	 * @param deviation - A value 0 and 1 indicating how far from the chosen direction particles can deviate.
	 */
	public void setDirection(float[] direction, float deviation) {
		this.direction = direction;
		this.directionDeviation = (float) (deviation * Math.PI);
	}
	
	public void randomizeRotation() {
		randomRotation = true;
	}
	
	/**
	 * @param error - A number between 0 and 1, where 0 means no error margin.
	 */
	public void setSpeedError(float error) {
		this.speedError = error * averageSpeed;
	}
	
	/**
	 * @param error - A number between 0 and 1, where 0 means no error margin.
	 */
	public void setLifeError(float error) {
		this.lifeError = error * averageLifeLength;
	}
	
	/**
	 * @param error - A number between 0 and 1, where 0 means no error margin.
	 */
	public void setScaleError(float error) {
		this.scaleError = error * averageScale;
	}
	
	public void generateParticles(float[] systemCenter) {
		float delta = MasterRenderer.getFrameTimeSeconds();
		float particlesToCreate = pps * delta;
		int count = (int) Math.floor(particlesToCreate);
		float partialParticle = particlesToCreate % 1;
		for (int i = 0; i < count; i++) {
			emitParticle(systemCenter);
		}
		if (Math.random() < partialParticle) {
			emitParticle(systemCenter);
		}
	}
	
	private void emitParticle(float[] center) {
		float[] velocity = new float[3];
		if (direction != null) {
			velocity = generateRandomUnitVectorWithinCone(direction, directionDeviation);
		} else {
			velocity = generateRandomUnitVector();
		}
		velocity = Maths.normaliseArray(velocity);
		velocity = Maths.scaleArray(velocity, generateValue(averageSpeed, speedError));
		float scale = generateValue(averageScale, scaleError);
		float lifeLength = generateValue(averageLifeLength, lifeError);
		new Particle(texture, center, velocity, gravityComplient, lifeLength, generateRotation(), scale);
	}
	
	private float generateValue(float average, float errorMargin) {
		float offset = (random.nextFloat() - 0.5f) * 2f * errorMargin;
		return average + offset;
	}
	
	private float generateRotation() {
		if (randomRotation) {
			return random.nextFloat() * 360f;
		} else {
			return 0;
		}
	}
	
	private static float[] generateRandomUnitVectorWithinCone(float[] coneDirection, float angle) {
		float cosAngle = (float) Math.cos(angle);
		Random random = new Random();
		float theta = (float) (random.nextFloat() * 2f * Math.PI);
		float z = cosAngle + (random.nextFloat() * (1 - cosAngle));
		float rootOneMinusZSquared = (float) Math.sqrt(1 - z * z);
		float x = (float) (rootOneMinusZSquared * Math.cos(theta));
		float y = (float) (rootOneMinusZSquared * Math.sin(theta));
		
		float[] direction = {x, y, z, 1};
		if (coneDirection[0] != 0 || coneDirection[1] != 0 || (coneDirection[2] != 1 && coneDirection[2] != -1)) {
			float[] vec = {0, 0, 1};
			float[] rotateAxis = Maths.crossMulVec3Arrays(coneDirection, vec);
			rotateAxis = Maths.normaliseArray(rotateAxis);
			float rotateAngle = (float) Math.acos(Maths.dotMulVec3Arrays(coneDirection, vec));
			Matrix4f rotationMatrix = new Matrix4f();
			rotationMatrix.rotate(-rotateAngle, rotateAxis[0], rotateAxis[1], rotateAxis[2]);
			direction = Maths.transformMatrix(rotationMatrix, direction);
		} else if (coneDirection[2] == -1) {
			direction[2] *= -1;
		}
		
		return direction;
	}
	
	private float[] generateRandomUnitVector() {
		float theta = (float) (random.nextFloat() * 2f * Math.PI);
		float z = (random.nextFloat() * 2) - 1;
		float rootOneMinusZSquared = (float) Math.sqrt(1 - z * z);
		float x = (float) (rootOneMinusZSquared * Math.cos(theta));
		float y = (float) (rootOneMinusZSquared * Math.sin(theta));
		float[] vec = {x, y, z};
		
		return vec;
	}
}
