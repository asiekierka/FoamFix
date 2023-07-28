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
package pl.asie.foamfix.coremod.patches.jei;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraft.client.util.SearchTreeManager;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import pl.asie.foamfix.FoamFix;
import pl.asie.foamfix.client.jei.SearchTreeJEIItems;
import pl.asie.foamfix.shared.FoamFixShared;

import java.lang.reflect.Field;
import java.util.Map;

public class SearchTreeJEIManagerInject implements IResourceManagerReloadListener {
	@Override
	public void onResourceManagerReload(IResourceManager resourceManager) {
		if (FoamFixShared.config.clJeiCreativeSearch && ((Object) this) == Minecraft.getMinecraft().getSearchTreeManager() && SearchTreeJEIPatchGlue.isValid()) {
			try {
				Field f = ReflectionHelper.findField(SearchTreeManager.class, "trees", "field_194013_c");
				Map m = (Map) f.get(Minecraft.getMinecraft().getSearchTreeManager());
				m.put(SearchTreeManager.ITEMS, new SearchTreeJEIItems());
				FoamFix.getLogger().info("JEI search tree manager injection complete!");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		onResourceManagerReload_foamfix_old(resourceManager);
	}

	public void onResourceManagerReload_foamfix_old(IResourceManager resourceManager) {
		Minecraft.getMinecraft().getSearchTreeManager().onResourceManagerReload(resourceManager);
	}
}
