#version 150
#moj_import <minecraft:globals.glsl>

uniform sampler2D InSampler;

in vec2 texCoord;
out vec4 fragColor;

// Source - https://stackoverflow.com/a/17897228
// Posted by sam hocevar, modified by community. See post 'Timeline' for change history
// Retrieved 2026-03-25, License - CC BY-SA 4.0

// All components are in the range [0…1], including hue.
vec3 rgb2hsv(vec3 c)
{
    vec4 K = vec4(0.0, -1.0 / 3.0, 2.0 / 3.0, -1.0);
    vec4 p = mix(vec4(c.bg, K.wz), vec4(c.gb, K.xy), step(c.b, c.g));
    vec4 q = mix(vec4(p.xyw, c.r), vec4(c.r, p.yzx), step(p.x, c.r));

    float d = q.x - min(q.w, q.y);
    float e = 1.0e-10;
    return vec3(abs(q.z + (q.w - q.y) / (6.0 * d + e)), d / (q.x + e), q.x);
}

// All components are in the range [0…1], including hue.
vec3 hsv2rgb(vec3 c)
{
    vec4 K = vec4(1.0, 2.0 / 3.0, 1.0 / 3.0, 3.0);
    vec3 p = abs(fract(c.xxx + K.xyz) * 6.0 - K.www);
    return c.z * mix(K.xxx, clamp(p - K.xxx, 0.0, 1.0), c.y);
}

vec3 hue_shift (vec3 rgb) {
    vec3 hsv = rgb2hsv(rgb);
    float delta = fract(GameTime * 600.0);// Magnifies GameTime so it cycles through the entire hue shift every 2 seconds, should probably be configurable but idk how to do that.
    hsv.x = fract(hsv.x + delta);
    return hsv2rgb(hsv);
}

void main() {
    vec4 color = texture(InSampler, texCoord);
    fragColor = vec4(hue_shift(color.rgb), color.a);
}
