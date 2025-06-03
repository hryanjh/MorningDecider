import java.util.*;
import java.time.*;

public class ClassDecisionAssistant {
    public static void main(String[] args) {
        TaskManager.loadTasks();
        ScheduleManager.loadSchedule();
        Scanner in = new Scanner(System.in);

        System.out.println("\n=== Morning Check-In Assistant ===");
        System.out.println("1) Morning check-in  2) Agenda  3) Upload schedule  4) Quit");
        System.out.print("Choice: ");
        String c = in.nextLine().trim();

        if (c.equals("1"))
            morning(in);
        else if (c.equals("2"))
            agenda();
        else if (c.equals("3"))
            uploadSchedule(in);
        else
            System.out.println("Bye!");
        in.close();
    }

    /* ---------- morning flow ---------- */
    private static void morning(Scanner in) {
        System.out.print("Sleep hours last night: ");
        double sleep = Double.parseDouble(in.nextLine());
        System.out.print("Stress (1-10): ");
        int stress = Integer.parseInt(in.nextLine());

        System.out.print("\nAdd a task now? (y/N): ");
        if (in.nextLine().equalsIgnoreCase("y")) {
            addTask(in);
            TaskManager.saveTasks();
        }

        System.out.println("\n↪ " + headline(sleep, stress));
        Task nxt = TaskManager.getNextTask();
        if (nxt != null)
            System.out.println("📌 Upcoming: " + nxt.name + " (" + nxt.course + ") – due " + nxt.due + " in "
                    + nxt.daysLeft() + " days");

        classAdvice(sleep, stress);
        System.out.printf("\n👉 Study about %.1f h today.\n", TaskManager.todayStudyHours());
    }

    private static String headline(double sleep, int stress) {
        if (!TaskManager.getUrgentCourses().isEmpty())
            return "Focus on imminent assessments.";
        if (sleep < 6)
            return "Consider skipping an elective to rest.";
        if (stress >= 7)
            return "Maybe skip an elective for mental space.";
        return "Go to class – stay consistent.";
    }

    private static void classAdvice(double sleep, int stress) {
        List<Course> today = ScheduleManager.todayCourses();
        if (today.isEmpty()) {
            System.out.println("No classes today!");
            return;
        }
        Set<String> urgent = TaskManager.getUrgentCourses();
        boolean skipped = false;
        for (Course c : today) {
            String act = "Attend";
            if (!c.core && !skipped && !urgent.contains(c.name) && (sleep < 6 || stress >= 7)) {
                act = "SKIP";
                skipped = true;
            }
            System.out.println("  " + c.start + " – " + c.name + " → " + act);
        }
    }

    /* ---------- add task ---------- */
    private static void addTask(Scanner in) {
        System.out.print("Task name: ");
        String name = in.nextLine();
        System.out.print("Due (yyyy-MM-dd): ");
        LocalDate due = LocalDate.parse(in.nextLine());
        System.out.print("Total prep hrs: ");
        double hrs = Double.parseDouble(in.nextLine());
        String course = "(General)";
        if (!ScheduleManager.schedule.isEmpty()) {
            List<String> list = new ArrayList<>();
            for (Course c : ScheduleManager.schedule)
                if (!list.contains(c.name))
                    list.add(c.name);
            for (int i = 0; i < list.size(); i++)
                System.out.println("  " + (i + 1) + ") " + list.get(i));
            System.out.print("Course # (0 none): ");
            int n = Integer.parseInt(in.nextLine()) - 1;
            if (n >= 0 && n < list.size())
                course = list.get(n);
        }
        TaskManager.tasks.add(new Task(name, due, hrs, 0, course));
    }

    /* ---------- agenda ---------- */
    private static void agenda() {
        if (TaskManager.tasks.isEmpty()) {
            System.out.println("\n(No tasks)\n");
            return;
        }
        Collections.sort(TaskManager.tasks, new Comparator<Task>() {
            public int compare(Task a, Task b) {
                return a.due.compareTo(b.due);
            }
        });
        System.out.println("\nAgenda:");
        for (Task t : TaskManager.tasks)
            System.out.println(" " + t.due + " | " + t.name + " (" + t.course + ") – "
                    + String.format("%.1f", t.hoursRemaining()) + " h left");
    }

    /* ---------- upload schedule ---------- */
    private static void uploadSchedule(Scanner in) {
        ScheduleManager.schedule.clear();
        System.out.println("\nEnter schedule rows (blank course to finish)");
        while (true) {
            System.out.print("Course: ");
            String name = in.nextLine();
            if (name.isBlank())
                break;
            System.out.print("Days (e.g. MONDAY,WEDNESDAY): ");
            String days = in.nextLine();
            System.out.print("Core class? (y/N): ");
            boolean core = in.nextLine().trim().equalsIgnoreCase("y");
            System.out.print("Start (HH:mm): ");
            LocalTime start = LocalTime.parse(in.nextLine());
            for (String tok : days.split("[ ,]+")) {
                DayOfWeek d = DayOfWeek.valueOf(tok.toUpperCase());
                ScheduleManager.schedule.add(new Course(name, d, core, start));
            }
        }
        ScheduleManager.saveSchedule();
        System.out.println("Schedule saved.\n");
    }
}
