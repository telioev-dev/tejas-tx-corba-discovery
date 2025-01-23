package com.teliolabs.corba.data.mapper;

import java.util.List;
import java.util.Map;

public interface CorbaMapper<I, O> {
    O mapFromCorba(I input);

    Map<String, O> toMap(List<O> list);

    List<O> mapFromCorbaList(List<I> list);
}


