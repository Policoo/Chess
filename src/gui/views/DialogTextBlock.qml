import QtQuick

Rectangle {
    id: root

    property string text: ""
    property bool debug: false
    property bool bold: false
    property bool rich: false

    color: "#3d3d3d"
    width: parent ? parent.width : 270
    radius: 4
    implicitHeight: textItem.implicitHeight + 10

    Text {
        id: textItem
        anchors.fill: parent
        anchors.margins: 6
        text: root.text
        color: "white"
        wrapMode: Text.WordWrap
        font.pixelSize: debug ? 11 : 13
        font.bold: root.bold
        textFormat: root.rich ? Text.RichText : Text.PlainText
        font.family: debug ? "Fira Code" : "Noto Sans"
        lineHeight: 1.2
        lineHeightMode: Text.ProportionalHeight
    }
}
