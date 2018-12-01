package io.github.jb_aero.perms;

import com.laytonsmith.PureUtilities.Common.StringUtils;
import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.annotations.api;
import com.laytonsmith.core.MSVersion;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.constructs.CArray;
import com.laytonsmith.core.constructs.CBoolean;
import com.laytonsmith.core.constructs.CNull;
import com.laytonsmith.core.constructs.CString;
import com.laytonsmith.core.constructs.CVoid;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.exceptions.CRE.CREFormatException;
import com.laytonsmith.core.exceptions.CRE.CREThrowable;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.functions.AbstractFunction;
import com.laytonsmith.core.natives.interfaces.Mixed;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.PluginManager;

import java.util.HashMap;
import java.util.Map;

public class Permissions {

	private static Map<String, Permission> permissions = new HashMap<String, Permission>();

	public static class Convertor {
		public static CArray permission(Permission p, Target t) {
			CArray ret = new CArray(t);
			ret.set("name", new CString(p.getName(), t), t);
			Mixed description;
			if (p.getDescription() == null) {
				description = CNull.NULL;
			} else {
				description = new CString(p.getDescription(), t);
			}
			ret.set("description", description, t);
			ret.set("default", new CString(p.getDefault().name(), t), t);
			Mixed children;
			if (p.getChildren() == null) {
				children = CNull.NULL;
			} else {
				CArray ca = new CArray(t);
				for (Map.Entry<String, Boolean> perm : p.getChildren().entrySet()) {
					ca.set(perm.getKey(), CBoolean.get(perm.getValue()), t);
				}
				children = ca;
			}
			ret.set("children", children, t);
			return ret;
		}

		public static Permission permission(Mixed c, Target t) {
			if (c instanceof CArray) {
				CArray ca = (CArray) c;
				String description = null;
				Map<String, Boolean> children = null;
				PermissionDefault def = Permission.DEFAULT_PERMISSION;
				String name;
				if (ca.containsKey("name")) {
					name = ca.get("name", t).val();
				} else {
					throw new CREFormatException("The array did not contain key 'name'", t);
				}
				if (ca.containsKey("default") && !(ca.get("default", t) instanceof CNull)) {
					try {
						def = PermissionDefault.valueOf(ca.get("default", t).val());
					} catch (IllegalArgumentException iae) {
						throw new CREFormatException("Default must be one of: "
								+ StringUtils.Join(PermissionDefault.values(), ", ", ", or "), t);
					}
				}
				if (ca.containsKey("description") && !(ca.get("description", t) instanceof CNull)) {
					description = ca.get("description", t).val();
				}
				if (ca.containsKey("children") && !(ca.get("children", t) instanceof CNull)) {
					if (ca.get("children", t) instanceof CArray) {
						children = new HashMap<String, Boolean>();
						for (String key : ((CArray) ca.get("children", t)).stringKeySet()) {
							children.put(key, Static.getBoolean(ca, t));
						}
					} else {
						throw new CREFormatException("Key children was expected to be an array", t);
					}
				}
				return new Permission(name, description, def, children);
			} else {
				throw new CREFormatException("A permission array was expected", t);
			}
		}
	}

	public abstract static class PermFunction extends AbstractFunction {

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
			return MSVersion.V3_3_1;
		}
	}

	@api
	public static class get_permissions extends PermFunction {

		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			PluginManager pm = (PluginManager) Static.getServer().getPluginManager().getHandle();
			boolean customOnly = false;
			if (args.length == 1) {
				customOnly = Static.getBoolean(args[0], t);
			}
			CArray ret = new CArray(t);
			if (customOnly) {
				for (Map.Entry<String, Permission> entry : permissions.entrySet()) {
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
		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CREFormatException.class};
		}

		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			boolean overwrite = false;
			if (args.length == 2) {
				overwrite = Static.getBoolean(args[1], t);
			}
			PluginManager pm = (PluginManager) Static.getServer().getPluginManager().getHandle();
			Permission perm = Convertor.permission(args[0], t);
			if (overwrite) {
				pm.removePermission(perm.getName());
			}
			try {
				pm.addPermission(perm);
			} catch (IllegalArgumentException iae) {
				throw new CREFormatException("The given permission already exists", t);
			}
			permissions.put(perm.getName(), perm);
			return CVoid.VOID;
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

		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			((PluginManager) Static.getServer().getPluginManager().getHandle()).removePermission(args[0].val());
			permissions.remove(args[0].val());
			return CVoid.VOID;
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
}
