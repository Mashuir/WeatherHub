package com.example.weatherhub.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.weatherhub.Model.WeatherRvModel;
import com.example.weatherhub.R;
import com.squareup.picasso.Picasso;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class WeatherRvAdapter extends RecyclerView.Adapter<WeatherRvAdapter.ImageViewHolder>{

    Context context;
    List<WeatherRvModel> list;

    public WeatherRvAdapter(Context context, List<WeatherRvModel> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.weather_rv_item,parent,false);
        return new WeatherRvAdapter.ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {

        WeatherRvModel model = list.get(position);

        holder.idTVTemperature.setText(model.getTemperature()+"°c");
        holder.idTVWindSpeed.setText(model.getWindSpeed()+"km/h");
        Picasso.get().load("https:".concat(model.getIcon())).into(holder.idIVCondition);
;
        SimpleDateFormat input = new SimpleDateFormat("yyyy-MM-dd hh:mm");
        SimpleDateFormat output = new SimpleDateFormat("hh:mm aa");

        try {
            Date t = input.parse(model.getTime());
            holder.idTVTime.setText(output.format(t));
        } catch (ParseException e) {
            e.printStackTrace();
        }


    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ImageViewHolder extends RecyclerView.ViewHolder{

        TextView idTVTime,idTVTemperature,idTVWindSpeed;
        ImageView idIVCondition;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);

            idTVTime = itemView.findViewById(R.id.idTVTime);
            idTVTemperature = itemView.findViewById(R.id.idTVTemperature);
            idIVCondition = itemView.findViewById(R.id.idIVCondition);
            idTVWindSpeed = itemView.findViewById(R.id.idTVWindSpeed);
        }
    }
}
