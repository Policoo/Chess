import QtQuick
import QtQuick.Layouts

Window {
    id: root
    width: 1200
    height: 600
    visible: true
    title: qsTr("Chess")

    property bool debugMode: false
    property bool opponentInitialized: false

    function isValidFen(fen) {
        var trimmed = fen.trim()
        if (trimmed.length === 0)
            return false

        var parts = trimmed.split(/\s+/)
        if (parts.length !== 6)
            return false

        var placement = parts[0]
        var ranks = placement.split("/")
        if (ranks.length !== 8)
            return false

        for (var r = 0; r < 8; ++r) {
            var rank = ranks[r]
            if (rank.length === 0)
                return false
            var count = 0
            for (var i = 0; i < rank.length; ++i) {
                var ch = rank[i]
                if (ch >= "1" && ch <= "8") {
                    count += parseInt(ch)
                } else if ("prnbqkPRNBQK".indexOf(ch) !== -1) {
                    count += 1
                } else {
                    return false
                }
            }
            if (count !== 8)
                return false
        }

        var sideToMove = parts[1]
        if (sideToMove !== "w" && sideToMove !== "b")
            return false

        var castling = parts[2]
        if (!/^(-|[KQkq]+)$/.test(castling))
            return false

        var ep = parts[3]
        if (!/^(-|[a-h][36])$/.test(ep))
            return false

        if (isNaN(parseInt(parts[4])) || isNaN(parseInt(parts[5])))
            return false

        return true
    }

    RowLayout {
        anchors.fill: parent
        spacing: 0

        OptionsPanel {
            id: optionsPanel
            Layout.preferredWidth: 1
            Layout.fillWidth: true
            Layout.fillHeight: true

            onResetRequested: {
                boardController.resetBoard()
                if (!root.debugMode) {
                    dialogPanel.displayMessage("Board has been reset")
                }
            }

            onDebugToggled: function(enabled) {
                root.debugMode = enabled
                if (root.debugMode) {
                    dialogPanel.displayDebugString(boardController.debugString())
                } else {
                    dialogPanel.displayMessage("Debug mode off")
                }
            }

            onUndoRequested: {
                gameController.undo()
            }

            onFlipRequested: {
                boardController.togglePerspective()
                if (!root.debugMode) {
                    var perspective = boardController.whitePerspective ? "white" : "black"
                    dialogPanel.displayMessage("You are now playing as " + perspective + "!")
                }
            }

            onOpponentChanged: function(index, name) {
                gameController.setOpponent(index)
                if (!root.opponentInitialized) {
                    root.opponentInitialized = true
                    return
                }
                if (!root.debugMode) {
                    dialogPanel.displayMessage("Opponent set to " + name)
                }
            }

            onPerftRequested: function(depth) {
                gameController.goPerft(depth)
                if (!root.debugMode) {
                    dialogPanel.displayMessage("Running perft to depth " + depth + "…")
                }
            }

            onEngineMatchRequested: function(engine1, engine2) {
                gameController.startEngineMatch(engine1, engine2)
                if (!root.debugMode) {
                    dialogPanel.displayMessage("Starting engine battle: " + engine1 + " vs " + engine2 + "…")
                }
            }

            onFenSubmitted: function(fen) {
                var trimmed = fen.trim()
                if (!root.isValidFen(trimmed)) {
                    dialogPanel.displayMessage("Invalid FEN string")
                    return
                }

                boardController.makeBoardFromFen(trimmed)
                if (!root.debugMode) {
                    dialogPanel.displayMessage("Loaded position from FEN")
                }
            }
        }

        BoardPanel {
            Layout.preferredWidth: 2
            Layout.fillWidth: true
            Layout.fillHeight: true
        }

        DialogPanel {
            id: dialogPanel
            Layout.preferredWidth: 1
            Layout.fillWidth: true
            Layout.fillHeight: true
        }
    }

    // Keep debug dialog output in sync with board changes
    Connections {
        target: boardController
        function onBoardChanged() {
            if (root.debugMode) {
                dialogPanel.displayDebugString(boardController.debugString())
            }
        }
    }

    // Show perft results in the dialog box
    Connections {
        target: gameController
        function onPerftResultsReady(results) {
            if (!results || results.length === 0) {
                return
            }

            var decorated = []
            decorated.push(results[0])

            var correct = true
            var nodesIndex = -1
            var i

            for (i = 1; i < results.length; ++i) {
                var line = results[i]

                if (line.indexOf("Nodes") !== -1) {
                    nodesIndex = i
                    continue
                }

                if (line.indexOf("found") !== -1) {
                    correct = false
                    break
                }

                var isError = line.indexOf("StockFish") !== -1
                var suffix = isError
                        ? ' <font size="+1" color="red">&#10008;</font>'
                        : ' <font size="+1" color="green">&#10004;</font>'
                decorated.push(line + suffix)
            }

            if (correct) {
                // Append the nodes line with a green check if available
                if (nodesIndex !== -1) {
                    var nodesLine = results[nodesIndex]
                    var nodesSuffix = ' <font size="+1" color="green">&#10004;</font>'
                    decorated.push(nodesLine + nodesSuffix)
                }
            } else {
                // If incorrect, append the remaining lines unchanged
                for (var j = i - 1; j < results.length; ++j) {
                    decorated.push(results[j])
                }
            }

            dialogPanel.displayCountResults(decorated)
        }
    }

    // Show engine battle results
    Connections {
        target: gameController
        function onEngineMatchResultsReady(results) {
            dialogPanel.displayMessage(results)
        }
    }
}
