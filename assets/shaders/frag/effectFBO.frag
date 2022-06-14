#version 420 core

in vec2 uv_frag;
out vec4 color;

layout(binding = 0) uniform sampler2D depth;
layout(binding = 1) uniform sampler2D color_0;
layout(binding = 2) uniform sampler2D color_1;

void main(void){


color = texture(color_0,uv_frag) + texture(color_1,uv_frag);
}
