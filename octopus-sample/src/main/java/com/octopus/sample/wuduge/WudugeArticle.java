package com.octopus.sample.wuduge;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.pinyin.PinyinUtil;
import com.octopus.core.Octopus;
import com.octopus.core.Request;
import com.octopus.core.WebSite;
import com.octopus.core.processor.annotation.Attr;
import com.octopus.core.processor.annotation.Denoiser;
import com.octopus.core.processor.annotation.Extractor;
import com.octopus.core.processor.annotation.Xpath;
import com.octopus.core.processor.matcher.Matchers;
import lombok.Data;
import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

import java.io.File;
import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * @author shoulai.yang@gmail.com
 * @date 2024/06/28
 */
@Data
@Extractor
public class WudugeArticle {

    private static final HanyuPinyinOutputFormat FORMAT = new HanyuPinyinOutputFormat();

    static {
        FORMAT.setCaseType(HanyuPinyinCaseType.LOWERCASE);
        FORMAT.setToneType(HanyuPinyinToneType.WITH_TONE_MARK);
        FORMAT.setVCharType(HanyuPinyinVCharType.WITH_U_UNICODE);
    }

    @Xpath("//h1/text()")
    private String title;

    @Xpath("//section/article/header/p[@class='source']/a[@class='author']/text()")
    private String author;

    @Xpath(value = "//section/article/header/p[@class='source']/a[@class='dynasty']/text()",
            denoiser = @Denoiser(regex = "^〔(.*)〕$", groups = 1))
    private String dynasty;

    @Xpath("//div[@id='More']/div[@class='contson content']/p/text()")
    private List<String> content;

    @Xpath("//p[strong[text()='译文']]/following-sibling::p[following-sibling::p[strong[text()='注释']]]/text()")
    private List<String> translation;

    @Xpath(value = "//p[strong[text()='注释']]/following-sibling::p",
            denoiser = @Denoiser(regex = "^<p>(<strong>)?(.*?)(</strong>)?</p>$", groups = 2))
    private List<String> comment;

    @Xpath(value = "//h2[text()='赏析']/../div/div/p/text()")
    private List<String> appreciation = new ArrayList<>();

    @Xpath(value = "//h2[text()='赏析一']/../div/div/p/text()")
    private List<String> appreciation1 = new ArrayList<>();


    @Attr(value = "index")
    private int index;

    private List<ContentLine> lines = new ArrayList<>();


    public static void main(String[] args) {
        Octopus.builder()
                .addSite(WebSite.of("www.wuduge.com").setRateLimiter(1))
                .addSeeds(Request.get("https://www.wuduge.com/article/225953.html").putAttribute("index", 1))
                .addSeeds(Request.get("https://www.wuduge.com/article/204035.html").putAttribute("index", 2))
                .addSeeds(Request.get("https://www.wuduge.com/article/197268.html").putAttribute("index", 3))
                .addSeeds(Request.get("https://www.wuduge.com/article/102019.html").putAttribute("index", 4))
                .addSeeds(Request.get("https://www.wuduge.com/article/11307.html").putAttribute("index", 5))
                .addSeeds(Request.get("https://www.wuduge.com/article/22884.html").putAttribute("index", 6))
                .addSeeds(Request.get("https://www.wuduge.com/article/19614.html").putAttribute("index", 7))
                .addSeeds(Request.get("https://www.wuduge.com/article/33262.html").putAttribute("index", 8))
                .addSeeds(Request.get("https://www.wuduge.com/article/29349.html").putAttribute("index", 9))
                .addSeeds(Request.get("https://www.wuduge.com/article/4444.html").putAttribute("index", 10))
                .addSeeds(Request.get("https://www.wuduge.com/article/101207.html").putAttribute("index", 11)
                        .putAttribute("title", "饮湖上初晴后雨"))
                .addSeeds(Request.get("https://www.wuduge.com/article/13351.html").putAttribute("index", 12))
                .addSeeds(Request.get("https://www.wuduge.com/article/28454.html").putAttribute("index", 13))
                .addSeeds(Request.get("https://www.wuduge.com/article/6294.html").putAttribute("index", 14))
                .addSeeds(Request.get("https://www.wuduge.com/article/19145.html").putAttribute("index", 15))
                .addProcessor(
                        Matchers.HTML,
                        WudugeArticle.class,
                        (article, r) -> {
                            if (article.getAppreciation().isEmpty()) {
                                article.getAppreciation().addAll(article.getAppreciation1());
                            }
                            try {
                                if (article.getContent() != null) {
                                    for (String line : article.getContent()) {
                                        String pre = line.substring(0, line.length() / 2);
                                        String next = line.substring(line.length() / 2);
                                        ContentLine l1 = new ContentLine();
                                        ContentLine l2 = new ContentLine();
                                        l1.items.addAll(getPinyin(pre));
                                        l2.items.addAll(getPinyin(next));
                                        article.lines.add(l1);
                                        article.lines.add(l2);
                                    }
                                }
                            } catch (BadHanyuPinyinOutputFormatCombination e) {
                                throw new RuntimeException(e);
                            }
                            if (StrUtil.isNotBlank(r.getRequest().getAttribute("title"))) {
                                article.setTitle(r.getRequest().getAttribute("title"));
                            }
                            String html = WuduArticleFormatter.format(article);
                            File file = new File("/Users/yangshoulai/Downloads/古诗/" + article.getIndex() + " " + article.getTitle() + ".html");
                            FileUtil.writeUtf8String(html, file);
                        })
                .build()
                .start();
    }

    @Data
    public static class ContentLine {
        private List<PinyinItem> items = new ArrayList<>();
    }

    @Data
    public static class PinyinItem {
        private String pinyin;

        private String ch;

        public PinyinItem(String ch, String pinyin) {
            this.pinyin = pinyin;
            this.ch = ch;
        }

        public PinyinItem() {
        }
    }

    private static List<PinyinItem> getPinyin(String content) throws BadHanyuPinyinOutputFormatCombination {
        List<PinyinItem> pinyin = new ArrayList<>();
        for (int i = 0; i < content.length(); i++) {
            char c = content.charAt(i);
            if (PinyinUtil.isChinese(c)) {
                String[] array = PinyinHelper.toHanyuPinyinStringArray(c, FORMAT);
                if (array.length > 0) {
                    pinyin.add(new PinyinItem(String.valueOf(c), array[0]));
                }
            } else {
                pinyin.add(new PinyinItem(String.valueOf(c), ""));
            }
        }

        return pinyin;
    }
}
