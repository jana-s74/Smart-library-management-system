import utils.PasswordUtils;

public class CheckHash {
    public static void main(String[] args) {
        String p1 = "janaSK@1123";
        String hash = PasswordUtils.hashPassword(p1);
        System.out.println("Password: " + p1);
        System.out.println("Hash: " + hash);
        System.out.println("Matches: " + hash.equals("e977a3b6f4afab749127c9a839af124a3f13ae40c8fb371fea808705b4500327"));
    }
}
