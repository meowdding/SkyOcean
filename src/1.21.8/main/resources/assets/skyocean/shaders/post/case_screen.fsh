#version 150

const float PI2 = 6.28318530718;

const float ZOOM = 1.5;
const float ZOOM_RADIUS = 0.2;

const float BLUR_DIRECTIONS = 32.0;
const float BLUR_QUALITY = 9.0;
const float BLUR_SIZE = 8.0;

uniform sampler2D InSampler;

layout (std140) uniform SamplerInfo {
    vec2 OutSize;
    vec2 InSize;
};

in vec2 texCoord;
out vec4 fragColor;

vec4 getGardient(vec4 color, vec2 coord) {
    if (coord.x < 0.2 || coord.x > 0.8) {
        float progress = (coord.x < 0.2) ? coord.x / 0.2 : (1.0 - coord.x) / 0.2;
        color = mix(vec4(0.0, 0.0, 0.0, 0.8), color, progress);
    }
    return color;
}

void main() {
    vec2 center = vec2(0.5, 0.5);

    float aspectRatio = InSize.x / InSize.y;
    vec2 aspectCorrectedCenter = vec2(center.x, center.y / aspectRatio);
    vec2 aspectCorrectedUV = vec2(texCoord.x, texCoord.y / aspectRatio);

    float dist = distance(aspectCorrectedUV, aspectCorrectedCenter) / ZOOM_RADIUS;

    if (dist > 1.0) {
        vec2 radius = BLUR_SIZE / InSize;
        vec4 color = getGardient(texture(InSampler, texCoord), texCoord);

        for (float d = 0.0; d < PI2; d += PI2 / BLUR_DIRECTIONS) {
            for (float i = 1.0 / BLUR_QUALITY; i <= 1.0; i += 1.0 / BLUR_QUALITY) {
                vec2 coord = texCoord + vec2(cos(d), sin(d)) * radius * i;
                color += getGardient(texture(InSampler, coord), coord);
            }
        }

        fragColor = color / (BLUR_QUALITY * BLUR_DIRECTIONS - 15.0);
    } else {
        vec2 newCoords = center + (texCoord - center) * mix(1.0, 1.0 / ZOOM, 1.0);
        fragColor = vec4(texture(InSampler, newCoords).rgb, 1.0);
    }
}
