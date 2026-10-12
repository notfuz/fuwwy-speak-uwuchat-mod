package com.fuz.uwuchat;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.PlainTextContents;
import net.minecraft.network.chat.contents.TranslatableContents;
import java.util.Set;

public final class ChatTranslator {
    private static final Set<String> SPEECH_KEYS = Set.of(
            "chat.type.text",
            "chat.type.announcement",
            "chat.type.emote",
            "chat.type.admin",
            "chat.type.team.text",
            "chat.type.team.sent",
            "commands.message.display.incoming",
            "commands.message.display.outgoing");

    private ChatTranslator() {}

    public static Component incoming(Component message) {
        UwuChatMod mod = UwuChatMod.get();
        if (mod == null || !mod.config().enabled || !mod.config().translateIncoming || message == null) return message;
        Uwuifier uwu = mod.uwuifier();

        if (message.getContents() instanceof TranslatableContents tc && SPEECH_KEYS.contains(tc.getKey())) {
            Object[] args = tc.getArgs().clone();
            if (args.length == 0) return message;
            int last = args.length - 1;
            if (args[last] instanceof Component said) {
                args[last] = translateLiterals(said, uwu);
            } else if (args[last] instanceof String said) {
                args[last] = uwu.uwuify(said);
            } else {
                return message;
            }
            MutableComponent rebuilt = MutableComponent.create(new TranslatableContents(tc.getKey(), tc.getFallback(), args));
            rebuilt.setStyle(message.getStyle());
            for (Component sibling : message.getSiblings()) rebuilt.append(sibling);
            return rebuilt;
        }

        if (mod.config().translateServerMessages && message.getContents() instanceof PlainTextContents) {
            return translateLiterals(message, uwu);
        }
        return message;
    }

    private static Component translateLiterals(Component c, Uwuifier uwu) {
        ComponentContents contents = c.getContents();
        MutableComponent out;
        if (contents instanceof PlainTextContents plain) {
            out = Component.literal(uwu.uwuify(plain.text()));
        } else {
            out = MutableComponent.create(contents);
        }
        out.setStyle(c.getStyle());
        for (Component sibling : c.getSiblings()) out.append(translateLiterals(sibling, uwu));
        return out;
    }
}
