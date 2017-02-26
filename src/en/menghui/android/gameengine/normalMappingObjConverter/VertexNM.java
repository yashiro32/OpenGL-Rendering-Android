package en.menghui.android.gameengine.normalMappingObjConverter;

import java.util.ArrayList;
import java.util.List;

import en.menghui.android.gameengine.toolbox.Maths;

public class VertexNM {
	/* private static final int NO_INDEX = -1;
	
	private float[] position;
	private int textureIndex = NO_INDEX;
	private int normalIndex = NO_INDEX;
	private VertexNM duplicateVertex = null;
	private int index;
	private float length;
	private List<float[]> tangents = new ArrayList<float[]>();
	private float[] averagedTangent = {0, 0, 0};
	
	protected VertexNM(int index, float[] position) {
		this.index = index;
		this.position = position;
		this.length = position.length;
	}
	
	protected void addTangent(float[] tangent) {
		tangents.add(tangent);
	}
	
	// NEW
	protected VertexNM duplicate(int newIndex) {
		VertexNM vertex = new VertexNM(newIndex, position);
		vertex.tangents = this.tangents;
		return vertex;
	}
	
	protected void averageTangents() {
		if (tangents.isEmpty()) {
			return;
		}
		for (float[] tangent : tangents) {
			for (int i = 0; i < tangent.length; i++) {
				averagedTangent[i] += tangent[i];
			}
			averagedTangent = Maths.normaliseArray(averagedTangent);
		}
	}
	
	protected float[] getAverageTangent() {
		return averagedTangent;
	}
	
	protected int getIndex() {
		return index;
	}
	
	protected float getLength() {
		return length;
	}
	
	protected boolean isSet() {
		return textureIndex != NO_INDEX && normalIndex != NO_INDEX;
	}
	
	protected boolean hasSameTextureAndNormal(int textureIndexOther, int normalIndexOther) {
		return textureIndexOther == textureIndex && normalIndexOther == normalIndex;
	}
	
	protected void setTextureIndex(int textureIndex) {
		this.textureIndex = textureIndex;
	}
	
	protected void setNormalIndex(int normalIndex) {
		this.normalIndex = normalIndex;
	}
	
	protected float[] getPosition() {
		return position;
	}
	
	protected int getTextureIndex() {
		return textureIndex;
	}
	
	protected int getNormalIndex() {
		return normalIndex;
	}
	
	protected VertexNM getDuplicateVertex() {
		return duplicateVertex;
	}
	
	protected void setDuplicateVertex(VertexNM duplicateVertex) {
		this.duplicateVertex = duplicateVertex;
	} */
	
	
	private static final int NO_INDEX = -1;
    
    private float[] position;
    private int textureIndex = NO_INDEX;
    private int normalIndex = NO_INDEX;
    private VertexNM duplicateVertex = null;
    private int index;
    private float length;
    private List<float[]> tangents = new ArrayList<float[]>();
    private float[] averagedTangent = {0, 0, 0};
    
    protected VertexNM(int index, float[] position){
        this.index = index;
        this.position = position;
        this.length = Maths.arrayLength(position);
    }
     
    protected void addTangent(float[] tangent){
        tangents.add(tangent);
    }
     
    //NEW
    protected VertexNM duplicate(int newIndex){
        VertexNM vertex = new VertexNM(newIndex, position);
        vertex.tangents = this.tangents;
        return vertex;
    }
     
    protected void averageTangents(){
        if (tangents.isEmpty()) {
			return;
		}
		for (float[] tangent : tangents) {
			for (int i = 0; i < tangent.length; i++) {
				averagedTangent[i] += tangent[i];
			}
			averagedTangent = Maths.normaliseArray(averagedTangent);
		}
    }
    
    protected float[] getAverageTangent(){
        return averagedTangent;
    }
     
    protected int getIndex(){
        return index;
    }
     
    protected float getLength(){
        return length;
    }
     
    protected boolean isSet(){
        return textureIndex!=NO_INDEX && normalIndex!=NO_INDEX;
    }
     
    protected boolean hasSameTextureAndNormal(int textureIndexOther,int normalIndexOther){
        return textureIndexOther==textureIndex && normalIndexOther==normalIndex;
    }
     
    protected void setTextureIndex(int textureIndex){
        this.textureIndex = textureIndex;
    }
     
    protected void setNormalIndex(int normalIndex){
        this.normalIndex = normalIndex;
    }
 
    protected float[] getPosition() {
        return position;
    }
 
    protected int getTextureIndex() {
        return textureIndex;
    }
 
    protected int getNormalIndex() {
        return normalIndex;
    }
 
    protected VertexNM getDuplicateVertex() {
        return duplicateVertex;
    }
 
    protected void setDuplicateVertex(VertexNM duplicateVertex) {
        this.duplicateVertex = duplicateVertex;
    }
    
    
}
