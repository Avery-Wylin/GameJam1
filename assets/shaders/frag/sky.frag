#version 400 core


in vec3 normal_frag;

out vec4 color;

uniform float world_exponent;
uniform vec3 zenith;
uniform vec3 horizon;
uniform vec3 albedo;

void main(void){

float dotUp = pow( max(dot(vec3(0,-1,0),normal_frag),0),world_exponent);
float dotDown = pow( max(dot(vec3(0,1,0),normal_frag),0),world_exponent);

vec3 world = mix(mix(horizon,albedo,dotDown),zenith,dotUp);

color.xyz = world;
}
