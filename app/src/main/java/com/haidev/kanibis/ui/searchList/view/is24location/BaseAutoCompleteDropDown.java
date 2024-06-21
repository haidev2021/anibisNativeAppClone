package com.haidev.kanibis.ui.searchList.view.is24location;


import java.util.List;
public interface BaseAutoCompleteDropDown {

    boolean isValid();

    boolean isHistory();

    boolean equals(BaseAutoCompleteDropDown item);

    List<Integer> getMatchedMap();

    String getText();

    String getSubMatched();

    String toString();

    boolean isPrefixOnly();
}
