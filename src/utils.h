#pragma once

#include <string>
#include <unordered_map>

std::string bitString(int num);

std::string bitboardString(uint64_t bitboard);

std::string getChessCoords(int index);

int getIndexFromChessCoordinates(char column, char row);

std::unordered_map<std::string, int> parsePerftResults(const std::string& results);

std::unordered_map<std::string, int> stockFishPerft(const std::string& fenString, int depth);
