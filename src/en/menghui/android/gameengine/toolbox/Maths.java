package en.menghui.android.gameengine.toolbox;

import en.menghui.android.gameengine.entities.Camera;
import android.annotation.TargetApi;
import android.os.Build;
import android.renderscript.Matrix4f;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class Maths {
	public static Matrix4f createTransformationMatrix(float[] translation, float rx, float ry, float rz, float scale) {
		Matrix4f matrix = new Matrix4f();
		matrix.loadIdentity();
		matrix.translate(translation[0], translation[1], translation[2]);
		matrix.rotate(rx, 1, 0, 0);
		matrix.rotate(ry, 0, 1, 0);
		matrix.rotate(rz, 0, 0, 1);
		matrix.scale(scale, scale, scale);
		
		return matrix;
	}
	
	public static Matrix4f createTransformationMatrix(float[] translation, float[] scale) {
		Matrix4f matrix = new Matrix4f();
		matrix.loadIdentity();
		matrix.translate(translation[0], translation[1], 0);
		matrix.scale(scale[0], scale[1], 1f);
		return matrix;
	}
	
	public static Matrix4f createViewMatrix(Camera camera) {
		Matrix4f viewMatrix = new Matrix4f();
		viewMatrix.loadIdentity();
		viewMatrix.rotate(camera.getPitch(), 1, 0, 0);
		viewMatrix.rotate(camera.getYaw(), 0, 1, 0);
		viewMatrix.translate(-camera.getPosition()[0], -camera.getPosition()[1], -camera.getPosition()[2]);
		
		return viewMatrix;
	}
	
	public static float barryCentric(float[] p1, float[] p2, float[] p3, float[] pos) {
		float det = (p2[2] - p3[2]) * (p1[0] - p3[0]) + (p3[0] - p2[0]) * (p1[2] - p3[2]);
		float l1 = ((p2[2] - p3[2]) * (pos[0] - p3[0]) + (p3[0] - p2[0]) * (pos[1] - p3[2])) / det;
		float l2 = ((p3[2] - p1[2]) * (pos[0] - p3[0]) + (p1[0] - p3[0]) * (pos[1] - p3[2])) / det;
		float l3 = 1.0f - l1 - l2;
		return l1 * p1[1] + l2 * p2[1] + l3 * p3[1];
	}
	
	public static float[] normalizeArray(float[] arr) {
		float max = -Float.MAX_VALUE;
		float min = Float.MAX_VALUE;
		for (int i = 0; i < arr.length; i++) {
			if (arr[i] > max) {
				max = arr[i];
			} else if (arr[i] < min) {
				min = arr[i];
			}
		}
		
		float[] array = arr.clone();
		for (int i = 0; i < arr.length; i++) {
			// array[i] = array[i] / max;
			array[i] = (arr[i] - min) / (max - min);
		}
		
		return array;
	}
	
	public static float[] normaliseArray(float[] arr) {
		float l = 0;
		
		for (int i = 0; i < arr.length; i++) {
			l += arr[i] * arr[i];
		}
		l = (float) Math.sqrt(l);
		
		float[] dest = arr.clone();
		
		for (int i = 0; i < arr.length; i++) {
			dest[i] /= l;
		}
		
		return dest;
	}
	
	public static float[] scaleArray(float[] arr, float scale) {
		float[] array = arr.clone();
		
		for (int i = 0; i < arr.length; i++) {
			array[i] *= scale;
		}
		
		return array;
	}
	
	public static float[] transformMatrix(Matrix4f matrix, float[] vector) {
		float x = matrix.get(0, 0) * vector[0] + matrix.get(1, 0) * vector[1] + matrix.get(2, 0) * vector[2] + matrix.get(3, 0) * vector[3];
		float y = matrix.get(0, 1) * vector[0] + matrix.get(1, 1) * vector[1] + matrix.get(2, 1) * vector[2] + matrix.get(3, 1) * vector[3];
		float z = matrix.get(0, 2) * vector[0] + matrix.get(1, 2) * vector[1] + matrix.get(2, 2) * vector[2] + matrix.get(3, 2) * vector[3];
		float w = matrix.get(0, 3) * vector[0] + matrix.get(1, 3) * vector[1] + matrix.get(2, 3) * vector[2] + matrix.get(3, 3) * vector[3];
		
		float[] vec = {x, y, z, w};
		
		return vec;
	}
	
	public static float arrayLength(float[] arr) {
		float l = 0;
		
		for (int i = 0; i < arr.length; i++) {
			l += arr[i] * arr[i];
		}
		l = (float) Math.sqrt(l);
		
		return l;
	}
	
	public static float arrayLengthSquared(float[] arr) {
		float l = 0;
		
		for (int i = 0; i < arr.length; i++) {
			l += arr[i] * arr[i];
		}
		
		return l;
	}
	
	public static float[] addArrays(float[] arr1, float[] arr2) {
		float[] arr = new float[arr1.length];
		for (int i = 0; i < arr1.length; i++) {
			arr[i] = arr1[i] + arr2[i];
		}
		
		return arr;
	}
	
	public static float[] subArrays(float[] arr1, float[] arr2) {
		float[] arr = new float[arr1.length];
		for (int i = 0; i < arr1.length; i++) {
			arr[i] = arr1[i] - arr2[i];
		}
		
		return arr;
	}
	
	public static float[] crossMulVec3Arrays(float[] left, float[] right) {
		float[] dest = new float[left.length];
		dest[0] = left[1] * right[2] - left[2] * right[1];
		dest[1] = right[0] * left[2] - right[2] * left[0];
		dest[2] = left[0] * right[1] - left[1] * right[0];
		
		return dest;
	}
	
	public static float dotMulVec3Arrays(float[] left, float[] right) {
		return left[0] * right[0] + left[1] * right[1] + left[2] * right[2];
	}
	
	public static float[] deepCloneArray(float[] arr) {
		float[] array = new float[arr.length];
		for (int i = 0; i < arr.length; i++) {
			array[i] = arr[i];
		}
		
		return array;
	}
	
	public static float[] negateArray(float[] arr) {
		float[] array = new float[arr.length];
		for (int i = 0; i < arr.length; i++) {
			array[i] = -arr[i];
		}
		
		return array;
	}
}
