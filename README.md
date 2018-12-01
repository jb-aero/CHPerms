CHPerms
=======

An implementation of the Bukkit permissions system, controlled by CommandHelper

Download the correct version for your version of CommandHelper:
<br>[CHPerms 3.0.0](https://letsbuild.net/jenkins/job/CHPerms/2/) (CommandHelper 3.3.3)
<br>[CHPerms 3.0.1](https://letsbuild.net/jenkins/job/CHPerms/5/) (CommandHelper 3.3.4)

PERMISSION FUNCTIONS
=========
```
get_permissions()
  Type: 	array
  Return: array
  Args: 	customOnly
  Description:
    Returns an array of all registered permissions. If
    customOnly is true, only the permissions you have created will be in the array,
    defaults to false.
```
```
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
```
```
unregister_permission()
Type:  void
Return:	void
Args:	permission
Description: Removes the specified permission if it is registered, otherwise 
nothing happens.
```


PLAYER PERMISSION FUNCTIONS
=========
```
phas_permission()
  Type: 	boolean
  Return:   boolean
  Args: 	[player], permission
  Description:
    Returns whether the target (function user if not given) has a
    permission, based on the server's built in permission system.
```
```
set_permissions
  Type: 	void
  Return: void
  Args: 	[player], permission(s)
  Description:
    Sets an array of permissions at once before	recalculating permissions
    for player. Permissions must be an array of permission arrays in the
    format array('perm.node': true). This overrides permission defaults.
```
```
set_permission
  Type: 	void
  Return:   void
  Args: 	[player], permission, boolean
  Description:
    Sets the value of a permission for a player, defaulting to the
    current user. This overrides permission defaults.
```
```
unset_permission()
  Type: 	void
  Return:   void
  Args: 	[player], permission
  Description:
    Unsets a permission, so only that permission's default will apply.
```
```
unperm_player()
Type:	    boolean
Return:	    boolean
Args:       player
Description: Removes the attachment from the player, returns whether anything 
actually changed.
```
```
hijack_permissions()
  Type: 	void
  Return:   void
  Args:     [player]
  Description: 
    Runs through the given player's permissions, imports any that 
    commandhelper hasn't set, and removes the setting from the player sothat the 
    other plugin can't change it anymore. If no player is given, all players are 
    used.
```
