package core.instances;

import core.animation.Armature;
import core.scene.Scene;
import org.joml.Vector3f;

public abstract class Instance {
    
    protected boolean isVisible;
    protected Armature armature;
    protected Vector3f color;
    
    public Instance(){
        isVisible = true;
        armature = null;
        color = new Vector3f(1,1,1);
    }
    
    public void setVisibility(boolean isVisible){
        this.isVisible = isVisible;
    }
    
    public boolean isVisible(){
        return isVisible;
    }
    
    public boolean hasArmature(){
        return armature != null;
    }
    
    public void setArmature(Armature armature){
        this.armature = armature;
    }
    
    public Armature getArmature(){
        return armature;
    }
    
    public Vector3f getColor(){
        return color;
    }
    
    public void setColor(Vector3f color){
        this.color = color;
    }
    
    /**
     * Used to mark an instance as deleted and cleanly removes any linked objects.
     * All deleted objects should not be visible or participating in any simulation or inheritance.
     * After being called, the instance can safely be removed from a list based on its deleted state.
     */
    public abstract void delete();
    
    /**
     * Returns whether the instance is deleted and can be safely removed.
     * @return 
     */
    public abstract boolean isDeleted();
    
    public abstract float[] getGLTransform(float[] dest);
}