package hexlet.code;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import hexlet.code.repository.BaseRepository;
import io.javalin.Javalin;
import nz.net.ultraq.thymeleaf.layoutdialect.LayoutDialect;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.extras.java8time.dialect.Java8TimeDialect;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import io.javalin.rendering.template.JavalinThymeleaf;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.SQLException;
import java.util.stream.Collectors;

public class App {
    private static final String JDBC_URL_H2 = "jdbc:h2:mem:project;DB_CLOSE_DELAY=-1";

    private static int getPort() {
        String port = System.getenv().getOrDefault("PORT", "8080");
        return Integer.valueOf(port);
    }

    private static String getJdbcUrlFromEnv() {
        return System.getenv().getOrDefault("JDBC_DATABASE_URL", JDBC_URL_H2);
    }

    private static boolean isDevelopment() {
        return getJdbcUrlFromEnv().equals(JDBC_URL_H2);
    }

    private static void addRoutes(Javalin app) {
        app.get("/", ctx -> ctx.render("startPage.html"));
    }

    private static TemplateEngine getTemplateEngine() {
        TemplateEngine templateEngine = new TemplateEngine();

        ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
        templateResolver.setPrefix("/templates/");

        templateEngine.addTemplateResolver(templateResolver);
        templateEngine.addDialect(new LayoutDialect());
        templateEngine.addDialect(new Java8TimeDialect());

        return templateEngine;
    }

    public static Javalin getApp() throws IOException, SQLException {
        var hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(getJdbcUrlFromEnv());

        var dataSource = new HikariDataSource(hikariConfig);
        var url = App.class.getClassLoader().getResource("schema.sql");
        assert url != null;
        var file = new File(url.getFile());
        var sql = Files.lines(file.toPath())
                .collect(Collectors.joining("\n"));

        try (var connection = dataSource.getConnection();
        var statement = connection.createStatement()) {
            statement.execute(sql);
        }
        BaseRepository.dataSource = dataSource;

        Javalin app = Javalin.create(config -> {
            if (isDevelopment()) {
                config.plugins.enableDevLogging();
            }
            config.staticFiles.enableWebjars();
            JavalinThymeleaf.init(getTemplateEngine());
        });

        addRoutes(app);

        //для чего это?
        app.before(ctx -> {
            ctx.attribute("ctx", ctx);
        });

    return app;
    }

    public static void main(String[] args) throws SQLException, IOException {
        Javalin app = getApp();
        app.start(getPort());
    }
}
