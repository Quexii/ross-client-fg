#version 120

uniform sampler2D uTex;
uniform vec2 uTexelSize;

varying vec4 texCoord;

void main() {
    vec2 uv = texCoord.xy;
    vec4 tex = texture2D(uTex, uv);

    gl_FragColor = tex;
}