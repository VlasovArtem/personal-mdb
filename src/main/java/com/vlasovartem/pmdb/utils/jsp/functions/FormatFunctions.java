package com.vlasovartem.pmdb.utils.jsp.functions;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Created by artemvlasov on 02/12/15.
 */
public class FormatFunctions {
    public static long floor(double number) {
        return Math.round(Math.floor(number));
    }
    public static String joining(List<String> strings) {
        if(Objects.nonNull(strings)) {
            return strings.stream().collect(Collectors.joining(", "));
        }
        return null;
    }
    public static long closest(double number) {
        return Math.round(number) + ((Math.round(number) % 5) == 0 ? 0 : (5 - (Math.round(number) % 5)));
    }
}
