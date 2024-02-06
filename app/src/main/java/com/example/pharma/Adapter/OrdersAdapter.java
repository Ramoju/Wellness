package com.example.pharma.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pharma.Model.CartModel;
import com.example.pharma.Model.OrderModel;
import com.example.pharma.OrdersActivity;
import com.example.pharma.R;

import java.util.List;

public class OrdersAdapter extends RecyclerView.Adapter<OrdersAdapter.MyViewHolder>{
    private List<OrderModel> orders;
    private OrdersActivity activity;

    public OrdersAdapter(List<OrderModel> orders, OrdersActivity activity){
        this.activity = activity;
        this.orders = orders;
    }
    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.order_card, parent, false);
        return new OrdersAdapter.MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        final OrderModel item = orders.get(position);
        holder.ordernum.setText("Order #: " + String.valueOf(item.getOrdernum()));
        holder.orderprice.setText("Order amount: " + String.valueOf(item.getOrderAmount()));
        holder.prodlist.setText("Products: " + item.getProductsList());
    }

    public void setOrders(List<OrderModel> orders) {
        this.orders = orders;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder{

        TextView ordernum;
        TextView orderprice;
        TextView prodlist;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            ordernum = itemView.findViewById(R.id.ordernum);
            orderprice = itemView.findViewById(R.id.orderprice);
            prodlist = itemView.findViewById(R.id.productslist);
        }
    }
}
