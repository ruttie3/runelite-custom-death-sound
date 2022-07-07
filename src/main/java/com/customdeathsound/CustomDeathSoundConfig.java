package com.customdeathsound;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("Custom death sound")
public interface CustomDeathSoundConfig extends Config
{
	@ConfigItem(
			keyName = "soundFile",
			name = "Sound file",
			description = "Specify a path to a sound file you would like to be played when you die."
	)
	default String soundFile()
	{
		return "";
	}

	@ConfigItem(
			keyName = "volume",
			name = "Volume",
			description = "The sound's volume"
	)
	default int volume()
	{
		return 80;
	}
}
