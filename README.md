# ActionBroadcaster

A Sponge minecraft server plugin to create automated messages
that will be printed into the action chat slot.

## Features

* Auto broadcasting
* Configurable messages
* Random message selection
* Lightweight

## Commands

	/ab list - Shows all messages in seperate pages
	/ab add <message> - Adds a new message to the messages
	/ab reload - Reloads the configuration from disk. It will then cancel all running tasks and start new ones
	/ab remove <index> - Removes an existing message from the list
	/ab broadcast <message/index> - Broadcasts a new specific message or one from the config
	/ab - Shows plugin name and version

## Config

    # Disable the entire broadcast functionality
    enabled=true
    # Interval in seconds to wait for the next message
    interval=320
    # All messages which will be displayed
    messages=[
        "§cExample 1",
        "§cExample 2",
        "§cExample 3",
        "§cExample 4",
        "§cExample 5",
        "§cExample 6",
        "§cExample 7",
        "§cExample 8",
        "§cExample 9",
        "§cExample 10",
        "§cExample 11",
        "§cExample 12",
        "§cExample 13",
        "§cExample 14",
        "§cExample 15",
        "§cExample 16",
        "§cExample 17",
        "§cExample 18",
        "§cExample 19",
        "§cExample 20",
        "§cExample 21",
        "§cExample 22"
    ]
    # Should the message be selected in random order
    random=false

## Permissions

	actionbroadcaster.receive
	actionbroadcaster.add
	actionbroadcaster.broadcast
	actionbroadcaster.reload
	actionbroadcaster.list
	actionbroadcaster.remove

## Images

### Example Broadcast Command

![Broadcast](https://i.imgur.com/rM4qZ3x.png)

### Example List Command
![List command](https://i.imgur.com/3uAg70U.png)
(You can click on the X in order to remove the message directly)

