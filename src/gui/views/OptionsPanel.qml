import QtQuick
import QtQuick.Layouts
import QtQuick.Controls // for other controls later if needed

Rectangle {
    id: root
    color: "#000000"

    // Emitted when the Reset button is pressed
    signal resetRequested()
    // Emitted when the Debug button is toggled
    signal debugToggled(bool enabled)
    // Emitted when the Undo button is pressed
    signal undoRequested()
    // Emitted when the Flip button is pressed
    signal flipRequested()
    // Emitted when the FEN confirm button is pressed
    signal fenSubmitted(string fen)
    // Emitted when the user starts a perft search
    signal perftRequested(int depth)
    // Emitted when the opponent selection changes
    signal opponentChanged(int index, string name)
    // Emitted when an engine match is requested
    signal engineMatchRequested(string engine1, string engine2)

    ColumnLayout {
        anchors.fill: parent
        anchors.margins: 8
        spacing: 8

        // Top controls: Reset / Debug / Undo / Flip
        Rectangle {
            Layout.fillWidth: true
            Layout.preferredHeight: 40
            color: "#3d3d3d"
            radius: 4

            Row {
                anchors.centerIn: parent
                spacing: 24

                ChessButton {
                    id: resetButton
                    text: qsTr("Reset")
                    onClicked: root.resetRequested()
                }

                ChessButton {
                    id: debugButton
                    text: qsTr("Debug")
                    checkable: true
                    onClicked: root.debugToggled(checked)
                }

                ChessButton {
                    id: undoButton
                    text: qsTr("Undo")
                    onClicked: root.undoRequested()
                }

                ChessButton {
                    id: flipButton
                    iconSource: "qrc:/resources/flip board.png"
                    onClicked: root.flipRequested()
                }
            }
        }

        // FEN section
        Rectangle {
            Layout.fillWidth: true
            Layout.preferredHeight: 100
            color: "#3d3d3d"
            radius: 4

            Column {
                anchors.fill: parent
                anchors.margins: 8
                spacing: 6

                Text {
                    id: fenLabel
                    text: qsTr("Make board from FEN string")
                    color: "white"
                    font.bold: true
                    font.pixelSize: 13
                    horizontalAlignment: Text.AlignHCenter
                    anchors.horizontalCenter: parent.horizontalCenter
                }

                ChessTextField {
                    id: fenField
                    anchors.horizontalCenter: parent.horizontalCenter
                    width: parent.width - 16
                    placeholderText: qsTr("e.g. rnbqkbnr/pppppppp/8/...")
                }

                Row {
                    anchors.horizontalCenter: parent.horizontalCenter

                    ChessButton {
                        id: confirmFenButton
                        text: qsTr("Confirm")
                        width: 80
                        onClicked: {
                            root.fenSubmitted(fenField.text)
                            fenField.text = ""
                        }
                    }
                }
            }
        }

        // perft section
        Rectangle {
            Layout.fillWidth: true
            Layout.preferredHeight: 60
            color: "#3d3d3d"
            radius: 4

            RowLayout {
                anchors.fill: parent
                anchors.margins: 8
                spacing: 8

                Text {
                    text: qsTr("Depth:")
                    color: "white"
                    font.bold: true
                    font.pixelSize: 13
                    verticalAlignment: Text.AlignVCenter
                    Layout.alignment: Qt.AlignVCenter
                }

                ChessTextField {
                    id: perftDepthField
                    Layout.preferredWidth: 40
                    inputMethodHints: Qt.ImhDigitsOnly
                    horizontalAlignment: Text.AlignHCenter
                    Layout.alignment: Qt.AlignVCenter
                }

                Item { implicitWidth: 8; implicitHeight: 1 }

                ChessButton {
                    id: goPerftButton
                    text: qsTr("Go perft")
                    Layout.preferredWidth: 90
                    Layout.alignment: Qt.AlignVCenter
                    onClicked: {
                        var d = parseInt(perftDepthField.text)
                        if (!isNaN(d))
                            root.perftRequested(d)
                    }
                }

                Item { Layout.fillWidth: true }
            }
        }

        // Opponent selection
        Rectangle {
            Layout.fillWidth: true
            Layout.preferredHeight: 60
            color: "#3d3d3d"
            radius: 4

            RowLayout {
                anchors.fill: parent
                anchors.margins: 8
                spacing: 8

                Text {
                    text: qsTr("Opponent:")
                    color: "white"
                    font.bold: true
                    font.pixelSize: 13
                    Layout.alignment: Qt.AlignVCenter
                }

                ChessComboBox {
                    id: opponentCombo
                    Layout.preferredWidth: 170
                    Layout.alignment: Qt.AlignVCenter

                    model: [qsTr("Yourself :("), qsTr("Random"), qsTr("Greedy")]

                    onCurrentIndexChanged: {
                        if (currentIndex >= 0 && currentIndex < model.length)
                            root.opponentChanged(currentIndex, model[currentIndex])
                    }
                }

                Item { Layout.fillWidth: true }
            }
        }

        // Engine battle section
        Rectangle {
            Layout.fillWidth: true
            Layout.preferredHeight: 100
            color: "#3d3d3d"
            radius: 4

            ColumnLayout {
                anchors.fill: parent
                anchors.margins: 8
                spacing: 6

                Text {
                    text: qsTr("Set up engine battle")
                    color: "white"
                    font.bold: true
                    font.pixelSize: 14
                    horizontalAlignment: Text.AlignHCenter
                    Layout.alignment: Qt.AlignHCenter
                }

                Row {
                    anchors.horizontalCenter: parent.horizontalCenter
                    spacing: 8

                    ChessComboBox {
                        id: engine1Combo
                        width: 110
                        model: [qsTr("Random"), qsTr("Greedy")]
                    }

                    Text {
                        text: qsTr("vs.")
                        color: "white"
                        font.bold: true
                        font.pixelSize: 14
                        horizontalAlignment: Text.AlignHCenter
                        verticalAlignment: Text.AlignVCenter
                    }

                    ChessComboBox {
                        id: engine2Combo
                        width: 110
                        model: [qsTr("Random"), qsTr("Greedy")]
                    }
                }

                RowLayout {
                    Layout.fillWidth: true

                    Item { Layout.fillWidth: true }

                    ChessButton {
                        id: startMatchButton
                        text: qsTr("Start battle ⚔️")
                        Layout.preferredWidth: 130
                        onClicked: root.engineMatchRequested(engine1Combo.currentText,
                                                             engine2Combo.currentText)
                    }

                    Item { Layout.fillWidth: true }
                }
            }
        }

        // TODO: Eval section
        Rectangle {
            Layout.fillWidth: true
            Layout.fillHeight: true
            color: "#262626"
        }
    }
}
