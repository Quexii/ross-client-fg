package eu.shoroa.ross.utils.proj;

import net.minecraft.util.Vec3;
import org.lwjgl.BufferUtils;
import org.lwjgl.input.Keyboard;
import org.lwjgl.util.glu.GLU;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public class Projection {
    public static FloatBuffer MODELVIEW;
    public static FloatBuffer PROJECTION;
    public static IntBuffer VIEWPORT;
    public static FloatBuffer SCREEN_COORDS = BufferUtils.createFloatBuffer(3);

    public static void update(FloatBuffer modelView, FloatBuffer projection, IntBuffer viewport) {
        MODELVIEW = modelView;
        PROJECTION = projection;
        VIEWPORT = viewport;
    }

    public static Vec3 w2s(float x, float y, float z) {
        SCREEN_COORDS.clear();

        boolean valid = GLU.gluProject(x, y, z, MODELVIEW, PROJECTION, VIEWPORT, SCREEN_COORDS);

        if (!valid) return null;

        float screenX = SCREEN_COORDS.get(0);
        float screenY = SCREEN_COORDS.get(1);
        float screenZ = SCREEN_COORDS.get(2);

        if (screenZ < 0 || screenZ > 1) {
            return null;
        }

        return new Vec3(screenX, VIEWPORT.get(3) - screenY, screenZ);
    }
}