package pl.asie.foamfix.common;

import gnu.trove.set.hash.TCustomHashSet;
import gnu.trove.strategy.HashingStrategy;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import pl.asie.foamfix.client.IDeduplicatingStorage;
import pl.asie.foamfix.util.DeduplicatingStorageTrove;
import pl.asie.foamfix.util.HashingStrategies;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Set;

public class PropertyValueDeduplicator {
    public int successfuls;

    @SuppressWarnings("unchecked")
    public void deduplicate() {
        IDeduplicatingStorage storage = new DeduplicatingStorageTrove(HashingStrategies.GENERIC);
        Set<IProperty> checkedProperties = new TCustomHashSet<>(HashingStrategies.IDENTITY);

        for (Block b : ForgeRegistries.BLOCKS) {
            for (IProperty property : b.getBlockState().getProperties()) {
                try {
                    if (checkedProperties.add(property)) {
                        Collection allowedValues = property.getAllowedValues();
                        Collection newAllowedValues = (Collection) storage.deduplicate(allowedValues);
                        if (newAllowedValues != allowedValues) {
                            for (Field f : property.getClass().getDeclaredFields()) {
                                f.setAccessible(true);
                                Object o = f.get(property);
                                if (o == allowedValues) {
                                    f.set(property, newAllowedValues);
                                    successfuls++;
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
