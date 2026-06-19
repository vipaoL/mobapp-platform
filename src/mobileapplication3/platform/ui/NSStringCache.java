// SPDX-License-Identifier: LGPL-2.1-only

package mobileapplication3.platform.ui;

import org.robovm.apple.foundation.NSString;

import java.util.LinkedHashMap;
import java.util.Map;

public class NSStringCache {
    private static final int MAX_ENTRIES = 500;
    private static final LinkedHashMap<String, NSString> cache =
            new LinkedHashMap<String, NSString>(MAX_ENTRIES, 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<String, NSString> eldest) {
                    return size() > MAX_ENTRIES;
                }
            };
    private static final SubstringKey LOOKUP_KEY = new SubstringKey();

    public static synchronized NSString get(String str, int offset, int len) {
        if (str == null || len <= 0) {
            return null;
        }

        LOOKUP_KEY.set(str, offset, len);

        NSString ns = cache.get(LOOKUP_KEY);

        if (ns == null) {
            String sub = str.substring(offset, offset + len);
            ns = new NSString(sub);
            cache.put(sub, ns);
        }

        return ns;
    }

    private static class SubstringKey {
        private String str;
        private int offset;
        private int len;

        public void set(String str, int offset, int len) {
            this.str = str;
            this.offset = offset;
            this.len = len;
        }

        @Override
        public int hashCode() {
            int h = 0;
            for (int i = 0; i < len; i++) {
                h = 31 * h + str.charAt(offset + i);
            }
            return h;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof String) {
                String s = (String) obj;
                if (s.length() != len) return false;
                for (int i = 0; i < len; i++) {
                    if (str.charAt(offset + i) != s.charAt(i)) return false;
                }
                return true;
            }
            return false;
        }
    }
}