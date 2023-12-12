import components.FilesReader;
import components.ThreadPool;
import components.concurrentMap.BuiltInConcurrentHashMapUse;
import components.concurrentMap.ConcurrentHashMapInterface;
import components.concurrentMap.CustomConcurrentHashMapUse;
import components.InvertedIndex;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server implements Runnable {

    private final ArrayList<ClientHandler> clientsConnections;
    private ServerSocket server;
    private boolean working;
    private ExecutorService threadPool;
    private CompletableFuture<Void> future;
    private FilesReader filesReader;
    private InvertedIndex index;
    private ThreadPool poolForIndex;
    private final int port = 1234;

    public Server() {
        clientsConnections = new ArrayList<>();
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        int selectedMode, selectedImplementation;
        do {
            System.out.println("What implementation would you like to use (1 - Built-in ConcurrentHashMap; 2 - Custom ConcurrentHashMap):");
            while (!scanner.hasNextInt()) {
                System.out.println("Given value is not a number. Try again.");
                scanner.next();
            }
            selectedImplementation = scanner.nextInt();
            if (selectedImplementation != 1 && selectedImplementation != 2) {
                System.out.println("Enter either 1 or 2.");
            }
        } while (selectedImplementation != 1 && selectedImplementation != 2);
        do {
            System.out.println("Enter work mode (1 - Work mode; 2 - Mode to calculate time building inverted index with different amount of cores):");
            while (!scanner.hasNextInt()) {
                System.out.println("Given value is not a number. Try again");
                scanner.next();
            }
            selectedMode = scanner.nextInt();
            if (selectedMode != 1 && selectedMode != 2) {
                System.out.println("Enter either 1 or 2.");
            }
        } while (selectedMode != 1 && selectedMode != 2);
        Server server = new Server();

        if (selectedMode == 1) {
            System.out.println("You have chosen WORK MODE. Creating inverted index...");
            if (selectedImplementation == 1) {
                server.buildIndexOneTime(16, new BuiltInConcurrentHashMapUse<>());
            } else {
                server.buildIndexOneTime(16, new CustomConcurrentHashMapUse<>());
            }
            server.run();
        } else {
            System.out.println("You have chosen the second mode - Mode to calculate time building inverted index with different amount of cores.");
            List<Integer> cores = new ArrayList<>(Arrays.asList(1, 2, 4, 8, 12, 16, 24, 32, 48, 96));
            if (selectedImplementation == 1) {
                server.buildAndTestIndexMultipleNumbersOfThreads(cores, BuiltInConcurrentHashMapUse.class);
            } else {
                server.buildAndTestIndexMultipleNumbersOfThreads(cores, CustomConcurrentHashMapUse.class);
            }
        }

    }

    public void buildIndexOneTime(int cores, ConcurrentHashMapInterface<String, Set<String>> map) {
        new Thread(() -> future = CompletableFuture.runAsync(() -> {
            index = new InvertedIndex(map);
            filesReader = new FilesReader();
            poolForIndex = new ThreadPool(cores, index);
            filesReader.readFilesFromDirectory("aclImdb");
            poolForIndex.createInvertedIndexThreadPool(filesReader.getFiles());
//            try {
//                Thread.sleep(30000);
//                System.out.println("ready");
//            } catch (InterruptedException e) {
//                throw new RuntimeException(e);
//            }
            System.out.println("Index has been built.");
        })).start();
    }

    public void buildAndTestIndexMultipleNumbersOfThreads(List<Integer> cores, Class mapClass) {
        for (int i = 0; i < cores.size(); i++) {
            ConcurrentHashMapInterface<String, Set<String>> map;
            try {
                map = (ConcurrentHashMapInterface<String, Set<String>>) mapClass.newInstance();
            } catch (InstantiationException e) {
                throw new RuntimeException(e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
            index = new InvertedIndex(map);
            filesReader = new FilesReader();
            poolForIndex = new ThreadPool(cores.get(i), index);
            filesReader.readFilesFromDirectory("aclImdb");
            long time = poolForIndex.createInvertedIndexThreadPool(filesReader.getFiles());
            System.out.println("Result: " + cores.get(i) + " cores - " + time);
        }
    }

    @Override
    public void run() {
        try (Scanner scanner = new Scanner(System.in)) {
            server = new ServerSocket(port);
            threadPool = Executors.newCachedThreadPool();
            new Thread(() -> {
                while (scanner.hasNextLine()) {
                    String line = scanner.nextLine();
                    if (line.equals("/shutdown")) {
                        System.out.println("You have entered command to shutdown the server. Server is being closed...");
                        shutdown();
                        break;
                    } else {
                        System.out.println("Unknown command " + line + ". Enter another command. ");
                    }
                }
            }).start();
            while (!working) {
                Socket client = server.accept();
                ClientHandler clientHandler = new ClientHandler(client);
                clientsConnections.add(clientHandler);
                threadPool.execute(clientHandler);
            }
        } catch (IOException e) {
            shutdown();
        }

    }

    public void shutdown() {
        try {
            working = true;
            if (!server.isClosed()) {
                server.close();
            }
            for (ClientHandler clientHandler : clientsConnections) {
                clientHandler.shutdown();
            }
            threadPool.shutdown();
            poolForIndex.shutdown();
        } catch (Exception e) {
            //nothing
        }
    }

    class ClientHandler implements Runnable {
        private final Socket client;
        private BufferedReader in;
        private PrintWriter out;

        public ClientHandler(Socket client) {
            this.client = client;
        }

        @Override
        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                out = new PrintWriter(client.getOutputStream(), true);
                out.println("Greetings, what's your name?");
                String name = in.readLine();
                String text;

                out.println(name + ", enter, please, command ('/help' for help)");
                while ((text = in.readLine()) != null) {
                    if (text.startsWith("/find ")) {
                        String[] receivedWord = text.split(" ", 2);
                        if (receivedWord.length == 2) {
                            String word = receivedWord[1];
                            if (!future.isDone()) {
                                out.println("Wait, please, preparations are underway.");
                            }
                            future.join();
                            out.println("You are looking for a word \"" + word + "\".");
                            Set<String> result = poolForIndex.searchInvertedIndexThreadPool(word);
                            if (result != null) {
                                if (result.size() == 1) {
                                    out.println("Search result for a word " + word + " : " + result);
                                } else {
                                    out.println("Found " + result.size() + " results for a word " + word + " : " + "{" + String.join(", ", result) + "}");

                                }
                            } else {
                                out.println("For a word " + word + " nothing was found.");
                            }
                        } else {
                            out.println("A word was not given.");
                        }
                    } else if (text.startsWith("/exit")) {
                        out.println("You entered a command to terminate communication with the server");
                        shutdown();
                    } else if (text.startsWith("/help")) {
                        out.println("To find a word, enter the command /find + word.\nTo exit, enter /exit");
                    } else {
                        out.println("You entered: " + text);
                    }
                    out.println(name + ", please enter the following command: ");
                }
            } catch (IOException e) {
                shutdown();
            }
        }

        public void shutdown() {

            try {
                out.println("/shutdown");
                in.close();
                out.close();
                if (!client.isClosed()) {
                    client.close();
                }
            } catch (IOException e) {
                //nothing
            }
        }
    }
}
