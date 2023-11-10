package com.example.imagetotext.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.imagetotext.R;
import com.example.imagetotext.adapters.ReceiptAdapter;
import com.example.imagetotext.core.TextOCR;

import java.util.ArrayList;
import java.util.Objects;


public class ReceiptActivity extends AppCompatActivity {

    private RecyclerView rvContentList;
    private int maxColumnCount = 1;
    private ReceiptAdapter receiptAdapter;
    private ArrayList<TextOCR> textOCRArrayList = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receipt);
        rvContentList = findViewById(R.id.rvContentList);
        Intent intent = getIntent();
        maxColumnCount = intent.getIntExtra("columCount",1);
        textOCRArrayList = (ArrayList<TextOCR>) intent.getSerializableExtra("textOcrList");
        int i = 0;
        while (i < textOCRArrayList.size()) {
            int sameCount = 1;
            for (int j = i+1; j < textOCRArrayList.size(); j++) {
                if (!TextUtils.isEmpty(textOCRArrayList.get(i).groupId) && Objects.equals(textOCRArrayList.get(i).groupId, textOCRArrayList.get(j).groupId)) {
                    sameCount++;
                } else {
                    break;
                }
            }
            if (sameCount > 1) {
                int span = maxColumnCount / sameCount;
                for (int j = i; j < i + sameCount; j++) {
                    textOCRArrayList.get(j).spanCount = span;
                }
            } else {
                textOCRArrayList.get(i).spanCount = maxColumnCount;
            }
            i+=sameCount;
        }
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, maxColumnCount);
        gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return textOCRArrayList.get(position).spanCount;
            }
        });
        rvContentList.setLayoutManager(gridLayoutManager);
        receiptAdapter = new ReceiptAdapter(textOCRArrayList);
        rvContentList.setAdapter(receiptAdapter);
    }
}