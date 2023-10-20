package hexlet.code.controllers;

import hexlet.code.dto.BasePage;
import io.javalin.http.Handler;

import java.util.Collections;

public final class RootController {
    public static Handler welcome = ctx -> {
        var page = new BasePage();
        page.setFlash(ctx.consumeSessionAttribute("flash"));
        page.setFlashType(ctx.consumeSessionAttribute("flash-type"));
        ctx.render("startPage.jte", Collections.singletonMap("page", page));
    };
}
