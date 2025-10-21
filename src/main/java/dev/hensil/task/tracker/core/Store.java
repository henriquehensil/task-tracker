package dev.hensil.task.tracker.core;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.time.Instant;
import java.util.*;

final class Store {

    // Static initializers

    private static @NotNull JsonObject serialize(@NotNull Task task) {
        // Variables
        int id = task.getId();
        @NotNull String description = task.getDescription();
        @NotNull Task.Status status = task.getStatus();
        @NotNull Instant createAt = task.getCreateAt();
        @Nullable Instant updateAt = task.getUpdateAt();

        @NotNull JsonObject object = new JsonObject();
        object.addProperty("id", id);
        object.addProperty("description", description);
        object.addProperty("status", status.name().toLowerCase());
        object.addProperty("createAt", createAt.toEpochMilli());

        object.addProperty("updateAt", updateAt == null ? null : updateAt.toEpochMilli());

        return object;
    }

    private static @NotNull Task deserialize(@NotNull Tracker tracker, @NotNull JsonObject object) {
        try {
            // Variables
            int id = object.getAsJsonPrimitive("id").getAsInt();
            @NotNull String description = object.getAsJsonPrimitive("description").getAsString();
            @NotNull Task.Status status = Task.Status.valueOf(
                    object.getAsJsonPrimitive("status").getAsString().toUpperCase()
            );
            @NotNull Instant createAt = Instant.ofEpochMilli(
                    object.getAsJsonPrimitive("createAt").getAsLong()
            );

            @Nullable Instant updateAt;
            @NotNull JsonElement element = object.get("updateAt");
            updateAt = element instanceof JsonNull ? null : Instant.ofEpochMilli(element.getAsLong());

            return new Task(tracker, id, description, status, createAt, updateAt);
        } catch (Throwable e) {
            throw new RuntimeException("Cannot deserialize json: " + object, e);
        }
    }

    // Objects

    private final @NotNull File root;
    private final @NotNull Set<@NotNull File> files;

    Store(@NotNull File root) {
        if (!root.exists()) {
            throw new IllegalArgumentException("The root file doest not exits: " + root.getAbsolutePath());
        } else if (!root.isDirectory()) {
            throw new IllegalArgumentException("The root file is not a directory: " + root.getAbsolutePath());
        }

        // Load files

        @NotNull File @Nullable [] files = root.listFiles(file -> file.getName().endsWith(".json"));
        @NotNull Set<@NotNull File> fileSet;

        if (files == null) {
            throw new RuntimeException("Cannot load all files in root directory: " + root.getAbsolutePath());
        }  else fileSet = new HashSet<>(Arrays.asList(files));

        this.files = fileSet;
        this.root = root;
    }

    // Modules

    public @NotNull File save(@NotNull Task task) throws IOException {
        @NotNull File file = getAbstractFile(task);
        if (!file.exists() && !file.createNewFile()) {
            throw new RuntimeException("Cannot create the file: " + file.getAbsolutePath());
        } else try (@NotNull FileWriter writer = new FileWriter(file, false)) {
            @NotNull String json = serialize(task).toString();
            writer.write(json);
            this.files.add(file);
            return file;
        }
    }

    public void delete(@NotNull Task task) {
        @NotNull File file = getAbstractFile(task);
        file.deleteOnExit();
        this.files.remove(file);
    }

    @NotNull Collection<Task> deserializeAll(@NotNull Tracker tracker) throws IOException {
        @NotNull List<@NotNull Task> tasks = new ArrayList<>();
        for (@NotNull File file : this.files) {
            try (@NotNull FileReader reader = new FileReader(file)) {
                @NotNull JsonObject object = JsonParser.parseReader(reader).getAsJsonObject();
                @NotNull Task task = deserialize(tracker, object);
                tasks.add(task);
            } catch (Throwable e) {
                throw new IOException("Cannot deserialize the file: " + file.getAbsolutePath(), e);
            }
        }

        return tasks;
    }

    private @NotNull File getAbstractFile(@NotNull Task task) {
        @NotNull String str = "task-" + task.getId() + ".json";
        return new File(root, str);
    }
}