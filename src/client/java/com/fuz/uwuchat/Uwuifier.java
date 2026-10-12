package com.fuz.uwuchat;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Uwuifier {
    private static final Pattern PROTECTED = Pattern.compile(
            "(https?://\\S+|www\\.\\S+|[@#][\\w.]+|:[a-zA-Z0-9_]+:|%(?:\\d+\\$)?[-#+ 0,(]*\\d*(?:\\.\\d+)?[a-zA-Z%]|\\u00a7.)");
    private static final Pattern WORD = Pattern.compile("[A-Za-z][A-Za-z']*");
    private static final Pattern NY = Pattern.compile("n(?=[aeiou])");

    private final Map<String, String> dictionary;
    private final boolean endings;

    public Uwuifier(Map<String, String> dictionary, boolean endings) {
        Map<String, String> copy = new HashMap<>(dictionary);
        for (String value : dictionary.values()) copy.putIfAbsent(value.toLowerCase(Locale.ROOT), value.toLowerCase(Locale.ROOT));
        this.dictionary = copy;
        this.endings = endings;
    }

    public String uwuify(String text) {
        if (text == null || text.isEmpty()) return text;
        StringBuilder out = new StringBuilder(text.length() + 16);
        Matcher m = PROTECTED.matcher(text);
        int last = 0;
        while (m.find()) {
            out.append(translatePlain(text.substring(last, m.start())));
            out.append(m.group());
            last = m.end();
        }
        out.append(translatePlain(text.substring(last)));
        String result = out.toString();
        if (endings) result = addEnding(result);
        return result;
    }

    private String translatePlain(String s) {
        Matcher m = WORD.matcher(s);
        StringBuilder sb = new StringBuilder();
        int last = 0;
        while (m.find()) {
            sb.append(s, last, m.start()).append(word(m.group()));
            last = m.end();
        }
        return sb.append(s.substring(last)).toString();
    }

    private String word(String original) {
        if (original.length() == 1) {
            return original.equalsIgnoreCase("l") || original.equalsIgnoreCase("r") ? recase(original, "w") : original;
        }
        String lower = original.toLowerCase(Locale.ROOT);
        String mapped = dictionary.get(lower);
        if (mapped == null) {
            mapped = NY.matcher(lower).replaceAll("ny").replace('l', 'w').replace('r', 'w');
        }
        return recase(original, mapped);
    }

    private static String recase(String original, String mapped) {
        if (original.length() > 1 && original.equals(original.toUpperCase(Locale.ROOT))) {
            return mapped.toUpperCase(Locale.ROOT);
        }
        if (Character.isUpperCase(original.charAt(0)) && !mapped.isEmpty()) {
            return Character.toUpperCase(mapped.charAt(0)) + mapped.substring(1);
        }
        return mapped;
    }

    private static String addEnding(String s) {
        String t = s.stripTrailing();
        if (t.length() < 25) return s;
        String tail = t.substring(Math.max(0, t.length() - 5)).toLowerCase(Locale.ROOT);
        if (tail.endsWith("owo!") || tail.endsWith("uwu?") || tail.endsWith("uwu!") || tail.endsWith("owo?")) return s;
        char c = t.charAt(t.length() - 1);
        if (c == '!') return t.substring(0, t.length() - 1) + " owo!";
        if (c == '?') return t.substring(0, t.length() - 1) + " uwu?";
        return s;
    }
}
