import QtQuick

Rectangle {
    id: boardPanel
    color: "#000000"

    ChessBoard {
        anchors.centerIn: parent
        width: Math.min(boardPanel.width, boardPanel.height)
        height: width
        piecesModel: boardController.piecesModel
        whitePerspective: boardController.whitePerspective
        selectedSquare: boardController.selectedSquare
        legalTargets: boardController.legalTargets
        lastMove: boardController.lastMove
        promotionChoices: boardController.promotionChoices

        onSquareClicked: boardController.handleSquareClick(square)
    }
}
