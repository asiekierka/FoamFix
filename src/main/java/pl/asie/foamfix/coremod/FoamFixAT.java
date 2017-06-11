package pl.asie.foamfix.coremod;

import net.minecraftforge.fml.common.asm.transformers.AccessTransformer;

import java.io.IOException;

public class FoamFixAT extends AccessTransformer {
    public FoamFixAT() throws IOException {
        super("foamfix_runtime_at.cfg");
    }
}
