/**
 * Copyright (C) 2016, 2017, 2018, 2019, 2020, 2021 Adrian Siekierka
 *
 * This file is part of FoamFix.
 *
 * FoamFix is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * FoamFix is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with FoamFix.  If not, see <http://www.gnu.org/licenses/>.
 */
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
