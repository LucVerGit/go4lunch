package com.example.myfirebaseapp.service;

import com.example.myfirebaseapp.BuildConfig;
import com.example.myfirebaseapp.models.api.Autocomplete;
import com.example.myfirebaseapp.models.api.NearbySearch;
import com.example.myfirebaseapp.models.api.Place;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Request with retrofit
 */
public interface IPlacesApi {

    String GOOGLE_MAP_API_KEY = BuildConfig.GOOGLE_MAP_API_KEY ;

    // Nearby Search Request
    @GET("maps/api/place/nearbysearch/json?key="+GOOGLE_MAP_API_KEY)
    Observable<NearbySearch> getNearbyRestaurants(@Query("location") String location, @Query("radius") int radius, @Query("type") String type);

    // Place Details Request
    @GET("maps/api/place/details/json?key="+GOOGLE_MAP_API_KEY)
    Observable<Place> getRestaurantDetails(@Query("place_id") String placeId);

    // Place Autocomplete Request
    @GET("maps/api/place/autocomplete/json?strictbounds&key="+GOOGLE_MAP_API_KEY)
    Observable<Autocomplete> getAutocomplete(@Query("input") String input, @Query("radius") int radius, @Query("location") String location, @Query("type") String type);

}
