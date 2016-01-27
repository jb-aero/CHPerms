package io.github.jb_aero.perms;

import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.annotations.api;
import com.laytonsmith.core.CHVersion;
import com.laytonsmith.core.constructs.CArray;
import com.laytonsmith.core.constructs.CString;
import com.laytonsmith.core.constructs.Construct;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.exceptions.CRE.CREPluginInternalException;
import com.laytonsmith.core.exceptions.CRE.CREThrowable;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.functions.AbstractFunction;
import net.milkbowl.vault.chat.Chat;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;

public class Vault {

	public static Chat chat = null;

	private static boolean setupChat() {
		if (chat == null) {
			RegisteredServiceProvider<Chat> chatProvider
					= Bukkit.getServer().getServicesManager().getRegistration(net.milkbowl.vault.chat.Chat.class);
			if (chatProvider != null) {
				chat = chatProvider.getProvider();
			}
		}
		return (chat != null);
	}

	public abstract static class VaultFunction extends AbstractFunction {
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREPluginInternalException.class};
		}

		public boolean isRestricted() {
			return true;
		}

		public Boolean runAsync() {
			return false;
		}

		public Version since() {
			return CHVersion.V3_3_1;
		}
	}

	@api
	public static class vault_group_prefix extends VaultFunction {

		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			if (!setupChat()) {
				throw new CREPluginInternalException("Could not connect to vault.", t);
			}
			return new CString(chat.getGroupPrefix(args[0].val(), args[1].val()), t);
		}

		public String getName() {
			return "vault_group_prefix";
		}

		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		public String docs() {
			return "string {world, group} Does exactly what you'd think it does. If you have to ask, you're stupid.";
		}
	}

	@api
	public static class vault_pgroup extends VaultFunction {

		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			if (!setupChat()) {
				throw new CREPluginInternalException("Could not connect to vault.", t);
			}
			CArray ret = new CArray(t);
			for (String group : chat.getPlayerGroups(args[0].val(), args[1].val())) {
				ret.push(new CString(group, t), t);
			}
			return ret;
		}

		public String getName() {
			return "vault_pgroup";
		}

		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		public String docs() {
			return "array {world, player} Returns an array of the groups the given player is in at the given world.";
		}
	}
}
