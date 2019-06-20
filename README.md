# admit
A simple Spigot Plugin that adds a permission that grants administrators the ability to bypass the player limit.

# Permissions
- `admit.bypass`: Allows holder to bypass player limit
- `admit.admin`: Allows access to the `/admit` command

# Bypassing
This plugin adds a permission `admit.bypass` which tags all holders as being able to bypass the player limit in one of two ways:
- `NO_COUNT`: Players with the `admit.bypass` permission will not count toward the player total
- `OVERRIDE`: Players with this permission will count towards the player total, but will be able to join regardless

The mode in use can be changed either in the `config.yml`
```yml
bypass-mode: no_count|override
```
or by running the command `/admit mode [mode]`

# Enabling/Disabling
The entire plugin can be enabled and disabled through the `config.yml` file
```yml
enabled: true|false
```
or by running the command `/admit <on/off>`

# Reloading
If any changes are made to the `config.yml` file, they can be imported via `/admit reload`
