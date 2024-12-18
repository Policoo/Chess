#include "board.h"

#include "moveGenerator.h"
#include "piece.h"
#include "precomputedGameData.h"
#include "utils.h"
#include "zobrist.h"

#include <iostream>

// <--> INITIALIZING BOARD <--> //

Board::Board(const std::string& fenString) :
    castleRights(0),
    gameOver(false) {
    positionHistory.reserve(100);
    boardState.reserve(100);
    piecePositions[Piece::BLACK].reserve(20);
    piecePositions[Piece::WHITE].reserve(20);

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
    initializeAttackTiles();
    initializePinLines();

    determineCheckLine();
    hashPosition();
}

void Board::startPos() {
    currentMove = 2;
    lastCaptOrPawnAdv = 2;
    enPassant = 0;
    turn = Piece::WHITE;
    castleRights = 0b1111;

    tile[0] = Piece::create(Piece::BLACK, Piece::ROOK);
    tile[1] = Piece::create(Piece::BLACK, Piece::KNIGHT);
    tile[2] = Piece::create(Piece::BLACK, Piece::BISHOP);
    tile[3] = Piece::create(Piece::BLACK, Piece::QUEEN);
    tile[4] = Piece::create(Piece::BLACK, Piece::KING);
    tile[5] = Piece::create(Piece::BLACK, Piece::BISHOP);
    tile[6] = Piece::create(Piece::BLACK, Piece::KNIGHT);
    tile[7] = Piece::create(Piece::BLACK, Piece::ROOK);

    for (int index = 8; index < 16; index++) {
        tile[index] = Piece::create(Piece::PAWN, Piece::BLACK);
    }

    for (int index = 48; index < 56; index++) {
        tile[index] = Piece::create(Piece::PAWN, Piece::WHITE);
    }

    tile[56] = Piece::create(Piece::ROOK, Piece::WHITE);
    tile[57] = Piece::create(Piece::KNIGHT, Piece::WHITE);
    tile[58] = Piece::create(Piece::BISHOP, Piece::WHITE);
    tile[59] = Piece::create(Piece::QUEEN, Piece::WHITE);
    tile[60] = Piece::create(Piece::KING, Piece::WHITE);
    tile[61] = Piece::create(Piece::BISHOP, Piece::WHITE);
    tile[62] = Piece::create(Piece::KNIGHT, Piece::WHITE);
    tile[63] = Piece::create(Piece::ROOK, Piece::WHITE);

    remainingPieces[Piece::create(Piece::QUEEN, Piece::WHITE)] = 1;
    remainingPieces[Piece::create(Piece::ROOK, Piece::WHITE)] = 2;
    remainingPieces[Piece::create(Piece::BISHOP, Piece::WHITE)] = 2;
    remainingPieces[Piece::create(Piece::KNIGHT, Piece::WHITE)] = 2;
    remainingPieces[Piece::create(Piece::PAWN, Piece::WHITE)] = 8;
    remainingPieces[Piece::create(Piece::QUEEN, Piece::BLACK)] = 1;
    remainingPieces[Piece::create(Piece::ROOK, Piece::BLACK)] = 2;
    remainingPieces[Piece::create(Piece::BISHOP, Piece::BLACK)] = 2;
    remainingPieces[Piece::create(Piece::KNIGHT, Piece::BLACK)] = 2;
    remainingPieces[Piece::create(Piece::PAWN, Piece::BLACK)] = 8;
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
        remainingPieces[tile[index]]++;
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
    int whiteIndex = 0;
    int blackIndex = 0;

    for (int index = 0; index < 64; index++) {
        if (tile[index] == 0) {
            continue;
        }

        if (Piece::color(tile[index]) == Piece::WHITE) {
            tile[index] = Piece::setIndex(tile[index], whiteIndex);
            whiteIndex++;
        }
        else {
            tile[index] = Piece::setIndex(tile[index], blackIndex);
            blackIndex++;
        }

        piecePositions[Piece::color(tile[index])].push_back(index);

        //initialize king positions
        if (isKing(index)) {
            kingPositions[Piece::color(tile[index])] = index;
        }
    }
}

void Board::initializeAttackTiles() {
    for (int color: { Piece::WHITE, Piece::BLACK }) {
        uint64_t attackMapBitboard = 0LL;

        for (const int piecePos: piecePositions[color]) {
            uint64_t pieceAttackMap = calculateAttackedTiles(piecePos);

            attackMapBitboard |= pieceAttackMap;
            attackedTiles[color].push_back(pieceAttackMap);
        }

        attackMap[color] = attackMapBitboard;
    }
}

void Board::initializePinLines() {
    //we have to switch the turn for calculatePinLine to work properly when initializing
    turn = (turn == Piece::WHITE) ? Piece::BLACK : Piece::WHITE;

    for (int color: { Piece::WHITE, Piece::BLACK }) {
        for (const int piecePos: piecePositions[color]) {
            uint64_t pinLine = calculatePinLine(piecePos);

            if (pinLine != 0LL) {
                pins[Piece::color(tile[piecePos])].push_back(pinLine);
            }
        }
    }

    turn = (turn == Piece::WHITE) ? Piece::BLACK : Piece::WHITE;
}

void Board::hashPosition() {
    hash = 0;

    for (int index = 0; index < 64; index++) {
        if (tile[index] == 0) {
            continue;
        }

        hash ^= Zobrist::getKey(Piece::ignoreIndex(tile[index]), index);
    }

    hash ^= Zobrist::getColorKey(turn);
    hash ^= Zobrist::getCastleKey(castleRights);
    if (enPassant != 0) {
        hash ^= Zobrist::getEnPassantKey(enPassant);
    }
}

// <--> INITIALIZING BOARD <--> //

// <--> MAKING MOVES <--> //

void Board::makeMove(const Move& move) {
    const int start = move.start();
    const int end = move.end();
    const Flag flag = move.flag();

    //save current position
    boardState.push_back(
            {
                tile[end],
                castleRights,
                enPassant,
                Piece::index(tile[enPassant]),
                lastCaptOrPawnAdv
            }
            );

    //remove start piece and end piece (if there is one) from the hash
    hash ^= Zobrist::getKey(Piece::ignoreIndex(tile[start]), start);
    if (tile[end] != 0) {
        hash ^= Zobrist::getKey(Piece::ignoreIndex(tile[end]), end);

        //handle a capture (since we are checking tile[end] != 0 anyway)
        remainingPieces[Piece::ignoreIndex(tile[end])]--;
        piecePositions[Piece::color(tile[end])][Piece::index(tile[end])] = -1;
        attackedTiles[Piece::color(tile[end])][Piece::index(tile[end])] = 0LL;
        lastCaptOrPawnAdv = currentMove;
    }

    //move the piece
    tile[end] = tile[start];
    tile[start] = 0;
    piecePositions[Piece::color(tile[end])][Piece::index(tile[end])] = end;

    switch (flag) {
        case Flag::NONE: {
            updateGameState(start, end);
            if (isPawn(end)) {
                lastCaptOrPawnAdv = currentMove;
            }

            return;
        }
        case Flag::CASTLE: {
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
            hash ^= Zobrist::getKey(Piece::ignoreIndex(tile[rookStart]), rookEnd);
            tile[rookEnd] = tile[rookStart];
            tile[rookStart] = 0;
            piecePositions[Piece::color(tile[rookEnd])][Piece::index(tile[rookEnd])] = rookEnd;
            hash ^= Zobrist::getKey(Piece::ignoreIndex(tile[rookEnd]), rookEnd);

            updateAttackedTiles(rookEnd, rookStart);
            updateGameState(start, end);
            return;
        }
        case Flag::EN_PASSANT: {
            //capture enemy pawn
            hash ^= Zobrist::getKey(tile[enPassant], enPassant);
            remainingPieces[Piece::ignoreIndex(tile[enPassant])]--;
            piecePositions[Piece::color(tile[enPassant])][Piece::index(tile[enPassant])] = -1;
            tile[enPassant] = 0;

            lastCaptOrPawnAdv = currentMove;
            updateGameState(start, end);
            return;
        }
        case Flag::PROMOTION: {
            //remove old pawn
            remainingPieces[Piece::ignoreIndex(tile[end])]--;

            const int index = Piece::index(tile[end]);
            int promotion = Piece::create(move.promotion(), Piece::color(tile[end]));
            promotion = Piece::setIndex(promotion, index);

            //create promoted piece
            tile[end] = promotion;
            remainingPieces[tile[end]]++;

            lastCaptOrPawnAdv = currentMove;
            updateGameState(start, end);
        }
    }
}

void Board::updateGameState(const int start, const int end) {
    updateAttackedTiles(start, end);
    updatePins();

    if (isKing(end)) {
        kingPositions[Piece::color(tile[end])] = end;
    }

    //update the hash with the moved piece
    hash ^= Zobrist::getKey(Piece::ignoreIndex(tile[end]), end);

    //undo castle rights from hash
    hash ^= Zobrist::getCastleKey(castleRights);

    //update castling rights if needed
    if (const int castleRightsColorMask = (Piece::color(tile[end]) == Piece::WHITE) ? 3 : 12;
        (castleRights & castleRightsColorMask) > 0) {
        if (isKing(end)) {
            castleRights &= (castleRightsColorMask ^ 0b1111);
        }

        //if the rooks are not on their start squares, that means they moved or were captured
        if (!isRook(63) || Piece::color(tile[63]) == Piece::BLACK) {
            castleRights &= 0b1110;
        }

        if (!isRook(56) || Piece::color(tile[56]) == Piece::BLACK) {
            castleRights &= 0b1101;
        }

        if (!isRook(7) || Piece::color(tile[7]) == Piece::WHITE) {
            castleRights &= 0b1011;
        }

        if (!isRook(0) || Piece::color(tile[0]) == Piece::WHITE) {
            castleRights &= 0b0111;
        }
    }

    //put new castleRights into hash
    hash ^= Zobrist::getCastleKey(castleRights);

    //if en passant was possible this move, make sure it's not possible next move
    if (enPassant > 0) {
        hash ^= Zobrist::getEnPassantKey(enPassant);
        enPassant = 0;
    }

    //if pawn moved up 2 squares, make en passant possible for next move
    if (isPawn(end) && abs(start - end) == 16) {
        enPassant = end;
        hash ^= Zobrist::getEnPassantKey(enPassant);
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
    determineCheckLine();
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
    hash ^= Zobrist::getKey(Piece::ignoreIndex(tile[end]), end);

    //move the piece back
    tile[start] = tile[end];
    tile[end] = 0;
    piecePositions[Piece::color(tile[start])][Piece::index(tile[start])] = start;

    //handle a capture
    if (const int targetTile = state.targetTile;
        targetTile != 0) {
        tile[end] = targetTile;
        remainingPieces[Piece::ignoreIndex(tile[end])]++;
        piecePositions[Piece::color(tile[end])][Piece::index(tile[end])] = end;
        attackedTiles[Piece::color(tile[end])][Piece::index(tile[end])] = calculateAttackedTiles(end);

        //update the hash with the captured piece
        hash ^= Zobrist::getKey(Piece::ignoreIndex(tile[end]), end);
    }

    switch (flag) {
        case Flag::NONE: {
            revertGameStats(start, end, state);
            return;
        }
        case Flag::CASTLE: {
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
            piecePositions[Piece::color(tile[rookStart])][Piece::index(tile[rookStart])] = rookStart;
            hash ^= Zobrist::getKey(tile[rookStart], rookStart);

            updateAttackedTiles(rookStart, rookEnd);
            revertGameStats(start, end, state);
            return;
        }
        case Flag::EN_PASSANT: {
            //create pawn and put him back
            const int enPassantIndex = state.enPassantIndex;
            const int pastEnPassant = state.enPassant;

            //create the pawn and set its index
            int piece = Piece::create(Piece::PAWN, turn);
            piece = Piece::setIndex(piece, enPassantIndex);
            tile[pastEnPassant] = piece;

            piecePositions[Piece::color(tile[pastEnPassant])][enPassantIndex] = pastEnPassant;
            remainingPieces[Piece::ignoreIndex(tile[pastEnPassant])]++;

            hash ^= Zobrist::getKey(tile[pastEnPassant], pastEnPassant);
            revertGameStats(start, end, state);
            return;
        }
        case Flag::PROMOTION: {
            //remove promotion from remaining pieces
            remainingPieces[Piece::ignoreIndex(tile[start])]--;
            if (remainingPieces[tile[start]] < 1) {
                remainingPieces.erase(tile[start]);
            }

            //make piece back into pawn
            const int color = Piece::color(tile[start]);
            const int index = Piece::index(tile[start]);
            tile[start] = Piece::create(Piece::PAWN, color);
            tile[start] = Piece::setIndex(tile[start], index);

            //put pawn back in remaining pieces
            remainingPieces[Piece::ignoreIndex(tile[start])]++;

            revertGameStats(start, end, state);
        }
    }
}

void Board::revertGameStats(const int start, const int end, const Board::BoardState& state) {
    if (isKing(start)) {
        kingPositions[Piece::color(tile[start])] = start;
    }

    //update hash with reverted piece
    hash ^= Zobrist::getKey(Piece::ignoreIndex(tile[start]), start);

    //revert castle rights
    hash ^= Zobrist::getCastleKey(castleRights);
    castleRights = state.castleRights;
    hash ^= Zobrist::getCastleKey(castleRights);

    //undo enPassant hash if needed
    if (enPassant > 0) {
        hash ^= Zobrist::getEnPassantKey(enPassant);
    }

    //update new enPassant
    enPassant = state.enPassant;
    if (enPassant > 0) {
        hash ^= Zobrist::getEnPassantKey(enPassant);
    }

    //revert turn, make sure hash is accurate
    hash ^= Zobrist::getColorKey(turn);
    turn = (turn == Piece::WHITE) ? Piece::BLACK : Piece::WHITE;
    hash ^= Zobrist::getColorKey(turn);

    lastCaptOrPawnAdv = state.lastCaptOrPawnAdv;

    gameOver = false;
    currentMove--;
    updateAttackedTiles(end, start);
    updatePins();
    determineCheckLine();
}

// <--> MAKING MOVES <--> //

// <--> KEEPING TRACK OF ATTACKED TILES, PINS AND CHECKS <--> //

void Board::updateAttackedTiles(const int oldIndex, const int newIndex) {
    int colors[] = { Piece::WHITE, Piece::BLACK };
    const int pieceIndex = Piece::index(tile[newIndex]);

    //calculate the attacked tiles for the moved piece
    attackedTiles[turn][pieceIndex] = calculateAttackedTiles(newIndex);

    for (int color: colors) {
        const std::vector<int>& piecePositionsColor = piecePositions[color];
        std::vector<uint64_t>& attackedTilesColor = attackedTiles[color];
        uint64_t attackMapBitboard = 0LL;


        for (int i = 0; i < attackedTilesColor.size(); i++) {
            //no point in looking at the moved piece, we already calculated it's attacked tiles
            if (i == pieceIndex) {
                attackMapBitboard |= attackedTilesColor[i];
                continue;
            }

            const int piecePosition = piecePositionsColor[i];
            const uint64_t moveBitboard = (1LL << oldIndex) | (1LL << newIndex);

            //if the move intersected with the piece attacked tiles, and the piece is on the board
            if ((attackedTilesColor[i] & moveBitboard) != 0 && piecePosition != -1) {
                attackedTilesColor[i] = calculateAttackedTiles(piecePosition);
            }

            attackMapBitboard |= attackedTilesColor[i];
        }

        attackMap[color] = attackMapBitboard;
    }
}

uint64_t Board::calculateAttackedTiles(const int index) {
    const int pieceColor = Piece::color(tile[index]);
    const std::vector<int>& directions = PGD::getPieceDirections(tile[index]);

    switch (Piece::type(tile[index])) {
        case Piece::PAWN:
        case Piece::KNIGHT:
        case Piece::KING:
            //we precomputed this data already
            return PGD::getAttackMap(tile[index], index);
        default:
            //sliding pieces would go in default
            uint64_t bitboard = 0LL;

            for (const int dir: directions) {
                const int numSteps = PGD::getEdgeOfBoard(dir, index);
                int curIndex = index;

                for (int step = numSteps; step > 0; step--) {
                    curIndex = curIndex + dir;
                    bitboard |= (1LL << curIndex);

                    //if the tile is empty, keep going
                    if (tile[curIndex] == 0) {
                        continue;
                    }

                    //if we find the opponents king, keep going. This is to cover an edge case in checks
                    if (isKing(curIndex) && Piece::color(tile[curIndex]) != pieceColor) {
                        continue;
                    }

                    //if we get here it means that we found a piece we don't care about, so break
                    break;
                }
            }

            return bitboard;
    }
}

void Board::updatePins() {
    pins[turn].clear();
    const std::vector<int>& piecePositionsTurn = piecePositions[turn];

    for (const int& piecePos: piecePositionsTurn) {
        if (piecePos == -1) {
            continue;
        }

        if (uint64_t pinLine = calculatePinLine(piecePos);
            pinLine != 0LL) {
            pins[turn].push_back(pinLine);
        }
    }
}

uint64_t Board::calculatePinLine(const int index) {
    //pawns, knights and kings can't pin
    if (isPawn(index) || isKnight(index) || isKing(index)) {
        return 0LL;
    }

    const int otherTurn = (turn == Piece::WHITE) ? Piece::BLACK : Piece::WHITE;
    const int kingIndex = kingPositions[otherTurn];
    const std::vector<int>& directions = PGD::getPieceDirections(tile[index]);

    for (const int dir: directions) {
        const int posDif = kingIndex - index;
        const int steps = PGD::getEdgeOfBoard(dir, index);

        //if piece can't see the king in this direction, or if it's at the edge of the board, don't search
        if (dir * posDif < 0 || posDif % dir != 0 || steps == 0) {
            continue;
        }

        //everything % 1 == 0, so make sure that it's actually worth searching this direction
        if (std::abs(dir) == 1 && index / 8 != kingIndex / 8) {
            continue;
        }

        int curIndex = index;
        bool canPin = false;

        //we add the square that the piece is on, so that captures can break the pin
        uint64_t bitboard = (1LL << index);

        for (int step = 0; step < steps; step++) {
            curIndex = curIndex + dir;

            //if tile is empty, add it to the bitboard
            if (tile[curIndex] == 0) {
                bitboard |= (1LL << curIndex);
                continue;
            }

            //if you see your own piece, or if you see the other king, but you can't pin
            if (Piece::color(tile[index]) == Piece::color(tile[curIndex]) || (isKing(curIndex) && !canPin)) {
                break;
            }

            //if this is true, that means we found a king of the opposite color and we can pin
            if (isKing(curIndex)) {
                bitboard |= (1LL << curIndex);
                return bitboard;
            }

            /*if we get here, the tile contains a piece that is of the other color, that is not a king. If canPin is true,
            this is the second opponents piece we encountered, so a pin is not possible*/
            if (canPin) {
                break;
            }

            //if we get here that means we found an opponents piece that is not a king, so a pin is possible
            bitboard |= (1LL << curIndex);
            canPin = true;
        }
    }

    return 0LL;
}

void Board::determineCheckLine() {
    const int attackingColor = (turn == Piece::WHITE) ? Piece::BLACK : Piece::WHITE;
    const std::vector<uint64_t>& attackedTilesColor = attackedTiles[attackingColor];
    const int kingPos = kingPositions[turn];
    const uint64_t kingPosBitboard = 1LL << kingPos;

    check = 0LL;
    doubleCheck = 0LL;

    //if the king isn't in check
    if ((attackMap[attackingColor] & kingPosBitboard) == 0LL) {
        return;
    }

    for (int index = 0; index < attackedTilesColor.size(); index++) {
        const uint64_t& pieceAttackTiles = attackedTilesColor[index];

        //if this piece can't see the king, continue
        if ((pieceAttackTiles & kingPosBitboard) == 0LL) {
            continue;
        }

        /* all this shit is to find out in which direction the king is getting checked, so that
           we can properly generate the check/double check line */
        const int& piecePos = piecePositions[attackingColor][index];
        const std::vector<int>& directions = PGD::getPieceDirections(tile[piecePos]);
        const int posDif = kingPos - piecePos;
        int finalDir = 0;

        for (const int& dir: directions) {
            //if the line of sight doesn't align with the kingPosition, don't search
            if (dir * posDif < 0 || posDif % dir != 0) {
                continue;
            }

            //everything % 1 == 0, so check that if direction is +/- 1, the piece and king are on the same row
            if (std::abs(dir) == 1 && kingPos / 8 != piecePos / 8) {
                continue;
            }

            //if we get here that means that this is the direction the piece can see the king in
            finalDir = dir;
            break;
        }

        //now create a bitboard to represent the check line
        const int numSteps = PGD::getEdgeOfBoard(finalDir, piecePos);
        uint64_t bitboard = 1LL << piecePos;
        int curIndex = piecePos;

        for (int step = numSteps; step > 0; step--) {
            curIndex += finalDir;
            bitboard |= (1LL << curIndex);

            //if we find a piece, it should be the king, so the check line is done
            if (tile[curIndex] != 0) {
                break;
            }
        }

        if (check == 0LL) {
            check = bitboard;
        }
        else {
            doubleCheck = bitboard;
        }
    }
}

// <--> KEEPING TRACK OF ATTACKED TILES, PINS AND CHECKS <--> //

// <--> GAME OVER LOGIC AND MOVE LEGALITY <--> //

bool Board::isLegalMove(const int start, const int end, const bool isEnPassant) {
    const int otherTurn = (turn == Piece::WHITE) ? Piece::BLACK : Piece::WHITE;
    const std::vector<uint64_t>& pinLinesColor = pins[otherTurn];

    //if we are moving the king, make sure he is not in a square attacked by the opponent
    if (isKing(start)) {
        //it is a legal move if the king is not attacked
        return (attackMap[otherTurn] & (1LL << end)) == 0;
    }

    //if we get here that means we are not moving the king, and it's a double check, so move is not legal
    if (doubleCheck != 0LL) {
        return false;
    }

    //if the piece is pinned, it can't move out of the pin line, so make sure it doesn't do that
    for (const uint64_t& pinLine: pinLinesColor) {
        //if this piece is not pinned, continue
        if ((pinLine & (1LL << start)) == 0) {
            continue;
        }

        //if we get here the piece is pinned, so make sure it's not illegally moving out of the pin
        if ((pinLine & (1LL << end)) == 0) {
            return false;
        }

        //if we get here that means the piece is pinned, but this move is in the pin line, so it could be legal
        break;
    }

    //if we are in check, make sure to either block it or capture the piece checking the king
    if (check != 0LL) {


        //if we are either capturing the piece checking us or moving in front of the check, it's legal
        return (check & (1LL << end)) != 0;
    }

    //if we get here, the piece isn't pinned, and it's not a check, so it's free to move
    return true;
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
    constexpr int colors[2] = { Piece::WHITE, Piece::BLACK };

    for (int index = 0; index < 2; index++) {
        //if there are pawns, there is sufficient material
        if (remainingPieces[Piece::create(Piece::PAWN, colors[index])] > 0) {
            return false;
        }

        //if there are rooks, there is sufficient material
        if (remainingPieces[Piece::create(Piece::ROOK, colors[index])] > 0) {
            return false;
        }

        //if there are queens, there is sufficient material
        if (remainingPieces[Piece::create(Piece::QUEEN, colors[index])] > 0) {
            return false;
        }

        //if color has no bishops, that means that he only has a king and knights left, so insufficient
        if (remainingPieces[Piece::create(Piece::BISHOP, colors[index])] == 0) {
            insufficientMat[index] = true;
            continue;
        }

        //if we get here, we know this color has bishops, so check if we have one bishop and no knights
        if (remainingPieces[Piece::create(Piece::BISHOP, colors[index])] == 1 &&
            remainingPieces[Piece::create(Piece::KNIGHT, colors[index])] == 0) {
            insufficientMat[index] = true;
        }
    }

    return insufficientMat[0] && insufficientMat[1];
}

bool Board::isCheck() const {
    return check != 0LL;
}

int Board::getKingIndex(int color) {
    return kingPositions[color];
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

    obj += "Piece positions:\n";
    for (int i = 0; i < 2; i++) {
        obj += "\t" + colorString[i] + ": [";
        for (int j = 0; j < piecePositions[colors[i]].size(); j++) {
            obj += std::to_string(piecePositions[colors[i]][j]);
            if (j != piecePositions[colors[i]].size() - 1) {
                obj += ", ";
            }
        }
        obj += "]\n";
    }

    obj += "Position history: [";
    for (auto it = positionHistory.begin(); it != positionHistory.end(); ++it) {
        obj += std::to_string(it->first) + "=" + std::to_string(it->second);
        if (std::next(it) != positionHistory.end()) {
            obj += ", ";
        }
    }
    obj += "]\n";

    obj += "King positions:\n";
    for (int i = 0; i < 2; i++) {
        obj += "\t" + colorString[i] + ": " + std::to_string(kingPositions[colors[i]]) + "\n";
    }

    obj += "Check:\n";
    obj += bitboardString(check) + "\n";
    obj += "Double check:\n";
    obj += bitboardString(doubleCheck) + "\n";

    obj += "Attack maps:\n";
    for (int i = 0; i < 2; i++) {
        obj += colorString[i] + ":\n" + bitboardString(attackMap[colors[i]]) + "\n";
    }

    obj += "Attacked tiles:\n";
    for (int i = 0; i < 2; i++) {
        obj += colorString[i] + ":\n";
        for (uint64_t bitboard: attackedTiles[colors[i]]) {
            obj += bitboardString(bitboard) + "\n";
        }
    }

    obj += "Pins:\n";
    for (int i = 0; i < 2; i++) {
        obj += colorString[i] + "\n";
        for (uint64_t bitboard: pins[colors[i]]) {
            obj += bitboardString(bitboard) + "\n";
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

std::vector<int>& Board::getPiecePositions(int color) {
    return piecePositions[color];
}

// <--> GETTERS AND SUCH <--> //
