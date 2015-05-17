package volley.volleyball;


public class StringUtils {

    public static boolean isEmpty(final String value){
        return value == null || value.trim().isEmpty();
    }

    public static String toString(final Object value){
        return value == null ? "" : value.toString();
    }
}
