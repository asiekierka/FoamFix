package pl.asie.foamfix.coremod.injections.client;

public interface IFoamFixBlockInfoDataProxy {
    int[][][] getRawS();
    int[][][] getRawB();

    void updateRawBS();
}
