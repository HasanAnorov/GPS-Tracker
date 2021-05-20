package com.example.gpstracker

import com.example.gpstracker.tools.Result
import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Query
import rx.Single

interface ApiInterface {

    @GET("maps/api/directions/json")
    fun getDirection(@Query("mode") mode:String,
                     @Query("transit_routing_preferance") preferance:String,
                     @Query("origin") origin:String,
                     @Query("destination")destination:String,
                     @Query("key")key:String):Observable<Result>

}