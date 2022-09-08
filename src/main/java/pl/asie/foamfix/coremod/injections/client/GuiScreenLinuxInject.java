package pl.asie.foamfix.coremod.injections.client;

import net.minecraft.util.Util;
import pl.asie.foamfix.FoamFix;
import pl.asie.foamfix.ProxyClient;

import java.net.URI;

public class GuiScreenLinuxInject {
    private void openWebLink(URI url) {
        Util.EnumOS osType = Util.getOSType();
        if (osType == Util.EnumOS.LINUX || osType == Util.EnumOS.UNKNOWN) {
            FoamFix.proxy.openUrlLinux(url);
        } else {
            openWebLink_foamfix_old(url);
        }
    }

    private void openWebLink_foamfix_old(URI url) {

    }
}
