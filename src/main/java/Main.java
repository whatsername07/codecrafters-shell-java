import java.util.*;

public class Main {
    public static void main(String[] args) throws Exception {
        String[] commands = {"echo", "exit", "type"};
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.print("$ ");
            String input = scanner.nextLine();
            if (input.contains("type")) {
                String typeInput = input.substring(5);
                if (Arrays.asList(commands).contains(typeInput)) {
                    System.out.println(typeInput + " is a shell builtin");
                } else {
                    System.out.println(typeInput + ": not found");
                }
            }
            else if (input.equals("exit")) {
                scanner.close();
                break;
            }
            else if (input.contains("echo")) {
                System.out.println(input.substring(5));
            }
            else {
                System.out.println(input + ": command not found");
            }
        }
    }
}
