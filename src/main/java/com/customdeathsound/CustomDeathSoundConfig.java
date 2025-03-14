package com.customdeathsound;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.Range;
import net.runelite.client.config.Units;

@ConfigGroup("custom-death-sound")
public interface CustomDeathSoundConfig extends Config
{
	@ConfigItem(
		keyName = "soundFile",
		name = "Sound file",
		description = "File name, including extension, of a compatible sound file to be played on death." +
			"<br>Must be placed in the '.runelite/custom-death-sound' folder." +
			"<br>Example: death.wav",
		position = 0
	)
	default String soundFile()
	{
		return "";
	}

	@ConfigItem(
		keyName = "soundVolume",
		name = "Sound volume",
		description = "Volume of sound to be played on death." +
			"<br>Setting this to 0 will disable sound playback.",
		position = 1
	)
	@Range(max = 100)
	@Units(Units.PERCENT)
	default int soundVolume()
	{
		return 100;
	}

	@ConfigItem(
		keyName = "debug",
		name = "Debug",
		description = "Prints debug information to chatbox.",
		position = 2
	)
	default boolean debug()
	{
		return false;
	}
}
