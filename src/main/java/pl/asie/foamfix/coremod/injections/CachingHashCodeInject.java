package pl.asie.foamfix.coremod.injections;

public class CachingHashCodeInject {
    private int foamfix_hashCode;
    private boolean foamfix_hashCode_calced;

    public int hashCode() {
        if (!foamfix_hashCode_calced)
            foamfix_hashCode = hashCode_foamfix_old();
        return foamfix_hashCode;
    }

    public int hashCode_foamfix_old() {
        return 0;
    }
}
