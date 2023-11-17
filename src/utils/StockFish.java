package utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;

/**
 * Class that can run commands on StockFish. Is used for go perft.
 */
public class StockFish {
    public static String goPerft(int depth, String fenString) throws IOException, InterruptedException {
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
        StringBuilder results = new StringBuilder();
        String line;
        while ((line = stockfishInput.readLine()) != null) {
            if (line.contains("Stockfish") || line.equals("")) {
                continue;
            }

            if (line.contains("Nodes searched")) {
                break;
            }

            results.append(line).append("~");
        }

        // Close the streams
        stockfishInput.close();
        stockfishOutput.close();

        // Wait for Stockfish to finish
        stockfish.waitFor();
        return results.toString();
    }

    private static void sendCommand(OutputStream output, String command) throws IOException {
        output.write(command.getBytes());
        output.flush();
    }
}
