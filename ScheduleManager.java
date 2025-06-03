import java.io.*;
import java.nio.file.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class ScheduleManager {
    private static final String SCHEDULE_FILE = "schedule.csv";
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("HH:mm");
    public static final List<Course> schedule = new ArrayList<>();

    public static void loadSchedule() {
        Path p = Paths.get(SCHEDULE_FILE);
        if (!Files.exists(p)) { // blank template
            try (BufferedWriter bw = Files.newBufferedWriter(p)) {
                bw.write("Course,DayOfWeek,Core?,StartTime\n");
            } catch (IOException e) {
            }
            return;
        }
        schedule.clear();
        try (BufferedReader br = Files.newBufferedReader(p)) {
            br.readLine(); // skip header
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty())
                    continue;
                String[] c = line.split(",", -1);
                schedule.add(new Course(c[0], DayOfWeek.valueOf(c[1].toUpperCase()),
                        Boolean.parseBoolean(c[2]), LocalTime.parse(c[3], FMT)));
            }
        } catch (Exception e) {
            System.err.println("Couldn't read schedule.csv: " + e);
        }
    }

    public static void saveSchedule() {
        try (BufferedWriter bw = Files.newBufferedWriter(Paths.get(SCHEDULE_FILE))) {
            bw.write("Course,DayOfWeek,Core?,StartTime\n");
            for (Course c : schedule) {
                bw.write(c.name.replace(',', ' ') + "," + c.day + "," + c.core + "," + c.start.format(FMT) + "\n");
            }
        } catch (IOException e) {
            System.err.println("Couldn't save schedule.csv: " + e);
        }
    }

    public static List<Course> todayCourses() {
        List<Course> list = new ArrayList<>();
        DayOfWeek d = LocalDate.now().getDayOfWeek();
        for (Course c : schedule)
            if (c.day == d)
                list.add(c);
        Collections.sort(list, new Comparator<Course>() {
            public int compare(Course a, Course b) {
                return a.start.compareTo(b.start);
            }
        });
        return list;
    }
}
