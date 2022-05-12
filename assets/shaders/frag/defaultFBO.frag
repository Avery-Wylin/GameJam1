#version 400 core

in vec2 uv_frag;
out vec4 color;

uniform sampler2D img_tex;
uniform sampler2D depth_tex;

void main(void){


//color.xyz = vec3(uv_frag,0);
color = texture(img_tex, uv_frag);
}
