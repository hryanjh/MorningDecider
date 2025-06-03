import java.time.*;

public class Course {
    public String name;
    public DayOfWeek day;
    public boolean core;
    public LocalTime start;

    public Course(String n, DayOfWeek d, boolean isCore, LocalTime t) {
        name = n;
        day = d;
        core = isCore;
        start = t;
    }
}
