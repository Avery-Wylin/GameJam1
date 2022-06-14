package core.instances;

import com.bulletphysics.collision.narrowphase.ManifoldPoint;
import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.dynamics.RigidBodyConstructionInfo;
import com.bulletphysics.linearmath.Transform;
import core.scene.Scene;
import org.joml.Vector3f;

public class PhysicsInstance extends Instance{
    boolean deleted;
    public RigidBody rigidBody;
    protected Transform transform;
    protected float scale;
    private static int count = 0;
    
    public PhysicsInstance(RigidBodyConstructionInfo constructionInfo){
        deleted = false;
        rigidBody = new RigidBody(constructionInfo);
        transform = new Transform();
        scale = 1;
        rigidBody.setUserPointer(this);
    }
    
    public void disable(){
        rigidBody.setActivationState(RigidBody.DISABLE_SIMULATION);
    }
    
    public void activate(){
        rigidBody.setActivationState(RigidBody.ACTIVE_TAG);
    }
    
    @Override
    public void delete() {
        deleted = true;
        isVisible = false;
        Scene.activeScene.physics.dynamicsWorld.removeRigidBody(rigidBody);
        rigidBody.destroy();
    }

    @Override
    public boolean isDeleted() {
        return deleted;
    }

    @Override
    public float[] getGLTransform(float[] dest) {
        rigidBody.getWorldTransform(transform);
        transform.basis.mul(scale);
        transform.getOpenGLMatrix(dest);
        return dest;
    }
    
    public void collisionAction(Object otherCollider, ManifoldPoint point){
        if(armature != null){
            armature.setTimeMultiplier(3f);
        }
    }
    
    
    
    
}
