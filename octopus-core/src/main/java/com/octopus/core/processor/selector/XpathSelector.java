package com.octopus.core.processor.selector;

import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.EscapeUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.XmlUtil;
import com.octopus.core.Response;
import com.octopus.core.properties.selector.XpathSelectorProperties;
import org.htmlcleaner.CleanerProperties;
import org.htmlcleaner.DomSerializer;
import org.htmlcleaner.HtmlCleaner;
import org.w3c.dom.Attr;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.xpath.XPathConstants;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author shoulai.yang@gmail.com
 * @date 2021/11/25
 */
public class XpathSelector extends AbstractCacheableSelector<Node, XpathSelectorProperties> {
    private static final String TAG_TD = "td";

    private static final String TAG_TR = "tr";

    private static final String TAG_TABLE = "table";

    private final HtmlCleaner cleaner;

    private final DomSerializer serializer;

    public XpathSelector() {
        CleanerProperties properties = new CleanerProperties();
        this.cleaner = new HtmlCleaner(properties);
        this.serializer = new DomSerializer(new CleanerProperties(), false, false, false);
    }

    private static String wrap(String tag, String content) {
        return "<" + tag + ">" + content + "</" + tag + ">";
    }

    private static boolean isWrappedBy(String tag, String content) {
        Pattern pattern =
                Pattern.compile("^<" + tag + "[\\s\\S]*</" + tag + ">$", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(content);
        return matcher.matches();
    }

    @Override
    public List<String> doSelectWithDoc(
            Node document, XpathSelectorProperties selector, boolean multi, Response response) {
        List<String> results = new ArrayList<>();
        if (selector.isNode()) {
            NodeList nodes = XmlUtil.getNodeListByXPath(selector.getExpression(), document);
            int end = multi ? nodes.getLength() : 1;
            for (int i = 0; i < end; i++) {
                Node node = nodes.item(i);
                String value = null;
                if (node instanceof CharacterData) {
                    value = ((CharacterData) node).getData();
                } else if (node instanceof Attr) {
                    value = ((Attr) node).getValue();
                } else {
                    value = XmlUtil.toStr(node, CharsetUtil.UTF_8, false, true);
                }
                if (!StrUtil.isBlank(value)) {
                    value = EscapeUtil.unescapeXml(value);
                }
                results.add(value);
            }
        } else {
            Object val = XmlUtil.getByXPath(selector.getExpression(), document, XPathConstants.STRING);
            if (val != null) {
                results.add(val.toString());
            }
        }
        return results;
    }

    @Override
    protected Node parse(String content) throws Exception {
        content = this.completeContent(content);
        return this.serializer.createDOM(this.cleaner.clean(content));
    }

    private String completeContent(String content) {
        if (StrUtil.isBlank(content)) {
            return content;
        }
        String html = content.trim();
        if (isWrappedBy(TAG_TD, html)) {
            return wrap(TAG_TABLE, wrap(TAG_TR, content));
        } else if (isWrappedBy(TAG_TR, html)) {
            return wrap(TAG_TABLE, content);
        }
        return content;
    }
}
