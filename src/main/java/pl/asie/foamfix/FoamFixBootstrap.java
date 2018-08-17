/*
 * Copyright (C) 2016, 2017, 2018 Adrian Siekierka
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

package pl.asie.foamfix;

import blue.endless.jankson.Jankson;
import blue.endless.jankson.JsonObject;
import blue.endless.jankson.JsonPrimitive;
import blue.endless.jankson.impl.SyntaxError;
import com.google.common.base.Charsets;
import com.google.common.cache.CacheBuilder;
import com.google.common.io.Files;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.launchwrapper.LaunchClassLoader;
import org.dimdev.riftloader.listener.InitializationListener;
import org.spongepowered.asm.mixin.Mixins;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Map;

public class FoamFixBootstrap implements InitializationListener {
    public static boolean optimizeBakedModelStorage;

    @Override
    public void onInitialization() {
        File configDir = new File(Launch.minecraftHome, "config");
        if (!configDir.exists()) configDir.mkdir();
        File configFile = new File(configDir, "foamfix.hjson");

        JsonObject object_;
        try {
            object_ = Jankson.builder().build().load(configFile);
        } catch (SyntaxError e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            object_ = new JsonObject();
        }
        final JsonObject object = object_;

        Arrays.asList("launchwrapper", "client").forEach((s) -> object.putDefault(s, new JsonObject(), null));
        JsonObject client = object.getObject("client");
        JsonObject launchwrapper = object.getObject("launchwrapper");

        client.putDefault("optimizeBakedModelStorage", new JsonPrimitive(true), "Optimize in-memory baked model storage.");
        launchwrapper.putDefault("weakenResourceCache", new JsonPrimitive(true), "Weaken LaunchWrapper resource cache, making it function like an actual cache.");

        try {
            Files.write(object.toJson(true, true), configFile, Charsets.UTF_8);
        } catch (Exception e) {
            e.printStackTrace();
        }

        optimizeBakedModelStorage = client.get(boolean.class, "optimizeBakedModelStorage");

        Mixins.addConfiguration("mixins.foamfix.json");

        if (launchwrapper.get(boolean.class, "weakenResourceCache")) {
            weakenResourceCache();
        }
    }

    public void weakenResourceCache() {
        if (this.getClass().getClassLoader() instanceof LaunchClassLoader) {
            try {
                LaunchClassLoader loader = (LaunchClassLoader) this.getClass().getClassLoader();
                Field resourceCacheField = loader.getClass().getDeclaredField("resourceCache");
                resourceCacheField.setAccessible(true);
                Map oldResourceCache = (Map) resourceCacheField.get(loader);
                Map newResourceCache = CacheBuilder.newBuilder().weakValues().build().asMap();
                newResourceCache.putAll(oldResourceCache);
                resourceCacheField.set(loader, newResourceCache);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
