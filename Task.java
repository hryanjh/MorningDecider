import java.time.*;
import java.time.temporal.ChronoUnit;

public class Task {
    public String name;
    public LocalDate due;
    public double totalPrep;
    public double prepDone;
    public String course;

    public Task(String n, LocalDate d, double tot, double done, String c) {
        name = n;
        due = d;
        totalPrep = tot;
        prepDone = done;
        course = c;
    }

    public long daysLeft() {
        return Math.max(ChronoUnit.DAYS.between(LocalDate.now(), due), 0);
    }

    public double hoursRemaining() {
        return Math.max(totalPrep - prepDone, 0);
    }

    public double dailyTarget() {
        long d = daysLeft();
        return d == 0 ? hoursRemaining() : hoursRemaining() / d;
    }

    public boolean urgent() {
        return daysLeft() <= 1;
    }

    public String toCSV() {
        return name.replace(',', ' ') + "," + due + "," + totalPrep + "," + prepDone + "," + course.replace(',', ' ');
    }

    public static Task fromCSV(String line) {
        String[] p = line.split(",", -1);
        String course = (p.length >= 5) ? p[4] : "(General)";
        return new Task(p[0], LocalDate.parse(p[1]), Double.parseDouble(p[2]), Double.parseDouble(p[3]), course);
    }
}
