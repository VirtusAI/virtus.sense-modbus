package org.virtus.sense.store;

import java.util.Collection;
import java.util.Map;

public abstract class DeviceStore implements AutoCloseable {
	
	protected Map<Integer, StoredDevice> cachedDevices;
	
	public DeviceStore() {
		this.loadStore();
	}
	
	abstract public Collection<StoredDevice> getCachedDevices();
	
	abstract public void cacheDevice(StoredDevice device);
	
	abstract protected void loadStore();
}
