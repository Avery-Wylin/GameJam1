#version 400 core

const int joint_count = 64;
const int weight_count = 3;

in vec3 pos;
in vec2 uv;
in vec3 vertex_color;
in vec3 normal;
in vec3 weight;
in ivec3 joint;

out vec2 uv_frag;
out vec3 pos_frag;
out vec3 normal_frag;
out vec3 vertex_color_frag;
out vec3 incoming;
out float fog_factor;

uniform mat4 joints[joint_count];
uniform mat4 transform;
uniform mat4 camera;
uniform mat4 perspective;
uniform float fog_density;
uniform float fog_exponent;
uniform vec4 clipping_plane;

void main(void){
    
    // Mesh Transformations
    vec4 weighted_pos = vec4(0.0);
    vec4 weighted_normal = vec4(0.0);
    vec4 test = vec4(0.0);

    for(int i = 0; i < weight_count; i++){
        if(joint[i] != -1){
            mat4 joint_transform = joints[joint[i]];
            weighted_pos += weight[i]*(joint_transform * vec4(pos,1.0));
            weighted_normal += weight[i]*(joint_transform * vec4(normal,0.0));
        }
        
    }

    // 3D Transformations
    vec4 world_space = transform*weighted_pos;
    vec4 camera_space = camera*world_space;
    gl_Position = perspective*camera_space;

    gl_ClipDistance[0] = dot(world_space,clipping_plane);
    
    // Fix normals when uniformly scaled
    normal_frag = normalize((transform*weighted_normal).xyz);

    // Calculate the vector to the camera
    incoming = normalize((inverse(camera)*vec4(0.0,0.0,0.0,1.0)).xyz-world_space.xyz);

    // Calculate the fog factor
    float depth = length(camera_space.xyz);
    fog_factor = exp(-pow((depth*fog_density),fog_exponent));
    fog_factor = clamp(fog_factor,0.0,1.0);
    
    // pass the vertex color
    vertex_color_frag = vertex_color;

    // pass the uv
    uv_frag = uv;
    pos_frag = normalize(camera_space.xyz);
}
