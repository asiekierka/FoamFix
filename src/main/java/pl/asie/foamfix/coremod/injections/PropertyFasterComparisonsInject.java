package pl.asie.foamfix.coremod.injections;

import com.google.common.base.Optional;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyHelper;

import java.util.Collection;

public class PropertyFasterComparisonsInject {
    // TODO: Staticify allowedValues as it's the same
    public static class Bool extends PropertyHelper<Boolean> {
        protected Bool(String name) {
            super(name, Boolean.class);
        }

        public boolean equals(Object other) {
            return other == this || (other instanceof PropertyBool && super.equals(other));
        }

        public int hashCode() {
            return super.hashCode();
        }

        @Override
        public Collection<Boolean> getAllowedValues() {
            return null;
        }

        @Override
        public Optional<Boolean> parseValue(String s) {
            return null;
        }

        @Override
        public String getName(Boolean aBoolean) {
            return null;
        }
    }
}
