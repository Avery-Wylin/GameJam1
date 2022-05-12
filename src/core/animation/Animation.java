package core.animation;

import core.animation.Armature.Joint;
import java.util.ArrayList;

public class Animation {
    
    ArrayList<Channel> channels;
    float duration, fps = 24;
    
    public Animation(){
        channels = new ArrayList<>();
    }
    
    public void setDuration(float duration){
        this.duration = duration;
    }
    
    public void setSpeed(float fps){
        this.fps = fps;
    }
    
    
    public void addChannel(Channel c){
        channels.add(c);
    }
    
    public void deformArmature(Armature a, float time){
        time *= fps;
        time %= duration;
        Joint j;
        for(Channel c:channels){
            j = a.getJoint(c.getJointID());
            c.lerpJoint(j, time);
            j.markUpdated();
        }
        a.update();
    }

}
