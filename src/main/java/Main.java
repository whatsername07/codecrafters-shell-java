import java.nio.file.Path;
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
                // Get the current working directory, and the path to change to
                Path currentDir = new File(System.getProperty("user.dir")).toPath();
                String path = input.substring(3).trim();
                File dir = new File(path);
                
                // tilde 
                if (input.endsWith("~")) {
                    // Get the user's home directory from the environment variable
                    String homeDir = System.getenv("HOME");
                    File home = new File(homeDir);
                    if (home.exists() && home.isDirectory()) {
                        System.setProperty("user.dir", home.getAbsolutePath());
                    } else {
                        System.out.println("cd: " + path + ": No such file or directory");
                    }
                }
                // directory path after ~ character
                else if (input.contains("~")) {
                    // Get the user's home directory from the environment variable
                    String homeDir = System.getenv("HOME");
                    String relativePath = input.substring(input.indexOf("~") + 1).trim();
                    File home = new File(homeDir);
                    // Create a new File object for the target directory
                    File targetDir = new File(home, relativePath);
                    if (targetDir.exists() && targetDir.isDirectory()) {
                        // Change the current working directory to the target directory
                        System.setProperty("user.dir", targetDir.getAbsolutePath());
                    } else {
                        System.out.println("cd: " + path + ": No such file or directory");
                }
                }
                // absolute path
                else if (dir.isAbsolute() && dir.exists() && dir.isDirectory()) {
                    System.setProperty("user.dir", dir.getAbsolutePath());
                }
                else if (dir.isAbsolute() && (!dir.exists() || !dir.isDirectory())) {
                    System.out.println("cd: " + path + ": No such file or directory");
                }
                // relative path
                else if (!dir.isAbsolute()) {
                    currentDir = currentDir.resolve(path).normalize();
                    if (currentDir.toFile().exists() && currentDir.toFile().isDirectory()) {
                        System.setProperty("user.dir", currentDir.toFile().getAbsolutePath());
                    } else {
                        System.out.println("cd: " + path + ": No such file or directory");
                    }
                }
            }
            else if (input.equals("exit")) {
                scanner.close();
                break;
            }
            else if (input.contains("echo")) {
                boolean inSingleQuotes = false;
                List<String> argsList = new ArrayList<>();
                StringBuilder currentArg = new StringBuilder();

                String message = input.substring(5);

                for (char c : message.toCharArray()) {
                    if (c == '\'') {
                        // Toggle single quote state (do not append the quote character)
                        inSingleQuotes = !inSingleQuotes;
                    } else if (c == ' ' && !inSingleQuotes) {
                        // Unquoted space marks the end of an argument token
                        if (currentArg.length() > 0) {
                            argsList.add(currentArg.toString());
                            currentArg.setLength(0); // Reset buffer for the next token
                        }
                    } else {
                        // Quoted spaces or regular characters are appended to the current argument
                        currentArg.append(c);
                    }
                }
                if (currentArg.length() > 0) {
                        argsList.add(currentArg.toString());
                }
                System.out.println(String.join(" ", argsList));
            }
            else {
                // Split the input into command and arguments
                String[] programs = input.split(" ");
                String command = programs[0];
                String executablePath = new Main().getExecutablePath(command);
                // If the executable path is found, execute the command using ProcessBuilder
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
