package io.github.phantamanta44.libnine.util.render.shader;

import io.github.phantamanta44.libnine.util.math.Vec2i;
import net.minecraft.client.renderer.Matrix4f;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL20;
import org.lwjgl.util.vector.Quaternion;

import javax.annotation.Nullable;
import java.nio.FloatBuffer;
import java.util.Objects;
import java.util.function.Supplier;

public class UniformType<T, C> {

    public static final UniformType<Float, Void> FLOAT = new UniformType<>((l, v, c) -> GL20.glUniform1f(l, v));
    public static final UniformType<Vec2f, Void> FLOAT2 = new UniformType<>((l, v, c) -> GL20.glUniform2f(l, v.x, v.y));
    public static final UniformType<Vec3d, Void> FLOAT3 = new UniformType<>((l, v, c) -> GL20.glUniform3f(l, (float)v.x, (float)v.y, (float)v.z));
    public static final UniformType<Quaternion, Void> FLOAT4 = new UniformType<>((l, v, c) -> GL20.glUniform4f(l, v.x, v.y, v.z, v.w));
    public static final UniformType<Integer, Void> INT = new UniformType<>((l, v, c) -> GL20.glUniform1i(l, v));
    public static final UniformType<Vec2i, Void> INT2 = new UniformType<>((l, v, c) -> GL20.glUniform2i(l, v.getX(), v.getY()));
    public static final UniformType<Vec3i, Void> INT3 = new UniformType<>((l, v, c) -> GL20.glUniform3i(l, v.getX(), v.getY(), v.getZ()));
    public static final UniformType<Matrix4f, FloatBuffer> MAT4 = new UniformType<>((l, v, c) -> {
        Objects.requireNonNull(c);
        c.position(0);
        c.put(v.m00);
        c.put(v.m01);
        c.put(v.m02);
        c.put(v.m03);
        c.put(v.m10);
        c.put(v.m11);
        c.put(v.m12);
        c.put(v.m13);
        c.put(v.m20);
        c.put(v.m21);
        c.put(v.m22);
        c.put(v.m23);
        c.put(v.m30);
        c.put(v.m31);
        c.put(v.m32);
        c.put(v.m33);
        GL20.glUniformMatrix4(l, true, c);
    }, () -> BufferUtils.createFloatBuffer(16));

    private final UniformWriter<T, C> writer;
    @Nullable
    private final Supplier<C> contextFactory;

    public UniformType(UniformWriter<T, C> writer, @Nullable Supplier<C> contextFactory) {
        this.writer = writer;
        this.contextFactory = contextFactory;
    }

    public UniformType(UniformWriter<T, C> writer) {
        this(writer, null);
    }

    @Nullable
    public C generateContext() {
        return contextFactory != null ? contextFactory.get() : null;
    }

    public void set(int location, T value, @Nullable C context) {
        writer.write(location, value, context);
    }

    @FunctionalInterface
    public interface UniformWriter<T, C> {

        void write(int location, T value, @Nullable C context);

    }

}
