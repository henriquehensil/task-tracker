package dev.hensil.task.tracker.core;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public final class Tracker {

    // Static initializers

    private static final @NotNull File ROOT = new File(System.getProperty("user.dir"), "tasks");
    private static final @NotNull Tracker INSTANCE;

    static {
        if (!ROOT.exists() && !ROOT.mkdirs()) {
            throw new RuntimeException("Cannot create root: " + ROOT.getAbsolutePath());
        }

        INSTANCE = new Tracker();
    }

    public static @NotNull Tracker getInstance() {
        return INSTANCE;
    }

    // Objects

    private final @NotNull Map<@NotNull Integer, @NotNull Task> tasks = new HashMap<>();
    private final @NotNull Store store = new Store(ROOT);
    private final @NotNull AtomicInteger autoIncrement;

    private Tracker() {
        try {
            @NotNull Collection<Task> tasks = store.deserializeAll(this);
            for (@NotNull Task task: tasks) {
                this.tasks.put(task.getId(), task);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        this.autoIncrement = new AtomicInteger(tasks.size());
    }

    // Getters

    public @Unmodifiable @NotNull Collection<@NotNull Task> getAll() {
        return Collections.unmodifiableCollection(tasks.values());
    }

    public @Unmodifiable @NotNull Collection<@NotNull Task> getDone() {
        return getAll()
                .stream()
                .filter(Task::isDone)
                .toList();
    }

    public @Unmodifiable @NotNull Collection<@NotNull Task> getTodos() {
        return getAll()
                .stream()
                .filter(task -> task.getStatus() == Task.Status.TODO)
                .toList();
    }

    public @Unmodifiable @NotNull Collection<@NotNull Task> getInProgress() {
        return getAll()
                .stream()
                .filter(Task::isInProgress)
                .toList();
    }

    @NotNull Store getStore() {
        return store;
    }

    // Modules

    public @NotNull Task add(@NotNull String description) {
        @NotNull Task task = new Task(this, this.autoIncrement.getAndIncrement(), description);

        try {
            store.save(task);
        } catch (IOException e) {
            throw new RuntimeException("Cannot save new task", e);
        }

        this.tasks.put(task.getId(), task);
        return task;
    }

    public void delete(@NotNull Task task) {
        if (!this.tasks.containsValue(task)) {
            throw new IllegalArgumentException("The task with id '" + task.getId() + "' doest not exists");
        }

        this.store.delete(task);
        this.tasks.remove(task.getId());
    }

    public @NotNull Optional<@NotNull Task> get(int id) {
        return Optional.ofNullable(this.tasks.get(id));
    }
}