package com.pawlowski.shopisto.base;

import java.util.ArrayList;
import java.util.List;

import androidx.recyclerview.widget.RecyclerView;

public abstract class BaseSelectableAdapter<ViewHolder extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<ViewHolder> {

    private final List<Boolean> positionsSelected = new ArrayList<>();

    protected void initNewSelections(int positionsNumber)
    {
        positionsSelected.clear();
        for(int i=0;i<positionsNumber;i++)
        {
            positionsSelected.add(false);
        }
    }

    protected <ListElType> List<ListElType> getSelectedElements(List<ListElType> list)
    {
        if(positionsSelected.size() != list.size())
            throw new RuntimeException("List size has to be the same as positionsSelected list size!");

        List<ListElType> selectedElements = new ArrayList<>();

        for(int i=0;i<list.size();i++)
        {
            if(positionsSelected.get(i))
            {
                selectedElements.add(list.get(i));
            }
        }

        return selectedElements;
    }

    protected boolean isPositionSelected(int position)
    {
        return positionsSelected.get(position);
    }

    protected List<Integer>getSelectedPositions()
    {
        List<Integer> selectedPositions = new ArrayList<>();
        for(int i=0;i<positionsSelected.size();i++)
        {
            if(positionsSelected.get(i))
            {
                selectedPositions.add(i);
            }
        }
        return selectedPositions;
    }

    public int getNumberOfSelectedElements()
    {
        int number = 0;
        for(int i=0;i<positionsSelected.size();i++)
        {
            if(positionsSelected.get(i))
            {
                number++;
            }
        }
        return number;
    }

    public boolean isSomethingSelected()
    {
        for(int i=0;i<positionsSelected.size();i++)
        {
            if(positionsSelected.get(i))
            {
                return true;
            }
        }
        return false;
    }

    protected void selectElement(int position)
    {
        positionsSelected.set(position, true);
    }

    protected void unselectElement(int position)
    {
        positionsSelected.set(position, false);
    }

    protected void changeSelectionOfElement(int position)
    {
        if(positionsSelected.get(position))
        {
            positionsSelected.set(position, false);
        }
        else
        {
            positionsSelected.set(position, true);
        }
    }

    protected void deleteElement(int position)
    {
        positionsSelected.remove(position);
    }

    protected void addElement(int position, boolean isSelected)
    {
        positionsSelected.add(position, isSelected);
    }

    protected void unselectAllElements()
    {
        for(int i=0;i<positionsSelected.size();i++)
        {
            positionsSelected.set(i, false);
        }
    }

    protected void unselectAllElementsAndNotify()
    {
        for(int i=0;i<positionsSelected.size();i++)
        {
            if(positionsSelected.get(i))
            {
                positionsSelected.set(i, false);
                notifyItemChanged(i);
            }
        }
    }
}
