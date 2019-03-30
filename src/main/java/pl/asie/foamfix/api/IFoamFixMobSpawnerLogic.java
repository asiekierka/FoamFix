package pl.asie.foamfix.api;

public interface IFoamFixMobSpawnerLogic {
	/**
	 * Forces the MobSpawnerBaseLogic's isActivated() check to a given value
	 * on this specific tick.
	 *
	 * @param value The value to be set.
	 * @return Whether or not the force succeeded.
	 */
	boolean forceSpawnActivationFlag(boolean value);
}
