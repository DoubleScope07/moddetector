package com.matteo.moddetector;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.network.ServerPlayerEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.fabricmc.loader.api.ModContainer;

public class ModDetector implements ModInitializer {
    private static final Logger LOGGER = LoggerFactory.getLogger("moddetector");
    private static final Path CONFIG_PATH = FabricLoader.getInstance()
        .getConfigDir().resolve("moddetector_blacklist.json");

    @Override
    public void onInitialize() {
        LOGGER.info("ModDetector initialized.");
        ensureConfigExists();

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayerEntity player = handler.player;
            Set<String> mods = detectClientMods();
            LOGGER.info("[ModDetector] {} joined with mods: {}", 
                        player.getEntityName(), mods);
        });
    }

    private void ensureConfigExists() {
        try {
            if (!Files.exists(CONFIG_PATH)) {
                Files.writeString(CONFIG_PATH, "[]", StandardCharsets.UTF_8);
            }
        } catch (Exception e) {
            LOGGER.error("Could not create config file:", e);
        }
    }

    private Set<String> detectClientMods() {
        Set<String> names = new HashSet<>();
        List<ModContainer> all = FabricLoader.getInstance().getAllMods();
        for (ModContainer mc : all) {
            ModMetadata m = mc.getMetadata();
            names.add(m.getId());
        }
        return names;
    }
}
