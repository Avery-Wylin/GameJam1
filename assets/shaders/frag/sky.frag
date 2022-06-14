#version 400 core


in vec3 normal_frag;
in vec3 pos_frag;

layout(location = 0) out vec4 color_0;

uniform float world_exponent;
uniform vec3 sky_up;
uniform vec3 sky_down;
uniform vec3 sky_vec;

void main(void){

color_0.xyz = mix(sky_down,sky_up,(dot(normal_frag,-sky_vec)+1)/2);
}
