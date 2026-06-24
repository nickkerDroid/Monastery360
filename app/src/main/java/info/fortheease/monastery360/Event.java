package info.fortheease.monastery360;

public class Event {
    private String title;
    private int year, month, day;
    private String description;

    public Event(String title, int year, int month, int day, String description) {
        this.title = title;
        this.year = year;
        this.month = month;
        this.day = day;
        this.description = description;
    }

    public String getTitle() { return title; }
    public int getYear() { return year; }
    public int getMonth() { return month; }
    public int getDay() { return day; }
    public String getDescription() { return description; }
}
