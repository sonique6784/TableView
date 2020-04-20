/*
 * Copyright (c) 2018. Evren Coşkun
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package com.evrencoskun.tableview.adapter.recyclerview;

import android.content.Context;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.evrencoskun.tableview.ITableView;
import com.evrencoskun.tableview.adapter.ITableAdapter;
import com.evrencoskun.tableview.adapter.recyclerview.holder.AbstractSorterViewHolder;
import com.evrencoskun.tableview.adapter.recyclerview.holder.AbstractViewHolder;
import com.evrencoskun.tableview.adapter.recyclerview.holder.AbstractViewHolder.SelectionState;
import com.evrencoskun.tableview.handler.ISelectableModel;
import com.evrencoskun.tableview.sort.ColumnSortHelper;
import com.evrencoskun.tableview.sort.SortState;

import java.util.List;

/**
 * Created by evrencoskun on 10/06/2017.
 */

public class ColumnHeaderRecyclerViewAdapter<CH> extends AbstractRecyclerViewAdapter<CH> {
    @NonNull
    private ITableAdapter mTableAdapter;
    private ITableView mTableView;
    private ColumnSortHelper mColumnSortHelper;

    public ColumnHeaderRecyclerViewAdapter(@NonNull Context context, @Nullable List<CH> itemList, @NonNull ITableAdapter
            tableAdapter) {
        super(context, itemList);
        this.mTableAdapter = tableAdapter;
        this.mTableView = tableAdapter.getTableView();
    }

    @NonNull
    @Override
    public AbstractViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return mTableAdapter.onCreateColumnHeaderViewHolder(parent, viewType);
    }

    @Override
    public void onBindViewHolder(@NonNull AbstractViewHolder viewHolder, int position) {
        Object value = getItem(position);

        // Apply Selection Style
        if (mTableAdapter.getTableView().isSelectable()) {
            if (value instanceof ISelectableModel) {
                viewHolder.setSelected(((ISelectableModel) value).getSelectionState());
                final int color = mTableAdapter.getColorForSelection(((ISelectableModel) value).getSelectionState());
                viewHolder.setBackgroundColor(color);
            } else if(value != null){
                // trigger exception, if isSelectable, Cells MUST implements ISelectableModel
                throw new ClassCastException("Item at position " + position + " must implement ISelectableModel to be selectable.");

            }
        }

        mTableAdapter.onBindColumnHeaderViewHolder(viewHolder, value, position);
    }

    @Override
    public int getItemViewType(int position) {
        return mTableAdapter.getColumnHeaderItemViewType(position);
    }

    @Override
    public void onViewAttachedToWindow(@NonNull AbstractViewHolder viewHolder) {
        super.onViewAttachedToWindow(viewHolder);

        if (mTableAdapter.getTableView().isSelectable()) {
            SelectionState selectionState = mTableView.getSelectionHandler()
                    .getSelectionStateColumnHeader
                (viewHolder.getAdapterPosition());

            // Control to ignore selection color
            if (!mTableView.isIgnoreSelectionColors()) {

                // Change background color of the view considering it's selected state
                mTableView.getSelectionHandler()
                        .changeColumnBackgroundColorBySelectionStatus(viewHolder, selectionState);
            }

            // Change selection status
            viewHolder.setSelected(selectionState);
        }

        // Control whether the TableView is sortable or not.
        if (mTableView.isSortable()) {
            if (viewHolder instanceof AbstractSorterViewHolder) {
                // Get its sorting state
                SortState state = getColumnSortHelper().getSortingStatus(viewHolder
                        .getAdapterPosition());
                // Fire onSortingStatusChanged
                ((AbstractSorterViewHolder) viewHolder).onSortingStatusChanged(state);
            }
        }
    }

    @NonNull
    public ColumnSortHelper getColumnSortHelper() {
        if (mColumnSortHelper == null) {
            // It helps to store sorting state of column headers
            this.mColumnSortHelper = new ColumnSortHelper(mTableView.getColumnHeaderLayoutManager
                    ());
        }
        return mColumnSortHelper;
    }
}
