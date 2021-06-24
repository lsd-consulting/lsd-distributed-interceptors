package io.lsdconsulting.lsd.distributed.teststate.indent;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.util.Optional;

import static com.google.gson.JsonParser.parseString;
import static java.util.Optional.empty;
import static org.apache.commons.lang3.StringUtils.isBlank;

public class JsonPrettyPrinter {

    private final static Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static Optional<String> indentJson(final String document) {
        if (isBlank(document)) {
            return empty();
        }

        final JsonElement jsonElement;
        try {
            jsonElement = parseString(document);
            return Optional.of(GSON.toJson(jsonElement));
        } catch (final JsonParseException e) {
            return empty();
        }
    }
}