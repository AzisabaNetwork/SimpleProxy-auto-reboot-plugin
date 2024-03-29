# AutoReboot plugin

## Prerequisites

- [BungeeProxyAnnouncer](https://github.com/AzisabaNetwork/BungeeProxyAnnouncer/tree/velocity)

## Configuration

### Velocity

```yml
redis:
  hostname: localhost
  port: 6379
  username: # optional
  password: # optional

# the message when the proxy is restarting. you can use json or legacy (&) format.
reboot-component: # list
  - "&cThis proxy is restarting soon. Please reconnect to avoid disruption."
```

### SimpleProxy

```yml
redis:
  hostname: localhost
  port: 6379
  username: # optional
  password: # optional

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
