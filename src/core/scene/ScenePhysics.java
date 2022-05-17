package core.scene;

import com.bulletphysics.collision.broadphase.*;
import com.bulletphysics.collision.dispatch.*;
import com.bulletphysics.collision.shapes.*;
import com.bulletphysics.dynamics.*;
import com.bulletphysics.dynamics.constraintsolver.*;
import com.bulletphysics.linearmath.*;
import core.instances.Instance;
import java.util.ArrayList;
import javax.vecmath.Matrix4f;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

public class ScenePhysics {
    private DynamicsWorld dynamicsWorld;
    private ArrayList<RigidBody> rigidBodies;
    
    public RigidBody testRigidBody;
    CollisionObject groundObject;

    public void init(){
        
        // Initialize the World
        BroadphaseInterface broadphase = new DbvtBroadphase();
        CollisionConfiguration collisionConfiguration = new DefaultCollisionConfiguration();
        CollisionDispatcher dispatcher = new CollisionDispatcher(collisionConfiguration);
        ConstraintSolver constraintSolver = new SequentialImpulseConstraintSolver();
        dynamicsWorld = new DiscreteDynamicsWorld(dispatcher, broadphase, constraintSolver, collisionConfiguration);
        dynamicsWorld.setGravity(new Vector3f(0,-10f,0));
        
        // Create Ground
        
        // Bottom
        groundObject = new RigidBody(0, new DefaultMotionState(), new StaticPlaneShape(new Vector3f(0,1,0), 0f));
        groundObject.setRestitution(.6f);
        groundObject.setFriction(4f);
        dynamicsWorld.addCollisionObject(groundObject);
        
        // +Z
        groundObject = new RigidBody(0, new DefaultMotionState(), new StaticPlaneShape(new Vector3f(0,.5f,1), -20f));
        groundObject.setRestitution(.6f);
        groundObject.setFriction(4f);
        dynamicsWorld.addCollisionObject(groundObject);
        
        // -Z
        groundObject = new RigidBody(0, new DefaultMotionState(), new StaticPlaneShape(new Vector3f(0,.5f,-1), -20f));
        groundObject.setRestitution(.6f);
        groundObject.setFriction(4f);
        dynamicsWorld.addCollisionObject(groundObject);
        
        // +X
        groundObject = new RigidBody(0, new DefaultMotionState(), new StaticPlaneShape(new Vector3f(1,.5f,0), -20f));
        groundObject.setRestitution(.6f);
        groundObject.setFriction(4f);
        dynamicsWorld.addCollisionObject(groundObject);
        
        // -X
        groundObject = new RigidBody(0, new DefaultMotionState(), new StaticPlaneShape(new Vector3f(-1,.5f,0), -20f));
        groundObject.setRestitution(.6f);
        groundObject.setFriction(4f);
        dynamicsWorld.addCollisionObject(groundObject);
        
        // Create Test Sphere
        CollisionShape testCollision = new SphereShape(1f);
        MotionState testMotion = new DefaultMotionState(new Transform(new Matrix4f(new Quat4f(0,0,0,1f), new Vector3f(0,10f,0),1f)));
        Vector3f inertia = new Vector3f();
        testCollision.calculateLocalInertia(1f, inertia);
        RigidBodyConstructionInfo testConstructionInfo = new RigidBodyConstructionInfo(1, testMotion, testCollision, inertia);
        
        rigidBodies = new ArrayList<>();
        
        for(int i=0; i<200; i++){
            testRigidBody = new RigidBody(testConstructionInfo);
            testRigidBody.setWorldTransform(new Transform(new Matrix4f(new Quat4f(0,0,0,1), new Vector3f((float)Math.random()*10,(float)Math.random()*10+10,(float)Math.random()*10),1)));
            //testRigidBody.setLinearVelocity(new Vector3f(0,.1f,0));
            testRigidBody.setAngularVelocity(new Vector3f((float)Math.random()*1,(float)Math.random()*1,(float)Math.random()*1));
            testRigidBody.setRestitution(.25f);
            testRigidBody.setDamping(.5f, .5f);
            rigidBodies.add(testRigidBody);
            dynamicsWorld.addRigidBody(testRigidBody);
        }
        
        testRigidBody = rigidBodies.get(0);
        
    }
    
    public void attractTowards(float x, float y, float z, float v){
        Transform t = new Transform();
        Vector3f force = new Vector3f();
        for(RigidBody r:rigidBodies){
            r.setActivationState(RigidBody.ACTIVE_TAG);
            r.getWorldTransform(t);
            force.set(x, y, z);
            force.sub(t.origin);
            force.normalize();
            force.scale(v);
            r.applyCentralForce(force);
        }
    }
    
    public void update(ArrayList<Instance> instances, float delta){
        dynamicsWorld.stepSimulation(delta);
        
        for(int i=0;i<200;i++){
            Instance instance = instances.get(i);
            Transform testTransform = new Transform();
            rigidBodies.get(i).getWorldTransform(testTransform);
            instance.pos.x = testTransform.origin.x;
            instance.pos.y = testTransform.origin.y;
            instance.pos.z = testTransform.origin.z;

            Quat4f testQuat = new Quat4f();
            testTransform.getRotation(testQuat);
            org.joml.Quaternionf quat = new org.joml.Quaternionf();
            quat.x = testQuat.x;
            quat.y = testQuat.y;
            quat.z = testQuat.z;
            quat.w = testQuat.w;
            quat.getEulerAnglesXYZ(instance.rot);
            instance.updateTransform();
        }
        
    }

}