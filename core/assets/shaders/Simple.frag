uniform sampler2D unTexture;

varying vec2 fUV;

void main() {
	gl_FragColor = texture2D(unTexture, fUV);
}
