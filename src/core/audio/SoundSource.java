package core.audio;

import core.AssetManager;
import java.util.ArrayList;
import org.joml.Vector3f;
import static org.lwjgl.openal.AL10.*;

public class SoundSource {
    
    private static ArrayList<SoundSource> loadedSources = new ArrayList<>();
    
    private int bufferID = 0 ;
    private int sourceID = 0;
    
    
    
    public SoundSource(boolean loops, boolean relative, String sound){
        alListener3f(AL_POSITION, 0, 0, 0);
        sourceID = alGenSources();
        if(loops)
            alSourcei(sourceID, AL_LOOPING, AL_TRUE);
        if(relative)
            alSourcei(sourceID, AL_SOURCE_RELATIVE, AL_TRUE);
        setSound(AssetManager.getSound(sound));
        setGain(2f);
        alDistanceModel(AL_INVERSE_DISTANCE);
    }
    
    public void setSound(int bufferID){
        this.bufferID = bufferID;
        alSourcei(sourceID, AL_BUFFER, bufferID);
    }
    
    public void setPos(Vector3f pos){
        alSource3f(sourceID, AL_POSITION, pos.x, pos.y, pos.z);
    }
    
    public void setVel(Vector3f vel){
        alSource3f(sourceID, AL_POSITION, vel.x, vel.y, vel.z);
    }
    
    public void setGain(float gain){
        alSourcef(sourceID, AL_GAIN, gain);
    }
    
    public void play() {
        alSourcePlay(sourceID);
    }

    public boolean isPlaying() {
        return alGetSourcei(sourceID, AL_SOURCE_STATE) == AL_PLAYING;
    }

    public void pause() {
        alSourcePause(sourceID);
    }

    public void stop() {
        alSourceStop(sourceID);
    }

    public void free() {
        stop();
        alDeleteSources(sourceID);
        loadedSources.remove(this);
    }
    
    public static void freeAll(){
        for(int i = loadedSources.size()-1; i>=0; i--){
            loadedSources.get(i).free();
            loadedSources.remove(i);
        }
    }
    
    
}
