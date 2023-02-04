package ppssppftpbot.pccb.net.twitch.events;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.github.philippheuer.events4j.simple.SimpleEventHandler;
import com.github.twitch4j.eventsub.domain.PollChoice;
import com.github.twitch4j.helix.domain.Poll;
import com.github.twitch4j.helix.domain.User;
import com.github.twitch4j.pubsub.events.PollsEvent;

import ppssppftpbot.pccb.net.twitch.features.TwitchAPI;

public class PollHandler {
	
	private static Map<String, Poll> activePolls;
	
	public PollHandler(SimpleEventHandler eventHandler) {
		eventHandler.onEvent(PollsEvent.class, event -> onPollEvent(event));
		activePolls = new HashMap<>();
	}
	
	
	/**
	 * Handles all of the poll events.
	 * @param event
	 * @return 
	 * @return
	 */
	private void onPollEvent(PollsEvent event) {
		
		switch (event.getType()) {
			case POLL_COMPLETE -> {
				return;
			}
			
			default -> {
				return;
			}
			
		}
	}


	public static void createPoll(List<String> choices, User broadcaster) {
		List<PollChoice> pollChoices = new ArrayList<>();
		for (String ch: choices) {
			PollChoice choice = PollChoice.builder()
					.title(ch).build();
			pollChoices.add(choice);
		}
		
		
		Poll poll = Poll.builder()
				.choices(pollChoices)
				.id(UUID.randomUUID().toString())
				.broadcasterId(broadcaster.getId())
				.broadcasterLogin(broadcaster.getLogin())
				.durationSeconds(30)
				.build();
		
		activePolls.put(poll.getId(), poll);
		TwitchAPI.createPoll(poll);
		
	}
}
