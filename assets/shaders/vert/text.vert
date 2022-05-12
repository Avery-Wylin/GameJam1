#version 400 core

in vec2 pos;
in vec2 uv;

uniform mat4 perspective;
uniform mat4 transform;

out vec2 uv_frag;

void main(void){

    gl_Position = transform*vec4(pos,0.0,1.0);
    uv_frag = uv;
}
