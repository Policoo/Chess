import QtQuick
import QtQuick.Controls

ComboBox {
    id: control

    implicitHeight: 27
    font.pixelSize: 13

    // Main field background
    background: Rectangle {
        radius: 3
        color: control.enabled ? "#565656" : "#3a3a3a"
        border.color: control.activeFocus ? "#00A2E8" : "#1f1f1f"
        border.width: 2
    }

    // Selected text (closed state)
    contentItem: Text {
        text: control.displayText
        color: "white"
        verticalAlignment: Text.AlignVCenter
        elide: Text.ElideRight
        leftPadding: 6
        rightPadding: 20
        font.pixelSize: 13
    }

    // Drop-down indicator
    indicator: Text {
        text: "\u25BE" // ▼
        color: "#e5e7eb"
        font.pixelSize: 11
        anchors.right: parent.right
        anchors.rightMargin: 6
        anchors.verticalCenter: parent.verticalCenter
    }

    popup: Popup {
        y: control.height + 2
        width: control.width
        implicitHeight: listView.contentHeight + 4
        padding: 2

        background: Rectangle {
            radius: 4
            color: "#111827"
            border.width: 1
            border.color: "#1f2933"
        }

        contentItem: ListView {
            id: listView
            clip: true
            implicitHeight: contentHeight

            // Use the combo's model directly so nothing gets filtered out
            model: control.model
            currentIndex: control.currentIndex

            delegate: ItemDelegate {
                required property int index
                required property var modelData

                width: listView.width

                contentItem: Text {
                    text: modelData
                    color: "white"
                    verticalAlignment: Text.AlignVCenter
                    elide: Text.ElideRight
                    leftPadding: 8
                    rightPadding: 8
                    font.pixelSize: 13
                }

                background: Rectangle {
                    anchors.fill: parent
                    color: (listView.currentIndex === index || hovered)
                           ? "#4b5563"      // hover / current
                           : "#374151"      // normal
                }

                onClicked: {
                    control.currentIndex = index
                    control.popup.close()
                }
            }

            ScrollIndicator.vertical: ScrollIndicator { }
        }
    }
}

