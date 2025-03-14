package com.customdeathsound;

import com.google.inject.Provides;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.events.ActorDeath;
import net.runelite.api.events.ClientTick;
import static net.runelite.client.RuneLite.RUNELITE_DIR;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

@Slf4j
@PluginDescriptor(
	name = "Custom Death Sound",
	description = "Plays a custom sound from your computer upon death.",
	tags = {"sound", "custom", "death"}
)
public class CustomDeathSoundPlugin extends Plugin
{
	@Inject
	private Client client;
	@Inject
	private ClientThread clientThread;
	@Inject
	private CustomDeathSoundConfig config;

	private static final String DEBUG_PREFIX = "<col=FF0000>[Custom Death Sound]</col> ";
	private static final File DIRECTORY = new File(RUNELITE_DIR, "custom-death-sound");

	// Inject if merged into RuneLite
	// see: https://github.com/runelite/runelite/pull/18745
	private final AudioPlayer audioPlayer = new AudioPlayer();
	private final ExecutorService audioDispatcher = Executors.newSingleThreadExecutor();

	private boolean playDeathSound;

	@Provides
	CustomDeathSoundConfig provideConfig(final ConfigManager configManager)
	{
		return configManager.getConfig(CustomDeathSoundConfig.class);
	}

	@Override
	protected void startUp()
	{
		DIRECTORY.mkdir();
	}

	@Override
	protected void shutDown()
	{
		playDeathSound = false;
	}

	@Subscribe
	public void onActorDeath(final ActorDeath event)
	{
		if (event.getActor() == client.getLocalPlayer() && config.soundVolume() > 0)
		{
			playDeathSound = true;
			sendDebugMessage("Player died.");
		}
	}

	@Subscribe
	public void onClientTick(final ClientTick event)
	{
		if (playDeathSound)
		{
			audioDispatcher.execute(() -> {
				try
				{
					var soundFile = new File(DIRECTORY, config.soundFile());
					if (soundFile.isDirectory()) {
						try (final Stream<Path> stream = Files.list(soundFile.toPath())) {
							final var paths = stream.filter(Files::isRegularFile).collect(Collectors.toList());
							if (!paths.isEmpty()) {
								soundFile = paths.get(ThreadLocalRandom.current().nextInt(paths.size())).toFile();
							}
						}
					}
					final var gain = 20f * (float) Math.log10(config.soundVolume() / 100f);
					sendDebugMessage("Playing audio file: " + soundFile.getAbsolutePath());
					audioPlayer.play(soundFile, gain);
				}
				catch (final Exception e)
				{
					log.warn("play audio {}", config.soundFile(), e);
					sendDebugMessage("There was an error playing the audio file: " + config.soundFile());
				}
			});

			playDeathSound = false;
		}
	}

	private void sendDebugMessage(final String msg) {
		if (config.debug())
		{
			clientThread.invoke(() -> {
				client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", DEBUG_PREFIX + msg, "custom-death-sound", false);
			});
		}
	}
}
