package volley.volleyball.adapters;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import volley.volleyball.ColorUtils;

public class ObjectArrayAdapter<T> extends ArrayAdapter<T>{

    protected static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

    protected final int itemResourceId;

    public ObjectArrayAdapter(Context context, int textViewResourceId, List<T> objects) {
        super(context, textViewResourceId, objects);
        itemResourceId = textViewResourceId;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(itemResourceId, null);
        }

        return convertView;
    }

    protected <V extends View> V setBackgroundColor(final V view, final int color){
        view.setBackgroundColor(color);
        return view;
    }

    protected int getBackgroundColor(final int position){
        return position%2 != 0 ? ColorUtils.EVEN_LIST_ITEM_COLOR : ColorUtils.ODD_LIST_ITEM_COLOR;
    }
}
