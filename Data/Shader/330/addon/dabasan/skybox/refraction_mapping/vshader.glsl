#version 330

struct Camera{
    vec3 position;
    vec3 target;
    mat4 projection;
    mat4 view_transformation;
    float near;
    float far;
};

uniform Camera camera;

layout(location=0) in vec3 vs_in_position;
layout(location=1) in vec2 vs_in_uv;
layout(location=2) in vec3 vs_in_normal;
out vec3 vs_out_position;
out vec2 vs_out_uv;
out vec3 vs_out_normal;

void main(){
    mat4 camera_matrix=camera.projection*camera.view_transformation;
    gl_Position=camera_matrix*vec4(vs_in_position,1.0);

    vs_out_position=vs_in_position;
    vs_out_uv=vs_in_uv;
    vs_out_normal=vs_in_normal;
}
