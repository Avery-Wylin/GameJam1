package core.animation;

import org.joml.Vector3f;

public class KeyframeVec{
    public float time = 0;
    public Vector3f val;
    
    public KeyframeVec(){
        val = new Vector3f();
    }
    
    public KeyframeVec(Vector3f val, float time){
        this.val = val;
        this.time = time;
    }
        
}