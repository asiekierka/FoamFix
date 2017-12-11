package pl.asie.foamfix.api;

/**
 * An interface for FoamFix "fast" texture atlas sprites.
 */
public interface IFoamFixSprite {
    /**
     * @return Whether or not this texture is stored on the GPU.
     * For vanilla textures, this equals !hasAnimationMetadata();
     */
    boolean isStoredOnGPU();
}
