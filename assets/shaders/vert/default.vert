#version 400 core

in vec3 pos;
in vec2 uv;
in vec3 vertex_color;
in vec3 normal;

out vec2 uv_frag;
out vec3 normal_frag;
out vec3 vertex_color_frag;
out vec3 incoming;
out float fog_factor;

uniform mat4 transform;
uniform mat4 camera;
uniform mat4 perspective;
uniform float fog_density;
uniform float fog_exponent;

void main(void){
    
    // 3D Transformations
    vec4 worldSpace = transform*vec4(pos,1.0);
    vec4 cameraSpace = camera*worldSpace;
    gl_Position = perspective*cameraSpace;

    // Fix normals when uniformly scaled
    normal_frag = normalize((transform*vec4(normal,0.0)).xyz);

    // Calculate the vector to the camera
    incoming = normalize((inverse(camera)*vec4(0.0,0.0,0.0,1.0)).xyz-worldSpace.xyz);

    // Calculate the fog factor
    float depth = length(cameraSpace.xyz);
    fog_factor = exp(-pow((depth*fog_density),fog_exponent));
    fog_factor = clamp(fog_factor,0.0,1.0);
    
    // pass the vertex color
    vertex_color_frag = vertex_color;

    // pass the uv
    uv_frag = uv;
}
