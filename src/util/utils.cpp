#include <iostream>
#include <QProcess>
#include <QCoreApplication>
#include <QDir>
#include <sstream>
#include <bitset>
#include <cctype>

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

#ifdef _MSC_VER
  #include <intrin.h>
  #pragma intrinsic(_BitScanForward64)
  inline int bitScanForward(uint64_t bb) {
      // MSVC: returns the index of the least significant 1-bit in bb
      unsigned long idx;
      _BitScanForward64(&idx, bb);
      return static_cast<int>(idx);
  }
#else
  // GCC / Clang have a built-in for exactly this:
  inline int bitScanForward(uint64_t bb) {
      // caller must ensure bb != 0
      return __builtin_ctzll(bb);
  }
#endif

int popLSB(uint64_t& bitboard) {
    const int index = bitScanForward(bitboard);
    bitboard &= bitboard - 1;
    return index;
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

    while (std::getline(iss, line)) {
        if (line.empty())
            continue;

        // Skip any line that contains "info"
        if (line.find("info") != std::string::npos)
            continue;

        // We only care about per-move lines like "e2e4: 20"
        auto pos = line.find(':');
        if (pos == std::string::npos)
            continue;

        std::string move = line.substr(0, pos);
        std::string countStr = line.substr(pos + 1);

        // Trim whitespace around move and count
        auto trim = [](std::string& s) {
            auto first = s.find_first_not_of(" \t\r\n");
            auto last  = s.find_last_not_of(" \t\r\n");
            if (first == std::string::npos) {
                s.clear();
                return;
            }
            s = s.substr(first, last - first + 1);
        };

        trim(move);
        trim(countStr);

        // Validate move format: a-h1-8a-h1-8[optional promotion]
        if (move.size() < 4 || move.size() > 5)
            continue;
        if (move[0] < 'a' || move[0] > 'h') continue;
        if (move[1] < '1' || move[1] > '8') continue;
        if (move[2] < 'a' || move[2] > 'h') continue;
        if (move[3] < '1' || move[3] > '8') continue;
        if (move.size() == 5) {
            char promo = std::tolower(static_cast<unsigned char>(move[4]));
            if (promo != 'q' && promo != 'r' && promo != 'b' && promo != 'n')
                continue;
        }

        // Validate that the count is an integer
        if (countStr.empty())
            continue;
        for (char ch : countStr) {
            if (!std::isdigit(static_cast<unsigned char>(ch)) && ch != '+' && ch != '-')
                return result;
        }

        int count = std::stoi(countStr);
        result[move] = count;
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
