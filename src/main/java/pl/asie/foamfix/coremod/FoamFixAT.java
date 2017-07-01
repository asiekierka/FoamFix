package pl.asie.foamfix.coremod;

import net.minecraftforge.fml.common.asm.transformers.AccessTransformer;
import pl.asie.foamfix.shared.FoamFixShared;

import java.io.IOException;

public class FoamFixAT extends AccessTransformer {
    public FoamFixAT() throws IOException {
        super("foamfix_runtime_at.cfg");
    }

    @Override
    public byte[] transform(String name, String transformedName, byte[] bytes) {
        if (!FoamFixShared.isCoremod)
            return bytes;
        else
            return super.transform(name, transformedName, bytes);
    }
}
