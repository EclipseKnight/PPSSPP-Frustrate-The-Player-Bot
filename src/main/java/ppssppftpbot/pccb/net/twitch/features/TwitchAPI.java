package ppssppftpbot.pccb.net.twitch.features;

import java.util.Collections;

import com.github.twitch4j.helix.domain.Poll;
import com.github.twitch4j.helix.domain.PollsList;
import com.github.twitch4j.helix.domain.User;
import com.github.twitch4j.helix.domain.UserList;

import ppssppftpbot.pccb.net.logger.Logger;
import ppssppftpbot.pccb.net.logger.Logger.Level;
import ppssppftpbot.pccb.net.twitch.TwitchBot;

public class TwitchAPI {

	/**
	 * Gets a twitch user.
	 * @param name
	 * @return
	 */
	public static User getTwitchUser(String name) {
		UserList list = TwitchBot.twitchClient.getHelix().getUsers(null, null, Collections.singletonList(name)).execute();
		
		if (list.getUsers().size() > 0 && list.getUsers().get(0).getLogin().equalsIgnoreCase(name)) {
			return list.getUsers().get(0);
		}
		return null;
	}
	
	/**
	 * create and start a poll for twitch chat.
	 * @param poll
	 * @return PollsList
	 */
	public static PollsList createPoll(Poll poll) {
		return TwitchBot.twitchClient.getHelix().createPoll(null, poll).execute();
	}
	

}


