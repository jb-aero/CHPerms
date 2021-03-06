package io.github.jb_aero.perms;

import com.laytonsmith.PureUtilities.SimpleVersion;
import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.core.extensions.AbstractExtension;
import com.laytonsmith.core.extensions.MSExtension;

@MSExtension("CHPerms")
public class CHPerms extends AbstractExtension {

	public Version getVersion() {
		return new SimpleVersion(3, 0, 1);
	}

	@Override
	public void onStartup() {
		System.out.println("CHPerms " + getVersion() + " loaded.");
	}

	@Override
	public void onShutdown() {
		System.out.println("CHPerms " + getVersion() + " unloaded.");
	}
}