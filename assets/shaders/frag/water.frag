#version 420 core

in vec4 worldSpace;
in vec4 cameraSpace;
in vec4 clipSpace;
in vec3 incoming;

out vec4 color;

layout(binding = 1) uniform sampler2D reflection;
layout(binding = 2) uniform sampler2D refraction;
layout(binding = 0) uniform sampler2D depth;
layout(binding = 3) uniform sampler2D ripple;
uniform vec3 sky_up;
uniform vec3 sky_down;
uniform vec3 sky_vec;
uniform float fog_density;
uniform float fog_exponent;
uniform float time;

const float near = 0.1;
const float far = 350;

void main(void){

    // Calculate screen space
    vec2 screenSpace = (clipSpace.xy/clipSpace.w)/2+.5;

    // Calculate plane depth
    float plane_depth = (2.0f * near * far) / (far + near - (gl_FragCoord.z * 2.0f - 1.0f) * (far - near));

    // Linearize the depth buffer, use refracted uv for depth extraction
    float  d = texture(depth,screenSpace).x;
    d = (2.0f * near * far) / (far + near - (d * 2.0f - 1.0f) * (far - near));
    d = abs(d-plane_depth);

    // Calculate refracted coordinates
    vec2 refrac_uv = screenSpace;
    refrac_uv += (
    texture(ripple,worldSpace.xz/16 + vec2(time,0)).xy +
    texture(ripple,worldSpace.xz/16 + vec2(0,time)).xy -
    vec2(1,1)
    )/30;

    refrac_uv = mix(screenSpace,refrac_uv,clamp(d,0,1));



    // Set color to refraction
    color = texture(refraction,refrac_uv);

    // Calculate the mix factor based on depth and clamped
    //float mixFactor = clamp(d/50,.2,.7);

    // Tint the refracted color according to mix factor
    //color *= vec4(1-pow(mixFactor*2,1.5),1-pow(mixFactor*1.2,1.5),1-pow(mixFactor,1.5),1);

    // Surface shine
    vec3 normal = texture(ripple,worldSpace.xz/18 + vec2(0,time)).xzy-vec3(.5,0,.5);
    normal += texture(ripple,worldSpace.xz/18 + vec2(time,0)).xzy-vec3(.5,0,.5);
    normal/=2;
    float shine = clamp(abs(dot(reflect(normalize(-incoming),normal),sky_vec))*8,0,.8);

    // Mix in reflection
    color.xyz = mix(texture(reflection,vec2(refrac_uv.x,1-refrac_uv.y)).xyz,color.xyz,shine);

    // Mix in edge foam effect
    //color.xyz = mix(texture(refraction,screenSpace).xyz,color.xyz,clamp(d,0,1));

    // Calculate fog factor
    float fogFactor = exp(-pow((length(cameraSpace)*fog_density),fog_exponent));
    fogFactor = clamp(fogFactor,0.0,1.0);

    // Mix in the fog
    vec3 fog_color = mix(sky_down,sky_up,.5);
    color.xyz = mix(fog_color,color.xyz,fogFactor);
}
