import java.util.List;
import java.util.Scanner;
import java.time.LocalDate;
import java.time.DayOfWeek;
import java.time.LocalTime;

public class ClassDecisionAssistant {
    public static void main(String[] args) {
        TaskManager.loadTasks();
        ScheduleManager.loadSchedule();
        Scanner in = new Scanner(System.in);

        System.out.println("\n=== Morning Check-In Assistant ===");
        System.out.println("1) Morning check-in   2) Agenda   3) Upload schedule   4) Quit");
        System.out.print("Choice: ");
        String choice = in.nextLine().trim();

        if (choice.equals("1")) {
            morning(in);
        } else if (choice.equals("2")) {
            agenda();
        } else if (choice.equals("3")) {
            uploadSchedule(in);
        } else {
            System.out.println("Bye!");
        }

        in.close();
    }

    // Morning check in
    private static void morning(Scanner in) {
        // Sleep and stress input
        System.out.print("Sleep hours last night: ");
        double sleep = Double.parseDouble(in.nextLine());
        System.out.print("Stress (1-10): ");
        int stress = Integer.parseInt(in.nextLine());

        // Optionally add new task
        System.out.print("\nAdd a task now? (y/N): ");
        String add = in.nextLine().trim();
        if (add.equalsIgnoreCase("y")) {
            addTask(in);
            TaskManager.saveTasks();
        }

        // recommendation
        System.out.println("\n↪ " + headline(sleep, stress));

        // Show most urgent task
        Task next = TaskManager.getNextTask();
        if (next != null) {
            System.out.println("📌 Upcoming: " + next.name +
                    " (" + next.course + ") – due " + next.due +
                    " in " + next.daysLeft() + " days");
        }

        // Advice
        classAdvice(sleep, stress);

        System.out.printf("\n👉 Study about %.1f h today.%n",
                TaskManager.todayStudyHours());
    }

    // decision logic
    private static String headline(double sleep, int stress) {

        if (!TaskManager.getUrgentCourses().isEmpty()) {
            return "Focus on imminent assessments.";
        }

        if (sleep < 6) {
            return "Consider skipping an elective to rest.";
        }

        if (stress >= 7) {
            return "Maybe skip an elective for mental space.";
        }

        return "Go to class – stay consistent.";
    }

    // Print Attend or SKIP for each class in the scedule.csv today
    private static void classAdvice(double sleep, int stress) {
        List<Course> today = ScheduleManager.todayCourses();
        if (today.size() == 0) {
            System.out.println("No classes today!");
            return;
        }

        // Build list of course-names that have urgent tasks
        List<Task> allTasks = TaskManager.tasks;
        List<String> urgentCourses = new java.util.ArrayList<String>();
        for (int i = 0; i < allTasks.size(); i++) {
            Task t = allTasks.get(i);
            if (t.urgent() && !urgentCourses.contains(t.course)) {
                urgentCourses.add(t.course);
            }
        }

        boolean skippedOne = false;
        System.out.println("Today’s classes:");
        for (int i = 0; i < today.size(); i++) {
            Course c = today.get(i);
            String action = "Attend";
            // If elective and not skipped yet, no urgent task for this course, and
            // tired/stressed
            if (!c.core && !skippedOne &&
                    !urgentCourses.contains(c.name) &&
                    (sleep < 6 || stress >= 7)) {
                action = "SKIP";
                skippedOne = true;
            }
            System.out.println("  " + c.start + " – " + c.name + " → " + action);
        }
    }

    private static void agenda() {
        List<Task> all = TaskManager.tasks;
        if (all.size() == 0) {
            System.out.println("\n(No tasks)\n");
            return;
        }
        for (int i = 0; i < all.size() - 1; i++) {
            int earliest = i;
            for (int j = i + 1; j < all.size(); j++) {
                if (all.get(j).due.isBefore(all.get(earliest).due)) {
                    earliest = j;
                }
            }

            Task temp = all.get(i);
            all.set(i, all.get(earliest));
            all.set(earliest, temp);
        }

        System.out.println("\nAgenda:");
        for (int i = 0; i < all.size(); i++) {
            Task t = all.get(i);
            System.out.println(" "
                    + t.due + " | "
                    + t.name + " (" + t.course + ") – "
                    + String.format("%.1f", t.hoursRemaining())
                    + " h left");
        }
    }

    // optional schedule upload
    private static void uploadSchedule(Scanner in) {
        ScheduleManager.schedule.clear();
        System.out.println("\nEnter schedule rows (blank course to finish)");

        while (true) {
            System.out.print("Course: ");
            String name = in.nextLine().trim();
            if (name.equals("")) {
                break;
            }

            System.out.print("Days (e.g. MONDAY,WEDNESDAY): ");
            String days = in.nextLine().trim();

            System.out.print("Core class? (y/N): ");
            boolean core = in.nextLine().trim().equalsIgnoreCase("y");

            System.out.print("Start (HH:mm): ");
            LocalTime start = LocalTime.parse(in.nextLine().trim());

            String[] tokens = days.split("[, ]+");
            for (int i = 0; i < tokens.length; i++) {
                DayOfWeek d = DayOfWeek.valueOf(tokens[i].toUpperCase());
                ScheduleManager.schedule.add(new Course(name, d, core, start));
            }
        }

        ScheduleManager.saveSchedule();
        System.out.println("Schedule saved.\n");
    }

    private static void addTask(Scanner in) {
        System.out.print("Task name: ");
        String name = in.nextLine().trim();

        System.out.print("Due (yyyy-MM-dd): ");
        LocalDate due = LocalDate.parse(in.nextLine().trim());

        System.out.print("Total prep hrs: ");
        double hrs = Double.parseDouble(in.nextLine().trim());

        String course = "(General)";

        List<Course> sched = ScheduleManager.schedule;
        if (sched.size() > 0) {
            List<String> distinct = new java.util.ArrayList<String>();
            for (int i = 0; i < sched.size(); i++) {
                String nm = sched.get(i).name;
                if (!distinct.contains(nm)) {
                    distinct.add(nm);
                }
            }
            System.out.println("Select course:");
            for (int i = 0; i < distinct.size(); i++) {
                System.out.println("  " + (i + 1) + ") " + distinct.get(i));
            }

            System.out.print("Course # (0 none): ");
            int idx = Integer.parseInt(in.nextLine().trim()) - 1;
            if (idx >= 0 && idx < distinct.size()) {
                course = distinct.get(idx);
            }
        }

        TaskManager.tasks.add(new Task(name, due, hrs, 0, course));
    }
}
