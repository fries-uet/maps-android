package com.example.map_uet;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by TooNies1810 on 10/21/15.
 */
public class ListViewChatAdapter extends BaseAdapter {
    private ArrayList<String> stringArr;

    //true: My text,
    //false: Bot's text
    private ArrayList<Boolean> typeChatArr;

    private Context mContext;
    private LayoutInflater lf;

    public ListViewChatAdapter(Context mContext) {
        this.mContext = mContext;
        lf = LayoutInflater.from(mContext);
        stringArr = new ArrayList<String>();
        typeChatArr = new ArrayList<Boolean>();
    }

    public void addItem(String text, boolean type){
        stringArr.add(text);
        typeChatArr.add(type);
        notifyDataSetChanged();
    }

    public void clearData(){
        stringArr = new ArrayList<String>();
        typeChatArr = new ArrayList<Boolean>();
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return stringArr.size();
    }

    @Override
    public String getItem(int position) {
        return stringArr.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null){
            convertView = lf.inflate(R.layout.item_chat_layout,null);
        }
        TextView tvMyText = (TextView) convertView.findViewById(R.id.tv_mytext);
        TextView tvBotText = (TextView) convertView.findViewById(R.id.tv_bottext);
        if (typeChatArr.get(position) == true){
            tvMyText.setText(stringArr.get(position));
            tvBotText.setVisibility(View.GONE);
        } else {
            tvBotText.setText(stringArr.get(position));
            tvMyText.setVisibility(View.GONE);
        }
        return convertView;
    }
}
