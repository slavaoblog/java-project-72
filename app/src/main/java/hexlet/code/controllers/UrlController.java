package hexlet.code.controllers;

import hexlet.code.dto.urls.UrlPage;
import hexlet.code.dto.urls.UrlsPage;
import hexlet.code.model.Url;
import hexlet.code.repository.UrlRepository;
import io.javalin.http.Context;
import io.javalin.http.NotFoundResponse;

import java.net.MalformedURLException;
import java.net.URL;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Collections;

public final class UrlController {
    public static void listUrls(Context ctx) throws SQLException {
        var urls = UrlRepository.getEntities();
        var page = new UrlsPage(urls);
        page.setFlash(ctx.consumeSessionAttribute("flash"));
        page.setFlashType(ctx.consumeSessionAttribute("flash-type"));
        ctx.render("urls/urls.jte", Collections.singletonMap("page", page));
    }

    public static void addUrl(Context ctx) throws SQLException {
        String inputUrl = ctx.formParamAsClass("url", String.class).get();
        String parsedUrl = parseUrl(inputUrl);
        long now = System.currentTimeMillis();
        Timestamp sqlTimestamp = new Timestamp(now);
        if (parsedUrl == null) {
            ctx.sessionAttribute("flash", "Incorrect URL");
            ctx.sessionAttribute("flash-type", "danger");
            RootController.welcome(ctx);
            return;
        }
        if (UrlRepository.existsByName(parsedUrl)) {
            ctx.sessionAttribute("flash", "Page already exists");
            ctx.sessionAttribute("flash-type", "info");
            listUrls(ctx);
            return;
        }
            ctx.sessionAttribute("flash", "Page added successfully");
            ctx.sessionAttribute("flash-type", "success");
            UrlRepository.save(new Url(parsedUrl, sqlTimestamp));
            listUrls(ctx);
    }
    public static String parseUrl(String inputUrl) {
        try {
            URL url = new URL(inputUrl);
            var protocol = url.getProtocol();
            var authority = url.getAuthority();
            return protocol + "://" + authority;
        } catch (MalformedURLException e) {
            return null;
        }
    }

    public static void show(Context ctx) throws SQLException {
        var id = ctx.pathParamAsClass("id", Long.class).get();
        var url = UrlRepository.find(id)
                .orElseThrow(() -> new NotFoundResponse("Url not found"));

        var page = new UrlPage(url);
        ctx.render("urls/show.jte", Collections.singletonMap("page", page));
    }
}
