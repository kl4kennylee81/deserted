uniform sampler2D unTexture;
uniform vec4 unTint;

varying vec2 fUV;

void main() {
	gl_FragColor = texture2D(unTexture, fUV) * unTint;
}
