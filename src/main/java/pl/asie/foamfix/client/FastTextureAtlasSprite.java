/*
 * Copyright (C) 2016, 2017 Adrian Siekierka
 *
 * This file is part of FoamFix.
 *
 * FoamFix is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * FoamFix is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with FoamFix.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Additional permission under GNU GPL version 3 section 7
 *
 * If you modify this Program, or any covered work, by linking or
 * combining it with the Minecraft game engine, the Mojang Launchwrapper,
 * the Mojang AuthLib and the Minecraft Realms library (and/or modified
 * versions of said software), containing parts covered by the terms of
 * their respective licenses, the licensors of this Program grant you
 * additional permission to convey the resulting work.
 */

/*
  Copyright (c) 2015 Kobata
  Copyright (c) 2017 asiekierka

  Permission is hereby granted, free of charge, to any person obtaining a copy
  of this software and associated documentation files (the "Software"), to deal
  in the Software without restriction, including without limitation the rights
  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  copies of the Software, and to permit persons to whom the Software is
  furnished to do so, subject to the following conditions:

  The above copyright notice and this permission notice shall be included in all
  copies or substantial portions of the Software.

  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
  SOFTWARE.
 */

package pl.asie.foamfix.client;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureUtil;
import org.lwjgl.opengl.ARBCopyImage;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL14;
import org.lwjgl.util.glu.GLU;
import pl.asie.foamfix.FoamFix;
import pl.asie.foamfix.api.IFoamFixSprite;
import pl.asie.foamfix.shared.FoamFixShared;

import java.util.List;

public class FastTextureAtlasSprite extends TextureAtlasSprite implements IFoamFixSprite {
    private int textureId = -1;
    private int mipLevels = 0;

    public FastTextureAtlasSprite(String p_i1282_1_) {
        super(p_i1282_1_);
    }

    @Override
    public void updateAnimation() {
        if (FoamFixShared.config.clDisableTextureAnimations)
            return;

        ++tickCounter;

        if (tickCounter >= animationMetadata.getFrameTimeSingle(frameCounter)) {
            int i = animationMetadata.getFrameIndex(frameCounter);
            int j = animationMetadata.getFrameCount() == 0 ? framesTextureData.size() : animationMetadata.getFrameCount();
            frameCounter = (frameCounter + 1) % j;
            tickCounter = 0;
            int k = animationMetadata.getFrameIndex(frameCounter);

            if (i != k && k >= 0 && k < framesTextureData.size()) {

                if (textureId != -1) {
                    int destTex = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
                    checkGLError("updateAnimation | fastPath getPreviousTexture");

                    // Unbinding texture for safety, since copy image has an explicit destination.
                    GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
                    checkGLError("updateAnimation | fastPath unbindTex");

                    for (int mip = 0; mip <= mipLevels; ++mip) {
                        ARBCopyImage
                                .glCopyImageSubData(textureId, GL11.GL_TEXTURE_2D, mip, (width * k) >> mip, 0, 0, destTex, GL11.GL_TEXTURE_2D, mip, originX >> mip,
                                        originY >> mip,
                                        0, width >> mip, height >> mip, 1);
                        checkGLError("updateAnimation | fastPath mip="+mip);
                    }

                    GL11.glBindTexture(GL11.GL_TEXTURE_2D, destTex);
                    checkGLError("updateAnimation | fastPath rebindTex");
                } else {
                    int[][] frameData = this.framesTextureData.get(k);
                    uploadTextureMaxMips(mipLevels, frameData, width, height, originX, originY, false, false, frameData.length > 1);
                    checkGLError("updateAnimation | slowPath");
                }
            }
        } else if (this.animationMetadata.isInterpolate()) {
            this.updateAnimationInterpolated();
        }
    }

    private int interpolateColor(double ratio, int from, int to) {
        return (int)((ratio * from) + ((1.0D - ratio) * to));
    }

    private boolean interpolateFrame(int[] to, int[] from1, int[] from2, double ratio) {
        if (from1.length == from2.length) {
            for (int i = 0; i < from1.length; ++i) {
                int color1 = from1[i];
                int color2 = from2[i];
                int colorRed = this.interpolateColor(ratio, color1 >> 16 & 0xFF, color2 >> 16 & 0xFF);
                int colorGreen = this.interpolateColor(ratio, color1 >> 8 & 0xFF, color2 >> 8 & 0xFF);
                int colorBlue = this.interpolateColor(ratio, color1 & 0xFF, color2 & 0xFF);
                to[i] = color1 & 0xFF000000 | colorRed << 16 | colorGreen << 8 | colorBlue;
            }
            return true;
        } else {
            return false;
        }
    }

    @Override
    protected void updateAnimationInterpolated() {
        double ratio = 1.0D - (double)this.tickCounter / (double)this.animationMetadata.getFrameTimeSingle(this.frameCounter);
        int currentFrameIndex = this.animationMetadata.getFrameIndex(this.frameCounter);
        int frameCount = this.animationMetadata.getFrameCount() == 0 ? this.framesTextureData.size() : this.animationMetadata.getFrameCount();
        int nextFrameIndex = this.animationMetadata.getFrameIndex((this.frameCounter + 1) % frameCount);

        if (currentFrameIndex != nextFrameIndex && nextFrameIndex >= 0 && nextFrameIndex < this.framesTextureData.size()) {
            int[][] frame1 = this.framesTextureData.get(currentFrameIndex);
            int[][] frame2 = this.framesTextureData.get(nextFrameIndex);
            int mipLvl;
            if (this.interpolatedFrameData == null || this.interpolatedFrameData.length != mipLevels + 1) {
                this.interpolatedFrameData = new int[mipLevels + 1][];
            }

            for (mipLvl = 0; mipLvl <= mipLevels; ++mipLvl) {
                if (mipLvl >= frame1.length || mipLvl >= frame2.length) {
                    break;
                }

                if (this.interpolatedFrameData[mipLvl] == null) {
                    this.interpolatedFrameData[mipLvl] = new int[frame1[mipLvl].length];
                }

                interpolateFrame(this.interpolatedFrameData[mipLvl], frame1[mipLvl], frame2[mipLvl], ratio);
            }

            uploadTextureMaxMips(mipLvl, this.interpolatedFrameData, width, height, originX, originY, false, false, interpolatedFrameData.length > 1);
        }
    }

    @Override
    public void setFramesTextureData(List<int[][]> textureData) {
        if(textureId != -1) {
            GL11.glDeleteTextures(textureId);
            textureId = -1;
            checkGLError("setFramesTextureData | deleteTexture");
        }

        super.setFramesTextureData(textureData);

        // TODO: Precalculate certain interpolated textures?

        // No need for extra texture if there's only one frame.
        // We're also not caching really long sets of animation.
        if (textureData.size() > 1 && textureData.size() <= FoamFixShared.config.txCacheAnimationMaxFrames && FoamFix.shouldFasterAnimation) {
            textureId = GL11.glGenTextures();
            checkGLError("setFramesTextureData | createTexture");

            int prevTex = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
            checkGLError("setFramesTextureData | getPreviousTexture");

            // Set up holding texture
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId);
            checkGLError("setFramesTextureData | bindTexture");

            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL12.GL_TEXTURE_MAX_LEVEL, mipLevels);
            checkGLError("setFramesTextureData | setTextureProperties[Mip Levels]");
            GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL12.GL_TEXTURE_MIN_LOD, 0.0F);
            checkGLError("setFramesTextureData | setTextureProperties[LOD Min]");
            GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL12.GL_TEXTURE_MAX_LOD, (float) mipLevels);
            checkGLError("setFramesTextureData | setTextureProperties[LOD Max]");
            GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL14.GL_TEXTURE_LOD_BIAS, 0.0F);
            checkGLError("setFramesTextureData | setTextureProperties[LOD Bias]");

            // Reserve memory for texture
            for (int i = 0; i <= mipLevels; ++i) {
                GL11.glTexImage2D(GL11.GL_TEXTURE_2D, i, GL11.GL_RGBA, (width * framesTextureData.size()) >> i, height >> i, 0, GL12.GL_BGRA,
                        GL11.GL_UNSIGNED_BYTE, (java.nio.ByteBuffer) null);
                checkGLError("setFramesTextureData | createMip " + i);
            }

            // Copy animation frames to holding texture
            for (int i = 0; i < framesTextureData.size(); ++i) {
                uploadTextureMaxMips(mipLevels, framesTextureData.get(i), width, height, width * i, 0, false, false, mipLevels > 0);
                checkGLError("setFramesTextureData | uploadFrame " + i);
            }

            // Restore old texture
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, prevTex);
            checkGLError("setFramesTextureData | restoreTexture");
        }
    }

    private static void uploadTextureMaxMips(int maxMips, int[][] data, int width, int height, int originX, int originY, boolean linearFiltering, boolean clamped, boolean mipFiltering) {
        int mips = maxMips >= 0 ? Math.min(maxMips, data.length-1) : data.length-1;
        for (int mip = 0; mip <= mips; ++mip) {
            if ((width >> mip) > 0 & (height >> mip) > 0) {
                TextureUtil.uploadTextureSub(mip, data[mip], width >> mip, height >> mip, originX >> mip, originY >> mip, linearFiltering,
                        clamped, mipFiltering);
                checkGLError("uploadTextureMaxMips mip=" + mip);
            }
        }
    }

    private static void checkGLError(String desc) {
        int error = GL11.glGetError();

        while(error != GL11.GL_NO_ERROR) {
            String errorString = GLU.gluErrorString(error);

            FoamFix.logger.error("GL Error: " + errorString + "(" + error + ") @ " + desc);

            error = GL11.glGetError();
        }
    }

    @Override
    public void clearFramesTextureData() {
        super.clearFramesTextureData();

        if(textureId != -1) {
            GL11.glDeleteTextures(textureId);
            textureId = -1;
        }
    }

    @Override
    public void generateMipmaps(int p_147963_1_) {
        mipLevels = FoamFixShared.config.txMaxAnimationMipLevel >= 0 ? Math.min(FoamFixShared.config.txMaxAnimationMipLevel, p_147963_1_) : p_147963_1_;

        super.generateMipmaps(p_147963_1_);
    }

    @Override
    public boolean isStoredOnGPU() {
        return textureId != -1 || !hasAnimationMetadata();
    }
}
