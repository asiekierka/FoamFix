package pl.asie.foamfix.coremod;

import com.google.common.collect.ImmutableList;
import com.google.common.eventbus.EventBus;
import net.minecraftforge.fml.common.DummyModContainer;
import net.minecraftforge.fml.common.LoadController;
import net.minecraftforge.fml.common.ModMetadata;

import java.util.ArrayList;
import java.util.List;

public class FoamFixCoreContainer extends DummyModContainer {
    private static final ModMetadata md;

    public FoamFixCoreContainer() {
        super(md);
    }

    @Override
    public boolean registerBus(EventBus bus, LoadController controller) {
        return true;
    }

    @Override
    public List<String> getOwnedPackages() {
        return ImmutableList.of("pl.asie.foamfix.coremod");
    }

    static {
        md = new ModMetadata();
        md.modId = "foamfixcore";
        md.name = "FoamFixCore";
        md.description = "I'm actually just an optional part of FoamFix, available exclusively as part of the Anarchy version!";
        (md.authorList = new ArrayList()).add("asie");
        md.version = "7.7.4";
    }
}
