package builtIn;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
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

    private ThreadPool poolForIndex;


    public Server() {
        clientsConnections = new ArrayList<>();
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        int userInput;
        do {
            System.out.println("Введіть режим роботи (1 - Робочий режим; 2 - режим для підрахунку часу виконання різною кількістю потоків):");
            while (!scanner.hasNextInt()) {
                System.out.println("Отримане значення не є числом. Спробуйте знову");
                scanner.next();
            }
            userInput = scanner.nextInt();
            if (userInput != 1 && userInput != 2) {
                System.out.println("Введіть або 1 або 2.");
            }
        } while (userInput != 1 && userInput != 2);
        scanner.close();
        Server server = new Server();
        if (userInput == 1) {
            System.out.println("Ви вибрали Робочий режим. Створюю індекс...");
            server.buildIndexOneTime(16);
            server.run();
        } else {
            System.out.println("Ви вибрали 2 режим - режим для тестування часу виконання побудови різною кількістю потоків.");
            List<Integer> cores = new ArrayList<>(Arrays.asList(1, 2, 4, 8, 12, 16, 24, 32, 48, 96));
            server.buildAndTestIndexMultipleNumbersOfThreads(cores);
        }


    }

    public void buildIndexOneTime(int cores) {
        working = false;
        new Thread(new Runnable() {
            @Override
            public void run() {
                future = CompletableFuture.runAsync(() -> {
                    filesReader = new FilesReader();
                    poolForIndex = new ThreadPool(cores, filesReader.getIndex());
                    filesReader.readFilesFromDirectory("aclImdb");
                    long time = poolForIndex.createInvertedIndexThreadPool(filesReader.getFiles());
//                    try {
//                        Thread.sleep(15000);
//                        System.out.println("ready");
//                    } catch (InterruptedException e) {
//                        throw new RuntimeException(e);
//                    }

                    System.out.println("Індекс створено.");
                });
            }
        }).start();
    }

    public void buildAndTestIndexMultipleNumbersOfThreads(List<Integer> cores) {
        for (int i = 0; i < cores.size(); i++) {
            FilesReader filesReader = new FilesReader();
            ThreadPool poolForIndex = new ThreadPool(cores.get(i), filesReader.getIndex());
            filesReader.readFilesFromDirectory("aclImdb");
            long time = poolForIndex.createInvertedIndexThreadPool(filesReader.getFiles());

            if (cores.size() != 1) {
                System.out.println("Результат: " + cores.get(i) + " ядер дорівнює - " + time);
            }

        }
    }

    @Override
    public void run() {
        try {
            server = new ServerSocket(1234);
            threadPool = Executors.newCachedThreadPool();
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
                out.println("Вітаю, як я можу до вас звертатися?");
                String name = in.readLine();
                String text;

                out.println(name + ", введіть, будь ласка, команду ('/help' для довідки)");
                while ((text = in.readLine()) != null) {
                    if (text.startsWith("/find ")) {
                        String[] receivedWord = text.split(" ", 2);
                        if (receivedWord.length == 2) {
                            String word = receivedWord[1];
                            if (!future.isDone()) {
                                out.println("Зачекайте, будь ласка, йде підготовка.");
                            }
                            future.join();
                            out.println("Ви шукаєте слово \"" + word + "\".");
                            Set<String> result = poolForIndex.searchInvertedIndexThreadPool(word);
                            if (result != null) {
                                if (result.size() == 1) {
                                    out.println("Знайдений результат пошуку за словом " + word + " : " + result);
                                } else {
                                    out.println("Знайденo " + result.size()+" результатів за словом " + word + " : " + "{" + String.join(", ", result) + "}");

                                }
                            } else {
                                out.println("За словом " + word + " не було знайдено нічого.");
                            }
                        } else {
                            out.println("Слово не було надане.");
                        }
                    } else if (text.startsWith("/exit")) {
                        out.println("Ви ввели команду для припинення зв'язку з сервером");
                        shutdown();
                    } else if (text.startsWith("/help")) {
                        out.println("Для знаходження слова введіть команду /find + слово.\nДля виходу введіть /exit");
                    } else {
                        out.println("Ви ввели: " + text);
                    }
                    out.println(name + ", введіть, будь ласка, наступну команду: ");
                }
            } catch (IOException e) {
                shutdown();
            }
        }

        public void shutdown() {
            try {
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
