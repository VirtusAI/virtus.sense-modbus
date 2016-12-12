package org.virtus.sense.store;

import java.util.Collection;
import java.util.HashMap;

public final class MemoryStore extends DeviceStore {
	
	public MemoryStore() {
		super();
	}

	@Override
	public Collection<StoredDevice> getCachedDevices() {
		return cachedDevices.values();
	}

	@Override
	public void cacheDevice(StoredDevice device) {
		this.cachedDevices.put(device.networkAddress, device);
	}

	@Override
	public void close() throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	protected void loadStore() {
		this.cachedDevices = new HashMap<>();
		
	}

}
