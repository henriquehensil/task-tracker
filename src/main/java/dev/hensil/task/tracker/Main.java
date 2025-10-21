package dev.hensil.task.tracker;

import dev.hensil.task.tracker.core.Task;
import dev.hensil.task.tracker.core.Tracker;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public final class Main {

    // Static initializers

    private static final @NotNull Tracker TRACKER = Tracker.getInstance();
    private static final @NotNull Scanner SCANNER = new Scanner(System.in);

    static {
        SCANNER.useLocale(Locale.ENGLISH);
    }

    public static void main(String[] args) {
        System.out.println("Welcome\n");

        @NotNull String input;
        while (!(input = SCANNER.nextLine().toLowerCase()).equalsIgnoreCase("exit")) {
            try {
                if (input.startsWith("add ")) {
                    @NotNull String description = input = input.replace("add ", "").replaceAll("\"", "");
                    @NotNull Task task = TRACKER.add(description);
                    System.out.println("Task added successfully (ID: " + task.getId() + ")\n");
                } else if (input.startsWith("update ")) {
                    @NotNull String @NotNull [] parts = input.split("\\s", 3);
                    @NotNull String description = parts[2].replaceAll("\"", "");
                    int id = Integer.parseInt(parts[1]);

                    @NotNull Optional<Task> task = TRACKER.get(id);
                    if (task.isEmpty()) {
                        System.out.println("Cannot find the task (ID: " + id + ")\n");
                        break;
                    }
                    task.get().update(description);
                    System.out.println("Update successfully!\n");
                } else if (input.startsWith("delete ")) {
                    int id = Integer.parseInt(input.replace("delete ", ""));
                    @NotNull Optional<Task> task = TRACKER.get(id);
                    if (task.isEmpty()) {
                        System.out.println("Cannot find the task (ID: " + id + ")\n");
                        break;
                    }

                    TRACKER.delete(task.get());
                    System.out.println("Delete successfully!\n");
                } else if (input.startsWith("mark-in-progress")) {
                    int id = Integer.parseInt(input.replace("mark-in-progress ", ""));
                    @NotNull Optional<Task> task = TRACKER.get(id);
                    if (task.isEmpty()) {
                        System.out.println("Cannot find the task (ID: " + id + ")\n");
                        break;
                    }

                    task.get().markInProgress();
                } else if (input.startsWith("mark-done")) {
                    int id = Integer.parseInt(input.replace("mark-done ", ""));
                    @NotNull Optional<Task> task = TRACKER.get(id);
                    if (task.isEmpty()) {
                        System.out.println("Cannot find the task (ID: " + id + ")\n");
                        break;
                    }

                    task.get().done();
                } else if (input.equalsIgnoreCase("list")) {
                    System.out.println(TRACKER.getAll() + "\n");
                } else if (input.equalsIgnoreCase("list done")) {
                    System.out.println(TRACKER.getDone() + "\n");
                } else if (input.equalsIgnoreCase("list todo")) {
                    System.out.println(TRACKER.getTodos() + "\n");
                } else if (input.equalsIgnoreCase("list in-progress")) {
                    System.out.println(TRACKER.getInProgress() + "\n");
                } else {
                    System.out.println("Invalid command, try again\n");
                }
            } catch (Throwable e) {
                System.err.println(e.getMessage());
            }
        }
    }

    // Objects

    private Main() {
        throw new UnsupportedOperationException();
    }
}