import java.io.File;
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
                } 
                else {
                    String pathenv = System.getenv("PATH");
                    boolean found = false;

                    if (pathenv != null) {
                        String[] directories = pathenv.split(File.pathSeparator);
                    for (String dir : directories) {
                        File file = new File(dir, typeInput);
                        if (file.exists() && file.canExecute()) {
                            System.out.println(typeInput + " is " + file.getAbsolutePath());
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        System.out.println(typeInput + ": not found");
                    }
                    }   
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
