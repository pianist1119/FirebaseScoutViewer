package com.hotteam67.firebaseviewer.firebase;

import android.util.Log;

import com.annimon.stream.Stream;
import com.hotteam67.firebaseviewer.tableview.tablemodel.CellModel;
import com.hotteam67.firebaseviewer.tableview.tablemodel.ColumnHeaderModel;
import com.hotteam67.firebaseviewer.tableview.tablemodel.RowHeaderModel;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Jakob on 1/18/2018.
 */

public class DataTableProcessor implements Serializable {
    private List<ColumnHeaderModel> mColumnHeaderList;
    private List<List<CellModel>> cellList;
    private List<RowHeaderModel> rowHeaderList;

    private List<String> preferredOrder;

    private final String TeamNumber = "Team Number";

    public DataTableProcessor(HashMap<String, Object> rawData, List<String> preferredOrder)
    {
        /*
        Load the Raw Data into model
         */
        this.preferredOrder = preferredOrder;

        mColumnHeaderList = new ArrayList<>();
        cellList = new ArrayList<>();
        rowHeaderList = new ArrayList<>();

        if (rawData == null)
            return;

        cellList = new ArrayList<>();
        mColumnHeaderList = new ArrayList<>();
        rowHeaderList = new ArrayList<>();

        int row_id = 0;
        // Load rows and headers into cellmodels
        for (HashMap.Entry<String, Object> row : rawData.entrySet())
        {
            // Load the row
            try {
                HashMap<String, String> rowMap = (HashMap<String, String>) row.getValue();
                LoadRow(rowMap, row_id);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

            if (row_id == 0)
            {
                // Load column headers on first row
                try
                {
                    if (rawData.entrySet().size() > 1)
                    {
                        HashMap<String, String> rowMap = (HashMap<String, String>) row.getValue();
                        for (String column : preferredOrder)
                        {
                            if (rowMap.keySet().contains(column))
                            {
                                mColumnHeaderList.add(new ColumnHeaderModel(column));
                            }
                        }

                        // TeamNumber
                        rowMap.remove(TeamNumber);
                        //mColumnHeaderList.add(new ColumnHeaderModel(TeamNumber));
                        for (HashMap.Entry<String, String> column : rowMap.entrySet()) {
                            if (!preferredOrder.contains(column.getKey()))
                                mColumnHeaderList.add(new ColumnHeaderModel(column.getKey()));
                        }
                    }
                    else
                    {
                        Log.e("FirebaseScouter", "Failed to get fire result for columns");
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }

            ++row_id;
        }
    }

    private void LoadRow(HashMap<String, String> rowMap, int yIndex)
    {
        cellList.add(new ArrayList<>());

        // TeamNumber - before everything else
        String number = rowMap.get(TeamNumber);
        if (number == null)
            number = "";
        //cellList.get(row_id).add(new CellModel(row_id + "_0", number));
        rowHeaderList.add(new RowHeaderModel(number));
        rowMap.remove(TeamNumber);

        List<CellModel> row = cellList.get(yIndex);

        for (String column : preferredOrder)
        {
            if (rowMap.keySet().contains(column))
            {
                CellModel model = new CellModel("0_0", rowMap.get(column));
                row.add(model);
            }
        }

        for (HashMap.Entry<String, String> cell : rowMap.entrySet()) {

            CellModel model = new CellModel("0_0", cell.getValue());
            if (!preferredOrder.contains(cell.getKey()))
                row.add(model);
        }
    }

    public DataTableProcessor(List<ColumnHeaderModel> columnNames, List<List<CellModel>> cellValues, List<RowHeaderModel> rowNames)
    {
        mColumnHeaderList = columnNames;
        cellList = cellValues;
        rowHeaderList = rowNames;
    }


    public void SetTeamNumberFilter(String term)
    {
        teamNumberFilter = term;
    }

    private String teamNumberFilter = "";

    public List<ColumnHeaderModel> GetColumns()
    {
        return mColumnHeaderList;
    }

    public List<String> GetColumnNames()
    {
        List<String> nameList = new ArrayList<>();
        for (ColumnHeaderModel model : mColumnHeaderList)
            nameList.add(model.getData());

        return nameList;
    }

    public List<List<CellModel>> GetCells()
    {
        if (teamNumberFilter == null)
            return cellList;
        else if (teamNumberFilter.trim().isEmpty())
            return cellList;
        else
        {
            try {
                List<RowHeaderModel> filteredRows = GetRowHeaders();
                List<List<CellModel>> cells = new ArrayList<>();
                if (filteredRows.size() > 0)
                {
                    for (RowHeaderModel row : filteredRows)
                    {
                        cells.add(cellList.get(rowHeaderList.indexOf(row)));
                    }
                }
                // Log.d("FirebaseViewer", "Returning cells: " + cells.size());
                return cells;
            }
            catch (Exception e)
            {
                e.printStackTrace();
                return cellList;
            }
        }
    }

    public List<RowHeaderModel> GetRowHeaders()
    {
        if (teamNumberFilter == null)
            return rowHeaderList;
        if (teamNumberFilter.trim().isEmpty())
            return rowHeaderList;
        List<RowHeaderModel> filteredRows = new ArrayList<>();
        List<RowHeaderModel> unFilteredRows = new ArrayList<>();
        unFilteredRows.addAll(rowHeaderList);

        for (RowHeaderModel row : unFilteredRows)
        {
            if (row.getData().equals(teamNumberFilter))
            {
                filteredRows.add(row);
            }
        }
        // Log.d("FirebaseViewer", "Returning rows: " + filteredRows.size());
        return filteredRows;
    }
}
