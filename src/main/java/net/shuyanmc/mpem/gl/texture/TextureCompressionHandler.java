package net.shuyanmc.mpem.gl.texture;

import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GLCapabilities;

public class TextureCompressionHandler {
    private static final boolean SUPPORTS_S3TC = GL.getCapabilities().GL_EXT_texture_compression_s3tc;

    @SubscribeEvent
    public void onTextureStitch(TextureStitchEvent event) {
        if (!SUPPORTS_S3TC) return;

        TextureAtlas atlas = event.getAtlas();
        try {
            // 强制启用纹理压缩
            java.lang.reflect.Field field = TextureAtlas.class.getDeclaredField("blur");
            field.setAccessible(true);
            boolean blur = field.getBoolean(atlas);

            field = TextureAtlas.class.getDeclaredField("mipmap");
            field.setAccessible(true);
            boolean mipmap = field.getBoolean(atlas);

            // 修改内部参数以启用压缩
            if (!blur) {  // 通常不模糊的纹理更适合压缩
                atlas.setFilter(false, mipmap);
                // 使用S3TC/DXT压缩
                GL11.glTexParameteri(GL13.GL_TEXTURE_2D, GL30.GL_TEXTURE_COMPRESSION_HINT, GL30.GL_FASTEST);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}