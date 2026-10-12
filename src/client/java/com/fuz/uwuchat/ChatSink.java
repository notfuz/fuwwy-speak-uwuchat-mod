package com.fuz.uwuchat;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

final class ChatSink {
    private static boolean warned;

    private ChatSink() {}

    static boolean show(Minecraft mc, Component c) {
        try {
            Object player = mc.player;
            if (player != null) {
                if (call(player, "sendSystemMessage", c)) return true;
                if (call(player, "displayClientMessage", c, false)) return true;
            }
            Object chat = chat(mc);
            if (chat != null) {
                if (call(chat, "addClientSystemMessage", c)) return true;
                if (call(chat, "addMessage", c)) return true;
            }
        } catch (ReflectiveOperationException | RuntimeException e) {
            UwuChatMod.LOGGER.warn("Could not add chat line", e);
        }
        if (!warned) {
            warned = true;
            UwuChatMod.LOGGER.warn("UwUChat: no way to add a chat line found; incoming chat will stay untranslated");
        }
        return false;
    }

    private static Object chat(Minecraft mc) throws ReflectiveOperationException {
        Object gui = null;
        try {
            Field f = Minecraft.class.getField("gui");
            gui = f.get(mc);
        } catch (NoSuchFieldException ignored) {
        }
        if (gui == null) {
            Method m = findMethod(mc.getClass(), "getGui");
            if (m != null) gui = m.invoke(mc);
        }
        if (gui == null) return null;
        Method getChat = findMethod(gui.getClass(), "getChat");
        return getChat == null ? null : getChat.invoke(gui);
    }

    private static Method findMethod(Class<?> type, String name) {
        for (Method m : type.getMethods()) {
            if (m.getName().equals(name) && m.getParameterCount() == 0) return m;
        }
        return null;
    }

    private static boolean call(Object target, String name, Object... args) throws ReflectiveOperationException {
        for (Method m : target.getClass().getMethods()) {
            if (!m.getName().equals(name) || m.getParameterCount() != args.length) continue;
            Class<?>[] types = m.getParameterTypes();
            boolean ok = true;
            for (int i = 0; i < args.length; i++) {
                Class<?> t = types[i];
                if (t == boolean.class) t = Boolean.class;
                if (!t.isInstance(args[i])) { ok = false; break; }
            }
            if (ok) {
                m.invoke(target, args);
                return true;
            }
        }
        return false;
    }
}
