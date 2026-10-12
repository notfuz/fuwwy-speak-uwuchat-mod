package com.fuz.uwuchat;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public final class UwuChatMod implements ClientModInitializer {
    public static final String MOD_ID = "uwuchat";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    private static final int MAX_CHAT_LENGTH = 256;

    private static UwuChatMod instance;
    private UwuConfig config;
    private Uwuifier uwuifier;

    public static UwuChatMod get() { return instance; }
    public UwuConfig config() { return config; }
    public Uwuifier uwuifier() { return uwuifier; }

    @Override
    public void onInitializeClient() {
        instance = this;
        config = UwuConfig.load();
        uwuifier = new Uwuifier(loadDictionary(), config.owoEndings);

        ClientSendMessageEvents.MODIFY_CHAT.register(message -> {
            if (!config.enabled || !config.translateOutgoing) return message;
            String out = uwuifier.uwuify(message);
            return out.length() > MAX_CHAT_LENGTH ? out.substring(0, MAX_CHAT_LENGTH) : out;
        });

        ClientReceiveMessageEvents.ALLOW_CHAT.register((message, signedMessage, sender, params, receptionTimestamp) -> {
            if (!config.enabled || !config.translateIncoming) return true;
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null) return true;
            Component full = message.getContents() instanceof TranslatableContents ? message : params.decorate(message);
            Component translated = ChatTranslator.incoming(full);
            if (translated == full) return true;
            return !ChatSink.show(mc, translated);
        });

        ClientReceiveMessageEvents.MODIFY_GAME.register((message, overlay) ->
                overlay ? message : ChatTranslator.incoming(message));

        registerCommand();
        LOGGER.info("UwUChat loaded, wewcome two teh chat owo");
    }

    private void registerCommand() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, buildContext) ->
                dispatcher.register(ClientCommands.literal("uwu")
                        .executes(ctx -> {
                            config.enabled = !config.enabled;
                            config.save();
                            ctx.getSource().sendFeedback(Component.literal(
                                    config.enabled ? "UwUChat is now ON owo" : "UwUChat is now OFF qwq"));
                            return 1;
                        })
                        .then(ClientCommands.literal("send").executes(ctx -> {
                            config.translateOutgoing = !config.translateOutgoing;
                            config.save();
                            ctx.getSource().sendFeedback(Component.literal(
                                    "Twanswating ur messages: " + (config.translateOutgoing ? "ON" : "OFF")));
                            return 1;
                        }))
                        .then(ClientCommands.literal("read").executes(ctx -> {
                            config.translateIncoming = !config.translateIncoming;
                            config.save();
                            ctx.getSource().sendFeedback(Component.literal(
                                    "Twanswating chat u wead: " + (config.translateIncoming ? "ON" : "OFF")));
                            return 1;
                        }))
                        .then(ClientCommands.literal("status").executes(ctx -> {
                            ctx.getSource().sendFeedback(Component.literal(
                                    "UwUChat: " + (config.enabled ? "ON" : "OFF")
                                            + " | send: " + (config.translateOutgoing ? "ON" : "OFF")
                                            + " | wead: " + (config.translateIncoming ? "ON" : "OFF")));
                            return 1;
                        }))));
    }

    private static Map<String, String> loadDictionary() {
        Type type = new TypeToken<Map<String, String>>() {}.getType();
        try (InputStream in = UwuChatMod.class.getResourceAsStream("/assets/uwuchat/dictionary.json")) {
            if (in != null) {
                Map<String, String> map = new Gson().fromJson(new InputStreamReader(in, StandardCharsets.UTF_8), type);
                if (map != null) return map;
            }
        } catch (IOException e) {
            LOGGER.warn("Could not read dictionary.json", e);
        }
        return Map.of();
    }
}
