package io.github.jb_aero.perms;

import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.abstraction.MCCommandSender;
import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.abstraction.bukkit.entities.BukkitMCPlayer;
import com.laytonsmith.annotations.api;
import com.laytonsmith.commandhelper.CommandHelperPlugin;
import com.laytonsmith.core.CHVersion;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.constructs.CArray;
import com.laytonsmith.core.constructs.CBoolean;
import com.laytonsmith.core.constructs.CVoid;
import com.laytonsmith.core.constructs.Construct;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.CommandHelperEnvironment;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.exceptions.CRE.CRENullPointerException;
import com.laytonsmith.core.exceptions.CRE.CREPlayerOfflineException;
import com.laytonsmith.core.exceptions.CRE.CREReadOnlyException;
import com.laytonsmith.core.exceptions.CRE.CREThrowable;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.functions.AbstractFunction;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class PlayerPermissions {

	private static Map<String, PermissionAttachment> attachments = new HashMap<String, PermissionAttachment>();

	public static PermissionAttachment getAttachment(Player player) {
		if (!attachments.containsKey(player.getName())) {
			attachments.put(player.getName(), player.addAttachment(CommandHelperPlugin.self));
		}
		return attachments.get(player.getName());
	}

	public abstract static class PlayerPermFunction extends AbstractFunction {
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{};
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

	@api(environments = {CommandHelperEnvironment.class})
	public static class phas_permission extends PlayerPermFunction {

		public Construct exec(Target t, Environment environment,
							  Construct... args) throws ConfigRuntimeException {
			MCCommandSender mcs;
			String perm;
			if (args.length == 1) {
				mcs = environment.getEnv(CommandHelperEnvironment.class).GetCommandSender();
				perm = args[0].val();
			} else {
				mcs = Static.GetPlayer(args[0], t);
				perm = args[1].val();
			}

			if (mcs == null) {
				throw new CRENullPointerException("No commandsender was given", t);
			}

			return CBoolean.get(((CommandSender) mcs.getHandle()).hasPermission(perm));
		}

		public String getName() {
			return "phas_permission";
		}

		public Integer[] numArgs() {
			return new Integer[]{1, 2};
		}

		public String docs() {
			return "boolean {[player], permission} Returns whether the target (function user if not given) has a permission,"
					+ " based on the server's built in permission system.";
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class set_permission extends PlayerPermFunction {

		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			MCCommandSender mcs;
			String perm;
			boolean value;
			if (args.length == 2) {
				mcs = environment.getEnv(CommandHelperEnvironment.class).GetCommandSender();
				if (!(mcs instanceof MCPlayer)) {
					throw new CREPlayerOfflineException("Only players supported at this time", t);
				}
				perm = args[0].val();
				value = Static.getBoolean(args[1]);
			} else {
				mcs = Static.GetPlayer(args[0], t);
				perm = args[1].val();
				value = Static.getBoolean(args[2]);
			}
			Player player = (Player) mcs.getHandle();
			if (!attachments.containsKey(player.getName())) {
				attachments.put(player.getName(), player.addAttachment(CommandHelperPlugin.self));
			}
			attachments.get(player.getName()).setPermission(perm, value);
			return CVoid.VOID;
		}

		public String getName() {
			return "set_permission";
		}

		public Integer[] numArgs() {
			return new Integer[]{2, 3};
		}

		public String docs() {
			return "void {[player], permission, boolean} Sets the value of a permission for a player,"
					+ " defaulting to the current user. This overrides permission defaults.";
		}
	}

	@api(environments = {CommandHelperEnvironment.class})
	public static class set_permissions extends PlayerPermFunction {

		private Field pField;

		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			MCCommandSender mcs;
			CArray cperms;
			if (args.length == 1) {
				mcs = environment.getEnv(CommandHelperEnvironment.class).GetCommandSender();
				if (!(mcs instanceof MCPlayer)) {
					throw new CREPlayerOfflineException("Only players supported at this time", t);
				}
				cperms = Static.getArray(args[0], t);
			} else {
				mcs = Static.GetPlayer(args[0], t);
				cperms = Static.getArray(args[1], t);
			}
			Player player = (Player) mcs.getHandle();
			if (!attachments.containsKey(player.getName())) {
				attachments.put(player.getName(), player.addAttachment(CommandHelperPlugin.self));
			}
			PermissionAttachment attachment = attachments.get(player.getName());

			Map<String, Boolean> perms = new LinkedHashMap<String, Boolean>();
			for(String key : cperms.stringKeySet()) {
				perms.put(key, Static.getBoolean(cperms.get(key, t)));
			}

			Map<String, Boolean> permissions;
			try {
				if (pField == null) {
					pField = PermissionAttachment.class.getDeclaredField("permissions");
					pField.setAccessible(true);
				}
				permissions = (Map<String, Boolean>) pField.get(attachment);
			} catch (Exception e) {
				throw new CREReadOnlyException("Error trying to make permissions accessible in attachment", t);
			}
			permissions.clear();
			permissions.putAll(perms);
			attachment.getPermissible().recalculatePermissions();
			return CVoid.VOID;
		}

		public String getName() {
			return "set_permissions";
		}

		public Integer[] numArgs() {
			return new Integer[]{1, 2};
		}

		public String docs() {
			return "void {[player], permission(s)} Sets an array of permissions at once before"
					+ " recalculating permissions for player. Permissions must be an array of permission arrays"
					+ " in the format array('perm.node': true). This overrides permission defaults.";
		}
	}

	@api
	public static class unset_permission extends PlayerPermFunction {

		public Construct exec(Target t, Environment environment,
							  Construct... args) throws ConfigRuntimeException {
			MCCommandSender mcs;
			String perm;
			if (args.length == 1) {
				mcs = environment.getEnv(CommandHelperEnvironment.class).GetCommandSender();
				if (!(mcs instanceof MCPlayer)) {
					throw new CREPlayerOfflineException("Only players supported at this time", t);
				}
				perm = args[0].val();
			} else {
				mcs = Static.GetPlayer(args[0], t);
				perm = args[1].val();
			}
			Player player = (Player) mcs.getHandle();
			if (!attachments.containsKey(player.getName())) {
				attachments.put(player.getName(), player.addAttachment(CommandHelperPlugin.self));
			}
			attachments.get(player.getName()).unsetPermission(perm);
			return CVoid.VOID;
		}

		public String getName() {
			return "unset_permission";
		}

		public Integer[] numArgs() {
			return new Integer[]{1, 2};
		}

		public String docs() {
			return "void {[player], permission} Unsets a permission, so only that permission's default will apply.";
		}
	}

	@api
	public static class unperm_player extends PlayerPermFunction {

		public Construct exec(Target t, Environment environment,
							  Construct... args) throws ConfigRuntimeException {
			String player = args[0].val();
			boolean success = false;
			if (attachments.containsKey(player)) {
				success = attachments.get(player).remove();
				attachments.remove(player);
			}
			return CBoolean.get(success);
		}

		public String getName() {
			return "unperm_player";
		}

		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		public String docs() {
			return "boolean {player} Removes the attachment from the player, returns whether anything actually changed.";
		}
	}

	@api
	public static class hijack_permissions extends PlayerPermFunction {

		private void hijack(Player pl) {
			List<PermissionAttachment> checked = new ArrayList<PermissionAttachment>();
			PermissionAttachment pla = getAttachment(pl);
			for (PermissionAttachmentInfo pa : pl.getEffectivePermissions()) {
				if (!checked.contains(pa.getAttachment())
						&& pa.getAttachment().getPlugin() != CommandHelperPlugin.self) {
					for (Map.Entry<String, Boolean> perm : pa.getAttachment().getPermissions().entrySet()) {
						if (!pla.getPermissions().keySet().contains(perm.getKey())) {
							pla.setPermission(perm.getKey(), perm.getValue());
						}
						pa.getAttachment().unsetPermission(perm.getKey());
					}
					pa.getAttachment().remove();
				}
				checked.add(pa.getAttachment());
			}
		}

		public Construct exec(Target t, Environment environment,
							  Construct... args) throws ConfigRuntimeException {
			if (args.length == 1) {
				hijack(((BukkitMCPlayer) Static.GetPlayer(args[0], t))._Player());
			} else {
				for (Player pl : Bukkit.getServer().getOnlinePlayers()) {
					hijack(pl);
				}
			}
			return CVoid.VOID;
		}

		public String getName() {
			return "hijack_permissions";
		}

		public Integer[] numArgs() {
			return new Integer[]{0, 1};
		}

		public String docs() {
			return "void {[player]} Runs through the given player's permissions, imports any that commandhelper hasn't"
					+ " set, and removes the setting from the player so that the other plugin can't change it anymore."
					+ " If no player is given, all players are used.";
		}
	}
}
