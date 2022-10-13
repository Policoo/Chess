package board;

import java.util.*;

import utils.MoveGenerator;
import utils.Utils;

public class Board {
    private Piece[][] tile;
    private List<Integer> enPassant;
    private String colorToMove;
    private int currentMove;
    private int lastCaptureOrPawnAdv;
    private HashMap<String, Integer> positionHistory;
    private HashMap<String, Integer> remainingPieces;
    private boolean gameOver;

    public Board() {
        positionHistory = new HashMap<>();
        remainingPieces = new HashMap<>();
        gameOver = false;
        currentMove = 2;
        lastCaptureOrPawnAdv = 2;
        enPassant = new ArrayList<>();
        tile = new Piece[8][8];
        colorToMove = "w";
        reset();
    }

    public Board(String fenString) {
        positionHistory = new HashMap<>();
        remainingPieces = new HashMap<>();
        gameOver = false;
        currentMove = 0;
        lastCaptureOrPawnAdv = 0;
        enPassant = new ArrayList<>();
        tile = new Piece[8][8];
        colorToMove = "w";
        makeBoardFromFen(fenString);
    }

    public Board deepCopy() {
        Board boardCopy = new Board();
        for (int y = 0; y < 8; y++) {
            for (int x = 0; x < 8; x++) {
                if (this.tile[x][y] == null) {
                    boardCopy.tile[x][y] = null;
                    continue;
                }
                boardCopy.tile[x][y] = new Piece(this.tile[x][y].getColor(), this.tile[x][y].getType());
                boardCopy.tile[x][y].setHasMoved(this.tile[x][y].hasMoved());
                boardCopy.tile[x][y].setEnPassantPossible(this.tile[x][y].isEnPassantPossible());
            }
        }
        if (enPassant.size() > 0) {
            boardCopy.setEnPassant(new ArrayList<>(enPassant));
        }
        boardCopy.setColorToMove(colorToMove);
        boardCopy.setCurrentMove(currentMove);
        boardCopy.setLastCaptureOrPawnAdv(lastCaptureOrPawnAdv);
        boardCopy.setPositionHistory(new HashMap<>(positionHistory));
        boardCopy.setRemainingPieces(new HashMap<>(remainingPieces));
        boardCopy.setGameOver(gameOver);
        return boardCopy;
    }

    public void reset() {
        tile[0][0] = new Piece("b", "r");
        tile[1][0] = new Piece("b", "n");
        tile[2][0] = new Piece("b", "b");
        tile[3][0] = new Piece("b", "q");
        tile[4][0] = new Piece("b", "k");
        tile[5][0] = new Piece("b", "b");
        tile[6][0] = new Piece("b", "n");
        tile[7][0] = new Piece("b", "r");
        for (int index = 0; index < 8; index++) {
            tile[index][1] = new Piece("b", "p");
            tile[index][6] = new Piece("w", "p");
        }
        for (int y = 2; y < 6; y++) {
            for (int x = 0; x < 8; x++) {
                tile[x][y] = null;
            }
        }
        tile[0][7] = new Piece("w", "r");
        tile[1][7] = new Piece("w", "n");
        tile[2][7] = new Piece("w", "b");
        tile[3][7] = new Piece("w", "q");
        tile[4][7] = new Piece("w", "k");
        tile[5][7] = new Piece("w", "b");
        tile[6][7] = new Piece("w", "n");
        tile[7][7] = new Piece("w", "r");

        remainingPieces.put("wr", 2);
        remainingPieces.put("wq", 1);
        remainingPieces.put("wb", 2);
        remainingPieces.put("wn", 2);
        remainingPieces.put("wp", 8);
        remainingPieces.put("br", 2);
        remainingPieces.put("bq", 1);
        remainingPieces.put("bb", 2);
        remainingPieces.put("bn", 2);
        remainingPieces.put("bp", 8);
    }

    public boolean isGameOver() {
        if (isCheckMate() || isDraw()) {
            gameOver = true;
            return true;
        }
        return false;
    }

    private boolean legalMovesExist() {
        for (int y = 0; y < 8; y++) {
            for (int x = 0; x < 8; x++) {
                if (tile[x][y] == null) {
                    continue;
                }
                if (tile[x][y].isColor(colorToMove) && MoveGenerator.generateMoves(x, y, this).size() > 0) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isCheckMate() {
        if (legalMovesExist()) {
            return false;
        }
        return isCheck(colorToMove);
    }

    public boolean isDraw() {
        return isStalemate() || isRepetition() || fiftyMoveRule() || insufficientMaterial();
    }

    public boolean isStalemate() {
        if (legalMovesExist()) {
            return false;
        }
        return !isCheck(colorToMove);
    }

    public boolean isRepetition() {
        //only add pieces position and color to move to history
        String[] splitFen = positionToFen().split(" ");
        String fenHistory = splitFen[0] + " " + splitFen[1];
        return positionHistory.containsKey(fenHistory) && positionHistory.get(fenHistory) > 1;
    }

    public boolean fiftyMoveRule() {
        return currentMove - lastCaptureOrPawnAdv >= 100;
    }

    public boolean insufficientMaterial() {
        boolean insufficientMaterialWhite = false;
        boolean insufficientMaterialBlack = false;
        //if there are pawns on the board there is sufficient material
        if (remainingPieces.containsKey("wp") || remainingPieces.containsKey("bp")) {
            return false;
        }
        //if white or black has rooks or queens there is sufficient material
        if (remainingPieces.containsKey("wq") || remainingPieces.containsKey("wr") || remainingPieces.containsKey("bq") || remainingPieces.containsKey("br")) {
            return false;
        }
        //if white only has a king and one/two knight(s)
        if (remainingPieces.containsKey("wn") && !remainingPieces.containsKey("wb")) {
            insufficientMaterialWhite = true;
        }
        //if white only has a king and a bishop
        if (remainingPieces.containsKey("wb") && !remainingPieces.containsKey("wn") && remainingPieces.get("wb") == 1) {
            insufficientMaterialWhite = true;
        }
        //if black only has a king and one/two knight(s)
        if (remainingPieces.containsKey("bn") && !remainingPieces.containsKey("bb")) {
            insufficientMaterialBlack = true;
        }
        //if black only has a king and a bishop
        if (remainingPieces.containsKey("bb") && !remainingPieces.containsKey("bn") && remainingPieces.get("bb") == 1) {
            insufficientMaterialBlack = true;
        }
        return insufficientMaterialWhite && insufficientMaterialBlack;
    }

    public void makeMove(Move move) {
        int startX = move.startX();
        int startY = move.startY();
        int endX = move.endX();
        int endY = move.endY();
        tile[startX][startY].setHasMoved(true);

        //castle if true
        if (tile[startX][startY].isType("k") && Math.abs(startX - endX) == 2) {
            if (startX - endX < 0) {
                tile[startX + 1][startY] = tile[startX + 3][startY];
                tile[startX + 3][startY] = null;
            } else {
                tile[startX - 1][startY] = tile[startX - 4][startY];
                tile[startX - 4][startY] = null;
            }
        }

        //if en passant was possible this move, make sure it's not possible next move
        if (enPassant.size() > 0) {
            tile[enPassant.get(0)][enPassant.get(1)].setEnPassantPossible(false);
            enPassant.remove(0);
            enPassant.remove(0);
        }

        //if pawn moved up 2 squares, make en passant possible for next move
        if (tile[startX][startY].isType("p") && Math.abs(startY - endY) == 2) {
            tile[startX][startY].setEnPassantPossible(true);
            enPassant.add(endX);
            enPassant.add(endY);
        }

        if (colorToMove.equals("w")) {
            colorToMove = "b";
        } else {
            colorToMove = "w";
        }
        currentMove++;

        //if it's a capture or a pawn move, reset lastCaptureOrPawnAdv
        if (tile[endX][endY] != null || tile[startX][startY].isType("p")) {
            lastCaptureOrPawnAdv = currentMove;
            if (tile[endX][endY] != null) {
                removePieceFromRemainingPieces(endX, endY);
            }
        }

        //if piece is pawn that moved on x-axis (means capture) but endPosition is empty => en passant
        if (tile[startX][startY].isType("p") && startX != endX && tile[endX][endY] == null) {
            removePieceFromRemainingPieces(endX, startY);
            tile[endX][startY] = null;
            lastCaptureOrPawnAdv = currentMove;
        }

        //make the move
        tile[endX][endY] = tile[startX][startY];
        tile[startX][startY] = null;

        //only add pieces position and color to move to history
        String[] splitFen = positionToFen().split(" ");
        String fenHistory = splitFen[0] + " " + splitFen[1];
        if (positionHistory.containsKey(fenHistory)) {
            int nr = positionHistory.get(fenHistory);
            nr++;
            positionHistory.put(fenHistory, nr);
        } else {
            positionHistory.put(fenHistory, 0);
        }
    }

    public void promote(int startX, int startY, int endX, int endY, String type) {
        tile[endX][endY] = tile[startX][startY];
        tile[endX][endY].setType(type);
        tile[startX][startY] = null;
        if (colorToMove.equals("w")) {
            colorToMove = "b";
        } else {
            colorToMove = "w";
        }
        currentMove++;

        String typeAndColor = tile[endX][endY].getColor() + type;
        int pieceAmount = 1;
        if (remainingPieces.containsKey(typeAndColor)) {
            pieceAmount = remainingPieces.get(typeAndColor) + 1;
        }
        remainingPieces.replace(typeAndColor, pieceAmount);
    }

    private void removePieceFromRemainingPieces(int x, int y) {
        String piece = tile[x][y].getColor();
        piece += tile[x][y].getType();
        //remove piece if it's the last one
        if (remainingPieces.get(piece) == 1) {
            remainingPieces.remove(piece);
        } else {
            int nr = remainingPieces.get(piece);
            nr--;
            remainingPieces.replace(piece, nr);
        }
    }

    public boolean isLegalMove(Move move) {
        int startX = move.startX();
        int startY = move.startY();
        int endX = move.endX();
        int endY = move.endY();
        //moving on boardCopy changes hasMoved for actual board to, so if needed, make hasMoved false again
        boolean resetHasMoved = !getTile(startX, startY).hasMoved();

        //make move on boardCopy and check if it's a check
        Board boardCopy = deepCopy();
        boardCopy.makeMove(move);
        String color = boardCopy.getTile(endX, endY).getColor();
        boolean isLegalMove = !boardCopy.isCheck(color);

        //reset hasMoved if it was false before this
        if (resetHasMoved) {
            getTile(startX, startY).setHasMoved(false);
        }

        return isLegalMove;
    }

    public boolean isCheck(String color) {
        int[] kingCoordinates = getKingCoordinates(color);
        return isPawnOrKingCheck(color, kingCoordinates) || isQueenOrBishopCheck(color, kingCoordinates) || isQueenOrRookCheck(color, kingCoordinates) || isKnightCheck(color, kingCoordinates);
    }

    private boolean isPawnOrKingCheck(String color, int[] kingCoordinates) {
        List<int[]> directions = MoveGenerator.generateSlidingPieceDirections("k");
        for (int[] direction : directions) {
            int currentX = kingCoordinates[0];
            int currentY = kingCoordinates[1];
            if (validCoordinates(currentX + direction[0], currentY + direction[1]) && (tile[currentX + direction[0]][currentY + direction[1]] == null || !tile[currentX + direction[0]][currentY + direction[1]].getColor().equals(color))) {
                currentX = currentX + direction[0];
                currentY = currentY + direction[1];
                //if nothing there or same color piece, continue
                if (tile[currentX][currentY] == null || tile[currentX][currentY].isColor(color)) {
                    continue;
                }
                //if the other king could capture you
                if (tile[currentX][currentY].isType("k")) {
                    return true;
                }
                //if a white black pawn could capture you
                if (color.equals("w") && ((currentX == kingCoordinates[0] - 1 && currentY == kingCoordinates[1] - 1) || (currentX == kingCoordinates[0] + 1 && currentY == kingCoordinates[1] - 1)) && tile[currentX][currentY].isType("p")) {
                    return true;
                }
                //if a white pawn could capture you
                if (color.equals("b") && ((currentX == kingCoordinates[0] + 1 && currentY == kingCoordinates[1] + 1) || (currentX == kingCoordinates[0] - 1 && currentY == kingCoordinates[1] + 1)) && tile[currentX][currentY].isType("p")) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isQueenOrBishopCheck(String color, int[] kingCoordinates) {
        List<int[]> directions = MoveGenerator.generateSlidingPieceDirections("b");
        for (int[] direction : directions) {
            int currentX = kingCoordinates[0];
            int currentY = kingCoordinates[1];
            //while coordinates are valid, keep moving until you meet an opposite colored queen or bishop
            while (validCoordinates(currentX + direction[0], currentY + direction[1]) && (tile[currentX + direction[0]][currentY + direction[1]] == null || !tile[currentX + direction[0]][currentY + direction[1]].isColor(color))) {
                currentX = currentX + direction[0];
                currentY = currentY + direction[1];
                if (tile[currentX][currentY] == null) {
                    continue;
                }
                if (tile[currentX][currentY].isType("b") || tile[currentX][currentY].isType("q")) {
                    return true;
                }
                break;
            }
        }
        return false;
    }

    private boolean isQueenOrRookCheck(String color, int[] kingCoordinates) {
        List<int[]> directions = MoveGenerator.generateSlidingPieceDirections("r");
        for (int[] direction : directions) {
            int currentX = kingCoordinates[0];
            int currentY = kingCoordinates[1];
            //while coordinates are valid, keep moving until you meet an opposite colored queen or rook, stop if we meet our own piece
            while (validCoordinates(currentX + direction[0], currentY + direction[1]) && (tile[currentX + direction[0]][currentY + direction[1]] == null || !tile[currentX + direction[0]][currentY + direction[1]].isColor(color))) {
                currentX = currentX + direction[0];
                currentY = currentY + direction[1];
                if (tile[currentX][currentY] == null) {
                    continue;
                }
                if (tile[currentX][currentY].isType("r") || tile[currentX][currentY].isType("q")) {
                    return true;
                }
                break;
            }
        }
        return false;
    }

    private boolean isKnightCheck(String color, int[] kingCoordinates) {
        List<int[]> directions = MoveGenerator.generateKnightDirections();
        int x = kingCoordinates[0];
        int y = kingCoordinates[1];
        for (int[] direction : directions) {
            //see if there is an opposite colored knight on that tile
            if (validCoordinates(x + direction[0], y + direction[1]) && (tile[x + direction[0]][y + direction[1]] == null || !tile[x + direction[0]][y + direction[1]].getColor().equals(color))) {
                if (tile[x + direction[0]][y + direction[1]] == null) {
                    continue;
                }
                if (tile[x + direction[0]][y + direction[1]].isType("n")) {
                    return true;
                }
            }
        }
        return false;
    }

    public int[] getKingCoordinates(String color) {
        int[] kingCoordinates = new int[2];
        //white king is more likely to be at the end of the board
        if (color.equals("w")) {
            for (int y = 7; y >= 0; y--) {
                for (int x = 7; x >= 0; x--) {
                    if (tile[x][y] == null) {
                        continue;
                    }
                    if (tile[x][y].isType("k") && tile[x][y].isColor(color)) {
                        kingCoordinates[0] = x;
                        kingCoordinates[1] = y;
                        return kingCoordinates;
                    }
                }
            }
        } else {
            //black king is more likely to be at the start of the board
            for (int y = 0; y < 8; y++) {
                for (int x = 0; x < 8; x++) {
                    if (tile[x][y] == null) {
                        continue;
                    }
                    if (tile[x][y].isType("k") && tile[x][y].isColor(color)) {
                        kingCoordinates[0] = x;
                        kingCoordinates[1] = y;
                        return kingCoordinates;
                    }
                }
            }
        }
        return kingCoordinates;
    }

    public String positionToFen() {
        StringBuilder fen = new StringBuilder();
        StringBuilder castleRightsWhite = new StringBuilder();
        StringBuilder castleRightsBlack = new StringBuilder();
        String castleRights;
        String enPassant = "-";
        int emptyRowCont = 0;
        for (int y = 0; y < 8; y++) {
            for (int x = 0; x < 8; x++) {
                //count empty tiles
                if (tile[x][y] == null) {
                    emptyRowCont++;
                    continue;
                }
                //if we get here we found a piece, so add how many empty tiles there were
                if (emptyRowCont > 0) {
                    fen.append(emptyRowCont);
                    emptyRowCont = 0;
                }
                String piece = tile[x][y].getType();
                //add castling rights
                if (piece.equals("k")) {
                    if (tile[x][y].isColor("w")) {
                        castleRightsWhite.append(MoveGenerator.getCastleRights(x, y, this).toUpperCase());
                    } else {
                        castleRightsBlack.append(MoveGenerator.getCastleRights(x, y, this));
                    }
                }
                //add enPassant
                if (piece.equals("p") && tile[x][y].isEnPassantPossible()) {
                    if (tile[x][y].isColor("w")) {
                        enPassant = Utils.getChessCoordinates(x, y + 1);
                    } else {
                        enPassant = Utils.getChessCoordinates(x, y - 1);
                    }
                }
                //represent piece as white or black and add to fen
                if (tile[x][y].isColor("w")) {
                    piece = piece.toUpperCase();
                }
                fen.append(piece);
            }
            //add how many empty tiles were at the end, if any
            if (emptyRowCont > 0) {
                fen.append(emptyRowCont);
                emptyRowCont = 0;
            }
            //add the slash at the end of row
            if (y != 7) {
                fen.append("/");
            }
        }
        //constructing castleRights
        if (castleRightsWhite.length() == 0 && castleRightsBlack.length() == 0) {
            castleRights = "-";
        } else {
            castleRights = castleRightsWhite + castleRightsBlack.toString();
        }
        fen.append(" ").append(colorToMove).append(" ").append(castleRights).append(" ").append(enPassant).append(" ").append(currentMove - lastCaptureOrPawnAdv).append(" ").append(currentMove / 2);
        return fen.toString();
    }

    private void makeBoardFromFen(String fenString) {
        String[] fenSplit = fenString.split(" ");
        fenString = fenSplit[0];
        setColorToMove(fenSplit[1]);
        char[] castleRights = fenSplit[2].toCharArray();
        if (colorToMove.equals("w")) {
            if (fenSplit[5].equals("1")) {
                currentMove = 2;
            }
            else {
                currentMove = (Integer.parseInt(fenSplit[5]) * 2) - 1;
            }
        }
        else {
            currentMove = (Integer.parseInt(fenSplit[5]) * 2);
        }
        setLastCaptureOrPawnAdv(currentMove - Integer.parseInt(fenSplit[4]));
        int x = 0;
        int y = 0;
        String color;
        //place pieces on the board
        for (int index = 0; index < fenString.length(); index++) {
            if (Character.isDigit(fenString.charAt(index))) {
                x += Character.getNumericValue(fenString.charAt(index));
                continue;
            }
            if (fenString.charAt(index) == '/') {
                x = 0;
                y += 1;
                continue;
            }
            if (Character.isUpperCase(fenString.charAt(index))) {
                color = "w";
            } else {
                color = "b";
            }
            tile[x][y] = new Piece(color, Character.toString(Character.toLowerCase(fenString.charAt(index))));

            //assume they can't castle
            if (tile[x][y].isType("k") || tile[x][y].isType("r")) {
                tile[x][y].setHasMoved(true);
            }

            //pawn moved if it's not on its start position
            if (tile[x][y].isType("p") && (y != 6 && y != 1)) {
                tile[x][y].setHasMoved(true);
            }
            x++;
        }

        //set up castling rights
        for (char castle : castleRights) {
            if (castle == 'K') {
                if (tile[4][7].isType("k") && tile[7][7].isType("r")) {
                    tile[4][7].setHasMoved(false);
                    tile[7][7].setHasMoved(false);
                }
                continue;
            }
            if (castle == 'Q') {
                if (tile[4][7].isType("k") && tile[0][7].isType("r")) {
                    tile[4][7].setHasMoved(false);
                    tile[0][7].setHasMoved(false);
                }
                continue;
            }
            if (castle == 'k') {
                if (tile[4][0].isType("k") && tile[7][0].isType("r")) {
                    tile[4][0].setHasMoved(false);
                    tile[7][0].setHasMoved(false);
                }
                continue;
            }
            if (castle == 'q') {
                if (tile[4][0].isType("k") && tile[7][0].isType("r")) {
                    tile[4][0].setHasMoved(false);
                    tile[0][0].setHasMoved(false);
                }
            }
        }

        //make en passant possible if necessary
        if (!fenSplit[3].equals("-")) {
            int[] enPassantCoordinates = Utils.getXYFromChessCoordinates(fenSplit[3]);
            //if it's on y = 2 that means we are dealing with black
            if (enPassantCoordinates[1] == 2) {
                tile[enPassantCoordinates[0]][enPassantCoordinates[1] + 1].setEnPassantPossible(true);
            }
            else {
                tile[enPassantCoordinates[0]][enPassantCoordinates[1] - 1].setEnPassantPossible(true);
            }
        }

        //make sure remainingPieces is accurate
        remainingPieces.clear();
        String[] colors = {"w", "b"};
        String[] pieces = {"q", "b", "r", "p", "n"};
        for (String currentColor : colors) {
            for (String piece : pieces) {
                String colorPlusPiece = currentColor + piece;
                if (currentColor.equals("w")) {
                    piece = piece.toUpperCase();
                }
                int pieceNumber = fenString.length() - fenString.replace(piece, "").length();
                remainingPieces.put(colorPlusPiece, pieceNumber);
            }
        }
    }

    public void printBoard() {
        for (int y = 0; y < 8; y++) {
            for (int x = 0; x < 8; x++) {
                if (tile[x][y] == null) {
                    System.out.print("- ");
                    continue;
                }
                String piece = tile[x][y].getType();
                if (tile[x][y].isColor("w")) {
                    piece = piece.toUpperCase();
                }
                System.out.print(piece + " ");
            }
            System.out.println();
        }
    }

    public boolean validCoordinates(int x, int y) {
        return x >= 0 && x < 8 && y >= 0 && y < 8;
    }

    public Piece getTile(int x, int y) {
        return tile[x][y];
    }

    public boolean isKing(int x, int y) {
        return tile[x][y].getType().equals("k");
    }

    public boolean isPawn(int x, int y) {
        return tile[x][y].getType().equals("p");
    }

    public String getColorToMove() {
        return colorToMove;
    }

    public void setTile(Piece[][] tile) {
        this.tile = tile;
    }

    public void setEnPassant(List<Integer> enPassant) {
        this.enPassant = enPassant;
    }

    public void setColorToMove(String colorToMove) {
        this.colorToMove = colorToMove;
    }

    public void setCurrentMove(int currentMove) {
        this.currentMove = currentMove;
    }

    public void setLastCaptureOrPawnAdv(int lastCaptureOrPawnAdv) {
        this.lastCaptureOrPawnAdv = lastCaptureOrPawnAdv;
    }

    public void setPositionHistory(HashMap<String, Integer> positionHistory) {
        this.positionHistory = positionHistory;
    }

    public void setRemainingPieces(HashMap<String, Integer> remainingPieces) {
        this.remainingPieces = remainingPieces;
    }

    public void setGameOver(boolean gameOver) {
        this.gameOver = gameOver;
    }

    public int getCurrentMove() {
        return currentMove;
    }

    public boolean getGameOver() {
        return gameOver;
    }
}