#version 330

#ifdef NO_LAYOUT
//!moj_import <minecraft:dynamictransforms.glsl>
//!moj_import <minecraft:projection.glsl>

out vec2 texCoord0;
out vec4 vertexColor;


in vec4 Color;
in vec3 Position;
in vec2 UV0;

#else
#extension GL_ARB_separate_shader_objects : require

#include <minecraft:dynamictransforms.glsl>
#include <minecraft:projection.glsl>

layout(location = 0) out vec2 texCoord0;
layout(location = 1) out vec4 vertexColor;


layout(location = 0) in vec4 Color;
layout(location = 1) in vec3 Position;
layout(location = 2) in vec2 UV0;

#endif


void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);

    texCoord0 = UV0;
    vertexColor = Color;
}
