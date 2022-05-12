package core.instances;

import core.animation.Armature;
import java.util.LinkedList;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class Instance{
    private String name;
    private Armature armature = null;
    private Instance parent = null;
    private LinkedList<Instance> children = new LinkedList<>();
    public Vector3f pos, rot;
    public float scale;
    private Matrix4f transform;
    private boolean transformUpdated = false;
    public boolean visible = true;
    private boolean deleted = false;
    
    public Instance(){
        pos = new Vector3f(0.0f);
        rot = new Vector3f(0.0f);
        scale = 1f;
        transform = new Matrix4f();
        parent = null;
        transformUpdated = true;
    }
    
    public Instance( Instance parent){
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
    
    /**
     * Marks self and all non-updated-children as in need of an updated transform calculation.
     * Update flags are removed as soon as the calculation is made.
     * Call this function at the end of modifying any of the translation vectors.
     */
    public void updateTransform(){
        if( !transformUpdated ){
            transformUpdated = true;
            for(Instance child:children){
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
    public void setParent( Instance newParent){
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
    public void markDeletion() {
        deleted = true;
        visible = false;
        if (!children.isEmpty()) {
            for (Instance child : children) {
                child.setParent(parent);
            }
        }
        setParent(null);
    }
    
    public String getName(){
        return name;
    }
    
    private void setName(String name){
        this.name = name;
    }
    
    public void setArmature(Armature armature){
        this.armature = armature;
    }
    
    public boolean hasArmature(){
        return armature != null;
    }
    
    public Armature getArmature(){
        return armature;
    }
    
}

