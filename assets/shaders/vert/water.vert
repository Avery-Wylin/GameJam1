#version 400 core

in vec3 pos;

out vec4 worldSpace;
out vec4 cameraSpace;
out vec4 clipSpace;
out vec3 incoming;

uniform mat4 transform;
uniform mat4 camera;
uniform mat4 perspective;

void main(void){

    //transformations
    worldSpace = transform*vec4(pos,1.0);
    cameraSpace = camera*worldSpace;
    clipSpace = perspective*cameraSpace;
    gl_Position = clipSpace;
    incoming = normalize((inverse(camera)*vec4(0.0,0.0,0.0,1.0)).xyz-worldSpace.xyz);

}
