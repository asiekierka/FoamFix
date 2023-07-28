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
package pl.asie.foamfix.coremod;

import com.google.common.collect.ImmutableList;
import com.google.common.eventbus.EventBus;
import net.minecraftforge.fml.common.DummyModContainer;
import net.minecraftforge.fml.common.LoadController;
import net.minecraftforge.fml.common.ModMetadata;

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
        md.authorList = ImmutableList.of("asie");
        md.version = "7.7.4";
    }
}
