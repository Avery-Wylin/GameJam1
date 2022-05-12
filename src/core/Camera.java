package core;

import static java.lang.Math.cos;
import static java.lang.Math.sin;
import org.joml.Matrix4f;
import org.joml.Vector3f;

/**
 *
 * @author ajw This class provides settings for uniforms such as camera and
 * perspective transforms. An Camera instance should be stored client side in a
 * GLWindow and passed to the render function in Scene.
 */
public class Camera {
    
    // Variables for the perspective matrix uniform
    public Matrix4f perspective;
    public Matrix4f inversePerspective;
    public float FOV = 90f, near = 0.1f, far = 350f, aspect = 1f;
    
    // Variables for the camera matrix uniform
    public Matrix4f transform;
    public Matrix4f inverseTransform;
    
    // Variables for the camera location
    public Vector3f pos = new Vector3f(0,0,0);
    public Vector3f rot = new Vector3f(0,0,0);
    
    
    public Camera(){
        perspective = new Matrix4f();
        inversePerspective = new Matrix4f();
        transform = new Matrix4f();
        inverseTransform = new Matrix4f();
        
        updateCameraTransform();
        updatePerspective();
    }
    
    /**
     * Updates the perspective. Calls after any perspective properties are updated.
     */
    public void updatePerspective() {
        perspective.identity();
        perspective.perspective((float) Math.toRadians(FOV), aspect, near, far);
        perspective.invertPerspective(inversePerspective);
    }
    
    /**
     * Updates the camera transform matrix. Calls after any camera transforms are updated.
     */
    public void updateCameraTransform(){
       transform.identity().rotateXYZ(-rot.x,-rot.y,-rot.z).translate(-pos.x,-pos.y,-pos.z);
       transform.invert(inverseTransform);
    }

    
    /**
     * Sets the position and updates the camera.
     * @param pos Position in GL units
     */
    public void setPos(Vector3f pos){
        this.pos = pos;
        updateCameraTransform();
    }
    
    /**
     * Sets the rotation and updates the camera.
     * @param rot Euler Rotation in radians
     */
    public void setRot(Vector3f rot){
        this.rot = rot;
        updateCameraTransform();
    }
    
    /**
     * Sets both the position and the rotation then updates the camera.
     * @param pos Position in GL units
     * @param rot Euler Rotation in radians
     */
    public void setTransform(Vector3f pos, Vector3f rot){
        this.pos = pos;
        this.rot = rot;
        updateCameraTransform();
    }
    
    /**
     * Rotates the camera around a position given an angle and radius;
     * @param center Position to rotate around.
     * @param rot Angle of rotation in radians.
     * @param rad Radius from center
     */
    public void rotateAround(Vector3f center, Vector3f rot, float rad){
        pos.set(center);
        this.rot.set(rot);
        float angle = (float)cos(rot.x);
        pos.x+=angle*rad*sin(rot.y);
        pos.z+=angle*rad*cos(rot.y);
        pos.y+=rad*(-sin(rot.x))+1f;
        updateCameraTransform();
    }
}
