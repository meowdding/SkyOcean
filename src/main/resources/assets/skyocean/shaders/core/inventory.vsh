#version 150

//!moj_import <minecraft:dynamictransforms.glsl>
//!moj_import <minecraft:projection.glsl>

out vec2 texCoord0;
out vec4 vertexColor;

in vec4 Color;
in vec3 Position;
in vec2 UV0;

void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);

    texCoord0 = UV0;
    vertexColor = Color;
}
