public class StringReverse {
    public static String StrRev(String s){
    if (s == null || s.length()==1)
        return s;
    return s.charAt(s.length()-1)+StrRev(s.substring(0,s.length()-1));
    }

    public static void main(String[] args) {
        System.out.println(StrRev("HelloAll"));
    }
}
