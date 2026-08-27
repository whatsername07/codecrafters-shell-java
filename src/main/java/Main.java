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

            List<String> commandTokens = parseTokens(input);
            if (commandTokens.isEmpty()) {
                continue; // Skip empty input
            }

            String command = commandTokens.get(0);
            List<String> arguments = commandTokens.subList(1, commandTokens.size());

            if (command.equals("type")) {
                String typeInput = arguments.isEmpty() ? "" : arguments.get(0);
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
            else if (command.equals("pwd")) {
                String currentDir = System.getProperty("user.dir");
                System.out.println(currentDir);
            }
            else if (command.equals("cd")) {
                // Get the current working directory, and the path to change to
                Path currentDir = new File(System.getProperty("user.dir")).toPath();
                String path = arguments.isEmpty() ? "" : arguments.get(0);
                File dir = new File(path);

                // tilde
                if (path.endsWith("~")) {
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
                else if (path.contains("~")) {
                    // Get the user's home directory from the environment variable
                    String homeDir = System.getenv("HOME");
                    String relativePath = path.substring(path.indexOf("~") + 1).trim();
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
            else if (command.equals("exit")) {
                scanner.close();
                break;
            }
            else if (command.equals("echo")) {
                System.out.println(String.join(" ", arguments));
            }
            else {
                // Split the input into command and arguments
                List<String> tokens = parseTokens(input);
                String commandToken = tokens.get(0);
                String executablePath = new Main().getExecutablePath(commandToken);
                // If the executable path is found, execute the command using ProcessBuilder
                if (executablePath != null) {
                    ProcessBuilder processBuilder = new ProcessBuilder(tokens);
                    processBuilder.inheritIO();
                    Process process = processBuilder.start();
                    process.waitFor();
                } else {
                    System.out.println(commandToken + ": command not found");
                }
            }
        }
    }

    private static List<String> parseTokens(String input) {
        List<String> tokens = new ArrayList<>();
        StringBuilder currentToken = new StringBuilder();
        boolean inSingleQuotes = false;
        boolean inDoubleQuotes = false;
        boolean escaped = false;

        for (char c : input.toCharArray()) {
            if (escaped) {
                currentToken.append(c);
                escaped = !escaped;
                continue;
            }
            else if (c == '\\' && !inSingleQuotes && !inDoubleQuotes) {
                escaped = !escaped;
                continue;
            }
            if (c == '"' && !inSingleQuotes) {
             inDoubleQuotes = !inDoubleQuotes;
            }
            else if (inDoubleQuotes) {
                currentToken.append(c);
            }
            else if (c == '\'' && !inDoubleQuotes) {
                inSingleQuotes = !inSingleQuotes;
            } else if (c == ' ' && !inSingleQuotes && !inDoubleQuotes) {
                if (currentToken.length() > 0) {
                    tokens.add(currentToken.toString());
                    currentToken.setLength(0);
                }
            } else {
                currentToken.append(c);
            }
        }

        if (currentToken.length() > 0) {
            tokens.add(currentToken.toString());
        }

        return tokens;
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
