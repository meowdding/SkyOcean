#version 150

uniform sampler2D Sampler0;

uniform int Size;
uniform int Vertical;

in vec2 texCoord0;
in vec4 vertexColor;

out vec4 fragColor;

void main() {

    vec2 texCoord1;
    if (Vertical == 1) {
        texCoord1 = texCoord0.yx;
    } else {
        texCoord1 = texCoord0;
    }

    float middleBegin = 22.0 / 64;
    float middleSize = 20.0 / 64;
    int scale = 44 + 20 * (Size - 2);
    float x = texCoord1.x * (scale / 64.0);
    float uvWithoutBegin = x - middleBegin;
    float middleWidth = (Size - 2) * middleSize;

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
    fragColor = color * vec4(vertexColor.xyz, 1);
}
