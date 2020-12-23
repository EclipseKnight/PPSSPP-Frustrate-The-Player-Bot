package ppssppftpbot.pccb.net.twitch;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import java.util.List;
import java.util.Map;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public class TwitchConfiguration {

    private Map<String, String> bot;

    private Map<String, String> api;

    private Map<String, String> credentials;

    private List<String> channels;
    
    private List<String> listenerChannels;
    
    private Map<String, TwitchFeature> features;
    
    private String prefix;




    public Map<String, String> getBot() {
        return bot;
    }

    public void setBot(Map<String, String> bot) {
        this.bot = bot;
    }

    public Map<String, String> getApi() {
        return api;
    }

    public void setApi(Map<String, String> api) {
        this.api = api;
    }

    public Map<String, String> getCredentials() {
        return credentials;
    }

    public void setCredentials(Map<String, String> credentials) {
        this.credentials = credentials;
    }

    public List<String> getChannels() {
        return channels;
    }

    public void setChannels(List<String> channels) {
        this.channels = channels;
    }
    
    public List<String> getListenerChannels() {
    	return listenerChannels;
    }
    
    public void setListenerChannels(List<String> listenerChannels) {
    	this.listenerChannels = listenerChannels;
    }
    
    public Map<String, TwitchFeature> getFeatures() {
		return features;
	}

    public void setFeatures(Map<String, TwitchFeature> features) {
		this.features = features;
	}
    
    public String getPrefix() {
		return prefix;
	}
    
    public void setPrefix(String prefix) {
		this.prefix = prefix;
	}
    
    @Override
	public String toString() {
		return String.format("""
				Configuration:
					bot = %s
					channels = %s,
					listener_channels = %s
					features:
						prefix: %s
						discord_command_is_live:
					 		enabled = %s,
					 		mod_only = %s
					 		names = %s,
					 		channels = %s
				""", bot, channels, listenerChannels, prefix,
				features.get("twitch_command_is_live").isEnabled(),
				features.get("twitch_command_is_live").isModOnly(),
				features.get("twitch_command_is_live").getName(),
				features.get("twitch_command_is_live").getChannels());
	}
}
