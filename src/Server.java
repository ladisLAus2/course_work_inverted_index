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
            System.out.println("Яку імплементацію ви бажаєте використати (1 - На built-in ConcurrentHashMap; 2 - На власній реалізації ConcurrentHashMap):");
            while (!scanner.hasNextInt()) {
                System.out.println("Отримане значення не є числом. Спробуйте знову");
                scanner.next();
            }
            selectedImplementation = scanner.nextInt();
            if (selectedImplementation != 1 && selectedImplementation != 2) {
                System.out.println("Введіть або 1 або 2.");
            }
        } while (selectedImplementation != 1 && selectedImplementation != 2);
        do {
            System.out.println("Введіть режим роботи (1 - Робочий режим; 2 - режим для підрахунку часу виконання різною кількістю потоків):");
            while (!scanner.hasNextInt()) {
                System.out.println("Отримане значення не є числом. Спробуйте знову");
                scanner.next();
            }
            selectedMode = scanner.nextInt();
            if (selectedMode != 1 && selectedMode != 2) {
                System.out.println("Введіть або 1 або 2.");
            }
        } while (selectedMode != 1 && selectedMode != 2);
        Server server = new Server();

        if (selectedMode == 1) {
            System.out.println("Ви вибрали Робочий режим. Створюю індекс...");
            if (selectedImplementation == 1) {
                server.buildIndexOneTime(16, new BuiltInConcurrentHashMapUse<String, Set<String>>());
            } else {
                server.buildIndexOneTime(16, new CustomConcurrentHashMapUse<String, Set<String>>());
            }
            server.run();
        } else {
            System.out.println("Ви вибрали 2 режим - режим для тестування часу виконання побудови різною кількістю потоків.");
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
//                    try {
//                        Thread.sleep(15000);
//                        System.out.println("ready");
//                    } catch (InterruptedException e) {
//                        throw new RuntimeException(e);
//                    }

            System.out.println("Індекс створено.");
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
            System.out.println("Результат: " + cores.get(i) + " ядер дорівнює - " + time);
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
                        System.out.println("Ви ввели команду для закриття серверу. Сервер закривається...");
                        shutdown();
                        break;
                    } else {
                        System.out.println("Невідома команда " + line + ". Введіть іншу команду. ");
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
                                    out.println("Знайденo " + result.size() + " результатів за словом " + word + " : " + "{" + String.join(", ", result) + "}");

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
