package utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.HashMap;

/**
 * Class that can run commands on StockFish. Is used for go perft.
 */
public class StockFish {
    public static HashMap<String, Integer> goPerft(String fenString, int depth) throws IOException, InterruptedException {
        // Start Stockfish process
        ProcessBuilder processBuilder = new ProcessBuilder("src/utils/stockfish/stockfish-windows-x86-64-avx2.exe");
        processBuilder.redirectErrorStream(true);
        Process stockfish = processBuilder.start();

        // Open communication streams
        BufferedReader stockfishInput = new BufferedReader(new InputStreamReader(stockfish.getInputStream()));
        OutputStream stockfishOutput = stockfish.getOutputStream();

        // Send commands to Stockfish
        sendCommand(stockfishOutput, "position fen " + fenString + "\n");
        sendCommand(stockfishOutput, "go perft " + depth + "\n");

        // Read Stockfish's output
        HashMap<String, Integer> results = new HashMap<>();
        String line;
        while ((line = stockfishInput.readLine()) != null) {
            if (line.contains("Stockfish") || line.equals("")) {
                continue;
            }

            String[] lineSplit = line.split(" ");

            if (line.contains("Nodes")) {
                results.put(lineSplit[0] + " " + lineSplit[1].split(":")[0], Integer.parseInt(lineSplit[2]));
                break;
            }
            results.put(lineSplit[0].split(":")[0], Integer.parseInt(lineSplit[1]));
        }

        // Close the streams
        stockfishInput.close();
        stockfishOutput.close();

        // Wait for Stockfish to finish
        stockfish.waitFor();
        return results;
    }

    private static void sendCommand(OutputStream output, String command) throws IOException {
        output.write(command.getBytes());
        output.flush();
    }
}
