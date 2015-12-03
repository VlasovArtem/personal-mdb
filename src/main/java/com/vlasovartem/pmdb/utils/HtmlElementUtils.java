package com.vlasovartem.pmdb.utils;

import org.jsoup.nodes.Element;

import java.util.Objects;

/**
 * Created by artemvlasov on 03/12/15.
 */
public class HtmlElementUtils {
    public static String findText(String cssSelector, Element element) {
        Element selectedElement = element.select(cssSelector).first();
        if(Objects.nonNull(selectedElement)) {
            return selectedElement.text();
        } else {
            return null;
        }
    }
}
