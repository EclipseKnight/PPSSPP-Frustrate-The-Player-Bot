# Welcome to PPSSPP Frustrate The Player Bot.
## a.k.a ppssppftpbot

This program utilizes the PPSSPP Remote Debugger tool to send and receive events to and from a ppsspp game.
Similar to the Crowd Control program, this bot lets you and the stream chat mess with the streamer playing a game on the ppsspp.

The original intent of the bot was to do a "Stream vs man" type ordeal but for Monster Hunter Freedom Unite.
Of course, if you know the appropriate memory addresses of any game you can manipulate it to your liking and transform the entire game.
e.g. Removing items from the players inventory, making enemies invisible, setting the players health to 10%, etc.

Here is an example of just that...

![example usage](https://i.imgur.com/MN1K7Ys.png)

# Goals

Obviously this is still a big work in progress and I am continuing to conjure up more ideas as I'm working on it, but here are a few that I have to get done right now:

- Improve snippet configuration. My goal is to use a json database which will easily allow the end user to add their own snippets. 
- Multi-snippet commands. Use multiple snippets in 1 command e.g. clear player inventory & set their health to 10%.
- Implement chat controls. I still need to implement a system that lets chat control the streamer. Wether through bit donations, follows, etc. 
- Snippet vote. A system that allows users to vote on a snippet from a random selection of them. Randomly select 3 snippets to vote on. 

# Currently

Currently the bot can only do so many things. You can configure your own twitch bot and websocket, configuring snippets, and manually use those snippets or manual events. 
Clearly this isn't much, but they are the fundamentals functions required to achieve the current goals. It is important to have well thought out fundamental structure so that implementing the new features are easy and less bug prone. 

In the end, extreme configurability is one of my biggest goals to allow the end user as much control over the bot as possible. However, it is a very time consuming system to flesh out and requires a lot of future planning to save from having to rewrite old implementations later. It is also important to keep things simple for the end user, so they don't need a guide to show them how to set things up. 
