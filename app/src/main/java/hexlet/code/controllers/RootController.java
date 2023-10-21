package hexlet.code.controllers;

import hexlet.code.dto.BasePage;
import io.javalin.http.Context;

import java.util.Collections;

public final class RootController {

    public static void welcome(Context ctx) {
        var page = new BasePage();
        page.setFlash(ctx.consumeSessionAttribute("flash"));
        page.setFlashType(ctx.consumeSessionAttribute("flash-type"));
        ctx.render("startPage.jte", Collections.singletonMap("page", page));
    }
}
