package com.github.tartaricacid.mineretro.client.screen;

import com.github.tartaricacid.mineretro.client.jna.Rotation;
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
    private final int[] screenSize = new int[]{0, 0};
    private final VideoRefresh videoRefresh;
    private final GameGeometry geometry;
    private final int rotation;

    private int pixelFormat = GL_UNSIGNED_SHORT_5_6_5;
    private int pixelType = GL_RGB;
    private int bitsPerPixel = 2;

    public VideoManager(GameGeometry geometry, int rotation) {
        this.videoRefresh = refreshRecall();
        this.geometry = geometry;
        this.rotation = rotation;
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

    public void resize(int width, int height) {
        this.screenSize[0] = width;
        this.screenSize[1] = height;
    }

    public void renderVideo(int screenWidth, int screenHeight) {
        // 没有材质时，不渲染
        if (textureId[0] == 0) {
            return;
        }

        // 附加材质，进行渲染
        RenderSystem.setShaderTexture(0, textureId[0]);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.enableBlend();

        float aspectRatio;
        if (rotation == Rotation.ROTATE_0 || rotation == Rotation.ROTATE_180) {
            aspectRatio = (float) this.geometry.base_height / this.geometry.base_width;
        } else {
            // 90 度，或者 270 度旋转的
            aspectRatio = (float) this.geometry.base_width / this.geometry.base_height;
        }

        // 先以宽为参考
        float currentWidth = screenWidth;
        float currentHeight = screenWidth * aspectRatio;
        // 如果计算出的高比设定的还要大，那么得以设定的高为参考
        if (currentHeight > screenHeight) {
            currentWidth = screenHeight / aspectRatio;
            currentHeight = screenHeight;
        }

        drawGame(currentWidth, currentHeight);
        RenderSystem.disableBlend();
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
                bitsPerPixel = 2;
                break;
            case RETRO_PIXEL_FORMAT_XRGB8888:
                pixelFormat = GL_UNSIGNED_INT_8_8_8_8_REV;
                pixelType = GL_BGRA;
                bitsPerPixel = 4;
                break;
            case RETRO_PIXEL_FORMAT_RGB565:
            default:
                pixelFormat = GL_UNSIGNED_SHORT_5_6_5;
                pixelType = GL_RGB;
                bitsPerPixel = 2;
        }
    }

    private VideoRefresh refreshRecall() {
        return (data, width, height, pitch) -> {
            ByteBuffer byteBuffer = data.getByteBuffer(0, pitch);

            GlStateManager._bindTexture(textureId[0]);
            glPixelStorei(GL_UNPACK_SKIP_PIXELS, 0);
            glPixelStorei(GL_UNPACK_SKIP_ROWS, 0);
            glPixelStorei(GL_UNPACK_ROW_LENGTH, pitch / bitsPerPixel);
            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, width, height, 0,
                    pixelType, pixelFormat, byteBuffer);
        };
    }

    private void drawGame(float width, float height) {
        PoseStack poseStack = new PoseStack();
        poseStack.translate((this.screenSize[0] - width) / 2, (this.screenSize[1] - height) / 2, 0);
        Matrix4f pose = poseStack.last().pose();
        BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);

        if (rotation == Rotation.ROTATE_0) {
            bufferbuilder.vertex(pose, 0, 0, 0).uv(0f, 0f).color(255, 255, 255, 255).endVertex();
            bufferbuilder.vertex(pose, 0, height, 0).uv(0f, 1f).color(255, 255, 255, 255).endVertex();
            bufferbuilder.vertex(pose, width, height, 0).uv(1f, 1f).color(255, 255, 255, 255).endVertex();
            bufferbuilder.vertex(pose, width, 0, 0).uv(1f, 0f).color(255, 255, 255, 255).endVertex();
        } else if (rotation == Rotation.ROTATE_90) {
            bufferbuilder.vertex(pose, 0, 0, 0).uv(1f, 0f).color(255, 255, 255, 255).endVertex();
            bufferbuilder.vertex(pose, 0, height, 0).uv(0f, 0f).color(255, 255, 255, 255).endVertex();
            bufferbuilder.vertex(pose, width, height, 0).uv(0f, 1f).color(255, 255, 255, 255).endVertex();
            bufferbuilder.vertex(pose, width, 0, 0).uv(1f, 1f).color(255, 255, 255, 255).endVertex();
        } else if (rotation == Rotation.ROTATE_180) {
            bufferbuilder.vertex(pose, 0, 0, 0).uv(1f, 1f).color(255, 255, 255, 255).endVertex();
            bufferbuilder.vertex(pose, 0, height, 0).uv(1f, 0f).color(255, 255, 255, 255).endVertex();
            bufferbuilder.vertex(pose, width, height, 0).uv(0f, 0f).color(255, 255, 255, 255).endVertex();
            bufferbuilder.vertex(pose, width, 0, 0).uv(0f, 1f).color(255, 255, 255, 255).endVertex();
        } else if (rotation == Rotation.ROTATE_270) {
            bufferbuilder.vertex(pose, 0, 0, 0).uv(0f, 1f).color(255, 255, 255, 255).endVertex();
            bufferbuilder.vertex(pose, 0, height, 0).uv(1f, 1f).color(255, 255, 255, 255).endVertex();
            bufferbuilder.vertex(pose, width, height, 0).uv(1f, 0f).color(255, 255, 255, 255).endVertex();
            bufferbuilder.vertex(pose, width, 0, 0).uv(0f, 0f).color(255, 255, 255, 255).endVertex();
        }

        BufferUploader.drawWithShader(bufferbuilder.end());
    }
}
