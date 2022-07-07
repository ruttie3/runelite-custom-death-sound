package com.customdeathsound;

import com.google.inject.Provides;
import javax.inject.Inject;
import javax.sound.sampled.*;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.AnimationID;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.events.AnimationChanged;
import net.runelite.api.events.GameStateChanged;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;

@Slf4j
@PluginDescriptor(
	name = "Custom death sound"
)
public class CustomDeathSoundPlugin extends Plugin
{
	public Clip clip;

	@Inject
	private Client client;

	@Inject
	private CustomDeathSoundConfig config;

	@Subscribe
	public void onAnimationChanged(AnimationChanged animationChanged)
	{
		if (client.getGameState() != GameState.LOGGED_IN
				|| client.getLocalPlayer() == null
				|| client.getLocalPlayer().getHealthRatio() != 0
				|| client.getLocalPlayer().getAnimation() != AnimationID.DEATH
		) {
			return;
		}

		playSound();
	}

	private void playSound()
	{
		String soundFile = config.soundFile();
		if (soundFile.length() == 0) {
			return;
		}

		log.info("Playing sound");

		if (clip != null) {
			clip.close();
		}

		AudioInputStream inputStream = null;
		try {
			URL url = Paths.get(soundFile).toUri().toURL();
			inputStream = AudioSystem.getAudioInputStream(url);
		} catch (UnsupportedAudioFileException | IOException e) {
			e.printStackTrace();
		}

		if (inputStream == null) {
			return;
		}

		try
		{
			clip = AudioSystem.getClip();
			clip.open(inputStream);
		} catch (LineUnavailableException | IOException e) {
			log.warn("Could not load sound file: ", e);
		}

		FloatControl volume = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
		float volumeValue = config.volume() - 100;

		volume.setValue(volumeValue);
		clip.loop(0);
	}

	@Provides
	CustomDeathSoundConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(CustomDeathSoundConfig.class);
	}
}
