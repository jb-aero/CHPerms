package com.zeoldcraft.perms;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Map.Entry;

import net.milkbowl.vault.chat.Chat;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;

import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.PureUtilities.Common.StringUtils;
import com.laytonsmith.abstraction.MCCommandSender;
import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.abstraction.bukkit.BukkitMCPlayer;
import com.laytonsmith.annotations.api;
import com.laytonsmith.commandhelper.CommandHelperPlugin;
import com.laytonsmith.core.CHVersion;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.constructs.CArray;
import com.laytonsmith.core.constructs.CBoolean;
import com.laytonsmith.core.constructs.CNull;
import com.laytonsmith.core.constructs.CString;
import com.laytonsmith.core.constructs.CVoid;
import com.laytonsmith.core.constructs.Construct;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.CommandHelperEnvironment;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.functions.AbstractFunction;
import com.laytonsmith.core.functions.Exceptions.ExceptionType;

public class CHPerms {

	private static Map<String,PermissionAttachment> attachments = new HashMap<String,PermissionAttachment>();
	private static Map<String,Permission> permissions = new HashMap<String,Permission>();
	
	public static PermissionAttachment getAttachment(Player player) {
		if (!attachments.containsKey(player.getName())) {
			attachments.put(player.getName(), player.addAttachment(CommandHelperPlugin.self));
		}
		return attachments.get(player.getName());
	}
	
	public abstract static class PermFunction extends AbstractFunction {
		public ExceptionType[] thrown() {
			return new ExceptionType[]{};
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
	
	public static class Convertor {
		public static CArray permission(Permission p, Target t) {
			CArray ret = new CArray(t);
			ret.set("name", new CString(p.getName(), t), t);
			Construct description;
			if (p.getDescription() == null) {
				description = CNull.NULL(t);
			} else {
				description = new CString(p.getDescription(), t);
			}
			ret.set("description", description, t);
			ret.set("default", new CString(p.getDefault().name(), t), t);
			Construct children;
			if (p.getChildren() == null) {
				children = CNull.NULL(t);
			} else {
				CArray ca = new CArray(t);
				for (Entry<String, Boolean> perm : p.getChildren().entrySet()) {
					ca.set(perm.getKey(), new CBoolean(perm.getValue(), t), t);
				}
				children = ca;
			}
			ret.set("children", children, t);
			return ret;
		}
		
		public static Permission permission(Construct c, Target t) {
			if (c instanceof CArray) {
				CArray ca = (CArray) c;
				String description = null;
				Map<String,Boolean> children = null;
				PermissionDefault def = Permission.DEFAULT_PERMISSION;
				String name;
				if (ca.containsKey("name")) {
					name = ca.get("name", t).val();
				} else {
					throw new ConfigRuntimeException("The array did not contain key 'name'",
							ExceptionType.FormatException, t);
				}
				if (ca.containsKey("default") && !(ca.get("default", t) instanceof CNull)) {
					try {
						def = PermissionDefault.valueOf(ca.get("default", t).val());
					} catch (IllegalArgumentException iae) {
						throw new ConfigRuntimeException("Default must be one of: "
								+ StringUtils.Join(PermissionDefault.values(), ", ", ", or "),
								ExceptionType.FormatException, t);
					}
				}
				if (ca.containsKey("description") && !(ca.get("description", t) instanceof CNull)) {
					description = ca.get("description", t).val();
				}
				if (ca.containsKey("children") && !(ca.get("children", t) instanceof CNull)) {
					if (ca.get("children", t) instanceof CArray) {
						children = new HashMap<String, Boolean>();
						for (String key : ((CArray) ca.get("children", t)).keySet()) {
							children.put(key, Static.getBoolean(ca));
						}
					} else {
						throw new ConfigRuntimeException("Key children was expected to be an array",
								ExceptionType.FormatException, t);
					}
				}
				return new Permission(name, description, def, children);
			} else {
				throw new ConfigRuntimeException("A permission array was expected", ExceptionType.FormatException, t);
			}
		}
	}
	
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
	
	@api
	public static class vault_group_prefix extends PermFunction {

		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			if (!setupChat()) {
				throw new ConfigRuntimeException("Could not connect to vault.", ExceptionType.PluginInternalException, t);
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
	public static class vault_pgroup extends PermFunction {

		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			if (!setupChat()) {
				throw new ConfigRuntimeException("Could not connect to vault.", ExceptionType.PluginInternalException, t);
			}
			CArray ret = new CArray(t);
			for (String group : chat.getPlayerGroups(args[0].val(), args[1].val())) {
				ret.push(new CString(group, t));
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
	
	@api
	public static class get_permissions extends PermFunction {

		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			PluginManager pm = (PluginManager) Static.getServer().getPluginManager().getHandle();
			boolean customOnly = false;
			if (args.length == 1) {
				customOnly = Static.getBoolean(args[0]);
			}
			CArray ret = new CArray(t);
			if (customOnly) {
				for (Entry<String, Permission> entry : permissions.entrySet()) {
					ret.set(entry.getKey(), Convertor.permission(entry.getValue(), t), t);
				}
			} else {
				for (Permission p : pm.getPermissions()) {
					ret.set(p.getName(), Convertor.permission(p, t), t);
				}
			}
			return ret;
		}

		public String getName() {
			return "get_permissions";
		}

		public Integer[] numArgs() {
			return new Integer[]{0, 1};
		}

		public String docs() {
			return "array {customOnly} Returns an array of all registered permissions. If customOnly is true,"
					+ " only the permissions you have created will be in the array, defaults to false.";
		}
	}
	
	@api
	public static class register_permission extends PermFunction {

		@Override
		public ExceptionType[] thrown() {
			return new ExceptionType[]{ExceptionType.FormatException};
		}
		
		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			boolean overwrite = false;
			if (args.length == 2) {
				overwrite = Static.getBoolean(args[1]);
			}
			PluginManager pm = (PluginManager) Static.getServer().getPluginManager().getHandle();
			Permission perm = Convertor.permission(args[0], t);
			if (overwrite) {
				pm.removePermission(perm.getName());
			}
			try {
				pm.addPermission(perm);
			} catch (IllegalArgumentException iae) {
				throw new ConfigRuntimeException("The given permission already exists", ExceptionType.FormatException, t);
			}
			permissions.put(perm.getName(), perm);
			return CVoid.VOID(t);
		}

		public String getName() {
			return "register_permission";
		}

		public Integer[] numArgs() {
			return new Integer[]{1, 2};
		}

		public String docs() {
			return "void {permissionArray, overwrite} Registers a permission on the server. If overwrite is true,"
					+ " any conflicting permissions will be unregistered. The permissionArray must include a 'name'"
					+ " key containing the permission's name. Other keys can be 'default', 'description', and 'children'."
					+ " Default can be one of " + StringUtils.Join(PermissionDefault.values(), ", ", ", or ")
					+ ", but defaults to " + Permission.DEFAULT_PERMISSION + ". If description is not given,"
					+ " it won't have one. Children must be null or an array of permission name keys and boolean values."
					+ " The values given will be the values of the child when the parent is set."
					+ " This is the equivilent of setting permissions in the server permissions.yml.";
		}
	}
	
	@api
	public static class unregister_permission extends PermFunction {

		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			((PluginManager) Static.getServer().getPluginManager().getHandle()).removePermission(args[0].val());
			permissions.remove(args[0].val());
			return CVoid.VOID(t);
		}

		public String getName() {
			return "unregister_permission";
		}

		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		public String docs() {
			return "void {permission} Removes the specified permission if it is registered, otherwise nothing happens.";
		}
	}
	
	@api(environments={CommandHelperEnvironment.class})
	public static class phas_permission extends PermFunction {

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
				throw new ConfigRuntimeException("No commandsender was given", ExceptionType.NullPointerException, t);
			}
			
			return new CBoolean(((CommandSender) mcs.getHandle()).hasPermission(perm), t);
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
	
	@api(environments={CommandHelperEnvironment.class})
	public static class set_permission extends PermFunction {

		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			MCCommandSender mcs;
			String perm;
			boolean value;
			if (args.length == 2) {
				mcs = environment.getEnv(CommandHelperEnvironment.class).GetCommandSender();
				if (!(mcs instanceof MCPlayer)) {
					throw new ConfigRuntimeException("Only players supported at this time",
							ExceptionType.PlayerOfflineException, t);
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
			return CVoid.VOID(t);
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
	
	@api
	public static class unset_permission extends PermFunction {

		public Construct exec(Target t, Environment environment,
				Construct... args) throws ConfigRuntimeException {
			MCCommandSender mcs;
			String perm;
			if (args.length == 1) {
				mcs = environment.getEnv(CommandHelperEnvironment.class).GetCommandSender();
				if (!(mcs instanceof MCPlayer)) {
					throw new ConfigRuntimeException("Only players supported at this time",
							ExceptionType.PlayerOfflineException, t);
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
			return CVoid.VOID(t);
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
	public static class unperm_player extends PermFunction {

		public Construct exec(Target t, Environment environment,
				Construct... args) throws ConfigRuntimeException {
			String player = args[0].val();
			boolean success = false;
			if (attachments.containsKey(player)) {
				success = attachments.get(player).remove();
				attachments.remove(player);
			}
			return new CBoolean(success, t);
		}

		public String getName() {
			// TODO Come up with a better name
			return "unperm_player";
		}

		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		public String docs() {
			return "boolean {} Removes the attachment from the player, returns whether anything actually changed.";
		}
	}
	
	@api
	public static class hijack_permissions extends PermFunction {

		private void hijack(Player pl) {
			List<PermissionAttachment> checked = new ArrayList<PermissionAttachment>();
			PermissionAttachment pla = getAttachment(pl);
			for (PermissionAttachmentInfo pa : pl.getEffectivePermissions()) {
				if (!checked.contains(pa.getAttachment()) && pa.getAttachment().getPlugin() != CommandHelperPlugin.self) {
					for (Entry<String, Boolean> perm : pa.getAttachment().getPermissions().entrySet()) {
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
			return CVoid.VOID(t);
		}

		public String getName() {
			return "hijack_permissions";
		}

		public Integer[] numArgs() {
			return new Integer[]{0, 1};
		}

		public String docs() {
			return "void {} Runs through the given player's permissions, imports any that commandhelper hasn't set,"
					+ " and removes the setting from the player so that the other plugin can't change it anymore."
					+ " If no player is given, all players are used.";
		}
	}
}
