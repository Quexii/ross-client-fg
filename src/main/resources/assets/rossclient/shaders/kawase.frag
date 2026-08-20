#version 120

uniform sampler2D uTex;
uniform vec2 uResolution;
uniform float uRadius;
uniform bool doAlpha;

varying vec4 texCoord;

vec4 sampleClamped(sampler2D tex, vec2 uv){
    uv = clamp(uv, 0.001, 0.999);
    return texture2D(tex, uv);
}

void addSample( vec4 samp,  float weight,  inout vec3 color,  inout float alpha) {
    color += samp.rgb * samp.a * weight;
    alpha += samp.a * weight;
}

void main() {
    vec2 uv = texCoord.xy;
    vec2 texelSize = uRadius / uResolution;

    vec3 color = vec3(0.0);
    float alpha = 0.0;

    addSample(sampleClamped(uTex, uv), 4.0, color, alpha);
    addSample(sampleClamped(uTex, uv - texelSize), 1.0, color, alpha);
    addSample(sampleClamped(uTex, uv + texelSize), 1.0, color, alpha);
    addSample(sampleClamped(uTex, uv + vec2(texelSize.x, -texelSize.y)), 1.0, color, alpha);
    addSample(sampleClamped(uTex, uv - vec2(texelSize.x, -texelSize.y)), 1.0, color, alpha);
    addSample(sampleClamped(uTex, uv + vec2(-texelSize.x, 0.0)), 2.0, color, alpha);
    addSample(sampleClamped(uTex, uv + vec2(texelSize.x, 0.0)), 2.0, color, alpha);

    addSample(sampleClamped(uTex, uv + vec2(0.0, -texelSize.y)), 2.0, color, alpha);

    addSample(sampleClamped(uTex, uv + vec2(0.0, texelSize.y)), 2.0, color, alpha);

    color /= 16.0;
    alpha /= 16.0;

    if (doAlpha) {
        if (alpha > 0.00001)
        color /= alpha;

        gl_FragColor = vec4(color, 1.0);
    } else {
        if (alpha > 0.00001)
        color /= alpha;
        else
        color = vec3(0.0);

        gl_FragColor = vec4(color, alpha);
    }
}