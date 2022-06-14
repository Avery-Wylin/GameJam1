#version 420 core

in vec2 uv_frag;
in vec3 pos_frag;
in vec3 normal_frag;
in vec3 vertex_color_frag;
in vec3 incoming;
in float fog_factor;

layout (location = 0) out vec4 color_0;

uniform vec3 diffuse;
uniform float specular;
uniform float world_exponent;
uniform vec3 sky_up;
uniform vec3 sky_down;
uniform vec3 sky_vec;
uniform float time;
layout(binding = 0) uniform sampler2D tex;
layout(binding = 1) uniform sampler2D caustic;

void main(void){

// Calculate texture color
vec4 texture_col = texture(tex,uv_frag*10);

// Remove any alpha-values
if(texture_col.a==0)
    discard;

// Calculate caustic factor
float dot_up = dot(normal_frag,sky_vec);
float caustic_factor = pow(
(texture(caustic,pos_frag.xz/16+vec2(time,0)).x+
texture(caustic,pos_frag.xz/16+vec2(0,time)).y),3)*clamp(dot_up,0,1);

// Calculate ambient lighting
vec3 ambient = mix(sky_down,sky_up, (dot_up+1)/2);


vec3 fog_color = mix(sky_down,sky_up,.5);

float specular_mix = pow(max(dot(reflect(-incoming, normal_frag), sky_vec),0),specular);
color_0.xyz = mix(fog_color,ambient * (diffuse * (texture_col.rgb + vertex_color_frag)) + ((specular_mix+caustic_factor) * sky_up),fog_factor);
}
