package io.github.jb_aero.perms;

import com.laytonsmith.PureUtilities.Common.StringUtils;
import com.laytonsmith.core.ArgumentValidation;
import com.laytonsmith.core.constructs.*;
import com.laytonsmith.core.exceptions.CRE.CREFormatException;
import com.laytonsmith.core.natives.interfaces.Mixed;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

import java.util.HashMap;
import java.util.Map;

public class PermissionConvertor {
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
						children.put(key, ArgumentValidation.getBooleanObject(ca, t));
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
