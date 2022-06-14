package core.audio;

import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import org.lwjgl.BufferUtils;
import org.lwjgl.openal.AL10;
import static org.lwjgl.openal.AL10.alGenBuffers;
import org.lwjgl.stb.STBVorbis;
import org.lwjgl.system.libc.LibCStdlib;

public class SoundBuffer {
    private static ArrayList<Integer> loadedSoundBuffers = new ArrayList<>();
    private int bufferID = 0;
    
    public SoundBuffer(String name){
        String sep = System.getProperty("file.separator");
        String filename = "assets" + sep + "sounds"+ sep + name + ".ogg";
        bufferID = alGenBuffers();
        loadedSoundBuffers.add(bufferID);
        IntBuffer sampleRate = BufferUtils.createIntBuffer(1), channels = BufferUtils.createIntBuffer(1);
        ShortBuffer data = BufferUtils.createShortBuffer(10);
        data = STBVorbis.stb_vorbis_decode_filename(filename, channels, sampleRate);
        AL10.alBufferData(bufferID, AL10.AL_FORMAT_STEREO16, data, sampleRate.get());
        LibCStdlib.free(data);
    }
    
    public void free(){
        AL10.alDeleteBuffers(bufferID);
        loadedSoundBuffers.remove(bufferID);
    }
    
    public int getSoundID(){
        return bufferID;
    }
    
    public static void freeAll(){
        for(Integer buffer:loadedSoundBuffers){
            AL10.alDeleteBuffers(buffer);
        }
        loadedSoundBuffers.clear();
    }
}
