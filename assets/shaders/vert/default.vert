#version 400 core

in vec3 pos;
in vec2 uv;
in vec3 vertex_color;
in vec3 normal;

out vec3 pos_frag;
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
uniform vec4 clipping_plane;

void main(void){
    
    // 3D Transformations
    vec4 world_space = transform*vec4(pos,1.0);
    vec4 cameraSpace = camera*world_space;
    gl_Position = perspective*cameraSpace;

    gl_ClipDistance[0] = dot(world_space,clipping_plane);

    // Fix normals when uniformly scaled
    normal_frag = normalize((transform*vec4(normal,0.0)).xyz);

    // Calculate the vector to the camera
    incoming = normalize((inverse(camera)*vec4(0.0,0.0,0.0,1.0)).xyz-world_space.xyz);

    // Calculate the fog factor
    float depth = length(cameraSpace.xyz);
    fog_factor = exp(-pow((depth*fog_density),fog_exponent));
    fog_factor = clamp(fog_factor,0.0,1.0);
    
    // pass the vertex color
    vertex_color_frag = vertex_color;

    // pass the uv
    uv_frag = uv;
    pos_frag = world_space.xyz;
}
