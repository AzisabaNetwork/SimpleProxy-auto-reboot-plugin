# AutoReboot plugin

Note: This plugin is very unstable, don't use it on production.

## Configuration

### Velocity

```yml
token: # pre-configured token (50 characters or more)
```

### SimpleProxy

```yml
token: # pre-configured token (50 characters or more)

# If true, the plugin will attempt to reboot the machine.
# Otherwise, it will only send a notification and new connections will be denied.
do-real-reboot: false

# arbitrary command to run when attempting to reboot the machine
custom-reboot-command: ""

# If true, the plugin will always attempt to reboot the machine regardless of the system uptime.
debug-always-reboot: false

# If the system uptime is greater than this value (the value is in seconds), the plugin will attempt to reboot the machine.
# In this case, the plugin will attempt to reboot after 7 days of system uptime.
uptime-threshold: 604800 
```
