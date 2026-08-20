package cn.xiaozhou233.orangex.font.shader;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import java.nio.IntBuffer;

public class ShaderProgram {
    private static int currentProgramId = -1;
    int previousProgramId;
    int vertexShaderId = 0;
    int fragmentShaderId = 0;
    boolean linked;
    int programId = GL20.glCreateProgram();

    public ShaderProgram(String vertexShaderSource, String fragmentShaderSource) {
        this.vertexShaderId = GL20.glCreateShader(GL20.GL_VERTEX_SHADER);
        this.fragmentShaderId = GL20.glCreateShader(GL20.GL_FRAGMENT_SHADER);

        GL20.glShaderSource(this.vertexShaderId, vertexShaderSource);
        GL20.glShaderSource(this.fragmentShaderId, fragmentShaderSource);
        GL20.glCompileShader(this.vertexShaderId);
        GL20.glCompileShader(this.fragmentShaderId);

        GL20.glAttachShader(this.programId, this.vertexShaderId);
        GL20.glAttachShader(this.programId, this.fragmentShaderId);
        GL20.glLinkProgram(this.programId);

        IntBuffer linkStatus = BufferUtils.createIntBuffer(1);
        GL20.glGetProgram(this.programId, GL20.GL_LINK_STATUS, linkStatus);
        this.linked = linkStatus.get(0) == 1;
    }

    public boolean isLinked() {
        return this.linked;
    }

    public int getProgramId() {
        return this.programId;
    }

    public void restorePreviousProgram() {
        useProgram(this.previousProgramId);
    }

    public static void useProgram(int programId) {
        GL20.glUseProgram(programId);
        currentProgramId = programId;
    }

    public static int getCurrentProgramId() {
        if (currentProgramId == -1) {
            currentProgramId = GL11.glGetInteger(GL20.GL_CURRENT_PROGRAM);
        }
        return currentProgramId;
    }

    public boolean bind() {
        if (!this.linked) return false;
        this.previousProgramId = getCurrentProgramId();
        useProgram(this.programId);
        return true;
    }
}