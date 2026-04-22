package eu.shoroa.ross.render.gl;

import org.lwjgl.opengl.GL15;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class VBO {
    private final int vao = GL15.glGenBuffers();

    public void bind() {
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vao);
    }

    public void unbind() {
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
    }

    public void delete() {
        GL15.glDeleteBuffers(vao);
    }

    public void data(float[] data) {
        ByteBuffer bb = ByteBuffer.allocateDirect(data.length * 4);
        bb.order(ByteOrder.nativeOrder());

        FloatBuffer buffer = bb.asFloatBuffer();
        buffer.put(data);
        buffer.flip();

        bind();
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buffer, GL15.GL_STATIC_DRAW);
        unbind();
    }
}
