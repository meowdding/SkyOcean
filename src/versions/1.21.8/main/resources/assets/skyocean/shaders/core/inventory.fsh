#version 150

uniform sampler2D Sampler0;

layout (std140) uniform PolyInventoryUniform {
    ivec2 Size;
};

in vec2 texCoord0;
in vec4 vertexColor;

out vec4 fragColor;

void main() {
    float middleBegin = 22.0 / 64;
    float middleSize = 20.0 / 64;
    vec2 border = vec2(44, 44);
    vec2 middle = vec2(20, 20);
    vec2 size = border + vec2(middle.x * (Size.x - 2), middle.y * (Size.y - 2));
    vec2 uv = texCoord0 * (size / 64);
    vec2 uvWithoutBegin = uv - middleBegin;
    vec2 middleWidth = vec2(Size.x - 2, Size.y - 2) * middleSize;
    vec2 cappedUv = mod(uv - middleBegin, 1);

    vec2 outUv = vec2(0, 0);

    if (uv.x < middleBegin && uv.y < middleBegin) {
        outUv = uv;
    } else if (uvWithoutBegin.x > middleWidth.x || uvWithoutBegin.y > middleWidth.y) {
        if (uv.x < middleBegin) {
            outUv = vec2(uv.x, (uv.y - middleWidth.y + middleSize));
        } else if (uvWithoutBegin.x < middleWidth.x) {
            outUv = vec2(mod(uvWithoutBegin.x, middleSize) + middleBegin, (uv.y - middleWidth.y + middleSize));
        } else if (uv.y < middleBegin) {
            outUv = vec2((uv.x - middleWidth.x + middleSize), uv.y);
        } else if (uvWithoutBegin.y < middleWidth.y) {
            outUv = vec2((uv.x - middleWidth.x + middleSize), mod(uvWithoutBegin.y, middleSize) + middleBegin);
        } else {
            outUv = uv - middleWidth + middleSize;
        }
    } else if (uv.x < middleBegin) {
        outUv = vec2(uv.x, mod(uvWithoutBegin.y, middleSize) + middleBegin);
    } else if (uv.y < middleBegin) {
        outUv = vec2(mod(uvWithoutBegin.x, middleSize) + middleBegin, uv.y);
    } else if (uv.x > middleBegin && uv.y > middleBegin) {
        outUv = mod(uvWithoutBegin, middleSize) + middleBegin;
    }

    vec4 color = texture(Sampler0, outUv);
    if (color.a < 0.1) {
        discard;
    }
    fragColor = color * vec4(vertexColor.rgb, 1);
}
