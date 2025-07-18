#include "board.h"

#include <bit>

#include "moveGenerator.h"
#include "piece.h"
#include "util/precomputedGameData.h"
#include "util/utils.h"
#include "util/zobrist.h"

#include <iostream>

// <--> INITIALIZING BOARD <--> //

Board::Board(const std::string& fenString) :
    castleRights(0),
    gameOver(false) {
    positionHistory.reserve(100);
    boardState.reserve(100);

    for (int i = 0; i < 64; i++) {
        tile[i] = 0;
    }

    if (fenString.empty()) {
        startPos();
    }
    else {
        makeBoardFromFen(fenString);
    }

    initializePiecePositions();
    updatePiecePositionsColor();

    updateAttackedTiles();
    updatePins();

    determineCheckLine();
    hashPosition();
}

void Board::startPos() {
    currentMove = 2;
    lastCaptOrPawnAdv = 2;
    enPassant = 0;
    turn = Piece::WHITE;
    castleRights = 0b1111;

    tile[0] = Piece::create(Piece::ROOK,   Piece::BLACK);
    tile[1] = Piece::create(Piece::KNIGHT, Piece::BLACK);
    tile[2] = Piece::create(Piece::BISHOP, Piece::BLACK);
    tile[3] = Piece::create(Piece::QUEEN,  Piece::BLACK);
    tile[4] = Piece::create(Piece::KING,   Piece::BLACK);
    tile[5] = Piece::create(Piece::BISHOP, Piece::BLACK);
    tile[6] = Piece::create(Piece::KNIGHT, Piece::BLACK);
    tile[7] = Piece::create(Piece::ROOK,   Piece::BLACK);

    for (int index = 8; index < 16; index++) {
        tile[index] = Piece::create(Piece::PAWN, Piece::BLACK);
    }

    for (int index = 48; index < 56; index++) {
        tile[index] = Piece::create(Piece::PAWN, Piece::WHITE);
    }

    tile[56] = Piece::create(Piece::ROOK,   Piece::WHITE);
    tile[57] = Piece::create(Piece::KNIGHT, Piece::WHITE);
    tile[58] = Piece::create(Piece::BISHOP, Piece::WHITE);
    tile[59] = Piece::create(Piece::QUEEN,  Piece::WHITE);
    tile[60] = Piece::create(Piece::KING,   Piece::WHITE);
    tile[61] = Piece::create(Piece::BISHOP, Piece::WHITE);
    tile[62] = Piece::create(Piece::KNIGHT, Piece::WHITE);
    tile[63] = Piece::create(Piece::ROOK,   Piece::WHITE);
}

void Board::makeBoardFromFen(const std::string& fenString) {
    int index = 0;
    int fenIndex;

    for (fenIndex = 0; fenIndex < fenString.size(); fenIndex++) {
        char c = fenString[fenIndex];
        if (c == ' ') {
            break;
        }

        if (c == '/') {
            continue;
        }

        if (isdigit(c)) {
            index += static_cast<int>(c - '0');
            continue;
        }

        const int color = (isupper(c)) ? Piece::WHITE : Piece::BLACK;
        c = static_cast<char>(tolower(c));
        switch (c) {
            case 'k':
                tile[index] = Piece::create(Piece::KING, color);
                break;
            case 'q':
                tile[index] = Piece::create(Piece::QUEEN, color);
                break;
            case 'r':
                tile[index] = Piece::create(Piece::ROOK, color);
                break;
            case 'b':
                tile[index] = Piece::create(Piece::BISHOP, color);
                break;
            case 'n':
                tile[index] = Piece::create(Piece::KNIGHT, color);
                break;
            case 'p':
                tile[index] = Piece::create(Piece::PAWN, color);
                break;
        }
        index++;
    }

    //go past the space, at color to move
    fenIndex++;
    turn = (fenString[fenIndex] == 'w') ? Piece::WHITE : Piece::BLACK;

    //castle rights
    if (fenString[fenIndex + 2] != '-') {
        const int upperBound = fenIndex + 6;
        for (fenIndex = fenIndex + 2; fenIndex < upperBound; fenIndex++) {
            const char c = fenString[fenIndex];
            if (c == 'K') {
                castleRights |= 0b0001;
                continue;
            }
            if (c == 'Q') {
                castleRights |= 0b0010;
                continue;
            }
            if (c == 'k') {
                castleRights |= 0b0100;
                continue;
            }
            if (c == 'q') {
                castleRights |= 0b1000;
            }
            if (c == ' ') {
                break;
            }
        }
    }
    else {
        fenIndex += 3;
    }

    //en passant
    fenIndex += 1;
    if (fenString[fenIndex] != '-') {
        int enPassantCoord = getIndexFromChessCoordinates(fenString[fenIndex], fenString[fenIndex + 1]);
        enPassant = (turn == Piece::WHITE) ? enPassantCoord + 8 : enPassantCoord - 8;
        fenIndex += 3;
    }
    else {
        enPassant = 0;
        fenIndex += 2;
    }

    const int movesSinceCapt = static_cast<int>(fenString[fenIndex] - '0');
    currentMove = std::stoi(fenString.substr(fenIndex + 2, 2));
    lastCaptOrPawnAdv = currentMove - movesSinceCapt;
}

void Board::initializePiecePositions() {
    piecePositions.fill(0LL);

    for (int index = 0; index < 64; index++) {
        if (tile[index] == 0) {
            continue;
        }

        piecePositions[tile[index]] |= (1ULL << index);
    }
}

void Board::hashPosition() {
    hash = 0;

    for (int index = 0; index < 64; index++) {
        if (tile[index] == 0) {
            continue;
        }

        hash ^= Zobrist::getKey(tile[index], index);
    }

    hash ^= Zobrist::getColorKey(turn);
    hash ^= Zobrist::getCastleKey(castleRights);
    hash ^= Zobrist::getEnPassantKey(enPassant);
}

// <--> INITIALIZING BOARD <--> //

// <--> MAKING MOVES <--> //

void Board::makeMove(const Move& move, const bool skipGameOverCheck) {
    const int start = move.start();
    const int end = move.end();
    const Flag flag = move.flag();

    //save current position
    boardState.push_back(
        {
            tile[end],
            castleRights,
            enPassant,
            lastCaptOrPawnAdv
        }
    );

    //remove start piece from the hash
    hash ^= Zobrist::getKey(tile[start], start);

    //move the piece
    tile[end] = tile[start];
    tile[start] = 0;

    //update piece position
    piecePositions[tile[end]] &= ~(1LL << start);
    piecePositions[tile[end]] |= (1LL << end);

    switch (flag) {
        case Flag::QUIET: {
            updateGameState(start, end, skipGameOverCheck);
            if (isPawn(end)) {
                lastCaptOrPawnAdv = currentMove;
            }

            return;
        }
        case Flag::PROMO_N:
        case Flag::PROMO_B:
        case Flag::PROMO_R:
        case Flag::PROMO_Q: {
            //remove old pawn
            piecePositions[tile[end]] &= ~(1LL << end);

            int promotion = Piece::create(flag - 1, Piece::color(tile[end]));

            //create promoted piece
            tile[end] = promotion;
            piecePositions[tile[end]] |= (1LL << end);

            lastCaptOrPawnAdv = currentMove;
        }
        case Flag::CAPTURE: {
            //check this to make sure. Promos also get here and not all are captures
            int piece = boardState[boardState.size() - 1].targetTile;
            if (piece == 0) {
                updateGameState(start, end, skipGameOverCheck);
                return;
            }

            //remove captured piece from hash
            hash ^= Zobrist::getKey(piece, end);

            //remove captured piece from positions
            piecePositions[boardState[boardState.size() - 1].targetTile] &= ~(1LL << end);

            lastCaptOrPawnAdv = currentMove;
            updateGameState(start, end, skipGameOverCheck);
            return;
        }
        case Flag::EN_PASSANT: {
            piecePositions[tile[enPassant]] &= ~(1LL << enPassant);

            //capture enemy pawn
            hash ^= Zobrist::getKey(tile[enPassant], enPassant);
            tile[enPassant] = 0;

            lastCaptOrPawnAdv = currentMove;
            updateGameState(start, end, skipGameOverCheck);
            return;
        }
        case Flag::DBL_PUSH: {
            //record en passant key before we record it in updateGameState
            hash ^= Zobrist::getEnPassantKey(end);
            lastCaptOrPawnAdv = currentMove;

            updateGameState(start, end, skipGameOverCheck);

            //it's important to do this after updating game state, cause otherwise it resets to 0
            enPassant = end;

            return;
        }
        case Flag::CASTLE_K:
        case Flag::CASTLE_Q: {
            //figure out if we are moving left or right
            int rookStart, rookEnd;
            if (start - end < 0) {
                rookStart = start + 3;
                rookEnd = start + 1;
            }
            else {
                rookStart = start - 4;
                rookEnd = start - 1;
            }

            //move the rook, while handling the hash and piecePositions
            hash ^= Zobrist::getKey(tile[rookStart], rookStart);
            tile[rookEnd] = tile[rookStart];
            tile[rookStart] = 0;

            //update rook position
            piecePositions[tile[rookEnd]] &= ~(1LL << rookStart);
            piecePositions[tile[rookEnd]] |= (1LL << rookEnd);

            hash ^= Zobrist::getKey(tile[rookEnd], rookEnd);

            updateAttackedTiles();
            updateGameState(start, end, skipGameOverCheck);
            return;
        }
    }
}

void Board::updateGameState(const int start, const int end, const bool skipGameOverCheck) {
    //update the hash with the moved piece
    hash ^= Zobrist::getKey(tile[end], end);

    //undo castle rights from hash
    hash ^= Zobrist::getCastleKey(castleRights);

    //update castling rights; if that those squares see any action, no castling anymore
    castleRights &= PGD::castleMask[start] & PGD::castleMask[end];

    //put new castleRights into hash
    hash ^= Zobrist::getCastleKey(castleRights);

    //if en passant was possible this move, make sure it's not possible next move
    if (enPassant > 0) {
        hash ^= Zobrist::getEnPassantKey(enPassant);
        enPassant = 0;
    }

    //update turn, make sure hash is accurate
    hash ^= Zobrist::getColorKey(turn);
    turn = (turn == Piece::WHITE) ? Piece::BLACK : Piece::WHITE;
    hash ^= Zobrist::getColorKey(turn);

    if (const auto it = positionHistory.find(hash); it != positionHistory.end()) {
        it->second++;
    }
    else {
        positionHistory[hash] = 1;
    }

    currentMove++;
    updatePiecePositionsColor();
    updateAttackedTiles();
    updatePins();
    determineCheckLine();
    if (!skipGameOverCheck)
        checkGameOver();
}

void Board::undoMove(const Move& move) {
    const int start = move.start();
    const int end = move.end();
    const Flag flag = move.flag();

    //get board state to be restored and delete it from the list
    const BoardState state = boardState.back();
    boardState.pop_back();

    //remove the current hash before we start changing it to past state
    positionHistory[hash]--;
    if (positionHistory[hash] < 1) {
        positionHistory.erase(hash);
    }

    //undo the moved piece's hash
    hash ^= Zobrist::getKey(tile[end], end);

    //move the piece back
    tile[start] = tile[end];
    tile[end] = 0;

    //update piece position
    piecePositions[tile[start]] &= ~(1LL << end);
    piecePositions[tile[start]] |= (1LL << start);

    switch (flag) {
        case Flag::QUIET: {
            revertGameStats(start, end, state);
            return;
        }
        case Flag::PROMO_N:
        case Flag::PROMO_B:
        case Flag::PROMO_R:
        case Flag::PROMO_Q: {
            //remove promoted piece
            piecePositions[tile[start]] &= ~(1LL << start);

            //make piece back into pawn
            const int color = Piece::color(tile[start]);
            tile[start] = Piece::create(Piece::PAWN, color);

            //put pawn back
            piecePositions[tile[start]] |= (1LL << start);
        }
        case Flag::CAPTURE: {
            const int targetTile = state.targetTile;
            if (targetTile == 0) {
                revertGameStats(start, end, state);
                return;
            }
            tile[end] = targetTile;
            piecePositions[tile[end]] |= (1LL << end);

            //update the hash with the captured piece
            hash ^= Zobrist::getKey(tile[end], end);

            revertGameStats(start, end, state);
            return;
        }
        case Flag::EN_PASSANT: {
            //create pawn and put him back
            const int pastEnPassant = state.enPassant;

            //create the pawn
            int piece = Piece::create(Piece::PAWN, turn);
            tile[pastEnPassant] = piece;

            piecePositions[tile[pastEnPassant]] |= (1LL << pastEnPassant);

            hash ^= Zobrist::getKey(tile[pastEnPassant], pastEnPassant);
            revertGameStats(start, end, state);
            return;
        }
        case Flag::DBL_PUSH: {
            revertGameStats(start, end, state);
            return;
        }
        case Flag::CASTLE_K:
        case Flag::CASTLE_Q: {
            //figure out if we are moving left or right
            int rookStart, rookEnd;
            if (start - end < 0) {
                rookStart = start + 3;
                rookEnd = start + 1;
            }
            else {
                rookStart = start - 4;
                rookEnd = start - 1;
            }

            //move the rook, while handling the hash and piecePositions
            hash ^= Zobrist::getKey(tile[rookEnd], rookEnd);
            tile[rookStart] = tile[rookEnd];
            tile[rookEnd] = 0;
            hash ^= Zobrist::getKey(tile[rookStart], rookStart);

            piecePositions[tile[rookStart]] &= ~(1LL << rookEnd);
            piecePositions[tile[rookStart]] |= (1LL << rookStart);

            revertGameStats(start, end, state);
            return;
        }
    }
}

void Board::revertGameStats(const int start, const int end, const Board::BoardState& state) {
    //update hash with reverted piece
    hash ^= Zobrist::getKey(tile[start], start);

    //revert castle rights
    hash ^= Zobrist::getCastleKey(castleRights);
    castleRights = state.castleRights;
    hash ^= Zobrist::getCastleKey(castleRights);

    //revert turn, make sure hash is accurate
    hash ^= Zobrist::getColorKey(turn);
    turn = (turn == Piece::WHITE) ? Piece::BLACK : Piece::WHITE;
    hash ^= Zobrist::getColorKey(turn);

    lastCaptOrPawnAdv = state.lastCaptOrPawnAdv;

    //restore en passant square
    hash ^= Zobrist::getEnPassantKey(enPassant);
    enPassant = state.enPassant;
    hash ^= Zobrist::getEnPassantKey(enPassant);

    gameOver = false;
    currentMove--;
    updatePiecePositionsColor();
    updateAttackedTiles();
    updatePins();
    determineCheckLine();
}

// <--> MAKING MOVES <--> //

// <--> KEEPING TRACK OF ATTACKED TILES, PINS AND CHECKS <--> //

void Board::updateAttackedTiles() {
    attackedTiles.fill(0);
    attackMap.fill(0);
    const uint64_t blockerBitboard = piecePositionsColor[Piece::WHITE] | piecePositionsColor[Piece::BLACK];
    int colors[] = { Piece::WHITE, Piece::BLACK };

    for (int color: colors) {
        uint64_t piecePosColor = piecePositionsColor[color];

        while (piecePosColor) {
            int piecePosition = popLSB(piecePosColor);
            uint64_t attackMapBitboard = 0LL;

            switch (Piece::type(tile[piecePosition])) {
                case Piece::PAWN:
                case Piece::KNIGHT:
                case Piece::KING:
                    attackMapBitboard = PGD::getAttackMap(tile[piecePosition], piecePosition);
                    attackedTiles[indexWithColor(piecePosition, color)] = attackMapBitboard;
                    break;
                case Piece::QUEEN:
                    attackMapBitboard =  PGD::getPseudoMoves(Piece::ROOK, piecePosition, blockerBitboard)
                                        | PGD::getPseudoMoves(Piece::BISHOP, piecePosition, blockerBitboard);
                    attackedTiles[indexWithColor(piecePosition, color)] = attackMapBitboard;
                    break;
                default:
                    //rooks and bishops
                    attackMapBitboard = PGD::getPseudoMoves(Piece::type(tile[piecePosition]), piecePosition, blockerBitboard);
                    attackedTiles[indexWithColor(piecePosition, color)] = attackMapBitboard;
            }

            attackMap[color] |= attackMapBitboard;
        }
    }
}

void Board::updatePins() {
    //reset pins
    pins.fill(0);

    uint64_t friendlyPieces = piecePositionsColor[turn];
    uint64_t enemyPieces = piecePositionsColor[abs(turn - 1)];
    uint64_t enemyPieces_c = enemyPieces;

    const uint64_t kingPosition = piecePositions[Piece::create(Piece::KING, turn)];
    uint64_t kingCopy = kingPosition;
    int kingIndex = popLSB(kingCopy);

    while (enemyPieces_c) {
        int index = popLSB(enemyPieces_c);

        if (isPawn(index) || isKnight(index) || isKing(index)) {
            continue;
        }

        uint64_t attackMap = PGD::getAttackMap(tile[index], index);

        //piece can't see king
        if ((kingPosition & attackMap) == 0) {
            continue;
        }

        const int64_t between = betweenMask(kingIndex, index);
        const uint64_t friendlyIntersect = between & friendlyPieces;
        uint64_t enemyIntersect = between & enemyPieces;

        //if there is more than one piece in the way, or one of your own pieces, no pin
        if (std::popcount(friendlyIntersect) != 1 || enemyIntersect != 0) {
            continue;
        }

        const uint64_t pinLine = lineMask(kingIndex, index);
        uint64_t pinLine_copy = pinLine;

        //go through each square in the pin line and make sure it also knows the pin
        while (pinLine_copy) {
            const int pinIndex = popLSB(pinLine_copy);

            pins[indexWithColor(pinIndex, turn)] = pinLine;
        }
    }
}

void Board::determineCheckLine() {
    uint64_t enemyPieces = piecePositionsColor[abs(turn - 1)];
    const uint64_t kingPosition = piecePositions[Piece::create(Piece::KING, turn)];
    uint64_t kingCopy = kingPosition;
    int kingIndex = popLSB(kingCopy);

    //reset the checks
    check = 0LL;
    checkers = 0LL;

    while (enemyPieces) {
        const int index = popLSB(enemyPieces);
        uint64_t foundCheck = 0LL;

        //we already calculated the attacked tiles, so we can just get them here
        const uint64_t pieceAttackMap = attackedTiles[indexWithColor(index, Piece::color(tile[index]))];

        //if piece cannot see king, continue
        if (!(pieceAttackMap & kingPosition)) {
            continue;
        }

        //if we get here that means we can see the king, so get the checkline by drawing a line between king and piece
        foundCheck = lineMask(kingIndex, index);

       //if you can't make a line, that means it's a knight move
        if (!foundCheck) {
            foundCheck = kingPosition | (1LL << index);
        }

        //mark the checker
        checkers |= (1LL << index);

        //if we break here then it's a double check
        if (check) break;

        //remove king from checkline
        check = foundCheck & ~kingPosition;
    }
}

void Board::updatePiecePositionsColor() {
    piecePositionsColor.fill(0);

    for (int code = 0; code < int(piecePositions.size()); ++code) {
        uint64_t bb = piecePositions[code];
        if (!bb) continue;
        int color = code & 1;
        piecePositionsColor[color] |= bb;
    }
}


// <--> KEEPING TRACK OF ATTACKED TILES, PINS AND CHECKS <--> //

// <--> GAME OVER LOGIC AND MOVE LEGALITY <--> //

bool Board::isLegalMove(const int start, const int end, const Flag flag) {
    //this is currently only used for en passant, so we do this hacky shit. Fix this if we need more flags
    tile[end] = tile[start];
    tile[start] = 0;

    int enPassantPiece = tile[enPassant];
    tile[enPassant] = 0;

    piecePositions[tile[end]] &= ~(1LL << start);
    piecePositions[tile[end]] |= (1LL << end);
    piecePositions[enPassantPiece] &= ~(1LL << enPassant);

    updatePiecePositionsColor();
    updateAttackedTiles();
    determineCheckLine();

    bool isCheck = (check != 0);

    tile[start] = tile[end];
    tile[end] = 0;
    tile[enPassant] = enPassantPiece;

    piecePositions[tile[start]] &= ~(1LL << end);
    piecePositions[tile[start]] |= (1LL << start);
    piecePositions[tile[enPassant]] |= (1LL << enPassant);

    updatePiecePositionsColor();
    updateAttackedTiles();
    determineCheckLine();

    return !isCheck;
}

bool Board::isCheckMate() {
    return (!MoveGenerator::legalMovesExist(*this) && isCheck());
}

bool Board::isStaleMate() {
    return (!MoveGenerator::legalMovesExist(*this) && !isCheck());
}

void Board::checkGameOver() {
    //if no legal moves exist, it's either checkmate or stalemate
    if (!MoveGenerator::legalMovesExist(*this)) {
        gameOver = true;
        return;
    }

    if (isRepetition() || fiftyMoveRule() || insufficientMaterial()) {
        gameOver = true;
    }
}

bool Board::isRepetition() {
    return positionHistory[hash] > 2;
}

bool Board::fiftyMoveRule() const {
    return currentMove - lastCaptOrPawnAdv >= 100;
}

bool Board::insufficientMaterial() {
    bool insufficientMat[2] = { false, false };
    int colors[2] = { Piece::WHITE, Piece::BLACK };

    for (int index = 0; index < 2; index++) {
        //if there are pawns, there is sufficient material
        if (std::popcount(piecePositions[Piece::create(Piece::PAWN, colors[index])]) > 0) {
            return false;
        }

        //if there are rooks, there is sufficient material
        if (std::popcount(piecePositions[Piece::create(Piece::ROOK, colors[index])]) > 0) {
            return false;
        }

        //if there are queens, there is sufficient material
        if (std::popcount(piecePositions[Piece::create(Piece::QUEEN, colors[index])]) > 0) {
            return false;
        }

        //if color has no bishops, that means that he only has a king and knights left, so insufficient
        if (std::popcount(piecePositions[Piece::create(Piece::BISHOP, colors[index])]) == 0) {
            insufficientMat[index] = true;
            continue;
        }

        //if we get here, we know this color has bishops, so check if we have one bishop and no knights
        if (std::popcount(piecePositions[Piece::create(Piece::BISHOP, colors[index])]) == 1 &&
            std::popcount(piecePositions[Piece::create(Piece::KNIGHT, colors[index])]) == 0) {
            insufficientMat[index] = true;
        }
    }

    return insufficientMat[0] && insufficientMat[1];
}

bool Board::isCheck() const {
    return check != 0LL;
}

// <--> GAME OVER LOGIC AND MOVE LEGALITY <--> //

// <--> PRINTING BOARD STUFF <--> //

std::string Board::positionToFen() {
    std::string fen;
    std::string castleRightsFen;
    std::string enPassantFen = "-";
    int emptyRowCount = 0;

    for (int index = 0; index < 64; index++) {
        //count empty tiles
        if (tile[index] == 0) {
            emptyRowCount++;
            if ((index + 1) % 8 == 0 && index / 8 != 7) {
                fen += std::to_string(emptyRowCount) + "/";
                emptyRowCount = 0;
            }

            continue;
        }

        //if we get here we found a piece, so add how many empty tiles there were
        if (emptyRowCount > 0) {
            fen += std::to_string(emptyRowCount);
            emptyRowCount = 0;
        }

        //add piece string to fen
        fen += Piece::toString(tile[index]);

        //add enPassant
        if (isPawn(index) && enPassant == index && index > 0) {
            if (getPieceColor(index) == Piece::WHITE) {
                enPassantFen = getChessCoords(index + 8);
            }
            else {
                enPassantFen = getChessCoords(index - 8);
            }
        }

        //add the slash at the end of the row
        if ((index + 1) % 8 == 0 && index / 8 != 7) {
            fen += "/";
        }
    }

    //constructing castleRights
    if (canCastleKSide(Piece::WHITE)) {
        castleRightsFen += "K";
    }
    if (canCastleQSide(Piece::WHITE)) {
        castleRightsFen += "Q";
    }
    if (canCastleKSide(Piece::BLACK)) {
        castleRightsFen += "k";
    }
    if (canCastleQSide(Piece::BLACK)) {
        castleRightsFen += "q";
    }

    castleRightsFen = (castleRightsFen.empty()) ? "-" : castleRightsFen;
    std::string turnFen = (turn == Piece::WHITE) ? "w" : "b";

    fen += " " + turnFen + " " + castleRightsFen + " " + enPassantFen + " " +
            std::to_string(currentMove - lastCaptOrPawnAdv) + " " + std::to_string(currentMove / 2);

    return fen;
}

std::string Board::toString() {
    std::string board = "  +---+---+---+---+---+---+---+---+\n";

    for (int index = 0; index < 64; index++) {
        if (index % 8 == 0) {
            board += std::to_string(abs((index / 8) - 8)) + " |";
        }

        std::string piece(1, Piece::toString(tile[index]));
        board += " " + piece + " |";

        if ((index + 1) % 8 == 0) {
            board += "\n  +---+---+---+---+---+---+---+---+\n";
        }
    }

    board += "    a   b   c   d   e   f   g   h\n";
    return board;
}

std::string Board::debugString() {
    int colors[] = { Piece::WHITE, Piece::BLACK };
    std::string colorString[] = { "White", "Black" };

    std::string obj = positionToFen() + "\n";
    obj += toString();
    obj += "Turn: " + std::to_string(turn) + "\n";
    obj += "En passant: " + std::to_string(enPassant) + "\n";
    obj += "Current move: " + std::to_string(currentMove) + "\n";
    obj += "Last capture or pawn advancement: " + std::to_string(lastCaptOrPawnAdv) + "\n";
    obj += "Castle rights: " + bitString(castleRights).substr(bitString(castleRights).length() - 5) + "\n";
    obj += "Hash: " + std::to_string(hash) + "\n";
    obj += "Game over: " + std::to_string(gameOver) + "\n";

    obj += "Position history: [";
    for (auto it = positionHistory.begin(); it != positionHistory.end(); ++it) {
        obj += std::to_string(it->first) + "=" + std::to_string(it->second);
        if (std::next(it) != positionHistory.end()) {
            obj += ", ";
        }
    }
    obj += "]\n";

    obj += "Piece positions:\n";
    for (int c = 0; c < 2; ++c) {
        obj += "\t" + colorString[c] + ": [";
        for (size_t code = 0; code < piecePositionsColor.size(); ++code) {
            if ((code & 1) != c) continue;  // skip entries not matching this color
            obj += "\n" + bitboardString(piecePositionsColor[code]) + "\n";
        }
        obj += "]\n";
    }

    obj += "Check:\n";
    obj += bitboardString(check) + "\n";
    obj += "Checkers:\n";
    obj += bitboardString(checkers) + "\n";

    obj += "Attack maps:\n";
    for (int i = 0; i < 2; i++) {
        obj += colorString[i] + ":\n" + bitboardString(attackMap[colors[i]]) + "\n";
    }

    obj += "Attacked tiles (non-zero only):\n";
    for (int i = 0; i < 2; ++i) {
        obj += colorString[i] + ":\n";
        for (int sq = 0; sq < 64; ++sq) {
            uint64_t bb = attackedTiles[indexWithColor(sq, colors[i])];
            if (bb) obj += bitboardString(bb) + "\n";
        }
    }

    obj += "Pins (non-zero only):\n";
    for (int i = 0; i < 2; ++i) {
        obj += colorString[i] + ":\n";
        for (int sq = 0; sq < 64; ++sq) {
            uint64_t bb = pins[indexWithColor(sq, colors[i])];
            if (bb) obj += bitboardString(bb) + "\n";
        }
    }

    return obj;
}

// <--> PRINTING BOARD STUFF <--> //

// <--> GETTERS AND SUCH <--> //

bool Board::isEmpty(int index) {
    return tile[index] == 0;
}

int Board::getPieceType(int index) {
    return Piece::type(tile[index]);
}

int Board::getPieceColor(int index) {
    return Piece::color(tile[index]);
}

bool Board::isKing(int index) {
    return Piece::type(tile[index]) == Piece::KING;
}

bool Board::isQueen(int index) {
    return Piece::type(tile[index]) == Piece::QUEEN;
}

bool Board::isRook(int index) {
    return Piece::type(tile[index]) == Piece::ROOK;
}

bool Board::isBishop(int index) {
    return Piece::type(tile[index]) == Piece::BISHOP;
}

bool Board::isKnight(int index) {
    return Piece::type(tile[index]) == Piece::KNIGHT;
}

bool Board::isPawn(int index) {
    return Piece::type(tile[index]) == Piece::PAWN;
}

bool Board::isColor(int index, int color) {
    return Piece::color(tile[index]) == color;
}

bool Board::canCastleQSide(int color) const {
    if (color == Piece::WHITE) {
        return (castleRights & 0b0010) == 0b0010;
    }

    return (castleRights & 0b1000) == 0b1000;
}

bool Board::canCastleKSide(int color) const {
    if (color == Piece::WHITE) {
        return (castleRights & 0b0001) == 0b0001;
    }

    return (castleRights & 0b0100) == 0b0100;
}

int Board::getTurn() const {
    return turn;
}

int Board::getEnPassant() const {
    return enPassant;
}

bool Board::isGameOver() const {
    return gameOver;
}

uint64_t& Board::getPiecePositions(int piece) {
    return piecePositions[piece];
}

uint64_t& Board::getPiecePositionsColor(int color) {
    return piecePositionsColor[color];
}

uint64_t& Board::getPieceAttackMap(int position, int color) {
    return attackedTiles[indexWithColor(position, color)];
}

uint64_t& Board::getColorAttackMap(int color) {
    return attackMap[color];
}

uint64_t& Board::getPins(int index, int color) {
   return pins[indexWithColor(index, color)];
}

// <--> GETTERS AND SUCH <--> //
