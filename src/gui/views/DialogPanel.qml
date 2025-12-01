import QtQuick
import QtQuick.Layouts
import QtQuick.Controls

Rectangle {
    id: root
    color: "#000000"

    ColumnLayout {
        anchors.fill: parent
        anchors.margins: 8
        spacing: 8

        // Header bar
        Rectangle {
            Layout.fillWidth: true
            Layout.preferredHeight: 35
            color: "#3d3d3d"
            radius: 4

            Text {
                anchors.centerIn: parent
                text: qsTr("Dialog box")
                color: "white"
                font.pixelSize: 20
                font.bold: true
                font.family: "Noto Sans"
            }
        }

        // Scrollable content in a panel
        Rectangle {
            Layout.fillWidth: true
            Layout.fillHeight: true
            color: "#3d3d3d"
            radius: 4

            ScrollView {
                anchors.fill: parent
                anchors.margins: 0
                clip: true
                ScrollBar.vertical.policy: ScrollBar.AsNeeded

                background: Rectangle {
                    color: "transparent"
                }

                Rectangle {
                    id: scrollContent
                    color: "transparent"
                    width: parent.width
                    implicitHeight: messagesColumn.implicitHeight + 8

                    Column {
                        id: messagesColumn
                        width: parent.width
                        spacing: 4
                        anchors {
                            top: parent.top
                            left: parent.left
                            right: parent.right
                            margins: 4
                        }
                    }
                }
            }
        }
    }

    function clear() {
        for (let i = messagesColumn.children.length - 1; i >= 0; --i) {
            messagesColumn.children[i].destroy();
        }
    }

    function addTextBlock(text, isDebug, isBold, isRich) {
        const component = Qt.createComponent("qrc:/gui/DialogTextBlock.qml");
        if (component.status === Component.Ready) {
            const block = component.createObject(messagesColumn, {
                "text": text,
                "debug": isDebug,
                "bold": isBold,
                "rich": isRich
            });
            if (block === null) {
                console.warn("Failed to create DialogTextBlock");
            }
        } else {
            console.warn("DialogTextBlock component not ready:", component.status);
        }
    }

    function displayMessage(message) {
        clear();
        addTextBlock(message, false, false, false);
    }

    function displayDebugString(debugString) {
        clear();
        addTextBlock(debugString, true, false, false);
    }

    function displayCountResults(results) {
        clear();
        if (!results || results.length === 0)
            return;

        addTextBlock(results[0], false, true, false);

        for (let i = 1; i < results.length; ++i) {
            const rich = results[i].indexOf("<") !== -1 && results[i].indexOf(">") !== -1;
            addTextBlock(results[i], false, false, rich);
        }
    }
}
