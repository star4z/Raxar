package com.example.raxar.view.graph

class GraphAdapter(val data: List<String>) {

    /**
     * Returns data for a given position.
     * Must support negative positions!
     */
    fun getData(position: Int): String {
        return data[position]
    }

    var size = 0
        private set
        get() {
            return data.size
        }
}