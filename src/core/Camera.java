package core;

import static java.lang.Math.cos;
import static java.lang.Math.sin;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

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
    public float FOV = 90f, near = 0.1f, far = 350f, aspect = 1f, width = 0, height = 0;
    
    // Variables for the camera matrix uniform
    public Matrix4f transform;
    public Matrix4f inverseTransform;
    
    // Variables for the camera location
    public Vector3f pos = new Vector3f(0,0,0);
    public Vector3f rot = new Vector3f(0,0,0);
    public Vector3f focusPos = new Vector3f(0,0,0);
    
    
    public Camera(){
        perspective = new Matrix4f();
        inversePerspective = new Matrix4f();
        transform = new Matrix4f();
        inverseTransform = new Matrix4f();
        
        updateCameraTransform();
        updatePerspective();
    }
    
    /**
     * Sets the aspect and window dimensions.
     * Use this with window resize events.
     * updatePerspective() should be called following changes.
     * @param width
     * @param height 
     */
    public void setViewDimensions(float width, float height){
        this.width = width;
        this.height = height;
        aspect = (float)width/height;
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
    public void rotateAround(Vector3f rot, float rad){
        pos.set(focusPos);
        this.rot.set(rot);
        float angle = (float)cos(rot.x);
        pos.x+=angle*rad*sin(rot.y);
        pos.z+=angle*rad*cos(rot.y);
        pos.y+=rad*(-sin(rot.x))+1f;
        updateCameraTransform();
    }
    
    public void rotateAround(float rad){
        pos.set(focusPos);
        float angle = (float)cos(rot.x);
        pos.x+=angle*rad*sin(rot.y);
        pos.z+=angle*rad*cos(rot.y);
        pos.y+=rad*(-sin(rot.x))+1f;
        updateCameraTransform();
    }
    
    /**
     * 
     * @param x X value in screen space
     * @param y Y value in screen space
     * @param depth Depth component to be used as Z
     * @return 
     */
    public Vector3f rayCast(float x, float y, float depth){
        // Convert screen space to GL 2D space
        x = (2f*x)/width-1f;
        y = 1f-(2f*y)/height;
        
        // Create a ray under the cursor pointing out from the camera
        Vector3f ray = new Vector3f(x,y,-1);
        
        // Project the ray back into the world using the inverse perspective
        inversePerspective.transformProject(ray);
        
        // Scale the ray to match the depth given for z
        ray.mul((depth-near)/-ray.z);
        
        // Rotate the ray so it is aligned with the camera in 3D space
        ray.rotateX(rot.x);
        ray.rotateY(rot.y);
        ray.rotateZ(rot.z);
        
        ray.add(pos);
        
        return ray;
    }
    
    /**
     * Converts a depth buffer value to a linear depth (0-1).
     * @param depth
     * @return 
     */
    public float linearizeDepthBuffer(float depth){
        return (2.0f * near * far) / (far + near - (depth * 2.0f - 1.0f) * (far - near));
    }
}
