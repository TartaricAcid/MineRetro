package com.github.tartaricacid.mineretro.client.screen;

import com.github.tartaricacid.mineretro.Mineretro;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.renderer.GameRenderer;
import org.joml.Matrix4f;

import java.nio.ByteBuffer;

import static com.github.tartaricacid.mineretro.client.jna.MineretroMiddleTier.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.*;

public class VideoManager {
    private final int[] textureId = new int[]{0};
    private final int[] offset = new int[]{0, 0};
    private final VideoRefresh videoRefresh;

    private int pixelFormat = GL_UNSIGNED_SHORT_5_6_5;
    private int pixelType = GL_RGB;

    public VideoManager(int screenWidth, int screenHeight) {
        this.videoRefresh = refreshRecall(screenWidth, screenHeight);
    }

    public void init(int mineretroPixelFormat) {
        genTexture();
        setPixelFormat(mineretroPixelFormat);
    }

    public void clear() {
        if (textureId[0] != 0) {
            glDeleteTextures(textureId[0]);
        }
    }

    public VideoRefresh getVideoRefresh() {
        return videoRefresh;
    }

    public void resize(int left, int top) {
        this.offset[0] = left;
        this.offset[1] = top;
    }

    private void genTexture() {
        textureId[0] = glGenTextures();
        GlStateManager._bindTexture(textureId[0]);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        GlStateManager._bindTexture(0);
    }

    private void setPixelFormat(int mineretroPixelFormat) {
        switch (mineretroPixelFormat) {
            case RETRO_PIXEL_FORMAT_0RGB1555:
                pixelFormat = GL_UNSIGNED_SHORT_5_5_5_1;
                pixelType = GL_BGRA;
                break;
            case RETRO_PIXEL_FORMAT_XRGB8888:
                pixelFormat = GL_UNSIGNED_INT_8_8_8_8_REV;
                pixelType = GL_BGRA;
                break;
            case RETRO_PIXEL_FORMAT_RGB565:
                pixelFormat = GL_UNSIGNED_SHORT_5_6_5;
                pixelType = GL_RGB;
                break;
            default:
                pixelFormat = GL_UNSIGNED_SHORT_5_6_5;
                pixelType = GL_RGB;
                Mineretro.LOGGER.debug("Unknown pixel type {}", mineretroPixelFormat);
        }
    }

    private VideoRefresh refreshRecall(int screenWidth, int screenHeight) {
        return (data, width, height, pitch) -> {
            ByteBuffer byteBuffer = data.getByteBuffer(0, pitch);

            GlStateManager._bindTexture(textureId[0]);
            glPixelStorei(GL_UNPACK_SKIP_PIXELS, 0);
            glPixelStorei(GL_UNPACK_SKIP_ROWS, 0);
            glPixelStorei(GL_UNPACK_ROW_LENGTH, 0);
            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, width, height, 0,
                    pixelType, pixelFormat, byteBuffer);

            RenderSystem.setShaderTexture(0, textureId[0]);
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.enableBlend();

            drawGame(screenWidth, screenHeight);
            RenderSystem.disableBlend();
        };
    }

    private void drawGame(float width, float height) {
        PoseStack poseStack = new PoseStack();
        poseStack.translate(this.offset[0], this.offset[1], 0);
        Matrix4f pose = poseStack.last().pose();

        BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        bufferbuilder.vertex(pose, 0, 0, 0).uv(0f, 0f).color(255, 255, 255, 255).endVertex();
        bufferbuilder.vertex(pose, 0, height, 0).uv(0f, 1f).color(255, 255, 255, 255).endVertex();
        bufferbuilder.vertex(pose, width, height, 0).uv(1f, 1f).color(255, 255, 255, 255).endVertex();
        bufferbuilder.vertex(pose, width, 0, 0).uv(1f, 0f).color(255, 255, 255, 255).endVertex();

        BufferUploader.drawWithShader(bufferbuilder.end());
    }
}
