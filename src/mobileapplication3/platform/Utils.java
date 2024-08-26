/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mobileapplication3.platform;

/**
 *
 * @author vipaol
 */
public class Utils {
    
    public static String shortArrayToString(short [] arr) {
        try {
            if (arr == null) {
                return "null";
            }

            if (arr.length == 0) {
                return "[]";
            }

            StringBuffer sb = new StringBuffer(arr.length*6);
            sb.append("[");
            for (int i = 0; i < arr.length - 1; i++) {
                sb.append(arr[i]);
                sb.append(", ");
            }
            sb.append(arr[arr.length-1]);
            sb.append("]");
            return sb.toString();
        } catch(Exception ex) {
            ex.printStackTrace();
            return ex.toString();
        }
    }
    
    public static String[] split(String sb, String splitter){
        String[] strs = new String[sb.length()];
        int splitterLength = splitter.length();
        int initialIndex = 0;
        int indexOfSplitter = indexOf(sb, splitter, initialIndex);
        int count = 0;
        if(-1==indexOfSplitter) return new String[]{sb};
        while(-1!=indexOfSplitter){
            char[] chars = new char[indexOfSplitter-initialIndex];
            sb.getChars(initialIndex, indexOfSplitter, chars, 0);
            initialIndex = indexOfSplitter+splitterLength;
            indexOfSplitter = indexOf(sb, splitter, indexOfSplitter+1);
            strs[count] = new String(chars);
            count++;
        }
        // get the remaining chars.
        if(initialIndex+splitterLength<=sb.length()){
            char[] chars = new char[sb.length()-initialIndex];
            sb.getChars(initialIndex, sb.length(), chars, 0);
            strs[count] = new String(chars);
            count++;
        }
        String[] result = new String[count];
        for(int i = 0; i<count; i++){
            result[i] = strs[i];
        }
        return result;
    }

    public static int indexOf(String sb, String str, int start){
        int index = -1;
        if((start>=sb.length() || start<-1) || str.length()<=0) return index;
        char[] tofind = str.toCharArray();
        outer: for(;start<sb.length(); start++){
            char c = sb.charAt(start);
            if(c==tofind[0]){
                if(1==tofind.length) return start;
                inner: for(int i = 1; i<tofind.length;i++){ // start on the 2nd character
                    char find = tofind[i];
                    int currentSourceIndex = start+i;
                    if(currentSourceIndex<sb.length()){
                        char source = sb.charAt(start+i);
                        if(find==source){
                            if(i==tofind.length-1){
                                return start;
                            }
                            continue inner;
                        } else {
                            start++;
                            continue outer;
                        }
                    } else {
                        return -1;
                    }

                }
            }
        }
        return index;
    }

    public static String replace(String _text, String _searchStr, String _replacementStr) {
        // String buffer to store str
        StringBuffer sb = new StringBuffer();

        // Search for search
        int searchStringPos = _text.indexOf(_searchStr);
        int startPos = 0;
        int searchStringLength = _searchStr.length();

        // Iterate to add string
        while (searchStringPos != -1) {
            sb.append(_text.substring(startPos, searchStringPos)).append(_replacementStr);
            startPos = searchStringPos + searchStringLength;
            searchStringPos = _text.indexOf(_searchStr, startPos);
        }

        // Create string
        sb.append(_text.substring(startPos,_text.length()));

        return sb.toString();
    }
    
    public static int count(String s, char c) {
        int ret = 0;
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) == c) {
                ret++;
            }
        }
        return ret;
    }

}
