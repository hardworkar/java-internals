import java.lang.invoke.StringConcatFactory;

public class P1 {
    public static void main(String[] args) {
        String s = "";
        for (int i = 0; i < 10000; ++i) {
            s += " " + String.valueOf(i);
        }
        System.out.println(s.length());
    }
}
