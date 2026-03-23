package Chat_Bot;

public class LoggedInUser {
    private static String username;

    public static void setUsername(String user) {
        username = user;
    }

    public static String getUsername() {
        if (username == null) {
            return "Guest"; // ✅ Instead of crashing, return a default user.
        }
        return username;
    }

    public static boolean isUserLoggedIn() {
        return username != null;
    }
}
