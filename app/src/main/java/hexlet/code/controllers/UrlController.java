package hexlet.code.controllers;

import hexlet.code.dto.urls.UrlPage;
import hexlet.code.dto.urls.UrlsPage;
import hexlet.code.model.Url;
import hexlet.code.model.UrlCheck;
import hexlet.code.repository.UrlCheckRepository;
import hexlet.code.repository.UrlRepository;
import io.javalin.http.Context;
import io.javalin.http.NotFoundResponse;
import kong.unirest.UnirestException;
import kong.unirest.core.HttpResponse;
import kong.unirest.core.Unirest;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

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

        var urlCheckList = UrlCheckRepository.getEntitiesByUrlId(id);
        if (!urlCheckList.isEmpty()) {
            url.setChecks(urlCheckList);
        }

        var page = new UrlPage(url);

        page.setFlash(ctx.consumeSessionAttribute("flash"));
        page.setFlashType(ctx.consumeSessionAttribute("flash-type"));

        ctx.render("urls/show.jte", Collections.singletonMap("page", page));
    }

    public static void check(Context ctx) throws SQLException {
        var id = ctx.pathParamAsClass("id", Long.class).get();
        var url = UrlRepository.find(id)
                .orElseThrow(() -> new NotFoundResponse("Url not found"));
        try {
            String urlName = url.getName();
            HttpResponse<String> response = Unirest
                    .get(urlName)
                    .asString();

            String content = response.getBody();
            Document doc = Jsoup.parse(content);

            int statusCode = response.getStatus();
            String title = doc.title();
            String h1 = "";
            String description = "";
            long now = System.currentTimeMillis();
            Timestamp ts = new Timestamp(now);

            Element h1Element = doc.selectFirst("h1");
            Element descriptionElement = doc.selectFirst("meta[name=description]");

            if (h1Element != null) {
                h1 = h1Element.text();
            }
            if (descriptionElement != null) {
                description = descriptionElement.attr("content");
            }

            UrlCheck urlCheck = new UrlCheck(statusCode, title, h1, description, url.getId(), ts);
            UrlCheckRepository.save(urlCheck);

            ctx.sessionAttribute("flash", "Page was checked successfully");
            ctx.sessionAttribute("flash-type", "success");
        } catch (UnirestException e) {
            ctx.sessionAttribute("flash", "Incorrect URL");
            ctx.sessionAttribute("flash-type", "danger");
        }
        show(ctx);
    }
}
