package core.scene;

import com.bulletphysics.BulletGlobals;
import com.bulletphysics.ContactAddedCallback;
import com.bulletphysics.ContactDestroyedCallback;
import com.bulletphysics.collision.broadphase.*;
import com.bulletphysics.collision.dispatch.*;
import com.bulletphysics.collision.narrowphase.ManifoldPoint;
import com.bulletphysics.collision.shapes.*;
import com.bulletphysics.dynamics.*;
import com.bulletphysics.dynamics.constraintsolver.*;
import com.bulletphysics.linearmath.*;
import core.Mesh;
import core.Shader;
import core.instances.BasicInstance;
import core.instances.PhysicsInstance;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import javax.vecmath.Vector3f;

public class ScenePhysics {
    public DynamicsWorld dynamicsWorld;
    private ArrayList<PhysicsInstance> instances;
    
    CollisionObject groundObject;

    public void init(Scene scene){
        
        instances = new ArrayList<>();
        
        // Initialize the World
        BroadphaseInterface broadphase = new DbvtBroadphase();
        CollisionConfiguration collisionConfiguration = new DefaultCollisionConfiguration();
        CollisionDispatcher dispatcher = new CollisionDispatcher(collisionConfiguration);
        ConstraintSolver constraintSolver = new SequentialImpulseConstraintSolver();
        dynamicsWorld = new DiscreteDynamicsWorld(dispatcher, broadphase, constraintSolver, collisionConfiguration);
        dynamicsWorld.setGravity(new Vector3f(0,-9f,0));
        
        // Custom Contact Callback
        ContactAddedCallback contactAddedCallback = new ContactAddedCallback() {

            @Override
            public boolean contactAdded(

                ManifoldPoint cp, 
                CollisionObject colObj0, 
                int partId0, 
                int index0,
                CollisionObject colObj1, 
                int partId1, 
                int index1) {

                if( colObj0.getUserPointer() instanceof PhysicsInstance ){
                    ((PhysicsInstance)colObj0.getUserPointer()).collisionAction(colObj1.getUserPointer(), cp);
                }
                if( colObj1.getUserPointer() instanceof PhysicsInstance ){
                    ((PhysicsInstance)colObj1.getUserPointer()).collisionAction(colObj0.getUserPointer(), cp);
                }
                
                return false;
            }
        };
        
        
        BulletGlobals.setContactAddedCallback(contactAddedCallback);
        
    
        
        // Create Ground
        float[] pos = new float[]{-30,0,-30, 30,0,-30, 30,0,30, -30,0,30, 0,5,0};
        float[] uv = new float[]{-3,-3, 3,-3, 3,3, -3,3, 0,0};
        int[] index = new int[]{0,4,1, 1,4,2, 2,4,3, 3,4,0};
        
        Mesh ground = new Mesh();
        ground.allocate();
        ground.loadIndex(index);
        ground.loadAttributeFloat(Mesh.ATTRB_POS,pos);
        ground.loadAttributeFloat(Mesh.ATTRB_UV,uv);
        ground.smoothNormals(pos, index);
        scene.render.createInstanceRender("ground", ground, Shader.defaultShader, "tile");
        scene.render.addInstance(new BasicInstance(), "ground");
        
        IndexedMesh indexedMesh = new IndexedMesh();
        indexedMesh.numTriangles = index.length/3;
        indexedMesh.numVertices = pos.length/3;
        indexedMesh.triangleIndexBase = ByteBuffer.allocateDirect(Integer.BYTES*index.length);
        indexedMesh.triangleIndexBase.rewind().asIntBuffer().put(index);
        indexedMesh.triangleIndexStride = 3*Integer.BYTES;
        indexedMesh.vertexBase = ByteBuffer.allocateDirect(Float.BYTES*pos.length);
        indexedMesh.vertexBase.rewind().asFloatBuffer().put(pos);
        indexedMesh.vertexStride = 3*Float.BYTES;
        
        TriangleIndexVertexArray groundShape = new TriangleIndexVertexArray();
        groundShape.addIndexedMesh(indexedMesh);
        
        CollisionShape shape = new BvhTriangleMeshShape( groundShape, true);
        shape.setMargin(.5f);
        
        groundObject = new RigidBody(0, new DefaultMotionState(), shape);
        groundObject.setRestitution(.6f);
        groundObject.setFriction(4f);
        dynamicsWorld.addCollisionObject(groundObject);
       
    }
    
    public void addPhysicsInstance(PhysicsInstance instance){
        instances.add(instance);
        dynamicsWorld.addRigidBody(instance.rigidBody);
    }
    
    public void attractTowards(float x, float y, float z, float v){
        Transform t = new Transform();
        Vector3f force = new Vector3f();
        for(PhysicsInstance instance:instances){
            instance.rigidBody.activate();
            instance.rigidBody.getWorldTransform(t);
            force.set(x, y, z);
            force.sub(t.origin);
            force.normalize();
            force.scale(v);
            instance.rigidBody.applyCentralForce(force);
        }
    }
    
    public void update(float delta, Scene scene){
        dynamicsWorld.stepSimulation(delta);
        Transform t = new Transform();
        Vector3f force = new Vector3f(0,20,0);
        PhysicsInstance instance;
        for(int i=0; i<instances.size(); i++){
            instance = instances.get(i);
            
            instance.rigidBody.getWorldTransform(t);
            if(t.origin.y<scene.render.waterPlane.getWaterLevel()){
                instance.rigidBody.applyCentralForce(force);
                instance.rigidBody.setDamping(.9f,.9f);
            }
            else{
                instance.rigidBody.setDamping(.1f,.1f);
            }
        }
        
    }
    
    public void cleanInstances(){
        
    }

    void waterForce(float waterLevel) {
    }

}