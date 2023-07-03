package org.bikeshared.domain;

public class BikeSharedManager {

	// Singleton -------------------------------------------------------------

	private BikeSharedManager() {
	}

	/**
	 * SingletonHolder is loaded on the first execution of Singleton.getInstance()
	 * or the first access to SingletonHolder.INSTANCE, not before.
	 */
	private static class SingletonHolder {
		private static final BikeSharedManager INSTANCE = new BikeSharedManager();
	}

	public static synchronized BikeSharedManager getInstance() {
		return SingletonHolder.INSTANCE;
	}

	
	// TODO

}
