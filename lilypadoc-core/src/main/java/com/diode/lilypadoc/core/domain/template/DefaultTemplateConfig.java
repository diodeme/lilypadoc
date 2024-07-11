package com.diode.lilypadoc.core.domain.template;

import com.diode.lilypadoc.standard.api.ILilypadocComponent;
import com.diode.lilypadoc.standard.api.ILilypadocFusionComponent;
import com.diode.lilypadoc.standard.domain.html.Html;
import com.diode.lilypadoc.standard.domain.html.IHtmlElement;
import com.diode.lilypadoc.standard.domain.html.Text;
import com.diode.lilypadoc.standard.utils.ListTool;
import com.diode.lilypadoc.standard.utils.StringTool;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
public class DefaultTemplateConfig {

    private Theme theme;
    private Index index;
    private Header header;
    private Footer footer;

    @Getter
    @Setter
    public static class Theme implements ILilypadocComponent {

        private ThemeSelect themeSelect;

        @Override
        public Html parse() {
            return new Html().element(new Text(themeSelect.parse()));
        }
    }

    @Getter
    @Setter
    public static class ThemeSelect implements IHtmlElement {

        private Boolean enable;
        private List<String> themeList;

        @Override
        public String parse() {
            if (!enable) {
                return "";
            }
            String pre = "<li>\n" +
                    " <select data-choose-theme\n" +
                    " class=\"focus:outline-none h-10 rounded-lg bg-inherit\">";
            String suf = "\n</select>\n" +
                    "</li>";
            StringBuilder sb = new StringBuilder();
            String format = "<option value=\"%s\">%s</option>";
            for (String theme : ListTool.safeArrayList(themeList)) {
                sb.append("\n").append(String.format(format, theme, StringTool.capitalizeFirstLetterIfAlphabetic(theme)));
            }
            return pre + sb + suf;
        }
    }

    @Getter
    @Setter
    public static class Index implements ILilypadocFusionComponent {

        private String title;
        private String tips;
        private String background;
    }

    @Getter
    @Setter
    public static class Header implements ILilypadocFusionComponent {

        private String indexPath = "/index.html";
        private String title;
        private Logo logo;
        private Navbar navbar;
    }

    @Getter
    @Setter
    public static class Logo implements ILilypadocFusionComponent {

        private String path;
    }

    @Getter
    @Setter
    public static class Navbar implements IHtmlElement {

        private List<LinkItem> items;
        private static final String format = "<li>%s</li>";

        @Override
        public String parse() {
            StringBuilder sb = new StringBuilder();
            for (LinkItem linkItem : ListTool.safeArrayList(items)) {
                sb.append("\n").append(String.format(format, linkItem.parse()));
            }
            return sb.toString();
        }
    }

    @Getter
    @Setter
    public static class Footer implements ILilypadocFusionComponent {

        private FooterBar footerBar;
        private String copyRight;
        private FooterLink outerLink;
    }

    @Getter
    @Setter
    public static class FooterBar implements IHtmlElement {

        private List<FooterCol> footerCols;

        @Override
        public String parse() {
            return ListTool.safeArrayList(footerCols).stream().map(FooterCol::parse).collect(Collectors.joining());
        }
    }

    @Getter
    @Setter
    public static class FooterCol {

        private String title;
        private List<LinkItem> linkItems;

        public String parse() {
            return "<nav>\n" +
                    " <h6 class=\"footer-title\">" + title + "</h6>\n" +
                    ListTool.safeArrayList(linkItems).stream().map(LinkItem::parse).collect(Collectors.joining()) +
                    " </nav>\n";
        }
    }

    @Getter
    @Setter
    public static class FooterLink implements IHtmlElement {

        private List<LinkItem> linkItems;

        @Override
        public String parse() {
            return ListTool.safeArrayList(linkItems).stream().map(LinkItem::parse).collect(Collectors.joining());
        }
    }

    @Getter
    @Setter
    public static class LinkItem {

        private String href;
        private String value;
        private boolean newWindow;

        private static final String format = "<a href=\"%s\"%s>%s</a>\n";

        private String parse() {
            return String.format(format, href, newWindow ? " target=\"_blank\"" : "", value);
        }
    }


}
