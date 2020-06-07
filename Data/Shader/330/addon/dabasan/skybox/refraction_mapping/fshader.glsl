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
uniform samplerCube cubemap;
uniform float eta;

in vec3 vs_out_position;
in vec2 vs_out_uv;
in vec3 vs_out_normal;
out vec4 fs_out_color;

void main(){
    vec3 ref=refract(vs_out_position-camera.position,vs_out_normal,eta);
    fs_out_color=textureCube(cubemap,ref);
    fs_out_color.a=1.0;
}
