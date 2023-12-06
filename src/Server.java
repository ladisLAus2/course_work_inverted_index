import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server implements Runnable{

    private final ArrayList<ClientHandler> clientsConnections;
    private ServerSocket server;
    private boolean working;
    private ExecutorService threadPool;
    private CompletableFuture<Void> future;
    private FilesReader filesReader;
    private ThreadPool poolForIndex;
    public Server(){
        clientsConnections = new ArrayList<>();
        working = false;
        new Thread(new Runnable() {
            @Override
            public void run() {
                future = CompletableFuture.runAsync(()->{
                    filesReader = new FilesReader();
                    poolForIndex = new ThreadPool(Runtime.getRuntime().availableProcessors(), filesReader.getIndex());
                    filesReader.readFilesFromDirectory("aclImdb");
                    poolForIndex.createInvertedIndexThreadPool(filesReader.getFileList());
                    try {
                        Thread.sleep(5000);
                        System.out.println("ready");
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                });
            }
        }).start();
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
            } catch(IOException e){
                shutdown();
            }

    }

    public void shutdown(){
        try {
            working = true;
            if(!server.isClosed()) {
                server.close();
            }
            for(ClientHandler clientHandler : clientsConnections){
                clientHandler.shutdown();
            }
        } catch (Exception e) {
            //nothing
        }
    }

    class ClientHandler implements Runnable{
        private final Socket client;
        private BufferedReader in;
        private PrintWriter out;

        public ClientHandler(Socket client){
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
                while((text = in.readLine()) != null){
                    if(text.startsWith("/find ")){
                        String [] receivedWord = text.split(" ", 2);
                        if(receivedWord.length == 2){
                            String word = receivedWord[1];
                            if(!future.isDone()){
                                out.println("Зачекайте, будь ласка, йде підготовка.");
                            }
                            future.thenRun(()->{
                                out.println("Ви шукаєте слово \"" + word +"\".");
                                Set<String> result = poolForIndex.searchInvertedIndexThreadPool(word);
                                if(result != null){
                                    if(result.size() == 1){
                                        out.println("Знайдений результат пошуку за словом " + word +" : " + result);
                                    }else {
                                        out.println("Знайдені результати пошуку за словом " + word + " : " + "{" + String.join(", ", result) + "}");

                                    }
                                }else {
                                    out.println("За словом " + word + " не було знайдено нічого.");
                                }
                            });


                        }
                        else{
                            out.println("Слово не було надане.");
                        }
                    } else if (text.startsWith("/exit")) {
                        out.println("Ви ввели команду для припинення зв'язку з сервером");
                        shutdown();
                    } else if (text.startsWith("/help")) {
                        out.println("Для знаходження слова введіть команду /find + слово.\nДля виходу введіть /exit");
                    } else{
                        out.println("Ви ввели: " + text);
                    }
                    out.println(name + ", введіть, будь ласка, команду: ");
                }
            } catch (IOException e) {
                shutdown();
            }
        }

        public void shutdown(){
            try{
                in.close();
                out.close();
                if(!client.isClosed()){
                    client.close();
                }
            }catch (IOException e){
                //nothing
            }
        }
    }

    public static void main(String[] args) {
        Server server = new Server();
        server.run();
    }
}
