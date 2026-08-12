#version 330

// Can't moj_import in things used during startup, when resource packs don't exist.
// This is a copy of dynamicimports.glsl & globals.glsl
layout(std140) uniform DynamicTransforms {
    mat4 ModelViewMat;
    vec4 ColorModulator;
    vec3 ModelOffset;
    mat4 TextureMat;
};

layout(std140) uniform Globals {
    ivec3 CameraBlockPos;
    vec3 CameraOffset;
    vec2 ScreenSize;
    float GlintAlpha;
    float GameTime;
    int MenuBlurRadius;
    int UseRgss;
};

layout(std140) uniform SkyoceanRarityUniform {
    float AtlasDimensions;  // width.toFloat())
    float SlotSize;  // slotSize.toFloat())
    float SampleAmount;  // RarityOutlinesConfig.sampleAmount)
    float SampleDistance;  // RarityOutlinesConfig.sampleDistance)
    float AlphaCutoff;  // RarityOutlinesConfig.alphaCutoff)
    float OutlineAlpha;  // RarityOutlinesConfig.outlineAlpha)
};

const vec4 rarityColor = RARITY_COLOR;
#if defined(IS_RARITY_UPGRADE)
const vec4 baseRarityColor = BASE_RARITY_COLOR;
#endif
uniform sampler2D Sampler0;

in vec2 texCoord0;
in vec4 vertexColor;

out vec4 fragColor;

vec4 checkOutline(vec2 texCoord0, vec2 slotStart, float slotDimensions) {
    float step = (slotDimensions / 256) * SampleDistance;
    vec4 avg = vec4(0);
    for (float x = -SampleAmount; x <= SampleAmount; x++) {
        for (float y =  -SampleAmount; y <= SampleAmount; y++) {
            vec2 sampleCoord = texCoord0 + step * vec2(x, y);

            if (sampleCoord.x <= slotStart.x + step || sampleCoord.y <= slotStart.y + step || sampleCoord.x >= (slotStart.x + slotDimensions - step) || sampleCoord.y >= (slotStart.y + slotDimensions- step)) continue;
            vec4 sampleColor = texture(Sampler0, sampleCoord);
            if (sampleColor.a < AlphaCutoff) continue;
            return vec4(rarityColor.rgb, OutlineAlpha);
        }
    }
    return vec4(0);
}

void main() {
    vec4 color = texture(Sampler0, texCoord0);
    if (color.a < AlphaCutoff) {
        float AtlasStep = 1.0 / AtlasDimensions;
        float SlotWidth = AtlasStep * SlotSize;
        float scalar = 10000000.0;
        int slotInt = int(SlotWidth * scalar);
        vec2 slotStart = vec2(texCoord0.x - float(int(texCoord0.x * scalar) % slotInt) / scalar, texCoord0.y - float(int(texCoord0.y * scalar) % slotInt) / scalar);
        vec4 result = checkOutline(texCoord0, slotStart, SlotWidth);
        fragColor = result;
    } else {
        if (color.a == 0.0) {
            discard;
        }
        fragColor = color * ColorModulator * vertexColor;
    }
}
