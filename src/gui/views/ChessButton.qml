import QtQuick

Rectangle {
    id: root

    // Public API
    property string text: ""
    property url iconSource: ""
    property bool checkable: false
    property bool checked: false

    signal clicked()

    radius: 5
    border.width: 2

    // State flags
    property bool pressed: false
    property bool hovered: false

    // Color palette for different states
    property color normalColor: "#3b82f6"
    property color normalHoverColor: "#2563eb"
    property color normalPressedColor: "#1d4ed8"

    property color checkedColor: "#ef4444"
    property color checkedHoverColor: "#dc2626"
    property color checkedPressedColor: "#b91c1c"

    property color borderBaseColor: "#0f172a"

    border.color: borderBaseColor

    implicitHeight: 27
    implicitWidth: Math.max(50, contentRow.implicitWidth + 16)

    color: checkable && checked
           ? (pressed ? checkedPressedColor
                      : (hovered ? checkedHoverColor : checkedColor))
           : (pressed ? normalPressedColor
                      : (hovered ? normalHoverColor : normalColor))

    Row {
        id: contentRow
        anchors.centerIn: parent
        spacing: iconSource !== "" && root.text !== "" ? 4 : 0

        Image {
            visible: iconSource !== ""
            source: iconSource
            width: 20
            height: 20
            fillMode: Image.PreserveAspectFit
        }

        Text {
            id: label
            text: root.text
            anchors.centerIn: parent
            visible: root.text !== ""
            color: "black"
            font.family: "Arial"
            font.pixelSize: 13
            font.bold: true
            elide: Text.ElideRight
        }
    }

    MouseArea {
        anchors.fill: parent
        hoverEnabled: true

        onPressed: root.pressed = true
        onReleased: root.pressed = false

        onEntered: root.hovered = true
        onExited: root.hovered = false

        onClicked: {
            if (root.checkable)
                root.checked = !root.checked
            root.clicked()
        }
    }
}
