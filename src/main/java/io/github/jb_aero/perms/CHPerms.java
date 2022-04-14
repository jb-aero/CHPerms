package io.github.jb_aero.perms;

import com.laytonsmith.PureUtilities.SimpleVersion;
import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.extensions.AbstractExtension;
import com.laytonsmith.core.extensions.MSExtension;

import java.util.logging.Level;

@MSExtension("CHPerms")
public class CHPerms extends AbstractExtension {

	public Version getVersion() {
		return new SimpleVersion(3, 0, 2);
	}

	@Override
	public void onStartup() {
		Static.getLogger().log(Level.INFO, "CHPerms " + getVersion() + " loaded.");
	}

	@Override
	public void onShutdown() {
		Static.getLogger().log(Level.INFO, "CHPerms " + getVersion() + " unloaded.");
	}
}