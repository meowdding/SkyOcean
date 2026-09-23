#version 330

#ifdef NO_LAYOUT
in vec2 texCoord0;
in vec4 vertexColor;

out vec4 fragColor;
#else
#extension GL_ARB_separate_shader_objects : require

layout(location = 0) in vec2 texCoord0;
layout(location = 1) in vec4 vertexColor;

layout(location = 0) out vec4 fragColor;
#endif

uniform sampler2D Sampler0;

layout (std140) uniform MonoInventoryUniform {
    int Size;
    int Vertical;
};

void main() {

    vec2 texCoord1;
    if (Vertical == 1) {
        texCoord1 = texCoord0.yx;
    } else {
        texCoord1 = texCoord0;
    }

    float middleBegin = 22.0 / 64;               // [0;1] value where the middle slot starts
    float middleSize = 20.0 / 64;                // [0;1] value of how long the middle slot is
    int scale = 44 + 20 * (Size - 2);            // [0; [ value representing the relation to the actual texture
    float x = texCoord1.x * (scale / 64.0);      // Scaled text coord
    float uvWithoutBegin = x - middleBegin;      // x where the repetition should start
    float middleWidth = (Size - 2) * middleSize; // The width of the repeating section

    vec2 outUv;
    if (uvWithoutBegin > middleWidth) {
        outUv = vec2(x - middleWidth + middleSize, texCoord1.y);
    } else if (x > middleBegin) {
        outUv = vec2(mod(uvWithoutBegin, middleSize) + middleBegin, texCoord1.y);
    } else {
        outUv = vec2(x, texCoord1.y);
    }

    vec4 color = texture(Sampler0, outUv);
    if (color.a < 0.1) {
        discard;
    }
    fragColor = color;
}
