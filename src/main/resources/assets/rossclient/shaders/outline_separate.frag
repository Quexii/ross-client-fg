#version 120

uniform sampler2D uMask;
uniform sampler2D uOutlineTex;
uniform vec2 uTexelSize;

varying vec4 texCoord;

void main() {
    vec2 uv = texCoord.xy;
    vec4 maskColor = texture2D(uMask, uv);
    vec4 outlineColor = texture2D(uOutlineTex, uv);

    float outlineAlpha = clamp(outlineColor.a - maskColor.a, 0.0, 1.0);
    vec3 outRgb = outlineColor.rgb * step(0.001, outlineAlpha);

    gl_FragColor = vec4(outRgb, outlineAlpha);
}