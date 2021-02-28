package com.example.raxar.view.graph

data class Node(
    var xPos: Double = 0.0,
    var yPos: Double = 0.0,
    val state: NodeState = NodeState()
)