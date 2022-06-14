#version 400 core

in vec3 pos;
in vec2 uv;

out vec2 uv_frag;

void main(void){

    gl_Position = vec4(pos,1);

    // pass the uv
    uv_frag = uv;
}
