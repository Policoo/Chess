import QtQuick
import QtQuick.Controls

TextField {
    id: root

    FontLoader {
        id: firaCodeFont
        source: "qrc:/resources/FiraCode-VariableFont_wght.ttf"
    }

    implicitHeight: 28

    font.family: firaCodeFont.name !== "" ? firaCodeFont.name : font.family
    font.pixelSize: 13

    color: "white"
    selectionColor: "#00A2E8"
    selectedTextColor: "white"
    placeholderTextColor: "#b0b0b0"

    verticalAlignment: Text.AlignVCenter

    leftPadding: 6
    rightPadding: 6
    topPadding: 4
    bottomPadding: 4

    background: Rectangle {
        radius: 3
        color: "#565656"
        border.color: "#1f1f1f"
        border.width: 2
    }
}
