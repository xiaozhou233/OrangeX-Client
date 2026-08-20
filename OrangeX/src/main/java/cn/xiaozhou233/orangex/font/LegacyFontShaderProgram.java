package cn.xiaozhou233.orangex.font;

import cn.xiaozhou233.orangex.font.shader.ShaderProgram;

public class LegacyFontShaderProgram extends ShaderProgram {
    private static final String VERTEX_SHADER_SOURCE;
    private static final String FRAGMENT_SHADER_SOURCE;

    public LegacyFontShaderProgram() {
        super(VERTEX_SHADER_SOURCE, FRAGMENT_SHADER_SOURCE);
    }

    static {
        FRAGMENT_SHADER_SOURCE = "#version 120\nuniform sampler2D texture;\nvoid main() {\n    vec2 coords = gl_TexCoord[0].xy;\n    float alpha = texture2D(texture, coords).x;\n    alpha = max(0.0, alpha + 0.25 * (1.0 - alpha));\n    float smoothenAlpha = smoothstep(0.2, 0.8, alpha);\n    alpha = mix(alpha, 0.0, 1.0 - smoothenAlpha);\n    gl_FragColor = vec4(gl_Color.rgb, alpha * gl_Color.a);\n}";
        VERTEX_SHADER_SOURCE = "#version 120\nvoid main(void) {\n    gl_Position = gl_ModelViewProjectionMatrix * gl_Vertex;\n    gl_TexCoord[0] = gl_TextureMatrix[0] * gl_MultiTexCoord0;\n    gl_FrontColor = gl_Color;\n}";
    }
}