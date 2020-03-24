package com.mmi.demo.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.mmi.apis.place.autosuggest.AutoSuggest;
import com.mmi.apis.place.autosuggest.AutoSuggestManager;
import com.mmi.demo.R;

import java.util.ArrayList;
import java.util.List;

public class AutoCompleteAdapter extends BaseAdapter implements Filterable {


    private Context mContext;
    private List<AutoSuggest> resultList = new ArrayList<AutoSuggest>();

    public AutoCompleteAdapter(Context context) {
        mContext = context;
    }

    @Override
    public int getCount() {
        return resultList.size();
    }

    @Override
    public AutoSuggest getItem(int index) {
        return resultList.get(index);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.place_list_item, parent, false);
        }
        ((TextView) convertView.findViewById(R.id.text1)).setText(getItem(position).getAddr());

        return convertView;
    }

    @Override
    public Filter getFilter() {
        Filter filter = new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults filterResults = new FilterResults();
                if (constraint != null) {
                    List<AutoSuggest> suggestions = getSuggestions(constraint.toString());

                    // Assign the data to the FilterResults
                    filterResults.values = suggestions;
                    filterResults.count = suggestions.size();
                }
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                if (results != null && results.count > 0) {
                    resultList = (List<AutoSuggest>) results.values;
                    notifyDataSetChanged();
                } else {
                    notifyDataSetInvalidated();
                }
            }};
        return filter;
    }

    /**
     * Returns a search result for the given searchText.
     */
    private List<AutoSuggest> getSuggestions(String searchText) {

        try {
            AutoSuggestManager autoSuggestManager =new AutoSuggestManager();

            return autoSuggestManager.getSuggestions(searchText, null,false);
        } catch (Exception e) {
            e.printStackTrace();
        return null;
        }

    }
}