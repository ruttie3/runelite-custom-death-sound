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

	public boolean isPlaying = false;

	@Subscribe
	public void onAnimationChanged(AnimationChanged animationChanged)
	{
		// Check if the player died
		if (client.getGameState() == GameState.LOGGED_IN
			&& client.getLocalPlayer() != null
			&& client.getLocalPlayer().getHealthRatio() == 0
			&& client.getLocalPlayer().getAnimation() == AnimationID.DEATH
		) {
			playSound();
		}
	}

	private void playSound()
	{
		// Get the sound file
		String soundFile = config.soundFile();
		if (soundFile.length() == 0) {
			return;
		}

		// Don't play the clip if we're already playing a clip
		if (isPlaying) {
			log.info("Already playing");
			return;
		}

		log.info("Playing sound");

		// Close the clip if necessary
		if (clip != null) {
			clip.close();
		}

		// Try to create the input stream
		AudioInputStream inputStream = null;
		try {
			URL url = Paths.get(soundFile).toUri().toURL();
			inputStream = AudioSystem.getAudioInputStream(url);
		} catch (UnsupportedAudioFileException | IOException e) {
			log.warn("Unable to create audio input stream: ", e);
		}

		if (inputStream == null) {
			return;
		}

		// Try to open the clip
		try
		{
			clip = AudioSystem.getClip();
			clip.open(inputStream);
		} catch (LineUnavailableException | IOException e) {
			log.warn("Could not load sound file: ", e);
		}

		// Set the clip's volume
		FloatControl volume = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
		float volumeValue = config.volume() - 100;
		volume.setValue(volumeValue);

		// Play the clip
		clip.loop(0);
		isPlaying = true;

		// Change isPlaying when the clip has ended
		clip.addLineListener(e -> {
			if (e.getType() == LineEvent.Type.STOP) {
				isPlaying = false;
				log.info("Done playing sound");
			}
		});
	}

	@Provides
	CustomDeathSoundConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(CustomDeathSoundConfig.class);
	}
}
