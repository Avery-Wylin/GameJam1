#version 400 core

in vec3 pos;
in vec3 normal;

out vec3 normal_frag;

uniform mat4 transform;
uniform mat4 camera;
uniform mat4 perspective;

void main(void){
    
    // 3D Transformations
    vec4 worldSpace = transform*vec4(pos,1.0);
    mat4 camera_rot = camera;
    camera_rot[3] = vec4(0,0,0,1);
    vec4 cameraSpace = camera_rot*worldSpace;
    gl_Position = perspective*cameraSpace;

    // Fix normals when uniformly scaled
    normal_frag = normalize((transform*vec4(normal,0.0)).xyz);
}