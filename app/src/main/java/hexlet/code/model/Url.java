package hexlet.code.model;

import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Setter
@Getter
public class Url {
    private long id;
    private String name;
    private Timestamp createdAt;
    private List<UrlCheck> checks;

    public Url(String name, Timestamp createdAt) {
        this.name = name;
        this.createdAt = createdAt;
    }

    public String getFormattedLastCheckDate() {
        if (checks != null && !checks.isEmpty()) {
            return formatDate(checks.get(checks.size() - 1).getCreatedAt());
        }
        return null;
    }

    public Integer getLastCheckStatus() {
        if (checks != null && !checks.isEmpty()) {
            return checks.get(checks.size() - 1).getStatusCode();
        }
        return null;
    }

    public String getFormattedCreatedAt() {
        return formatDate(createdAt);
    }

    public static String formatDate(Timestamp ts) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date(ts.getTime());
        return sdf.format(date);
    }
}
