package com.example.gpstracker.tools



class Result {
    lateinit var routes:List<Route>
    lateinit var status:String

    constructor(routes: List<Route>, status: String) {
        this.routes = routes
        this.status = status
    }

    constructor()
}