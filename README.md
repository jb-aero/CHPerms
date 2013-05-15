CHPerms
=======

An implementation of the bukkit permissions system, controlled by CommandHelper



FUNCTIONS
=========

unregister_permission()
Type:  void
Return:	void
Args:	permission
Description: Removes the specified permission if it is registered, otherwise 
nothing happens.


unperm_player()
Type:	boolean
Return:	boolean
Args:	
Description: Removes the attachment from the player, returns whether anything 
actually changed.


register_permission()
  Type: 	void
  Return: void
  Args: 	permissionArray, overwrite
  Description: 
    Registers a permission on the server. If overwrite
    is true, any conflicting permissions will be unregistered. The permissionArray 
    must include a 'name' key containing the permission's name. Other keys can be 
    'default', 'description', and 'children'. Default can be one of true, false, op, 
    or !op, but defaults to op. If description is not given, it won't have one. 
    Children must be null or an array of permission name keys and boolean values. 
    The values given will be the values of the child when the parent is set. This 
    is the equivilent of setting permissions in the server permissions.yml.


phas_permission()
  Type: 	boolean
  Return: boolean
  Args: 	[player], permission
  Description: 
    Returns whether the target (function user if not given) has a 
    permission, based on the server's built in permission system.


hijack_permissions()
  Type: 	void
  Return: void
  Args:
  Description: 
    Runs through the given player's permissions, imports any that 
    commandhelper hasn't set, and removes the setting from the player sothat the 
    other plugin can't change it anymore. If no player is given, all players are 
    used.

get_permissions()
  Type: 	array
  Return: array
  Args: 	customOnly
  Description: 
    Returns an array of all registered permissions. If
    customOnly is true, only the permissions you have created will be in the array,
    defaults to false.

unset_permission()
  Type: 	void
  Return: void
  Args: 	[player], permission
  Description: 
    Unsets a permission, so only that permission's default will apply.

set_permission
  Type: 	void
  Return: void
  Args: 	[player], permission, boolean
  Description: 
    Sets the value of a permission for a player, defaulting to the 
    current user. This overrides permission defaults.
