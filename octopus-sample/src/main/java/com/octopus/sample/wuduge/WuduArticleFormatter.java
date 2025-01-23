package com.octopus.sample.wuduge;

import cn.hutool.http.HtmlUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author shoulai.yang@gmail.com
 * @date 2024/06/28
 */
public class WuduArticleFormatter {

    private static final String HTML_TEMPLATE = "<!DOCTYPE html>\n" +
            "<html>\n" +
            "  <head>\n" +
            "    <style>\n" +
            "      html {\n" +
            "        font-family: \"Microsoft YaHei\", sans-serif;\n" +
            "      }\n" +
            "      .poem {\n" +
            "        text-align: center;\n" +
            "        width: 720px;\n" +
            "        margin: 40px auto;\n" +
            "      }\n" +
            "      .poem-header__title {\n" +
            "        font-size: 24px;\n" +
            "        font-weight: 500;\n" +
            "      }\n" +
            "\n" +
            "      .poem-body {\n" +
            "        display: flex;\n" +
            "        flex-direction: column;\n" +
            "        align-items: center;\n" +
            "        gap: 10px;\n" +
            "      }\n" +
            "\n" +
            "      .poem-header__author {\n" +
            "        margin: 8px 0px 12px 0px;\n" +
            "      }\n" +
            "\n" +
            "      .poem-header__author_dynasty {\n" +
            "        color: #626675;\n" +
            "        font-size: 14px;\n" +
            "      }\n" +
            "      .poem-header__author_name {\n" +
            "        margin-left: 0.5rem;\n" +
            "        color: #626675;\n" +
            "        font-size: 14px;\n" +
            "      }\n" +
            "\n" +
            "      .poem-body__line {\n" +
            "        display: flex;\n" +
            "        flex-direction: row;\n" +
            "      }\n" +
            "\n" +
            "      .poem-body__line_item {\n" +
            "        display: flex;\n" +
            "        flex-direction: column;\n" +
            "        align-items: center;\n" +
            "        justify-content: end;\n" +
            "        width: 36px;\n" +
            "      }\n" +
            "      .item_pinyin {\n" +
            "        font-size: 12px;\n" +
            "        color: #848691;\n" +
            "      }\n" +
            "\n" +
            "      .item_ch {\n" +
            "        font-size: 20px;\n" +
            "        font-weight: 400;\n" +
            "      }\n" +
            "\n" +
            "      .poem-appreciation {\n" +
            "        text-align: left;\n" +
            "        margin-top: 20px;\n" +
            "      }\n" +
            "\n" +
            "      .poem-appreciation__title {\n" +
            "      }\n" +
            "      .poem-appreciation__body {\n" +
            "      }\n" +
            "      .appreciation {\n" +
            "        color: #626675;\n" +
            "        font-size: 14px;\n" +
            "      }\n" +
            "    </style>\n" +
            "  </head>\n" +
            "  <body>%s</body>\n" +
            "</html>";

    private static final String POEM_TEMPLATE = "<div class=\"poem\">" +
            "<div class=\"poem-header\">" +
            "<div class=\"poem-header__title\">%s</div>" +
            "<div class=\"poem-header__author\">" +
            "<span class=\"poem-header__author_dynasty\">[%s]</span>" + "<span class=\"poem-header__author_name\">%s</span>" +
            "</div>" +
            "</div>" +
            "<div class=\"poem-body\">%s</div>" +
            "%s" +
            "</div>";


    public static String format(WudugeArticle article) {
        List<String> body = new ArrayList<>();
        for (WudugeArticle.ContentLine line : article.getLines()) {
            String template = "<div class=\"poem-body__line\">%s</div>";
            List<String> items = new ArrayList<>();
            for (WudugeArticle.PinyinItem item : line.getItems()) {
                items.add(String.format("<div class=\"poem-body__line_item\"><div class=\"item_pinyin\">%s</div><div class=\"item_ch\">%s</div></div>", item.getPinyin(), item.getCh()));
            }
            body.add(String.format(template, String.join("", items)));
        }
        String appreciationTemplate = "<div class=\"poem-appreciation\">" +
                "<div class=\"poem-appreciation__title\">赏析</div>" +
                "<div class=\"poem-appreciation__body\">%s</div>";
        String appreciation = "";
        if (!article.getAppreciation().isEmpty()) {
            appreciation = String.format(appreciationTemplate, article.getAppreciation().stream().map(l -> "<p class=\"appreciation\">" + l + "</p>").collect(Collectors.joining("")));
        }
        String poem = String.format(POEM_TEMPLATE, article.getTitle(), article.getDynasty(), article.getAuthor(), String.join("", body)
                , appreciation);
        return String.format(HTML_TEMPLATE, poem);
    }

}
