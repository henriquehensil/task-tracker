package dev.hensil.task.tracker.core;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.time.Instant;
import java.util.Objects;

public final class Task {

    // Static initializers

    // Objects

    private final @NotNull Tracker tracker;
    private final int id;

    private @NotNull String description;
    private @NotNull Status status;

    private final @NotNull Instant createAt;
    private @Nullable Instant updateAt;

    Task(@NotNull Tracker tracker, int id, @NotNull String description) {
        this.tracker = tracker;
        if (id < 0) {
            throw new IllegalArgumentException("Illegal id value");
        }

        this.id = id;
        this.status = Status.TODO;
        this.createAt = Instant.now();
        this.updateAt = null;
        this.description = description;
    }

    Task(
            @NotNull Tracker tracker,
            int id,
            @NotNull String description,
            @NotNull Status status,
            @NotNull Instant createAt,
            @Nullable Instant updateAt
    ) {
        this.tracker = tracker;
        this.id = id;
        this.description = description;
        this.status = status;
        this.createAt = createAt;
        this.updateAt = updateAt;
    }

    // Getters

    public int getId() {
        return id;
    }

    public @NotNull String getDescription() {
        return description;
    }

    public @NotNull Status getStatus() {
        return status;
    }

    public @NotNull Instant getCreateAt() {
        return createAt;
    }

    public @Nullable Instant getUpdateAt() {
        return updateAt;
    }

    public boolean isDone() {
        return this.status == Status.DONE;
    }

    public boolean isInProgress() {
        return this.status == Status.IN_PROGRESS;
    }

    // Modules

    public void update(@NotNull String description) {
        if (isDone()) {
            throw new IllegalStateException("The task is done");
        }

        @NotNull String actualDesc = this.description;
        @Nullable Instant actualUpdate = this.updateAt;

        this.updateAt = Instant.now();
        this.description = description;

        try {
            this.tracker.getStore().save(this);
        } catch (IOException e) {
            this.updateAt = actualUpdate;
            this.description = actualDesc;
            throw new RuntimeException("Cannot update this task because an error occurs while trying to save", e);
        }
    }

    public void done() {
        if (isDone()) {
            throw new IllegalStateException("The task already is done");
        }

        @NotNull Status actualStatus = this.status;
        this.status = Status.DONE;

        try {
            this.tracker.getStore().save(this);
        } catch (IOException e) {
            this.status = actualStatus;
            throw new RuntimeException("Cannot update this task because an error occurs while trying to save", e);
        }
    }

    public void markInProgress() {
        if (isDone()) {
            throw new IllegalStateException("The task is done");
        }

        @NotNull Status actualStatus = this.status;
        this.status = Status.IN_PROGRESS;

        try {
            this.tracker.getStore().save(this);
        } catch (IOException e) {
            this.status = actualStatus;
            throw new RuntimeException("Cannot update this task because an error occurs while trying to save", e);
        }
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        @NotNull Task task = (Task) o;
        return id == task.id;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public @NotNull String toString() {
        return "\n{" +
                "id=" + id +
                ",\n description='" + description + '\'' +
                ",\n status=" + status +
                ",\n createAt=" + createAt +
                ",\n updateAt=" + updateAt +
                "\n}";
    }

    // Classes

    public enum Status {
        TODO,
        IN_PROGRESS,
        DONE
    }
}