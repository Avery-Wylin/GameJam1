#version 400 core

in vec2 uv_frag;
out vec4 color;

uniform sampler2D img_tex;
uniform sampler2D depth_tex;

void main(void){


//color.xyz = vec3(uv_frag,0);
vec4 a = texture(img_tex, uv_frag-vec2(.005));
vec4 b = texture(img_tex, uv_frag+vec2(.005));

color = vec4(floor(1-min(length(b-a),1)+.9))*texture(img_tex, uv_frag);
}
