package core.instances;

import com.bulletphysics.collision.dispatch.CollisionFlags;
import com.bulletphysics.collision.shapes.SphereShape;
import com.bulletphysics.dynamics.RigidBodyConstructionInfo;
import com.bulletphysics.linearmath.DefaultMotionState;
import com.bulletphysics.linearmath.Transform;
import core.audio.SoundSource;
import javax.vecmath.Matrix4f;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

public class SphereInstance extends PhysicsInstance{
    
    private static RigidBodyConstructionInfo construction =
    new RigidBodyConstructionInfo(
    1,
    new DefaultMotionState(),
    new SphereShape(1)
    );
    
    private static SoundSource sound = new SoundSource(true, true, "ringtone");
    
    
    public SphereInstance(){
        super(construction);
        color.set((float)Math.random(),(float)Math.random(),(float)Math.random());
        rigidBody.setWorldTransform(new Transform(new Matrix4f(new Quat4f(0,0,0,1), new Vector3f((float)Math.random()*10,(float)Math.random()*10+10,(float)Math.random()*10),1)));
        rigidBody.setAngularVelocity(new Vector3f((float)Math.random()*1,(float)Math.random()*1,(float)Math.random()*1));
        rigidBody.setRestitution(.25f);
        scale = (float)Math.random()*1f+.1f;
        rigidBody.setCollisionShape(new SphereShape(scale));
        rigidBody.setDamping(.1f, .1f);
        rigidBody.setCollisionFlags(CollisionFlags.CUSTOM_MATERIAL_CALLBACK);
    }
    
    
    
}
