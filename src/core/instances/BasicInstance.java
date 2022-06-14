package core.instances;

import core.animation.Armature;
import core.scene.Scene;
import java.util.LinkedList;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class BasicInstance extends Instance {
    private BasicInstance parent = null;
    private LinkedList<BasicInstance> children = new LinkedList<>();
    public Vector3f pos, rot;
    public float scale;
    private Matrix4f transform;
    private boolean transformUpdated = false;
    private boolean deleted = false;
    
    public BasicInstance(){
        pos = new Vector3f(0.0f);
        rot = new Vector3f(0.0f);
        scale = 1f;
        transform = new Matrix4f();
        parent = null;
        transformUpdated = true;
        color = new Vector3f(1,1,1);
    }
    
    public BasicInstance( BasicInstance parent){
        pos = new Vector3f(0.0f);
        rot = new Vector3f(0.0f);
        scale = 1f;
        transform = new Matrix4f();
        setParent(parent);
    }
    
    /**
     * Returns the transformation matrix of the instance.
     * @return 
     */
    public Matrix4f getTransform(){
        // Calculate the transformation again if updated
        if( transformUpdated ){
            transformUpdated = false;
            if( parent != null)
                transform.set(parent.getTransform()).translate(pos).scale(scale).rotateXYZ(rot);
            else
                transform.translation(pos).scale(scale).rotateXYZ(rot);
            return transform;
        }
        // Used the saved transformation if not updated
        else{
            return transform;
        }
    }
    
    @Override
    public float[] getGLTransform(float[] dest){
        getTransform().get(dest, 0);
        return dest;
    }
    
    /**
     * Marks self and all non-updated-children as in need of an updated transform calculation.
     * Update flags are removed as soon as the calculation is made.
     * Call this function at the end of modifying any of the translation vectors.
     */
    public void updateTransform(){
        if( !transformUpdated ){
            transformUpdated = true;
            for(BasicInstance child:children){
                child.updateTransform();
            }
        }
    }
    
    /**
     * 
     * @param newParent New Parent
     * Correctly sets a new parent (bidirectionally)
     * Set to null for no parent
     */
    public void setParent( BasicInstance newParent){
        // Only apply operations if new parent (prevents duplicates)
        if( parent != newParent){
            
            // Remove child from old parent
            if( parent != null){
                parent.children.remove(this);
            }
            
            // Add child to new parent
            if( newParent != null){
                newParent.children.add(this);
            }
            
            // Update & change parent status
            parent = newParent;
            updateTransform();
        }
    }
    
    @Override
    public boolean isDeleted(){
        return deleted;
    }
    
    /**
     * Marks the instance as deleted.
     * Assigns all children to parent,
     * disables rendering,
     * and marks it for future deletion from InstanceRender and updates.
     * Since it is no longer being used, it should be collected by garbage.
     */
    @Override
    public void delete() {
        deleted = true;
        isVisible = false;
        if (!children.isEmpty()) {
            for (BasicInstance child : children) {
                child.setParent(parent);
            }
        }
        setParent(null);
    }

}

