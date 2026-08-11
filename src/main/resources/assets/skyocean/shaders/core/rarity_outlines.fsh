#version 330
#define SAMPLE_MIN (-8)
#define SAMPLE_MAX (8)
#define MIN_ALPHA (0.03)


//!moj_import <minecraft:dynamictransforms.glsl>
//!moj_import <minecraft:globals.glsl>

layout(std140) uniform SkyoceanRarityUniform {
    vec2 AtlasSize;
    int GuiScale;
};

const vec4 rarityColor = RARITY_COLOR;
uniform sampler2D Sampler0;

in vec2 texCoord0;
in vec4 vertexColor;

out vec4 fragColor;

float scaled(int value) {
    return floor(value * clamp(float(GuiScale - 1) / 8.0, 0.0, 1.0));
}

float computeCoord(float coord) {
    return floor(coord / 16.0);
}

vec4 checkOutline(vec2 pixelCoord, vec2 atlasCoord) {
    for (float x = scaled(SAMPLE_MIN); x <= scaled(SAMPLE_MAX); x++) {
        for (float y = scaled(SAMPLE_MIN); y <= scaled(SAMPLE_MAX); y++) {
            vec2 sampleCoord = pixelCoord + vec2(x, y);
            vec2 sampleAtlasCoord = vec2(computeCoord(sampleCoord.x), computeCoord(sampleCoord.y));
            if (sampleAtlasCoord.x == atlasCoord.x && sampleAtlasCoord.y == atlasCoord.y && texture(Sampler0, texCoord0 + vec2(x / AtlasSize.x, y / AtlasSize.y)).a >= MIN_ALPHA) {
                return vec4(rarityColor.rgb, 0.5);
            }
        }
    }
    return vec4(1,0,0,1);
}

void main() {
    vec2 pixelCoord = vec2((texCoord0.x * float(AtlasSize.x)), (texCoord0.y * float(AtlasSize.y)));
    vec2 atlasCoord = vec2(computeCoord(pixelCoord.x), computeCoord(pixelCoord.y));
    vec4 color = texture(Sampler0, texCoord0);
    if (color.a < MIN_ALPHA) {
        vec4 result = checkOutline(pixelCoord, atlasCoord);
        fragColor = result;
    } else {
        if (color.a == 0.0) {
            discard;
        }
        fragColor = color * ColorModulator * vertexColor;
    }
}
