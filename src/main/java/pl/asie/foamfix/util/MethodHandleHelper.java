package pl.asie.foamfix.util;

import net.minecraftforge.fml.relauncher.ReflectionHelper;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;

public final class MethodHandleHelper {
    private MethodHandleHelper() {

    }

    public static MethodHandle findFieldGetter(Class c, String... names) {
        try {
            return MethodHandles.lookup().unreflectGetter(ReflectionHelper.findField(c, names));
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static MethodHandle findFieldSetter(Class c, String... names) {
        try {
            return MethodHandles.lookup().unreflectSetter(ReflectionHelper.findField(c, names));
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static MethodHandle findFieldGetter(String s, String... names) {
        try {
            return findFieldGetter(Class.forName(s), names);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static MethodHandle findFieldSetter(String s, String... names) {
        try {
            return findFieldSetter(Class.forName(s), names);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }
}
