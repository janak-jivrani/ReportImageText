package com.example.imagetotext.adapters;

import android.annotation.SuppressLint;
import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.view.menu.MenuPopupHelper;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;

import com.example.imagetotext.R;
import com.example.imagetotext.core.MyConstants;
import com.example.imagetotext.core.TextOCR;

import java.util.ArrayList;

public class ReceiptAdapter extends RecyclerView.Adapter<ReceiptAdapter.ItemHolder> {
    ArrayList<TextOCR> textOCRArrayList;
    public ReceiptAdapter(ArrayList<TextOCR> textOCRArrayList) {
        this.textOCRArrayList = textOCRArrayList;
    }

    @NonNull
    @Override
    public ItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.ocr_text_item_layout,parent,false);
        return new ItemHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemHolder holder, int position) {
        TextOCR textOCR = textOCRArrayList.get(position);
        holder.bindData(textOCR);
    }

    @Override
    public int getItemCount() {
        return textOCRArrayList.size();
    }

    class ItemHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView tvText;
        private AppCompatImageButton ibBold,ibUnderline,ibAlignment,ibAdd;

        public ItemHolder(@NonNull View itemView) {
            super(itemView);
            tvText = itemView.findViewById(R.id.tvText);
            ibBold = itemView.findViewById(R.id.ibBold);
            ibUnderline = itemView.findViewById(R.id.ibUnderline);
            ibAlignment = itemView.findViewById(R.id.ibAlignment);
            ibAdd = itemView.findViewById(R.id.ibAdd);

            ibBold.setOnClickListener(this);
            ibUnderline.setOnClickListener(this);
            ibAlignment.setOnClickListener(this);
            ibAdd.setOnClickListener(this);
        }

        public void bindData(TextOCR textOCR) {
            SpannableString spanString = new SpannableString(textOCR.line.trim());
            if (textOCR.isUnderline) {
                spanString.setSpan(new UnderlineSpan(), 0, spanString.length(), 0);
            }
            if (textOCR.isBold) {
                spanString.setSpan(new StyleSpan(Typeface.BOLD), 0, spanString.length(), 0);
            }
            //spanString.setSpan(new StyleSpan(Typeface.ITALIC), 0, spanString.length(), 0);
            if (textOCR.align == MyConstants.AlignCenter) {
                tvText.setGravity(Gravity.CENTER);
            } else if (textOCR.align == MyConstants.AlignRight) {
                tvText.setGravity(Gravity.RIGHT);
            } else {
                tvText.setGravity(Gravity.LEFT);
            }
            tvText.setText(spanString);
            //tvText.setText();
            ibBold.setSelected(textOCR.isBold);
            ibUnderline.setSelected(textOCR.isUnderline);
        }

        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.ibBold) {
                updateBold();
            } else if (v.getId() == R.id.ibUnderline) {
                updateUnderline();
            } else if (v.getId() == R.id.ibAlignment) {
                //TODO: show alignment popup
                applyAlignment(v);
            } else if (v.getId() == R.id.ibAdd) {

            }
        }

        private void updateBold() {
            int position = getBindingAdapterPosition();
            TextOCR textOCR = textOCRArrayList.get(position);
            textOCR.isBold = !textOCR.isBold;
            notifyItemChanged(position);
        }
        private void updateUnderline() {
            int position = getBindingAdapterPosition();
            TextOCR textOCR = textOCRArrayList.get(position);
            textOCR.isUnderline = !textOCR.isUnderline;
            notifyItemChanged(position);
        }

        @SuppressLint("RestrictedApi")
        private void applyAlignment(View v) {
            final int position = getBindingAdapterPosition();
            final TextOCR textOCR = textOCRArrayList.get(position);
            PopupMenu popup = new PopupMenu(v.getContext(), v);
            popup.getMenuInflater().inflate(R.menu.alignment_menu_list, popup.getMenu());

            popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem menu_item) {
                    switch (menu_item.getItemId()) {
                        case R.id.mnuLeft:
                            textOCR.align = MyConstants.AlignLeft;
                            notifyItemChanged(position);
                            break;
                        case R.id.mnuCenter:
                            textOCR.align = MyConstants.AlignCenter;
                            notifyItemChanged(position);
                            break;
                        case R.id.mnuRight:
                            textOCR.align = MyConstants.AlignRight;
                            notifyItemChanged(position);
                            break;
                    }
                    return true;
                }
            });

            MenuPopupHelper menuHelper = new MenuPopupHelper(v.getContext(), (MenuBuilder) popup.getMenu(), v);
            menuHelper.setForceShowIcon(true);
            menuHelper.setGravity(Gravity.END);
            menuHelper.show();
        }
    }
}
