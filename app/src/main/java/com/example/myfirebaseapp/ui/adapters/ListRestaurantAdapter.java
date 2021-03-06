package com.example.myfirebaseapp.ui.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.RequestManager;
import com.example.myfirebaseapp.R;
import com.example.myfirebaseapp.api.UserHelper;
import com.example.myfirebaseapp.models.api.Place;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Calendar;
import java.util.List;
import java.util.Objects;


import static com.example.myfirebaseapp.BuildConfig.GOOGLE_MAP_API_KEY;
import static com.example.myfirebaseapp.utils.TimeUtils.getCurrentTime;

public class ListRestaurantAdapter extends RecyclerView.Adapter<ListRestaurantAdapter.ListRestaurantViewHolder> {

    // Declarations
    private String mPosition;
    private RequestManager glide;
    private List<Place> place;

    public void setPosition(String position) {
        mPosition = position;
    }

    //Constructor
    public ListRestaurantAdapter(List<Place> placeDetails, RequestManager glide, String mPosition) {
        this.place = placeDetails;
        this.glide = glide;
        this.mPosition = mPosition;
    }

    //Create viewHolder
    @NonNull
    @Override
    public ListRestaurantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.restaurant_item, parent, false);

        return new ListRestaurantViewHolder(view);
    }

    //Update viewHolder with placeDetails
    @Override
    public void onBindViewHolder(@NonNull ListRestaurantViewHolder viewHolder, int position) {
        viewHolder.updateWithRestaurantDetails(this.place.get(position).getResult(), this.glide, this.mPosition);
    }

    //return the total count of items in the list
    @Override
    public int getItemCount() {
        return this.place.size();
    }

    public static class ListRestaurantViewHolder extends RecyclerView.ViewHolder {

        // --- Attribute ---
        private TextView name;
        private TextView address;
        private TextView openHours;
        private ImageView photo;
        private RatingBar ratingBar;
        private TextView distance;
        private TextView workmates;


        //public String GOOGLE_MAP_API_KEY = BuildConfig.GOOGLE_MAP_API_KEY;
        private static final long serialVersionUID = 1L;
        private float[] distanceResults = new float[3];
        private String closeHour;
        private int diff;


        public ListRestaurantViewHolder(View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.itemNameTextView);
            address = itemView.findViewById(R.id.itemAddressTextView);
            openHours = itemView.findViewById(R.id.itemHoursTextView);
            photo = itemView.findViewById(R.id.itemImageView);
            ratingBar = itemView.findViewById(R.id.itemAppCompatRatingBar);
            distance = itemView.findViewById(R.id.itemDistanceTextView);
            workmates = itemView.findViewById(R.id.itemWorkmateTextView);

            getCurrentTime();
        }


        public void updateWithRestaurantDetails(Place.Result result, RequestManager glide, String mPosition) {

            //restaurant name
            this.name.setText(result.getName());

            //restaurant address
            this.address.setText(result.getVicinity());

            //restaurant rating
            restaurantRating(result);

            //restaurant distance
            restaurantDistance(mPosition, result.getGeometry().getLocation());
            String distance = Math.round(distanceResults[0]) + "m";
            this.distance.setText(distance);

            //for numberWorkmates
            numberWorkmates(result.getPlaceId());

            //for retrieve opening hours (open or closed)
            if (result.getOpeningHours() != null) {

                if (result.getOpeningHours().getOpenNow().toString().equals("false")) {
                    this.openHours.setText(R.string.close);
                    this.openHours.setTextColor(Color.RED);
                } else if (result.getOpeningHours().getOpenNow().toString().equals("true")) {
                    getHoursInfo(result);
                }
            }
            if (result.getOpeningHours() == null) {
                this.openHours.setText(R.string.opening_hours_not_available);
                this.openHours.setTextColor(Color.BLACK);
            }

            //for add photos with Glide
            if (result.getPhotos() != null && !result.getPhotos().isEmpty()) {
                glide.load("https://maps.googleapis.com/maps/api/place/photo?maxwidth=400&maxheight=400&photoreference=" + result.getPhotos().get(0).getPhotoReference() + "&key=" + GOOGLE_MAP_API_KEY)
                        .into(photo);
            } else {
                photo.setImageResource(R.drawable.ic_list_person_outline);
            }
        }

        // For calculate restaurant distance
        private void restaurantDistance(String startLocation, Place.Location endLocation) {
            String[] separatedStart = startLocation.split(",");
            double startLatitude = Double.parseDouble(separatedStart[0]);
            double startLongitude = Double.parseDouble(separatedStart[1]);
            double endLatitude = endLocation.getLat();
            double endLongitude = endLocation.getLng();
            android.location.Location.distanceBetween(startLatitude, startLongitude, endLatitude, endLongitude, distanceResults);
        }

        //For rating
        private void restaurantRating(Place.Result result) {
            if (result.getRating() != null) {
                double restaurantRating = result.getRating();
                double rating = (restaurantRating / 5) * 3;
                this.ratingBar.setRating((float) rating);
                this.ratingBar.setVisibility(View.VISIBLE);

            } else {
                this.ratingBar.setVisibility(View.GONE);
            }
        }

        private String getHoursInfo(Place.Result result) {
            int[] days = {0, 1, 2, 3, 4, 5, 6};
            Calendar calendar = Calendar.getInstance();
            int day = calendar.get(Calendar.DAY_OF_WEEK) - 1;

            if (result.getOpeningHours() != null && result.getOpeningHours().getPeriods() != null) {
                try{
                    for (Place.Period p : result.getOpeningHours().getPeriods()) {
                        String closeHour = p.getClose().getTime();
                        int hourClose = Integer.parseInt(closeHour);
                        diff = getCurrentTime() - hourClose;

                        if (p.getOpen().getDay() == days[day] && diff < -100) {
                            openHours.setText(itemView.getContext().getString(R.string.open_until) + " " + convertStringToHours(closeHour));
                            this.openHours.setTextColor(itemView.getContext().getResources().getColor(R.color.colorAccent));


                        } else if (diff >= -100 && days[day] == p.getClose().getDay()) {
                            openHours.setText(itemView.getContext().getString(R.string.closing_soon));
                            this.openHours.setTextColor(itemView.getContext().getResources().getColor(R.color.colorPrimaryDark));

                        }
                    }
                } catch (Exception e){

                }
            }
            return closeHour;
        }


        private void numberWorkmates(String restaurantId) {
            UserHelper.getUsersCollection()
                    .whereEqualTo("restaurantId", restaurantId)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {

                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {

                                int numberWorkmates = Objects.requireNonNull(task.getResult()).size();
                                String workmatesNumber = "(" + numberWorkmates + ")";

                                if(numberWorkmates > 0) {
                                    workmates.setText(workmatesNumber);
                                } else {
                                    workmates.setVisibility(View.INVISIBLE);
                                }

                            }
                        }
                    });
        }

        public static String convertStringToHours(String hour){
            String hour1 = hour.substring(0,2);
            String hour2 = hour.substring(2,4);
            return hour1 + ":" + hour2;
        }

    }
}