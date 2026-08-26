import java.io.File;
import java.util.*;

public class Main {
    public static void main(String[] args) throws Exception {
        String[] commands = {"echo", "exit", "type", "pwd", "cd"};
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
            else if (input.equals("pwd")) {
                String currentDir = System.getProperty("user.dir");
                System.out.println(currentDir);
            }
            else if (input.startsWith("cd ")) {
                String path = input.substring(3).trim();
                File dir = new File(path);
                // absolute path
                if (dir.isAbsolute() && dir.exists() && dir.isDirectory()) {
                    System.setProperty("user.dir", dir.getAbsolutePath());
                }
                // relative path
                if (!dir.isAbsolute()) {
                    path = path.substring(2);
                    dir = new File(System.getProperty("user.dir"), path);
                }
                if (dir.exists() && dir.isDirectory()) {
                    System.setProperty("user.dir", dir.getAbsolutePath());
                } else {
                    System.out.println("cd: " + path + ": No such file or directory");
                }
                // ~ character
            }
            else if (input.equals("exit")) {
                scanner.close();
                break;
            }
            else if (input.contains("echo")) {
                System.out.println(input.substring(5));
            }
            else {
                String[] programs = input.split(" ");
                String command = programs[0];
                String executablePath = new Main().getExecutablePath(command);
                if (executablePath != null) {
                    ProcessBuilder processBuilder = new ProcessBuilder(programs);
                    processBuilder.inheritIO();
                    Process process = processBuilder.start();
                    process.waitFor();
                } else {
                    System.out.println(command + ": command not found");
                }
            }
        }
    }

    public String getExecutablePath(String command) {
        String pathenv = System.getenv("PATH");
        if (pathenv != null) {
            String[] directories = pathenv.split(File.pathSeparator);
            for (String dir : directories) {
                File file = new File(dir, command);
                if (file.exists() && file.canExecute()) {
                    return file.getAbsolutePath();
                }
            }
        }
        return null;
    }
}
