#include <iostream>
#include <QProcess>
#include <QCoreApplication>
#include <QDir>
#include <sstream>
#include <bitset>

#include "utils.h"

std::string bitString(int num) {
    int bits = sizeof(num) * 8;
    std::stringstream ss;

    for (int i = bits - 1; i >= 0; --i) {
        ss << ((num >> i) & 1);
        if (i % 4 == 0) {
            ss << ' ';
        }
    }

    return ss.str();
}

std::string bitboardString(uint64_t bitboard) {
    std::string result;
    std::bitset<64> bits(bitboard);

    for (int i = 0; i < 64; i++) {
        result += bits[i] ? "1 " : "0 ";

        if ((i + 1) % 8 == 0) {
            result += "\n";
        }
    }

    return result;
}


std::string getChessCoords(int index) {
    int startLetterAscii = (index % 8) + 97;
    int startY = 8 - (index / 8);
    return std::string(1, static_cast<char>(startLetterAscii)) + std::to_string(startY);
}

int getIndexFromChessCoordinates(char column, char row) {
    int x = static_cast<int>(column) - 97;
    int y = 8 * (8 - static_cast<int>(row - '0'));

    return y + x;
}

// <--> FOR TALKING TO STOCKFISH <--> //

std::unordered_map<std::string, int> parsePerftResults(const std::string& results) {
    std::unordered_map<std::string, int> result;

    std::istringstream iss(results);
    std::string line;

    // Parse the lines containing moves and counts
    while (std::getline(iss, line) && !line.empty()) {
        size_t pos = line.find(':');
        if (pos != std::string::npos) {
            std::string move = line.substr(0, pos);
            int count = std::stoi(line.substr(pos + 1));
            result[move] = count;
        }
    }

    return result;
}

std::unordered_map<std::string, int> stockFishPerft(const std::string& fenString, int depth) {
    // Set the path to your Stockfish executable
    QDir dir(QCoreApplication::applicationDirPath());;

    // Go up to Chess/bin
    dir.cdUp();
    // Go up to Chess/
    dir.cdUp();
    // Now go into resources/stockfish
    dir.cd("resources");
    dir.cd("stockfish");

    QString stockfishPath = dir.absoluteFilePath("stockfish-windows-x86-64-avx2.exe");

    std::unordered_map<std::string, int> results;

    QProcess stockfishProcess;
    stockfishProcess.start(stockfishPath);

    if (!stockfishProcess.waitForStarted()) {
        std::cout << "Error starting Stockfish process" << std::endl;
        return results;
    }

    // Set the position and depth
    QString command = QString("position fen %1\n").arg(fenString.c_str());
    command += QString("go perft %1\n").arg(depth);

    // Write the command to Stockfish
    stockfishProcess.write(command.toUtf8());
    stockfishProcess.waitForBytesWritten();

    // Wait for Stockfish to process the command
    if (!stockfishProcess.waitForReadyRead()) {
        std::cout << "Error waiting for Stockfish to process command" << std::endl;
        return results;
    }

    // Read and display the output from Stockfish
    std::string fishOutput;
    while (stockfishProcess.waitForReadyRead()) {
        QByteArray output = stockfishProcess.readAllStandardOutput();
        fishOutput += output.toStdString();

        if (output.contains("Nodes")) {
            break;
        }
    }

    // Close the Stockfish process
    stockfishProcess.closeWriteChannel();
    stockfishProcess.waitForFinished();

    return parsePerftResults(fishOutput);
}

// <--> FOR TALKING TO STOCKFISH <--> //