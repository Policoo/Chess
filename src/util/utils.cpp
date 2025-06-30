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

std::string trim(const std::string& s) {
    size_t start = s.find_first_not_of(" \t\r\n");
    size_t end = s.find_last_not_of(" \t\r\n");
    if (start == std::string::npos)
        return "";
    return s.substr(start, end - start + 1);
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
            std::string move = trim(line.substr(0, pos));
            std::string countStr = trim(line.substr(pos + 1));
            int count = std::stoi(countStr);
            result[move] = count;
        }
    }

    return result;
}

std::unordered_map<std::string, int> stockFishPerft(const std::string& fenString, int depth) {
    // Set the path to your Stockfish executable
    QDir dir(QCoreApplication::applicationDirPath());

    // Go up to Chess/bin
    dir.cdUp();
    // Go up to Chess/
    dir.cdUp();
    // Now go into resources
    dir.cd("resources");

    QString stockfishPath;
    
    // Detect operating system and set appropriate Stockfish path
#ifdef Q_OS_WIN
    dir.cd("stockfish_windows");
    stockfishPath = dir.absoluteFilePath("stockfish-windows-x86-64-avx2.exe");
#elif defined(Q_OS_MAC)
    dir.cd("stockfish_mac");
    stockfishPath = dir.absoluteFilePath("stockfish-macos-m1-apple-silicon");
#elif defined(Q_OS_LINUX)
    dir.cd("stockfish_linux");
    stockfishPath = dir.absoluteFilePath("stockfish-ubuntu-x86-64-avx2");
#endif

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

    // Close the write channel and wait for Stockfish to finish
    stockfishProcess.closeWriteChannel();
    if (!stockfishProcess.waitForFinished()) {
        std::cout << "Error waiting for Stockfish to process command" << std::endl;
        return results;
    }

    // Read the entire output from Stockfish
    QByteArray output = stockfishProcess.readAllStandardOutput();
    std::string fishOutput = output.toStdString();

    return parsePerftResults(fishOutput);
}

// <--> FOR TALKING TO STOCKFISH <--> //
