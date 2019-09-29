package http.controller;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.io.ClassPathTemplateLoader;
import com.github.jknack.handlebars.io.TemplateLoader;
import db.DataBase;
import http.model.*;
import model.User;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.google.common.net.HttpHeaders.CONTENT_TYPE;

public class ListController implements Controller {
    @Override
    public HttpResponse service(HttpRequest httpRequest) {
        Map<String, String> cookieMap = getCookieMap(httpRequest);

        if (cookieMap != null && cookieMap.get("JSESSIONID") != null) {
            Handlebars handlebars = getHandlebars();
            try {
                Template template = handlebars.compile("user/list");

                List<User> users = new ArrayList<>(DataBase.findAll());
                Map<String, Object> model = new HashMap<>();
                model.put("users", users);
                String listPage = template.apply(model);

                return new HttpResponse.Builder()
                        .body(listPage.getBytes())
                        .protocols(HttpProtocols.HTTP1_1)
                        .status(HttpStatus.OK)
                        .addHeader(CONTENT_TYPE, ContentType.HTML.getType())
                        .build();
            } catch (IOException e) {
                e.getStackTrace();
            }
        }

        return new HttpResponse.Builder()
                .sendRedirect("/user/login.html")
                .protocols(HttpProtocols.HTTP1_1)
                .status(HttpStatus.FOUND)
                .addHeader(CONTENT_TYPE, ContentType.HTML.getType())
                .build();
    }

    private Map<String, String> getCookieMap(HttpRequest httpRequest) {
        String cookies = httpRequest.getHeader("Cookie");
        if (cookies == null) {
            return null;
        }
        String[] eachCookies = cookies.split("; ");

        Map<String, String> cookieMap = new HashMap<>();
        for (String eachCookie : eachCookies) {
            String[] cookiePair = eachCookie.split("=");
            cookieMap.put(cookiePair[0], cookiePair[1]);
        }
        return cookieMap;
    }

    private Handlebars getHandlebars() {
        TemplateLoader loader = new ClassPathTemplateLoader();
        loader.setPrefix("/templates");
        loader.setSuffix(".html");
        Handlebars handlebars = new Handlebars(loader);
        handlebars.registerHelper("idx", ((context, options) -> (Integer) context + 1));
        return handlebars;
    }
}
