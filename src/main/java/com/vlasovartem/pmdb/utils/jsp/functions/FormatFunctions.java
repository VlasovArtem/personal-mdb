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
    public static String persons(List<String> cast) {
        if(Objects.nonNull(cast)) {
            return cast.stream().collect(Collectors.joining(", "));
        }
        return null;
    }
}
