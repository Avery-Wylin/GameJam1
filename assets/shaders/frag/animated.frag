#version 400 core

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
uniform float gloss;
uniform vec3 zenith;
uniform vec3 horizon;
uniform vec3 albedo;
uniform sampler2D t;

void main(void){

vec4 texture_col = texture(t,uv_frag*10);
if(texture_col.a==0)
    discard;
vec3 reflection = reflect(-incoming,normal_frag);
float dotUp = pow( max(dot(vec3(0,1,0),reflection),0),world_exponent);
float dotDown = pow( max(dot(vec3(0,-1,0),reflection),0),world_exponent);

vec3 world = mix(mix(horizon,albedo,dotDown),zenith,dotUp);
float specular_mix = pow(dotUp,gloss)*specular;

float fogDotUp =  max(dot(vec3(0,-1,0),incoming),0);
float fogDotDown = max(dot(vec3(0,1,0),incoming),0);
vec3 fog_color = mix(mix(horizon,albedo,fogDotDown),zenith,fogDotUp);

color_0.xyz = mix(fog_color,world * (vertex_color_frag + diffuse + texture_col.rgb) +(specular_mix * zenith),fog_factor);

}
