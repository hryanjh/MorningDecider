import java.io.*;
import java.nio.file.*;
import java.util.*;

public class TaskManager {
    private static final String TASK_FILE = "tasks.csv";
    public static final List<Task> tasks = new ArrayList<>();

    public static void loadTasks() {
        Path p = Paths.get(TASK_FILE);
        if (!Files.exists(p)) { // make blank template first run
            try (BufferedWriter bw = Files.newBufferedWriter(p)) {
                bw.write("Task,Due,TotalPrep,PrepDone,Course\n");
            } catch (IOException e) {
            }
            return;
        }
        tasks.clear();
        try (BufferedReader br = Files.newBufferedReader(p)) {
            br.readLine(); // skip header
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty())
                    continue;
                tasks.add(Task.fromCSV(line));
            }
        } catch (IOException e) {
            System.err.println("Couldn't read tasks.csv: " + e);
        }
    }

    public static void saveTasks() {
        try (BufferedWriter bw = Files.newBufferedWriter(Paths.get(TASK_FILE))) {
            bw.write("Task,Due,TotalPrep,PrepDone,Course\n");
            for (Task t : tasks)
                bw.write(t.toCSV() + "\n");
        } catch (IOException e) {
            System.err.println("Couldn't save tasks.csv: " + e);
        }
    }

    public static Task getNextTask() {
        if (tasks.isEmpty())
            return null;
        Task next = tasks.get(0);
        for (Task t : tasks)
            if (t.due.isBefore(next.due))
                next = t;
        return next;
    }

    public static Set<String> getUrgentCourses() {
        Set<String> set = new HashSet<>();
        for (Task t : tasks)
            if (t.urgent())
                set.add(t.course);
        return set;
    }

    public static double todayStudyHours() {
        double sum = 0;
        for (Task t : tasks)
            sum += t.dailyTarget();
        return sum;
    }
}
