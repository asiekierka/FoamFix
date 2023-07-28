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
