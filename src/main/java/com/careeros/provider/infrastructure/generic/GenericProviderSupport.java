package com.careeros.provider.infrastructure.generic;

import java.util.function.Supplier;

final class GenericProviderSupport {
    private GenericProviderSupport() {}
    static <T> T retry(int attempts, Supplier<T> action) {
        RuntimeException last = null;
        for (int i=0;i<attempts;i++) try { return action.get(); } catch (RuntimeException e) { last=e; }
        throw last;
    }
    static String value(org.jsoup.nodes.Element e, String selector) { var n=e.selectFirst(selector); return n==null?null:n.text().trim(); }
    static String attr(org.jsoup.nodes.Element e, String selector, String attr) { var n=e.selectFirst(selector); return n==null?null:n.absUrl(attr); }
}
