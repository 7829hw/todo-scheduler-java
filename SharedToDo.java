
public class SharedToDo extends ToDo {
    private String creator;
    private String id;

    public SharedToDo(String taskName, String location, boolean allDay,
            int startYear, int startMonth, int startDay, int startHour, int startMinute,
            int endYear, int endMonth, int endDay, int endHour, int endMinute,
            String alarm, String repeat, String memo, String creator) {
        super(taskName, location, allDay, startYear, startMonth, startDay, startHour, startMinute,
                endYear, endMonth, endDay, endHour, endMinute, alarm, repeat, memo);
        this.creator = creator;
        this.id = generateId();
    }

    public SharedToDo(String taskName, String location, boolean allDay,
            int startYear, int startMonth, int startDay, int startHour, int startMinute,
            int endYear, int endMonth, int endDay, int endHour, int endMinute,
            String alarm, String repeat, String memo, String creator, String id) {
        super(taskName, location, allDay, startYear, startMonth, startDay, startHour, startMinute,
                endYear, endMonth, endDay, endHour, endMinute, alarm, repeat, memo);
        this.creator = creator;
        this.id = id;
    }

    private String generateId() {
        return creator + "_" + System.currentTimeMillis() + "_" + (int) (Math.random() * 10000);
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String toNetworkString() {
        return String.format("%s|%s|%b|%d|%d|%d|%d|%d|%d|%d|%d|%d|%d|%s|%s|%s|%s|%s",
                getTaskName(), getLocation(), isAllDay(),
                getStartYear(), getStartMonth(), getStartDay(), getStartHour(), getStartMinute(),
                getEndYear(), getEndMonth(), getEndDay(), getEndHour(), getEndMinute(),
                getAlarm(), getRepeat(), getMemo(), creator, id);
    }

    public static SharedToDo fromNetworkString(String data, String creator) {
        try {
            String[] parts = data.split("\\|");
            if (parts.length < 16)
                return null;

            SharedToDo todo = new SharedToDo(
                    parts[0],
                    parts[1],
                    Boolean.parseBoolean(parts[2]),
                    Integer.parseInt(parts[3]),
                    Integer.parseInt(parts[4]),
                    Integer.parseInt(parts[5]),
                    Integer.parseInt(parts[6]),
                    Integer.parseInt(parts[7]),
                    Integer.parseInt(parts[8]),
                    Integer.parseInt(parts[9]),
                    Integer.parseInt(parts[10]),
                    Integer.parseInt(parts[11]),
                    Integer.parseInt(parts[12]),
                    parts[13],
                    parts[14],
                    parts[15],
                    creator);

            System.out.println("SharedToDo 생성됨 - ID: " + todo.getId() + ", 제목: " + todo.getTaskName());
            return todo;
        } catch (Exception e) {
            System.err.println("SharedToDo 파싱 오류: " + e.getMessage());
            return null;
        }
    }

    public static SharedToDo fromCompleteNetworkString(String data) {
        try {
            String[] parts = data.split("\\|");
            System.out.println("SharedToDo 파싱 시도 - 파트 개수: " + parts.length + ", 데이터: " + data);

            if (parts.length < 17) {
                System.err.println("파트 개수 부족: " + parts.length + " < 17");
                return null;
            }

            String creator = parts[16];
            String id = parts.length >= 18 ? parts[17] : "legacy_" + System.currentTimeMillis();

            SharedToDo todo = new SharedToDo(
                    parts[0], parts[1], Boolean.parseBoolean(parts[2]),
                    Integer.parseInt(parts[3]), Integer.parseInt(parts[4]), Integer.parseInt(parts[5]),
                    Integer.parseInt(parts[6]), Integer.parseInt(parts[7]), Integer.parseInt(parts[8]),
                    Integer.parseInt(parts[9]), Integer.parseInt(parts[10]), Integer.parseInt(parts[11]),
                    Integer.parseInt(parts[12]), parts[13], parts[14], parts[15], creator, id);

            System.out.println("SharedToDo 파싱 완료 - ID: " + todo.getId() + ", 제목: " + todo.getTaskName() + ", 생성자: "
                    + todo.getCreator());
            return todo;
        } catch (Exception e) {
            System.err.println("Complete SharedToDo 파싱 오류: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public String toString() {
        return getTaskName() + " (by " + creator + ")";
    }
}