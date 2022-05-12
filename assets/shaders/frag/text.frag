#version 400 core

in vec2 uv_frag;
out vec4 color;

uniform vec3 text_color;
uniform sampler2D char_atlas;

void main(void){


vec4 char_texture = texture2D(char_atlas, uv_frag);
if(char_texture.a==0)
      discard;
color = vec4(char_texture.rgb*text_color,1);
}
