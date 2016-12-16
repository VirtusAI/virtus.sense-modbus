package org.virtus.sense.store;

import java.util.Collection;
import java.util.Map;

public abstract class DeviceStore implements AutoCloseable {
	
	protected Map<Integer, StoredDevice> cachedDevices;
	
	abstract public Collection<StoredDevice> getCachedDevices();
	
	abstract public void cacheDevice(StoredDevice device);
}
