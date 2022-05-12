package core.animation;

import org.joml.Quaternionf;
import org.joml.Vector3f;

public class KeyframeQuat{
    public float time = 0;
    public Quaternionf val;
    
    public KeyframeQuat(){
        val = new Quaternionf();
    }
    
    public KeyframeQuat(Quaternionf val, float time){
        this.val = val;
        this.time = time;
    }
}