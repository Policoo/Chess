import QtQuick

Item {
    id: root

    property int pieceType: 0
    property int pieceColor: 0

    readonly property int tileSize: 200

    readonly property int baseIndex: {
        switch (pieceType) {
            case 2:  return 0; // KING
            case 6:  return 1; // QUEEN
            case 4:  return 2; // BISHOP
            case 3:  return 3; // KNIGHT
            case 5:  return 4; // ROOK
            case 1:  return 5; // PAWN
            default: return -1;
        }
    }

    readonly property int spriteIndex: baseIndex < 0
                                       ? -1
                                       : baseIndex + (pieceColor === 1 ? 6 : 0)

    readonly property int spriteRow: spriteIndex < 0 ? 0 : (spriteIndex >= 6 ? 1 : 0)
    readonly property int spriteCol: spriteIndex < 0 ? 0 : (spriteIndex % 6)

    readonly property rect clipRect: Qt.rect(spriteCol * tileSize,
                                             spriteRow * tileSize,
                                             tileSize,
                                             tileSize)

    Image {
        anchors.fill: parent
        source: "qrc:/resources/chess pieces.png"
        fillMode: Image.PreserveAspectFit
        sourceClipRect: root.clipRect
        smooth: true
        mipmap: true
        visible: root.spriteIndex >= 0
    }
}

