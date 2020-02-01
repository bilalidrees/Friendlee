package com.example.meetup.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.example.meetup.BR;
import com.example.meetup.R;
import com.example.meetup.databinding.ContactItemBinding;
import com.example.meetup.model.Contact;

import java.util.ArrayList;
import java.util.List;

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ViewHolder> implements  Filterable {

    public Context context;
    public static Click  click;
    public static ArrayList<Contact> mcontactlist,contactListFiltered;
    public static int mItemSelected=-1;
    public ContactAdapter(Context context, ArrayList<Contact> contactArrayList, Click click) {
        this.context = context;
        this.click=click;
        this.mcontactlist = contactArrayList;
        contactListFiltered=new ArrayList<>(mcontactlist);
    }

    @Override
    public Filter getFilter() {
        return exampleFilter;
    }
    //search view feature implementation
    private Filter exampleFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            ArrayList<Contact> filteredlist = new ArrayList<>();
            if (constraint == null || constraint.length() == 0) {
                filteredlist.addAll(contactListFiltered);
            } else {
                String filterpattern = constraint.toString().toLowerCase().trim();
                for (Contact user : contactListFiltered) {
                    if (user.getName().toLowerCase().contains(filterpattern)) {
                        filteredlist.add(user);
                    }
                }
            }
            FilterResults results = new FilterResults();
            results.values = filteredlist;
            return results;
        }



        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            mcontactlist.clear();
            mcontactlist.addAll((ArrayList<Contact>) results.values);
            notifyDataSetChanged();
        }
    };

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ContactItemBinding binding = DataBindingUtil.inflate(
                LayoutInflater.from(parent.getContext()),
                R.layout.contact_item, parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        final Contact contact=mcontactlist.get(position);
        holder.bind(contact,holder.getAdapterPosition());
    }

    @Override
    public int getItemCount() {
        return mcontactlist.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        //declaration
        ContactItemBinding binding;
        //constructor
        public ViewHolder(ContactItemBinding itemView) {
            super(itemView.getRoot());
            this.binding=itemView;
        }

        public void bind(Contact obj, final int pos) {
            binding.setContact(obj);
            if (mItemSelected == -1) {
                binding.rad.setChecked(false);
                binding.rad.setSelected(false);
            } else {
                if (mItemSelected == getAdapterPosition()) {
                    binding.rad.setChecked(true);
                    binding.rad.setSelected(true);
                } else {
                    binding.rad.setChecked(false);
                    binding.rad.setSelected(false);
                }
            }
            binding.rel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mItemSelected != getAdapterPosition()) {
                        notifyItemChanged(mItemSelected);
                        mItemSelected = getAdapterPosition();
                    }
                    click.onclick(mcontactlist.get(pos),binding,pos);
                }
            });
            binding.rad.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mItemSelected != getAdapterPosition()) {
                        notifyItemChanged(mItemSelected);
                        mItemSelected = getAdapterPosition();
                    }
                    click.onclick(mcontactlist.get(pos),binding,pos);
                }
            });
        }
    }

    public interface Click{
        void onclick(Contact contact,ContactItemBinding binding,int position);
    }

}
