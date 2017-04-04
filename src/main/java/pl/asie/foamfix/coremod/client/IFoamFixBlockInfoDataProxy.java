package pl.asie.foamfix.coremod.client;

public interface IFoamFixBlockInfoDataProxy {
    int[][][] getRawS();
    int[][][] getRawB();

    void updateRawBS();
}
