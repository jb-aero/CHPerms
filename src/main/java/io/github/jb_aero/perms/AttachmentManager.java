package io.github.jb_aero.perms;

import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.commandhelper.CommandHelperPlugin;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AttachmentManager {

	public static final Class ATTACHMENT_CLASS = PermissionAttachment.class;
	private static Map<String, PermissionAttachment> attachments = new HashMap<String, PermissionAttachment>();

	public static PermissionAttachment getAttachment(Player player) {
		if (!attachments.containsKey(player.getName())) {
			attachments.put(player.getName(), player.addAttachment(CommandHelperPlugin.self));
		}
		return attachments.get(player.getName());
	}
	
	public static Map<String, PermissionAttachment> getAttachments() {
		return attachments;
	}

	public static void hijack(MCPlayer pl) {
		List<PermissionAttachment> checked = new ArrayList<PermissionAttachment>();
		PermissionAttachment pla = AttachmentManager.getAttachment((Player) pl.getHandle());
		for (PermissionAttachmentInfo pa : ((Player) pl.getHandle()).getEffectivePermissions()) {
			if(pa.getAttachment() == null) continue;

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
}
