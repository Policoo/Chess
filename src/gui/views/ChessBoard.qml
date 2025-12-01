import QtQuick

Rectangle {
    id: root

    // Overall background around board + coordinates
    color: "#363636"

    // If true, board is shown from White's perspective (a1 bottom‑left).
    // If false, board is shown from Black's perspective (a8 bottom‑left).
    property bool whitePerspective: true

    // Exposed board state: list of { square, type, color }
    // squareIndex 0..63, where 0 is top‑left (a8)
    property var piecesModel: []

    // Highlighting from controller
    property int selectedSquare: -1
    property var legalTargets: []
    property var lastMove: ({})
    // Promotion choices: array of { square, type, color }
    property var promotionChoices: []

    signal squareClicked(int square)

    // Square board area (like the 512x512 inside a 552x552 widget)
    Rectangle {
        id: boardArea
        anchors.centerIn: parent
        // Leave a bit of padding around, but always keep a positive size
        width: Math.max(0, Math.min(root.width, root.height) - 40)
        height: width
        color: "transparent"
        radius: 6
        clip: true
    }

    // 8x8 board grid
    Grid {
        id: grid
        anchors.fill: boardArea
        rows: 8
        columns: 8

        Repeater {
            model: 64
            delegate: Rectangle {
                readonly property int file: index % 8
                readonly property int rank: Math.floor(index / 8)

                width: boardArea.width / 8
                height: boardArea.height / 8

                readonly property bool isLight: ((file + rank) % 2 === 0)

                readonly property bool isSelected: index === root.selectedSquare

                readonly property bool isLegalTarget: root.legalTargets &&
                                                      root.legalTargets.indexOf(index) !== -1

                readonly property bool isLastMoveStart: root.lastMove &&
                                                        root.lastMove.start === index

                readonly property bool isLastMoveEnd: root.lastMove &&
                                                      root.lastMove.end === index

                color: {
                    // legal move targets should take precedence so
                    // capture squares show up in red even if they
                    // were part of the last move.
                    if (isLegalTarget) {
                        return isLight ? "#f86d5b" : "#da4432";
                    }

                    // selection / last-move highlight
                    if (isSelected || isLastMoveStart || isLastMoveEnd) {
                        return isLight ? "#f4e979" : "#d9c252";
                    }

                    // base board colors
                    return isLight ? "#ebd5b1" : "#b48662";
                }
            }
        }
    }

    // Pieces layer on top of the board
    Item {
        id: piecesLayer
        anchors.fill: boardArea

        Repeater {
            model: root.piecesModel
            delegate: PieceSprite {
                width: boardArea.width / 8
                height: boardArea.height / 8

                pieceType: modelData.type
                pieceColor: modelData.color

                readonly property int square: modelData.square
                readonly property int file: square % 8
                readonly property int rank: Math.floor(square / 8)

                x: file * (boardArea.width / 8)
                y: rank * (boardArea.height / 8)
            }
        }
    }

    // Promotion overlay: clickable piece choices on top of the board
    Item {
        id: promotionLayer
        anchors.fill: boardArea
        visible: root.promotionChoices && root.promotionChoices.length > 0

        Repeater {
            model: root.promotionChoices
            delegate: Rectangle {
                readonly property int square: modelData.square
                readonly property int file: square % 8
                readonly property int rank: Math.floor(square / 8)

                width: boardArea.width / 8
                height: boardArea.height / 8
                x: file * (boardArea.width / 8)
                y: rank * (boardArea.height / 8)

                // Solid white promotion squares, no outline
                color: "white"

                PieceSprite {
                    anchors.centerIn: parent
                    width: parent.width * 0.9
                    height: parent.height * 0.9
                    pieceType: modelData.type
                    pieceColor: modelData.color
                }
            }
        }
    }

    // Mouse handling for clicks → squares
    MouseArea {
        anchors.fill: boardArea
        onClicked: function (mouse) {
            const squareSize = boardArea.width / 8;
            const file = Math.floor(mouse.x / squareSize);
            const rank = Math.floor(mouse.y / squareSize);
            if (file < 0 || file > 7 || rank < 0 || rank > 7)
                return;
            const square = rank * 8 + file;
            root.squareClicked(square);
        }
    }

    // Rank coordinates (8–1) on the left, outside the board
    Column {
        anchors.verticalCenter: boardArea.verticalCenter
        anchors.right: boardArea.left
        width: 20
        spacing: 0

        Repeater {
            model: 8
            delegate: Rectangle {
                width: 20
                height: boardArea.height / 8
                color: "#363636"

                Text {
                    anchors.centerIn: parent
                    text: root.whitePerspective ? (8 - index) : (index + 1)
                    color: "white"
                    font.bold: true
                    font.pixelSize: 14
                }
            }
        }
    }

    // File coordinates (A–H) below the board, with side spacers
    Row {
        anchors.top: boardArea.bottom
        anchors.horizontalCenter: boardArea.horizontalCenter
        height: 20
        spacing: 0

        Rectangle {
            width: 20
            height: 20
            color: "#363636"
        }

        Repeater {
            model: 8
            delegate: Rectangle {
                width: boardArea.width / 8
                height: 20
                color: "#363636"

                Text {
                    anchors.centerIn: parent
                    text: root.whitePerspective
                          ? String.fromCharCode(65 + index)      // A‑H
                          : String.fromCharCode(72 - index)      // H‑A
                    color: "white"
                    font.bold: true
                    font.pixelSize: 14
                }
            }
        }

        Rectangle {
            width: 20
            height: 20
            color: "#363636"
        }
    }
}
